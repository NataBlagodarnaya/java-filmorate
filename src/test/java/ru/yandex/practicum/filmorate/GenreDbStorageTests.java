package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({GenreDbStorage.class, GenreRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTests {

    private final GenreDbStorage genreStorage;

    @Test
    public void testFindAllGenres() {
        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .isNotNull()
                .isNotEmpty()
                .hasSize(6);

        assertThat(genres)
                .extracting(Genre::getName)
                .contains("Комедия");
    }

    @Test
    public void testGetGenreByValidId() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(1L);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1L);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    public void testGetGenreByInvalidId() {
        Optional<Genre> genreOptional = genreStorage.getGenreById(9999L);

        assertThat(genreOptional).isEmpty();
    }
}