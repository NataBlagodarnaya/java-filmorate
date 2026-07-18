package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Rating {
    G(1L, "G"),
    PG(2L, "PG"),
    PG_13(3L, "PG-13"),
    R(4L, "R"),
    NC_17(5L, "NC-17");

    private final Long id;
    private final String name;

    Rating(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Rating fromId(Long id) {
        for (Rating rating : values()) {
            if (rating.id.equals(id)) {
                return rating;
            }
        }
        throw new NotFoundException("Рейтинг с id = " + id + " не найден");
    }
}