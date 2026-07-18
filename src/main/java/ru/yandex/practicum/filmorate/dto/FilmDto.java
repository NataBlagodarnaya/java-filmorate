package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    private Set<Long> likes = new HashSet<>();
    @JsonProperty("genres")
    private Set<Genre> genres = new HashSet<>();
    @JsonProperty("mpa")
    private RatingResponseDto mpa;

    @Data
    public static class RatingResponseDto {
        private Long id;
        private String name;
    }
}
