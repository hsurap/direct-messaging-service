package com.parush.messaging_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
  private String id;
  private List<String> participantIds;
  private String lastMessagePreview;
  private Instant lastMessageAt;
}
