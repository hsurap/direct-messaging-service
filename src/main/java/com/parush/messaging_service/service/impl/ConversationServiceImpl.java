package com.parush.messaging_service.service.impl;

import com.parush.messaging_service.dto.response.ConversationResponse;
import com.parush.messaging_service.entity.Conversation;
import com.parush.messaging_service.exception.M2MException;
import com.parush.messaging_service.repository.ConversationRepository;
import com.parush.messaging_service.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {

  private static final Logger log = LoggerFactory.getLogger(ConversationServiceImpl.class);

  private final ConversationRepository conversationRepository;

  public ConversationServiceImpl(final ConversationRepository conversationRepository) {
    this.conversationRepository = conversationRepository;
  }

  @Override
  public List<ConversationResponse> getConversationsForUser(final String userId) {
    try {
      log.info("Fetching conversations for user {}", userId);
      List<Conversation> conversations = conversationRepository.findByParticipantIdsContainingOrderByLastMessageAtDesc(userId);

      return conversations.stream()
          .map(c -> ConversationResponse.builder()
              .id(c.getId())
              .participantIds(c.getParticipantIds())
              .lastMessagePreview(c.getLastMessagePreview())
              .lastMessageAt(c.getLastMessageAt())
              .build())
          .toList();

    } catch (Exception e) {
      log.error("Error fetching conversations for user {}", userId, e);
      throw new M2MException("Failed to fetch conversations");
    }
  }
}
