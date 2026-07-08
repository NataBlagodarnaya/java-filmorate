package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Object target = ex.getBindingResult().getTarget();
        log.error("Ошибка автоматической валидации для объекта: {}", target);//показываем в логе сам запрос где ошибка

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();//вытаскиваем из каждой ошибки поле
            String errorMessage = error.getDefaultMessage();//вытаскиваем из каждой ошибки сообщение
            errors.put(fieldName, errorMessage);//записываем в мапу чтобы потом это показать пользователю

            log.error("Детали ошибки валидации -> Поле '{}': {}", fieldName, errorMessage);//логируем каждую ошибку
        });

        return errors;//чтобы пользователь увидел в чем ошибка
    }

    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // Статус 409 Conflict, а тесты в Postman из ТЗ просят 500 или 404
    public Map<String, String> handleDuplicatedDataException(DuplicatedDataException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Передаем текст ошибки пользователю

        return error;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Передаем текст ошибки пользователю

        return error;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(ValidationException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Передаем текст ошибки пользователю

        return error;
    }
}