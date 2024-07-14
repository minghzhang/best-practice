package com.example.maincontroller.common.exception;

import com.example.maincontroller.common.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleException(Exception e) {
        return new ResponseEntity<>(Response.failure(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public final ResponseEntity<Response<?>> handleNotFoundException(NoHandlerFoundException ex, ServletWebRequest request) {
        String requestURI = request.getRequest().getRequestURI();
        Response<?> response = new Response<>(false, HttpStatus.NOT_FOUND.value(), requestURI + " Not Found", null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
