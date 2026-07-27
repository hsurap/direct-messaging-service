package com.parush.messaging_service.utils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

public class CursorUtil {

  public static String encode(Instant instant) {
    return Base64.getEncoder().encodeToString(instant.toString().getBytes(StandardCharsets.UTF_8));
  }

  public static Instant decode(String cursor) {
    String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
    return Instant.parse(decoded);
  }
}