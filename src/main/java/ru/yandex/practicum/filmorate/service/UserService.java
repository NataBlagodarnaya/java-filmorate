package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private User checkAndGetUser(Long id) {
        if (!userStorage.containsUser(id)) {
            log.error("Ошибка 404 Not Found: пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return userStorage.getUserById(id).get();
    }

    public void addFriend(Long userId, Long friendID) {
        if (userId.equals(friendID)) {
            log.error("Ошибка валидации. id пользователя и друга совпадают {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }
        checkAndGetUser(userId);
        checkAndGetUser(friendID);

        userStorage.addFriend(userId, friendID);
        log.info("В БД успешно добавлена связь: пользователю с id {} добавлен друг с id {}", userId, friendID);
    }


    public void deleteFriend(Long userId, Long friendID) {
        checkAndGetUser(userId);
        checkAndGetUser(friendID);

        userStorage.deleteFriend(userId, friendID);
        log.info("У пользователя с id {} удален друг с id {}", userId, friendID);
    }

    public Collection<User> getUserFriendsList(Long userId) {
        checkAndGetUser(userId);
        return userStorage.getFriends(userId);
    }

    public Collection<User> getUsersSameFriendsList(Long user1Id, Long user2Id) {
        checkAndGetUser(user1Id);
        checkAndGetUser(user2Id);
        return userStorage.getCommonFriends(user1Id, user2Id);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        User createdUser = userStorage.create(user);
        log.info("Создан новый пользователь с id: {}", createdUser.getId());
        return createdUser;
    }

    public User update(User newUser) {
        User updatedUser = userStorage.update(newUser);
        log.info("Обновлен пользователь с id: {}", updatedUser.getId());
        return updatedUser;
    }

    public Optional<User> getUserById(Long id) {
        return userStorage.getUserById(id);
    }
}