package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

@Repository
public class RatingDbStorage extends BaseDbStorage<Rating> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM rating ORDER BY rating_id ASC";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM rating WHERE rating_id = ?";

    public RatingDbStorage(JdbcTemplate jdbc, RatingRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Rating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Rating> getRatingById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}