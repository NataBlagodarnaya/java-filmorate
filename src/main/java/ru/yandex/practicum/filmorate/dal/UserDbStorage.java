package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String INSERT_QUERY = "INSERT INTO users (username, email, login, birthday_date) " +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String UPDATE_QUERY = "UPDATE users SET username = ?, email = ?, login = ?, " +
            "birthday_date = ? WHERE user_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String CONTAINS_QUERY = "SELECT COUNT(*) FROM users WHERE user_id = ?";

    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT u.* FROM users u " +
            "JOIN friends f ON u.user_id = f.friend_id WHERE f.user_id = ?";
    private static final String FIND_FRIENDS_BY_USER_ID_QUERY = "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT u.* FROM users u " +
            "JOIN friends f1 ON u.user_id = f1.friend_id " +
            "JOIN friends f2 ON u.user_id = f2.friend_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper, User.class);
    }

    @Override
    public User create(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public Collection<User> findAll() {
        Collection<User> users = jdbc.query(FIND_ALL_QUERY, mapper);

        users.forEach(user -> {
            java.util.List<Long> friendIds = jdbc.query(
                    FIND_FRIENDS_BY_USER_ID_QUERY,
                    (rs, rowNum) -> rs.getLong("friend_id"),
                    user.getId()
            );
            user.setFriends(new java.util.HashSet<>(friendIds));
        });

        return users;
    }

    @Override
    public User update(User newUser) {
        if (!containsUser(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        update(
                UPDATE_QUERY,
                newUser.getName(),
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getBirthday(),
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public void delete(User user) {
        if (!delete(DELETE_QUERY, user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
    }

    @Override
    public boolean containsUser(Long id) {
        Integer count = jdbc.queryForObject(CONTAINS_QUERY, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        Optional<User> userOptional = findOne(FIND_BY_ID_QUERY, id);

        userOptional.ifPresent(user -> {
            java.util.List<Long> friendIds = jdbc.query(
                    FIND_FRIENDS_BY_USER_ID_QUERY,
                    (rs, rowNum) -> rs.getLong("friend_id"),
                    user.getId()
            );
            user.setFriends(new java.util.HashSet<>(friendIds));
        });

        return userOptional;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(INSERT_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        return jdbc.query(FIND_FRIENDS_QUERY, mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY, mapper, userId, otherId);
    }
}