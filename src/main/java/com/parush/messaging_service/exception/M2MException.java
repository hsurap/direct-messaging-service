package com.parush.messaging_service.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Data
@NoArgsConstructor
public class M2MException extends RuntimeException {

  private String code;

  public M2MException(String message) {
    super(message);
  }

  public M2MException(String message, String code) {
    super(message);
    this.code = code;
  }

  public M2MException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
