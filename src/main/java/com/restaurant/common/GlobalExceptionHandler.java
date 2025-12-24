package com.restaurant.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;

// 2. GlobalExceptionHandler.java - 全局异常处理器
// 作用：统一捕获所有 Controller 层的异常
// 处理的异常：
// Exception.class - 所有未捕获的异常
// BindException.class - 参数验证异常
// 优点：代码简洁、统一格式、易于维护

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
