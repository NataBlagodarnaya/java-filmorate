package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Получен запрос GET /users на получение всех пользователей");
        return userService.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable long id) {
        log.info("Получен запрос GET /users/{} на получение пользователя по ID", id);
        return userService.getUserById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Получен запрос PUT /users/{}/friends/{} на добавление в друзья", id, friendId);
       userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Получен запрос DELETE /users/{}/friends/{} на удаление из друзей", id, friendId);
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getUserFriendsList(@PathVariable long id) {
        log.info("Получен запрос GET /users/{}/friends на получение списка друзей", id);
        return userService.getUserFriendsList(id).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> getUsersSameFriendsList(@PathVariable long id, @PathVariable long otherId) {
        log.info("Получен запрос GET /users/{}/friends/common/{} на получение общих друзей", id, otherId);
        return userService.getUsersSameFriendsList(id, otherId).stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        log.info("Получен запрос POST /users на создание пользователя с email: {}", request.getEmail());
        User userEntity = UserMapper.mapToUser(request);
        User createdUser = userService.create(userEntity);
        return UserMapper.mapToUserDto(createdUser);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateUserRequest request) {
        log.info("Получен запрос PUT /users на обновление пользователя с id={}", request.getId());
        User existingUser = userService.getUserById(request.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + request.getId() + " не найден"));

        User updatedUserFields = UserMapper.updateUserFields(existingUser, request);
        User savedUser = userService.update(updatedUserFields);
        return UserMapper.mapToUserDto(savedUser);
    }
}