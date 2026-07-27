package com.parush.messaging_service.entity;

import com.parush.messaging_service.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {

  @Id
  private String id;

  @Indexed
  private String conversationId;

  private String senderId;
  private String recipientId;
  private String content;

  @Indexed
  private Instant createdAt;

  private MessageStatus status;
}
