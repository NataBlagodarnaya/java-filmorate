package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class RatingService {

    public Collection<Rating> findAll() {
        log.info("Запрошен список всех рейтингов");
        return List.of(Rating.values());
    }

    public Rating findById(Long id) {
        log.info("Запрошен рейтинг с id = {}", id);
        return Rating.fromId(id);
    }
}
