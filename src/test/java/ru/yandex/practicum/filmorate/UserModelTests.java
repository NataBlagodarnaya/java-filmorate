package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc //нужна для тестирования обработчика ошибок, так как он работает с аннотациями
class UserModelTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; //для конвертации объекта в JSON

    User user = new User();

    @BeforeEach
    void makeGoodUser() {
        user.setEmail("user@mail.ru");
        user.setLogin("user123");
        user.setName("Nata");
        user.setBirthday(LocalDate.of(1990, 02, 15));
    }

    private ResultActions runSameActions(User user) throws Exception { //вынесла дублирующийся код в отдельный метод
        String baduserJson = objectMapper.writeValueAsString(user);
        //делаем запрос
        return mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baduserJson))
                .andExpect(status().isBadRequest()); //ожидаем ошибку с кодом 400
    }

    @Test
    void shouldReturnBadRequestWhenBadUserEmail() throws Exception {
        user.setEmail("sdf@");
        runSameActions(user)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.email")
                        .value("Электронная почта введена некорректно"));
    }

    @Test
    void shouldReturnBadRequestWhenEmptyUserEmail() throws Exception {
        user.setEmail("");
        runSameActions(user)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.email")
                        .value("Электронная почта не может быть пустой"));
    }

    @Test
    void shouldReturnBadRequestWhenUserLoginIsBlank() throws Exception {
        user.setLogin("");
        runSameActions(user)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.login").value("Логин пользователя не может быть пустым"));
    }

    @Test
    void shouldReturnBadRequestWhenUserLoginContainsSpace() throws Exception {
        user.setLogin("fsdf ds");
        runSameActions(user)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.login").value("Логин не должен содержать пробелы"));
    }

    @Test
    void shouldReturnBadRequestWhenUserBirthdayIsInFuture() throws Exception {
        user.setBirthday(LocalDate.of(2026, 06, 01));
        runSameActions(user)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.birthday")
                        .value("Дата рождения пользователя не может быть в будущем"));
    }

    @Test
    void shouldReturnBadRequestWhenEmptyRequest() throws Exception {
        user.setEmail(null);
        user.setLogin(null);
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 02, 15));
        runSameActions(user)
                .andExpect(jsonPath("$.login").value("Логин пользователя не может быть пустым"))
                .andExpect(jsonPath("$.email")
                        .value("Электронная почта не может быть пустой"));
    }
}