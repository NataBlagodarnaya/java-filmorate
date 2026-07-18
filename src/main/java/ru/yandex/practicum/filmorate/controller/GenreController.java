package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    @GetMapping
    public Collection<Genre> findAll() {
        log.info("Получен запрос GET /genres на получение всех жанров");
        return Arrays.asList(Genre.values());
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable Long id) {
        log.info("Получен запрос GET /genres/{} на получение жанра по ID", id);
        try {
            return Genre.fromId(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }
}