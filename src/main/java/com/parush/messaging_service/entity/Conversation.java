package com.parush.messaging_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
public class Conversation {

  @Id
  private String id;

  private List<String> participantIds; // exactly 2 for 1-1 messaging

  private String lastMessagePreview;
  private Instant lastMessageAt;

  private Instant createdAt;
}
