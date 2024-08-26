package com.primarchan.backend.handler;

import com.primarchan.backend.exception.ForbiddenException;
import com.primarchan.backend.exception.RateLimitException;
import com.primarchan.backend.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlerResourceNotFoundException(ResourceNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handlerResourceNotFoundException(RateLimitException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handlerResourceNotFoundException(ForbiddenException ex) {
        return ex.getMessage();
    }

}
