package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping
    public Collection<Rating> findAll() {
        log.info("Получен запрос GET /mpa на получение всех рейтингов");
        return ratingService.findAll();
    }

    @GetMapping("/{id}")
    public Rating findById(@PathVariable Long id) {
        log.info("Получен запрос GET /mpa/{} на получение рейтинга по ID");
        return ratingService.findById(id);
    }
}

