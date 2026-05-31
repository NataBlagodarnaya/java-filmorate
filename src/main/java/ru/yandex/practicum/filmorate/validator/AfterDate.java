package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AfterDateValidator.class) // Указываем класс-обработчик
@Target({ElementType.FIELD}) // Аннотацию можно ставить только над полями
@Retention(RetentionPolicy.RUNTIME) // Аннотация будет доступна во время работы программы
public @interface AfterDate {
        String message() default "Дата не может быть раньше {value}";

        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};

        String value() default "1895-12-27";
    }