package com.parush.messaging_service.service;

import com.parush.messaging_service.dto.response.ConversationResponse;
import com.parush.messaging_service.entity.Conversation;
import com.parush.messaging_service.repository.ConversationRepository;
import com.parush.messaging_service.service.impl.ConversationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

  @Mock
  private ConversationRepository conversationRepository;

  @InjectMocks
  private ConversationServiceImpl conversationService;

  @Test
  void getConversationsForUser_returnsMappedConversations_sortedByRecentActivity() {
    Conversation conv1 = Conversation.builder()
        .id("conv1")
        .participantIds(List.of("alice", "bob"))
        .lastMessagePreview("see you soon")
        .lastMessageAt(Instant.parse("2026-07-27T10:00:00Z"))
        .build();

    Conversation conv2 = Conversation.builder()
        .id("conv2")
        .participantIds(List.of("alice", "charlie"))
        .lastMessagePreview("sounds good")
        .lastMessageAt(Instant.parse("2026-07-26T09:00:00Z"))
        .build();

    when(conversationRepository.findByParticipantIdsContainingOrderByLastMessageAtDesc("alice"))
        .thenReturn(List.of(conv1, conv2)); // repository already returns them sorted

    List<ConversationResponse> result = conversationService.getConversationsForUser("alice");

    assertEquals(2, result.size());
    assertEquals("conv1", result.get(0).getId());
    assertEquals("see you soon", result.get(0).getLastMessagePreview());
    assertEquals("conv2", result.get(1).getId());
  }

  @Test
  void getConversationsForUser_returnsEmptyList_whenUserHasNoConversations() {
    when(conversationRepository.findByParticipantIdsContainingOrderByLastMessageAtDesc("newUser"))
        .thenReturn(List.of());

    List<ConversationResponse> result = conversationService.getConversationsForUser("newUser");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}