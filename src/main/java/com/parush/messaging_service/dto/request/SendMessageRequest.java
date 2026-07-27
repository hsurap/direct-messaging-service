package com.parush.messaging_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

  @NotBlank
  private String senderId;

  @NotBlank
  private String recipientId;

  @NotBlank
  private String content;
}