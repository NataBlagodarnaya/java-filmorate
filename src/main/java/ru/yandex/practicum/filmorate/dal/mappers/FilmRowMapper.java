package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("title"));
        film.setDescription(resultSet.getString("description"));

        if (resultSet.getDate("release_date") != null) {
            film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        }
        film.setDuration(resultSet.getInt("duration"));

        long rId = resultSet.getLong("rating_id");

        if (!resultSet.wasNull()) {
            Rating rating = new Rating();
            rating.setId(rId);
            rating.setName(resultSet.getString("mpa_name"));
            film.setMpa(rating);
        } else {
            film.setMpa(null);
        }
        return film;
    }
}