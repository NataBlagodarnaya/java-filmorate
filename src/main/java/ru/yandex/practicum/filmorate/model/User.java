package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validator.OnUpdate;

import java.time.LocalDate;

@Data
public class User {
    @NotNull(message = "Id должен быть указан", groups = OnUpdate.class)
    private Long id;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта введена некорректно")
    private String email;
    @NotBlank(message = "Логин пользователя не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;
    private String name;
    @PastOrPresent(message = "Дата рождения пользователя не может быть в будущем")
    private LocalDate birthday;
}
