package com.parush.messaging_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(M2MException.class)
  public ResponseEntity<Map<String, Object>> handleM2MException(M2MException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("error", ex.getMessage());
    if (ex.getCode() != null) {
      body.put("code", ex.getCode());
    }

    HttpStatus status = getStatusFromAnnotation(ex);
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(err ->
        errors.put(err.getField(), err.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
    return ResponseEntity.internalServerError().body(Map.of("error", "Something went wrong"));
  }

  private HttpStatus getStatusFromAnnotation(M2MException ex) {
    org.springframework.web.bind.annotation.ResponseStatus annotation =
        ex.getClass().getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class);
    return annotation != null ? annotation.value() : HttpStatus.BAD_REQUEST;
  }
}
