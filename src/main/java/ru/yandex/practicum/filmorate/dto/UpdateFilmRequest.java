package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.AfterDate;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateFilmRequest {
    @NotNull(message = "Id должен быть указан")
    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Длина описания не может быть больше 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    @AfterDate(message = "Дата релиза фильма должна быть после {value}")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    @com.fasterxml.jackson.annotation.JsonProperty("genres")
    private Set<GenreDto> genres;

    @com.fasterxml.jackson.annotation.JsonProperty("mpa")
    private RatingDto rating;

    @Data
    public static class GenreDto {
        private Long id;
    }

    @Data
    public static class RatingDto {
        private Long id;
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return releaseDate != null;
    }

    public boolean hasDuration() {
        return duration != null;
    }

    public boolean hasRating() {
        return rating != null && rating.getId() != null;
    }

    public boolean hasGenre() {
        return genres != null && !genres.isEmpty();
    }
}
