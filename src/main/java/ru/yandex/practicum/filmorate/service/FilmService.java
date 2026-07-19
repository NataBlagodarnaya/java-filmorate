package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final GenreService genreService;
    private final RatingService ratingService;

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
        checkAndGetFilm(filmId);

        if (!filmStorage.containsLike(filmId, userId)) {
            log.error("Ошибка 404 Not Found: нет лайка от указанного пользователя с id {}", userId);
            throw new NotFoundException("Лайк от пользователя с id " + userId + " не найден.");
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

    public Film create(NewFilmRequest request) {

        log.debug("!!! МЕТОД CREATE ВЫЗВАН !!!");
        log.debug("Данные из Postman: mpa объект = {}, mpa ID = {}",
                request.getMpa(),
                (request.getMpa() != null ? request.getMpa().getId() : "null"));


        Rating rating = null;
        if (request.getMpa() != null && request.getMpa().getId() != null) {
            rating = ratingService.getRatingById(request.getMpa().getId());

            log.debug("Результат из ratingService для ID {}: {}", request.getMpa().getId(), rating);
        }

        Set<Genre> genres = new LinkedHashSet<>();
        if (request.getGenres() != null) {
            for (GenreDto genreDto : request.getGenres()) {
                if (genreDto.getId() != null) {
                    Genre genre = genreService.getGenreById(genreDto.getId());
                    genres.add(genre);
                }
            }
        }

        Film film = FilmMapper.mapToFilm(request, rating, genres);

        Film createdFilm = filmStorage.create(film);

        log.info("Создан новый фильм с id {} и названием {}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    public Film update(UpdateFilmRequest request) {
        Film film = checkAndGetFilm(request.getId());

        Rating rating = film.getMpa();
        if (request.hasMpa() && request.getMpa() != null && request.getMpa().getId() != null) {
            rating = ratingService.getRatingById(request.getMpa().getId());
        }

        Set<Genre> genres = film.getGenres();
        if (request.hasGenre() && request.getGenres() != null) {
            genres = new LinkedHashSet<>();
            for (GenreDto genreDto : request.getGenres()) {
                if (genreDto.getId() != null) {
                    genres.add(genreService.getGenreById(genreDto.getId()));
                }
            }
        }

        Film updatedFieldsFilm = FilmMapper.updateFilmFields(film, request, rating, genres);

        Film updatedFilm = filmStorage.update(updatedFieldsFilm);
        log.info("Обновлен фильм с id {} и названием {}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public Optional<Film> getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }
}