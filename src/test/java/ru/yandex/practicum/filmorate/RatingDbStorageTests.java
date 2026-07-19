package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.RatingDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({RatingDbStorage.class, RatingRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RatingDbStorageTests {

    private final RatingDbStorage ratingStorage;

    @Test
    public void testFindAllRatings() {
        Collection<Rating> ratings = ratingStorage.findAll();

        assertThat(ratings)
                .isNotNull()
                .isNotEmpty()
                .hasSize(5);

        assertThat(ratings)
                .extracting(Rating::getId)
                .containsExactly(1L, 2L, 3L, 4L, 5L);
    }

    @Test
    public void testGetRatingByValidId() {
        Optional<Rating> ratingOptional = ratingStorage.getRatingById(1L);

        assertThat(ratingOptional)
                .isPresent()
                .hasValueSatisfying(rating -> {
                    assertThat(rating.getId()).isEqualTo(1L);
                    assertThat(rating.getName()).isEqualTo("G");
                });
    }

    @Test
    public void testGetRatingByInvalidId() {
        Optional<Rating> ratingOptional = ratingStorage.getRatingById(9999L);

        assertThat(ratingOptional).isEmpty();
    }
}