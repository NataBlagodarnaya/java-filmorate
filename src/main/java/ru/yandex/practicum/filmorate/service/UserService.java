package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserStorage userStorage;

    private User checkAndGetUser(Long id) {
        if (!userStorage.containsUser(id)) {
            log.error("Ошибка 404 Not Found: пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return userStorage.getUserById(id).get();
    }

    //добавление в друзья
    public void addFriend(Long userId, Long friendID) {
        if (userId.equals(friendID)) {
            log.error("Ошибка валидации. id пользователя и друга совпадают {}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }
        User user = checkAndGetUser(userId);
        user.getFriends().add(friendID);
        log.info("Пользователю с id {} добавлен друг с id {}", userId, friendID);
        User friend = checkAndGetUser(friendID);
        friend.getFriends().add(userId);
        log.info("Пользователю с id {} добавлен друг с id {}", friendID, userId);
    }

    //удаление из друзей
    public void deleteFriend(Long userId, Long friendID) {
        User user = checkAndGetUser(userId);
        user.getFriends().remove(friendID);
        log.info("У пользователя с id {} удален друг с id {}", userId, friendID);
        User friend = checkAndGetUser(friendID);
        friend.getFriends().remove(userId);
        log.info("У пользователя с id {} удален друг с id {}", friendID, userId);
    }

    //вывод списка друзей
    public Collection<User> detUserFriendsList(Long userId) {
        User user = checkAndGetUser(userId);
        return user.getFriends().stream()
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    //вывод списка общих друзей
    public Collection<User> getUsersSameFriendsList(Long user1Id, Long user2Id) {
        User user1 = checkAndGetUser(user1Id);
        User user2 = checkAndGetUser(user2Id);
        return user1.getFriends().stream()
                .filter(user2.getFriends()::contains)
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }
}
