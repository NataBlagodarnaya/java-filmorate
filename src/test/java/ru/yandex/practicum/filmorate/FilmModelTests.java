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
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc //нужна для тестирования обработчика ошибок, так как он работает с аннотациями
class FilmModelTests {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; //для конвертации объекта в JSON

    Film film = new Film();

    @BeforeEach
    void makeGoodFilm() {
        film.setName("Золушка");
        film.setDescription("Хороший фильм");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(90);
    }

    private ResultActions runSameActions(Film film) throws Exception { //вынесла дублирующийся код в отдельный метод
        String badFilmJson = objectMapper.writeValueAsString(film);
        //делаем запрос
        return mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badFilmJson))
                .andExpect(status().isBadRequest()); //ожидаем ошибку с кодом 400
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldReturnBadRequestWhenFilmNameIsBlank() throws Exception {
        film.setName("   ");
        runSameActions(film)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));
    }

    @Test
    void shouldReturnBadRequestWhenFilmNameIsNull() throws Exception {
        film.setName(null);
        runSameActions(film)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));
    }

    @Test
    void shouldReturnBadRequestWhenFilmDescriptionBigger() throws Exception {
        film.setDescription("a".repeat(201));
        runSameActions(film)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.description")
                        .value("Длина описания не может быть больше 200 символов"));
    }

    @Test
    void shouldReturnBadRequestWhenFilmReleaseDateIsBefore() throws Exception {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        runSameActions(film)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.releaseDate")
                        .value("Дата релиза фильма должна быть после 1895-12-27"));
    }

    @Test
    void shouldReturnBadRequestWhenFilmDurationIsNegative() throws Exception {
        film.setDuration(-345);
        runSameActions(film)
                //ожидаем какое сообщение об ошибке увидит пользователь
                .andExpect(jsonPath("$.duration")
                        .value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void shouldReturnBadRequestWhenEmptyRequest() throws Exception {
        film.setName(null);
        film.setDescription(null);
        film.setReleaseDate(null);
        film.setDuration(null);
        runSameActions(film)
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"))
                .andExpect(jsonPath("$.releaseDate")
                        .value("Дата релиза должна быть указана"));
    }
}