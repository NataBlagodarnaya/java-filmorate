package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        java.util.Set<Genre> modelGenres = new java.util.HashSet<>();
        if (request.getGenres() != null) {
            try {
                for (NewFilmRequest.GenreDto dto : request.getGenres()) {
                    if (dto.getId() != null) {
                        modelGenres.add(Genre.fromId(dto.getId()));
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Жанр не найден");
            }
        }
        film.setGenre(modelGenres);

        if (request.getRating() != null && request.getRating().getId() != null) {
            try {
                film.setRating(Rating.fromId(request.getRating().getId()));
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Рейтинг не найден");
            }
        }
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikes(film.getLikes());
        dto.setGenres(film.getGenre());

        if (film.getRating() != null) {
            FilmDto.RatingResponseDto mpaDto = new FilmDto.RatingResponseDto();
            mpaDto.setId(film.getRating().getId());
            mpaDto.setName(film.getRating().getName());
            dto.setMpa(mpaDto);
        } else {
            dto.setMpa(null);
        }

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
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
        if (request.hasRating()) {
            try {
                film.setRating(Rating.fromId(request.getRating().getId()));
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Рейтинг не найден");
            }
        }
        if (request.hasGenre()) {
            java.util.Set<Genre> modelGenres = new java.util.HashSet<>();
            try {
                for (UpdateFilmRequest.GenreDto dto : request.getGenres()) {
                    if (dto.getId() != null) {
                        modelGenres.add(Genre.fromId(dto.getId()));
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Жанр не найден");
            }
            film.setGenre(modelGenres);
        }
        return film;
    }
}