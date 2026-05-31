package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validator.OnUpdate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (isExistEmail(user)) {
            log.error("Ошибка 409 Conflict : введенный Email уже используется {}", user);
            throw new DuplicatedDataException("Этот Email уже используется");
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Успешно добавлен новый пользователь {}", user);
        return user;
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    //вспомогательный метод проверки существует ли такой имейл
    private boolean isExistEmail(User user) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
    }

    @PutMapping
    public User update(@Validated(OnUpdate.class) @RequestBody User newUser) {
        // проверяем необходимые условия
        if (isExistEmail(newUser)) {
            log.error("Ошибка 409 Conflict : введенный Email уже используется {}", newUser);
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            // если публикация найдена и все условия соблюдены, обновляем её содержимое
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(newUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
            oldUser.setBirthday(newUser.getBirthday());
            return oldUser;
        }
        log.error("Ошибка 404 Not Found: нет пользователя с указанным id {}", newUser);
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }
}