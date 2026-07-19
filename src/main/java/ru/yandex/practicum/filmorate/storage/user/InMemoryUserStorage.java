package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();


    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        if (isExistEmail(user)) {
            log.error("Ошибка 409 Conflict : введенный Email уже используется {}", user);
            throw new DuplicatedDataException("Этот Email уже используется");
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Успешно добавлен новый пользователь {}", user);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isExistEmail(User user) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equals(user.getEmail()));
    }

    @Override
    public User update(User newUser) {
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (isExistEmail(newUser) && !newUser.getEmail().equals(oldUser.getEmail())) {
                log.error("Ошибка 409 Conflict : введенный Email уже используется {}", newUser);
                throw new DuplicatedDataException("Этот Email уже используется");
            }
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

    @Override
    public void delete(User user) {
        if (users.containsKey(user.getId())) {
            users.remove(user.getId());
            log.info("Успешно удален пользователь c id {}", user.getId());
        }
        log.error("Ошибка 404 Not Found: нет пользователя с указанным id {}", user);
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    @Override
   public boolean containsUser(Long id) {
        return users.containsKey(id);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        throw new UnsupportedOperationException("InMemoryUserStorage не поддерживается.");
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        throw new UnsupportedOperationException("InMemoryUserStorage не поддерживается.");
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        throw new UnsupportedOperationException("InMemoryUserStorage не поддерживается.");
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        throw new UnsupportedOperationException("InMemoryUserStorage не поддерживается.");
    }

}