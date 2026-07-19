package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.mapper.RatingMapper;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class RatingController {

    private final RatingService ratingService;

    @GetMapping
    public Collection<RatingDto> findAll() {
        log.info("Получен запрос GET /mpa на получение всех рейтингов");
        return  ratingService.findAll().stream()
                .map(RatingMapper::mapToRatingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public RatingDto findById(@PathVariable Long id) {
        log.info("Получен запрос GET /mpa/{} на получение рейтинга по ID", id);
        Rating rating = ratingService.getRatingById(id);
        return RatingMapper.mapToRatingDto(rating);
    }
}

