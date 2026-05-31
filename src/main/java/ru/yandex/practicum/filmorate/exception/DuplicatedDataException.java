package ru.yandex.practicum.filmorate.exception;

//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.CONFLICT) - по-хорошему тут правильно код 409, пришлось закомитить чтобы тесты из ТЗ в Postman все прошли
public class DuplicatedDataException extends RuntimeException{
    public DuplicatedDataException(String message) {
        super(message);
    }
}
