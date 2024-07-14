package com.example.maincontroller.common.response;

import lombok.Data;

@Data
public class Response<T> {

    private boolean success;

    private int code;

    private String errorMessage;

    private T data;

    public Response(boolean success, int code, String errorMessage, T data) {
        this.success = success;
        this.code = code;
        this.errorMessage = errorMessage;
        this.data = data;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(true, 200, null, data);
    }

    public static <T> Response<T> failure(int code, String errorMessage) {
        return new Response<>(false, code, errorMessage, null);
    }
}
