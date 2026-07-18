package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({
        FilmDbStorage.class,
        FilmRowMapper.class,
        UserDbStorage.class,
        UserRowMapper.class
})
@AutoConfigureTestDatabase
class FilmDbStorageTests {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private Film createdFilm;

    @Autowired
    public FilmDbStorageTests(FilmDbStorage filmStorage, UserDbStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @BeforeEach
    public void setUp() {
        Film newFilm = new Film();
        newFilm.setName("Тестовый Фильм");
        newFilm.setDescription("Описание тестового фильма");
        newFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        newFilm.setDuration(148);
        newFilm.setRating(Rating.fromId(1L));

        createdFilm = filmStorage.create(newFilm);
    }

    // create
    @Test
    public void testCreateFilm() {
        assertThat(createdFilm)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Тестовый Фильм")
                .hasFieldOrPropertyWithValue("description", "Описание тестового фильма")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2010, 7, 16))
                .hasFieldOrPropertyWithValue("duration", 148);

        assertThat(createdFilm.getId())
                .isNotNull()
                .isPositive();
    }

    // findAll
    @Test
    public void testFindAll() {
        Film film2 = new Film();
        film2.setName("Фильм 2");
        film2.setDescription("Описание 2");
        film2.setReleaseDate(LocalDate.of(2020, 1, 1));
        film2.setDuration(100);
        film2.setRating(Rating.fromId(2L));

        Film savedFilm2 = filmStorage.create(film2);

        Collection<Film> films = filmStorage.findAll();

        assertThat(films)
                .isNotNull()
                .hasSize(2)
                .extracting(Film::getId)
                .containsExactlyInAnyOrder(createdFilm.getId(), savedFilm2.getId());
    }

    // update
    @Test
    public void testUpdate() {
        createdFilm.setName("Обновленное название");
        createdFilm.setDuration(150);

        Film updatedFilm = filmStorage.update(createdFilm);
        Optional<Film> filmOptional = filmStorage.getFilmById(updatedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Обновленное название");
                    assertThat(f).hasFieldOrPropertyWithValue("duration", 150);
                });
    }

    // delete
    @Test
    public void testDelete() {
        filmStorage.delete(createdFilm);

        Optional<Film> filmOptional = filmStorage.getFilmById(createdFilm.getId());
        assertThat(filmOptional).isEmpty();
    }

    // containsFilm
    @Test
    public void testContainsFilm() {
        boolean exists = filmStorage.containsFilm(createdFilm.getId());
        boolean notExists = filmStorage.containsFilm(9999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    // getFilmById
    @Test
    public void testGetFilmById() {
        Optional<Film> filmOptional = filmStorage.getFilmById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", createdFilm.getId())
                );
    }

    // addLike, deleteLike и getPopularFilms
    @Test
    public void testLikesAndPopularFilmsLogic() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("like-user1@mail.ru");
        user1.setLogin("l_user1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        User savedUser1 = userStorage.create(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("like-user2@mail.ru");
        user2.setLogin("l_user2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));
        User savedUser2 = userStorage.create(user2);

        Film film2 = new Film();
        film2.setName("Менее популярный фильм");
        film2.setDescription("Описание");
        film2.setReleaseDate(LocalDate.of(2015, 5, 5));
        film2.setDuration(90);
        film2.setRating(Rating.fromId(1L));
        Film savedFilm2 = filmStorage.create(film2);

        filmStorage.addLike(createdFilm.getId(), savedUser1.getId());
        filmStorage.addLike(createdFilm.getId(), savedUser2.getId());
        filmStorage.addLike(savedFilm2.getId(), savedUser1.getId());

        Optional<Film> loadedFilm1 = filmStorage.getFilmById(createdFilm.getId());
        assertThat(loadedFilm1).isPresent();
        assertThat(loadedFilm1.get().getLikes())
                .isNotNull()
                .hasSize(2)
                .contains(savedUser1.getId(), savedUser2.getId());

        Collection<Film> popularFilms = filmStorage.getPopularFilms(10);
        assertThat(popularFilms)
                .isNotNull()
                .hasSize(2);

        Film mostPopular = popularFilms.stream().findFirst().orElseThrow();
        assertThat(mostPopular.getId()).isEqualTo(createdFilm.getId());

        filmStorage.deleteLike(createdFilm.getId(), savedUser1.getId());

        Optional<Film> loadedFilm1AfterDelete = filmStorage.getFilmById(createdFilm.getId());
        assertThat(loadedFilm1AfterDelete).isPresent();
        assertThat(loadedFilm1AfterDelete.get().getLikes())
                .hasSize(1)
                .containsExactly(savedUser2.getId());
    }
}