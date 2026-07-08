package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем новый фильм в памяти приложения
        films.put(film.getId(), film);
        log.info("Успешно добавлен новый фильм {}", film);
        return film;
    }

    // вспомогательный метод для генерации идентификатора нового фильма
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film update(Film newFilm) {
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            // если фильм найден и все условия соблюдены, обновляем его
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Успешно изменен фильм {}", oldFilm);
            return oldFilm;
        }
        log.error("Ошибка 404 Not Found: нет фильма с указанным id {}", newFilm);
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public void delete(Film film) {
        if (films.containsKey(film.getId())) {
            films.remove(film.getId());
            log.info("Успешно удален фильм c id {}", film.getId());
        }
        log.error("Ошибка 404 Not Found: нет фильма с указанным id {}", film);
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
    }

    @Override
    public boolean containsFilm(Long id) {
        return films.containsKey(id);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }
}