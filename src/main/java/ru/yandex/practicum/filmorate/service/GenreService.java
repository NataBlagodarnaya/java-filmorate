package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Service
public class GenreService {

    public Collection<Genre> findAll() {
        log.info("Запрошен список всех жанров из enum");
        return Arrays.asList(Genre.values());
    }

    public Genre findById(int id) {
        log.info("Запрошен жанр с id = {}", id);
        try {
            return Genre.fromId((long) id);
        } catch (IllegalArgumentException e) {
            log.error("Ошибка 404 Not Found: жанр с id {} не найден", id);
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
    }
}