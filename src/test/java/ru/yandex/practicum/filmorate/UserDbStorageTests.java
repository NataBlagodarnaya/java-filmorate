package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class})
@AutoConfigureTestDatabase
class UserDbStorageTests {

    private final UserDbStorage userStorage;
    private User createdUser;

    @Autowired
    public UserDbStorageTests(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @BeforeEach
    public void setUp() {
        User newUser = new User();
        newUser.setName("Тестовый Пользователь");
        newUser.setEmail("test-user@yandex.ru");
        newUser.setLogin("test_login");
        newUser.setBirthday(LocalDate.of(1983, 2, 20));

        createdUser = userStorage.create(newUser);
    }

    //create
    @Test
    public void testCreateUser() {
        assertThat(createdUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Тестовый Пользователь")
                .hasFieldOrPropertyWithValue("email", "test-user@yandex.ru")
                .hasFieldOrPropertyWithValue("login", "test_login")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1983, 2, 20));

        assertThat(createdUser.getId())
                .isNotNull()
                .isPositive();
    }

    //findAll
    @Test
    public void testFindAll() {
        User user2 = new User();
        user2.setName("Name2");
        user2.setEmail("user2@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User savedUser2 = userStorage.create(user2);

        Collection<User> users = userStorage.findAll();

        assertThat(users)
                .isNotNull()
                .hasSize(2)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(createdUser.getId(), savedUser2.getId());
    }

    //update
    @Test
    public void testUpdate() {
        createdUser.setName("New Name");
        createdUser.setLogin("new_login");
        User updatedUser = userStorage.update(createdUser);

        Optional<User> userOptional = userStorage.getUserById(updatedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("name", "New Name");
                    assertThat(u).hasFieldOrPropertyWithValue("login", "new_login");
                });
    }

    //delete
    @Test
    public void testDelete() {
        userStorage.delete(createdUser);

        Optional<User> userOptional = userStorage.getUserById(createdUser.getId());
        assertThat(userOptional).isEmpty();
    }

    //containsUser
    @Test
    public void testContainsUser() {
        boolean exists = userStorage.containsUser(createdUser.getId());
        boolean notExists = userStorage.containsUser(9999L);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    //getUserById
    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.getUserById(createdUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", createdUser.getId())
                );
    }

    //addFriend getFriends deleteFriend
    @Test
    public void testAddFriendAndGetFriendsAndDeleteFriend() {
        User friend = new User();
        friend.setName("Друг");
        friend.setEmail("friend-unique@mail.ru");
        friend.setLogin("friend_login");
        friend.setBirthday(LocalDate.of(1990, 5, 10));
        User savedFriend = userStorage.create(friend);

        userStorage.addFriend(createdUser.getId(), savedFriend.getId());

        Collection<User> friends = userStorage.getFriends(createdUser.getId());
        assertThat(friends)
                .isNotNull()
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(savedFriend.getId());

        Optional<User> loadedUser = userStorage.getUserById(createdUser.getId());
        assertThat(loadedUser).isPresent();
        assertThat(loadedUser.get().getFriends())
                .isNotNull()
                .hasSize(1)
                .contains(savedFriend.getId());

        userStorage.deleteFriend(createdUser.getId(), savedFriend.getId());
        Collection<User> friendsAfterDelete = userStorage.getFriends(createdUser.getId());
        assertThat(friendsAfterDelete).isEmpty();
    }

    //getCommonFriends
    @Test
    public void testGetCommonFriends() {
        User user2 = new User();
        user2.setName("Второй пользователь");
        user2.setEmail("user2-unique@mail.ru");
        user2.setLogin("login2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));
        User savedUser2 = userStorage.create(user2);

        User commonFriend = new User();
        commonFriend.setName("Общий друг");
        commonFriend.setEmail("common-friend@mail.ru");
        commonFriend.setLogin("common_login");
        commonFriend.setBirthday(LocalDate.of(1992, 1, 1));
        User savedCommonFriend = userStorage.create(commonFriend);

        userStorage.addFriend(createdUser.getId(), savedCommonFriend.getId());
        userStorage.addFriend(savedUser2.getId(), savedCommonFriend.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(createdUser.getId(), savedUser2.getId());

        assertThat(commonFriends)
                .isNotNull()
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(savedCommonFriend.getId());
    }
}