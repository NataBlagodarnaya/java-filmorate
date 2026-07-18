package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private Film checkAndGetFilm(Long id) {
        if (!filmStorage.containsFilm(id)) {
            log.error("Ошибка 404 Not Found: фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return filmStorage.getFilmById(id).get();
    }

    public void addLike(Long filmId, Long userId) {
        if (!userStorage.containsUser(userId)) {
            log.error("Ошибка 404 Not Found: пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        checkAndGetFilm(filmId);

        filmStorage.addLike(filmId, userId);
        log.info("К фильму с id {} добавлен лайк от пользователя с id {}", filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        Film film = checkAndGetFilm(filmId);

        if (!film.getLikes().contains(userId)) {
            log.error("Ошибка 404 Not Found: нет лайка от указанного пользователя с id {}", userId);
            throw new NotFoundException("Лайк с id " + userId + " не найден.");
        }

        filmStorage.deleteLike(filmId, userId);
        log.info("У фильма с id {} удален лайк от пользователя с id {}", filmId, userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        Film createdFilm = filmStorage.create(film);
        log.info("Создан новый фильм с id {} и названием {}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    public Film update(Film newFilm) {
        Film updatedFilm = filmStorage.update(newFilm);
        log.info("Обновлен фильм с id {} и названием {}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public Optional<Film> getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }
}