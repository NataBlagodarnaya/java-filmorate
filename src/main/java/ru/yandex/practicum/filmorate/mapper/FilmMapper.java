package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request, Rating rating, Set<Genre> genres) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(rating);
        film.setGenres(genres);
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        RatingDto ratingDto = new RatingDto();
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            ratingDto.setId(film.getMpa().getId());
            ratingDto.setName(film.getMpa().getName());
        } else {
            ratingDto.setId(null);
            ratingDto.setName(null);
        }
        dto.setMpa(ratingDto);

        Set<GenreDto> genreDtos = new LinkedHashSet<>();
        if (film.getGenres() != null) {
            genreDtos = film.getGenres().stream()
                    .map(genre -> {
                        GenreDto genreDto = new GenreDto();
                        genreDto.setId(genre.getId());
                        genreDto.setName(genre.getName());
                        return genreDto;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
        dto.setGenres(genreDtos);

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request, Rating rating, Set<Genre> genres) {
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasMpa()) {
            film.setMpa(rating);
        }
        if (request.hasGenre()) {
            film.setGenres(genres);
        }
        return film;
    }
}