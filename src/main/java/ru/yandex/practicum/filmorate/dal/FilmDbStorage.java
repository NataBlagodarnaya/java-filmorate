package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String INSERT_QUERY = "INSERT INTO films (title, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String CONTAINS_QUERY = "SELECT COUNT(*) FROM films WHERE film_id = ?";

    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String FIND_GENRES_FOR_FILMS = "SELECT fg.film_id, g.genre_id, g.genre FROM genre g " +
            "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
            "WHERE fg.film_id IN (%s) ORDER BY g.genre_id";

    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String FIND_POPULAR_QUERY = "SELECT f.* FROM films f " +
            "LEFT JOIN likes l ON f.film_id = l.film_id " +
            "GROUP BY f.film_id " +
            "ORDER BY COUNT(l.user_id) DESC " +
            "LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper, Film.class);
    }

    @Override
    public Film create(Film film) {
        Long ratingId = (film.getRating() != null) ? film.getRating().getId() : null;

        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                ratingId
        );
        film.setId(id);

        if (film.getGenre() != null && !film.getGenre().isEmpty()) {
            for (Genre g : film.getGenre()) {
                jdbc.update(INSERT_FILM_GENRE_QUERY, id, g.getId());
            }
            loadGenresForSingleFilm(film);
        }
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        loadGenresForFilmList(films);
        loadLikesForFilmList(films);
        return films;
    }

    @Override
    public Film update(Film newFilm) {
        if (!containsFilm(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        Long ratingId = (newFilm.getRating() != null) ? newFilm.getRating().getId() : null;

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
        if (newFilm.getGenre() != null && !newFilm.getGenre().isEmpty()) {
            for (Genre g : newFilm.getGenre()) {
                jdbc.update(INSERT_FILM_GENRE_QUERY, newFilm.getId(), g.getId());
            }
        }
        loadGenresForSingleFilm(newFilm);
        return newFilm;
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
        filmOptional.ifPresent(film -> {
            loadGenresForSingleFilm(film);
            List<Long> userLikes = jdbc.query(FIND_LIKES_QUERY, (rs, rowNum) ->
                    rs.getLong("user_id"), film.getId());
            film.setLikes(new HashSet<>(userLikes));
        });
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
        loadLikesForFilmList(films);
        return films;
    }

    private void loadGenresForSingleFilm(Film film) {
        String query = String.format(FIND_GENRES_FOR_FILMS, film.getId());
        List<Genre> genres = jdbc.query(query, (rs, rowNum) ->
                Genre.fromId(rs.getLong("genre_id"))
        );
        film.setGenre(new java.util.LinkedHashSet<>(genres));
    }

    private void loadGenresForFilmList(Collection<Film> films) {
        if (films.isEmpty()) return;

        String inParams = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(", "));

        String query = String.format(FIND_GENRES_FOR_FILMS, inParams);
        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));

        jdbc.query(query, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = Genre.fromId(rs.getLong("genre_id"));

            Film film = filmMap.get(filmId);
            if (film != null) {
                if (film.getGenre() == null) {
                    film.setGenre(new java.util.LinkedHashSet<>());
                }
                film.getGenre().add(genre);
            }
            return null;
        });
    }

    private void loadLikesForFilmList(Collection<Film> films) {
        if (films.isEmpty()) return;

        String inParams = films.stream()
                .map(f -> String.valueOf(f.getId()))
                .collect(Collectors.joining(", "));

        String query = "SELECT * FROM likes WHERE film_id IN (" + inParams + ")";
        Map<Long, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getId, f -> f));

        jdbc.query(query, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");

            Film film = filmMap.get(filmId);
            if (film != null) {
                if (film.getLikes() == null) {
                    film.setLikes(new HashSet<>());
                }
                film.getLikes().add(userId);
            }
            return null;
        });
    }
}