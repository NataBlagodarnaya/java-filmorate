package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String INSERT_QUERY = "INSERT INTO films (title, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY =
            "SELECT f.*, r.rating AS mpa_name FROM films f LEFT JOIN rating r ON f.rating_id = r.rating_id";
    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, r.rating AS mpa_name FROM films f LEFT JOIN rating r ON f.rating_id = r.rating_id WHERE f.film_id = ?";
    private static final String CONTAINS_QUERY = "SELECT COUNT(*) FROM films WHERE film_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String FIND_GENRES_FOR_FILMS = "SELECT fg.film_id, fg.genre_id, g.genre FROM film_genre fg " +
            "JOIN genre g ON fg.genre_id = g.genre_id " +
            "WHERE fg.film_id IN (%s) ORDER BY fg.genre_id ASC";

    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String CONTAINS_LIKE_QUERY =
            "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_QUERY =
            "SELECT f.film_id, f.title, f.description, f.release_date, f.duration, f.rating_id, r.rating AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN rating r ON f.rating_id = r.rating_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id, f.title, f.description, f.release_date, f.duration, f.rating_id, r.rating " +
                    "ORDER BY COUNT(l.user_id) DESC " +
                    "LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {
        Long ratingId = (film.getMpa() != null) ? film.getMpa().getId() : null;

        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId
        );
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                jdbc.update(INSERT_FILM_GENRE_QUERY, id, genre.getId());
            }
        }
        return getFilmById(id).get();
    }

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        loadGenresForFilmList(films);
        return films;
    }

    @Override
    public Film update(Film newFilm) {
        if (!containsFilm(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        Long ratingId = (newFilm.getMpa() != null) ? newFilm.getMpa().getId() : null;

        update(
                UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                ratingId,
                newFilm.getId()
        );

        jdbc.update(DELETE_FILM_GENRES_QUERY, newFilm.getId());
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            for (Genre g : newFilm.getGenres()) {
                jdbc.update(INSERT_FILM_GENRE_QUERY, newFilm.getId(), g.getId());
            }
        }
        return getFilmById(newFilm.getId()).get();
    }

    @Override
    public void delete(Film film) {
        if (!delete(DELETE_QUERY, film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
    }

    @Override
    public boolean containsFilm(Long id) {
        Integer count = jdbc.queryForObject(CONTAINS_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> filmOptional = findOne(FIND_BY_ID_QUERY, id);
        filmOptional.ifPresent(this::loadGenresForSingleFilm);
        return filmOptional;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        Collection<Film> films = jdbc.query(FIND_POPULAR_QUERY, mapper, count);
        loadGenresForFilmList(films);
        return films;
    }

    private void loadGenresForSingleFilm(Film film) {
        String query = String.format(FIND_GENRES_FOR_FILMS, film.getId());
        List<Genre> genres = jdbc.query(query, (rs, rowNum) -> {
            Long genreId = rs.getLong("genre_id");

            if (genreId != null) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(rs.getString("genre"));
                return genre;
            }
            return null;
        });

        genres.removeIf(Objects::isNull);
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadGenresForFilmList(Collection<Film> films) {
        if (films.isEmpty()) return;

        String inParams = films.stream()
                .map(film -> String.valueOf(film.getId()))
                .collect(Collectors.joining(", "));

        String query = String.format(FIND_GENRES_FOR_FILMS, inParams);
        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, film -> film));

        jdbc.query(query, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Long genreId = rs.getLong("genre_id");

            if (!rs.wasNull()) {
                Film film = filmMap.get(filmId);
                if (film != null) {
                    if (film.getGenres() == null) {
                        film.setGenres(new LinkedHashSet<>());
                    }
                    Genre genre = new Genre();
                    genre.setId(genreId);
                    genre.setName(rs.getString("genre"));
                    film.getGenres().add(genre);
                }
            }
            return null;
        });
    }

    @Override
    public boolean containsLike(Long filmId, Long userId) {
        Integer count = jdbc.queryForObject(CONTAINS_LIKE_QUERY, Integer.class, filmId, userId);
        return count != null && count > 0;
    }
}