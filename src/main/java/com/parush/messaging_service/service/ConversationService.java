package com.parush.messaging_service.service;

import com.parush.messaging_service.dto.response.ConversationResponse;

import java.util.List;

public interface ConversationService {
  List<ConversationResponse> getConversationsForUser(String userId);
}
