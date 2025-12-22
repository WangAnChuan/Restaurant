package com.restaurant.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result<String> handleBindException(BindException e) {
        return Result.error(400, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}
