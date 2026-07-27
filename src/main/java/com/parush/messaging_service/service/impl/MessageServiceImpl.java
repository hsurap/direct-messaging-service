package com.parush.messaging_service.service.impl;

import com.parush.messaging_service.dto.response.PagedResponse;
import com.parush.messaging_service.entity.Conversation;
import com.parush.messaging_service.entity.Message;
import com.parush.messaging_service.enums.MessageStatus;
import com.parush.messaging_service.exception.M2MException;
import com.parush.messaging_service.utils.CursorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.parush.messaging_service.dto.request.SendMessageRequest;
import com.parush.messaging_service.dto.response.MessageResponse;
import com.parush.messaging_service.repository.ConversationRepository;
import com.parush.messaging_service.repository.MessageRepository;
import com.parush.messaging_service.service.MessageService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

  private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

  private final MessageRepository messageRepository;
  private final ConversationRepository conversationRepository;

  public MessageServiceImpl(final MessageRepository messageRepository, final ConversationRepository conversationRepository) {
    this.messageRepository = messageRepository;
    this.conversationRepository = conversationRepository;
  }

  @Override
  public MessageResponse sendMessage(final SendMessageRequest request) {

    log.info("Sending message from {} to {}", request.getSenderId(), request.getRecipientId());

    if (request.getSenderId().equals(request.getRecipientId())) {
      throw new M2MException("Sender and recipient cannot be the same user");
    }

    final Conversation conversation = findOrCreateConversation(request.getSenderId(), request.getRecipientId());

    final Instant now = Instant.now();
    Message message = Message.builder()
        .conversationId(conversation.getId())
        .senderId(request.getSenderId())
        .recipientId(request.getRecipientId())
        .content(request.getContent())
        .createdAt(now)
        .status(MessageStatus.SENT)
        .build();

    message = messageRepository.save(message);

    log.info("Message {} saved in conversation {}", message.getId(), conversation.getId());

    conversation.setLastMessagePreview(request.getContent());
    conversation.setLastMessageAt(now);
    conversationRepository.save(conversation);

    return MessageResponse.builder()
        .id(message.getId())
        .conversationId(message.getConversationId())
        .senderId(message.getSenderId())
        .recipientId(message.getRecipientId())
        .content(message.getContent())
        .createdAt(message.getCreatedAt())
        .status(message.getStatus())
        .build();

  }

  @Override
  public PagedResponse<MessageResponse> getConversationHistory(final String conversationId, final String requestingUserId, final String cursor, final int limit) {
    log.info("Fetching history for conversation {} requested by {}", conversationId, requestingUserId);

    Conversation conversation = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new M2MException("Conversation not found", "CONVERSATION_NOT_FOUND"));

    if (!conversation.getParticipantIds().contains(requestingUserId)) {
      log.error("User {} attempted to access conversation {} they are not part of",
          requestingUserId, conversationId);
      throw new M2MException("You are not authorized to view this conversation", "FORBIDDEN");
    }

    int safeLimit = Math.min(Math.max(limit, 1), 100); // if bigger number is passed so that why's to guard it

    // fetching 1 extra to get to know is there any pending left or not
    org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, safeLimit + 1);

    List<Message> messages;

    if (cursor == null || cursor.isBlank()) {
      messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    } else {
      Instant cursorInstant = CursorUtil.decode(cursor);
      messages = messageRepository.findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
          conversationId, cursorInstant, pageable);
    }

    boolean hasMore = messages.size() > safeLimit;

    List<Message> pageItems = hasMore ? messages.subList(0, safeLimit) : messages;
    String nextCursor = hasMore
        ? CursorUtil.encode(pageItems.get(pageItems.size() - 1).getCreatedAt())
        : null;

    List<MessageResponse> responses = pageItems.stream()
        .map(m -> MessageResponse.builder()
            .id(m.getId())
            .conversationId(m.getConversationId())
            .senderId(m.getSenderId())
            .recipientId(m.getRecipientId())
            .content(m.getContent())
            .createdAt(m.getCreatedAt())
            .status(m.getStatus())
            .build())
        .toList();

    return PagedResponse.<MessageResponse>builder()
        .items(responses)
        .nextCursor(nextCursor)
        .hasMore(hasMore)
        .build();
  }

  private Conversation findOrCreateConversation(final String userA, final String userB) {
    return conversationRepository
        .findByParticipants(userA, userB)
        .orElseGet(() -> {
          log.info("No existing conversation between {} and {}, creating new one", userA, userB);
          Conversation newConversation = Conversation.builder()
              .participantIds(java.util.List.of(userA, userB))
              .createdAt(Instant.now())
              .build();
          return conversationRepository.save(newConversation);
        });
  }

}
