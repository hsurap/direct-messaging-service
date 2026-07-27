package com.parush.messaging_service.dto.response;

import com.parush.messaging_service.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private String id;
  private String conversationId;
  private String senderId;
  private String recipientId;
  private String content;
  private Instant createdAt;
  private MessageStatus status;
}
