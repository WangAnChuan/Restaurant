package com.restaurant.common;

import lombok.Data;

// 1. Result.java - 统一响应格式封装
// 作用：为所有 API 提供统一的响应格式
// 核心方法：
// success(data) - 成功响应（带数据）
// success() - 成功响应（无数据）
// error(code, msg) - 失败响应（自定义）
// error(msg) - 失败响应（默认 500）
// 响应格式：{ code, message, data }

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("Success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }
}
