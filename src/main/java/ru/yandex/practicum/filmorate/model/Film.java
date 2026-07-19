package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.filmorate.validator.AfterDate;
import ru.yandex.practicum.filmorate.validator.OnUpdate;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Film.
     */
    @Getter
    @Setter
    @ToString
    public class Film {
    @NotNull(message = "Id должен быть указан", groups = OnUpdate.class) //только при обновлении
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

    private Set<Genre> genres = new LinkedHashSet<>();

    private Rating mpa;
}