package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        log.info("Получен HTTP-запрос GET /films на получение всех фильмов");
        return filmService.findAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable long id) {
        log.info("Получен запрос GET /films/{}", id);
        return filmService.getFilmById(id)
                .map(FilmMapper::mapToFilmDto)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос DELETE /films/{}/like/{}", id, userId);
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен запрос GET /films/popular?count={}", count);
        return filmService.getPopularFilms(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest request) {
        log.info("Получен запрос POST /films на создание фильма: {}", request.getName());
        Film filmEntity = FilmMapper.mapToFilm(request);
        Film createdFilm = filmService.create(filmEntity);
        return FilmMapper.mapToFilmDto(createdFilm);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest request) {
        log.info("Получен запрос PUT /films на обновление фильма с id={}", request.getId());
        Film existingFilm = filmService.getFilmById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getId() + " не найден"));

        Film updatedFilmFields = FilmMapper.updateFilmFields(existingFilm, request);
        Film savedFilm = filmService.update(updatedFilmFields);
        return FilmMapper.mapToFilmDto(savedFilm);
    }
}