package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private Film checkAndGetFilm(Long id) {
        if (!filmStorage.containsFilm(id)) {
            log.error("Ошибка 404 Not Found: фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return filmStorage.getFilmById(id).get();
    }

    //добавление лайка
    public void addLike(Long filmId, Long userId) {
        if (!userStorage.containsUser(userId)) {
            log.error("Ошибка 404 Not Found: пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        Film film = checkAndGetFilm(filmId);
        film.getLikes().add(userId);
        log.info("К фильму с id {} добавлен лайк от пользователя с id {}", filmId, userId);
    }

    //удаление лайка
    public void deleteLike(Long filmId, Long userId) {
        Film film = checkAndGetFilm(filmId);
        if (!film.getLikes().contains(userId)) {
            log.error("Ошибка 404 Not Found: нет лайка от указанного пользователя с id {}", userId);
            throw new NotFoundException("Лайк с id " + userId + " не найден.");
        }
        film.getLikes().remove(userId);
        log.info("У фильма с id {} удален лайк от пользователя с id {}", filmId, userId);
    }

    //вывод 10 наиболее популярных фильмов
    public Collection<Film> getPopularFilms(Integer count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Optional<Film> getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }
}