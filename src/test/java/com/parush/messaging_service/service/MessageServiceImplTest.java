package com.parush.messaging_service.service;

import com.parush.messaging_service.dto.request.SendMessageRequest;
import com.parush.messaging_service.dto.response.MessageResponse;
import com.parush.messaging_service.dto.response.PagedResponse;
import com.parush.messaging_service.entity.Conversation;
import com.parush.messaging_service.entity.Message;
import com.parush.messaging_service.exception.M2MException;
import com.parush.messaging_service.repository.ConversationRepository;
import com.parush.messaging_service.repository.MessageRepository;
import com.parush.messaging_service.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private ConversationRepository conversationRepository;

  @InjectMocks
  private MessageServiceImpl messageService;

  private Conversation conversation;

  @BeforeEach
  void setUp() {
    conversation = Conversation.builder()
        .id("conv1")
        .participantIds(List.of("alice", "bob"))
        .createdAt(Instant.now())
        .build();
  }

  @Test
  void sendMessage_createsNewConversation_whenNoneExists() {
    SendMessageRequest request = new SendMessageRequest();
    request.setSenderId("alice");
    request.setRecipientId("bob");
    request.setContent("hey there");

    when(conversationRepository.findByParticipants("alice", "bob"))
        .thenReturn(Optional.empty());
    when(conversationRepository.save(any(Conversation.class)))
        .thenReturn(conversation);
    when(messageRepository.save(any(Message.class)))
        .thenAnswer(invocation -> {
          Message m = invocation.getArgument(0);
          m.setId("msg1");
          return m;
        });

    MessageResponse response = messageService.sendMessage(request);

    assertNotNull(response);
    assertEquals("msg1", response.getId());
    assertEquals("alice", response.getSenderId());
    assertEquals("bob", response.getRecipientId());
    assertEquals("hey there", response.getContent());
    verify(conversationRepository, times(2)).save(any(Conversation.class));
  }

  @Test
  void sendMessage_reusesExistingConversation_whenOneAlreadyExists() {
    SendMessageRequest request = new SendMessageRequest();
    request.setSenderId("alice");
    request.setRecipientId("bob");
    request.setContent("second message");

    when(conversationRepository.findByParticipants("alice", "bob"))
        .thenReturn(Optional.of(conversation));
    when(messageRepository.save(any(Message.class)))
        .thenAnswer(invocation -> {
          Message m = invocation.getArgument(0);
          m.setId("msg2");
          return m;
        });

    MessageResponse response = messageService.sendMessage(request);

    assertEquals("conv1", response.getConversationId());
    // conversation should NOT be created again, only updated (saved once for the preview update)
    verify(conversationRepository, never()).save(argThat(c -> c.getId() == null));
  }

  @Test
  void sendMessage_throwsException_whenSenderEqualsRecipient() {
    SendMessageRequest request = new SendMessageRequest();
    request.setSenderId("alice");
    request.setRecipientId("alice");
    request.setContent("talking to myself");

    M2MException exception = assertThrows(M2MException.class,
        () -> messageService.sendMessage(request));

    assertEquals("Sender and recipient cannot be the same user", exception.getMessage());
    verifyNoInteractions(messageRepository);
  }

  @Test
  void getConversationHistory_returnsFirstPage_withCorrectHasMoreFlag() {
    Message m1 = buildMessage("msg1", Instant.parse("2026-07-27T10:00:00Z"));
    Message m2 = buildMessage("msg2", Instant.parse("2026-07-27T09:59:00Z"));
    Message m3 = buildMessage("msg3", Instant.parse("2026-07-27T09:58:00Z"));

    when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation));
    when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq("conv1"), any(Pageable.class)))
        .thenReturn(List.of(m1, m2, m3));

    PagedResponse<MessageResponse> response =
        messageService.getConversationHistory("conv1", "alice", null, 2);

    assertEquals(2, response.getItems().size());
    assertTrue(response.isHasMore());
    assertNotNull(response.getNextCursor());
    // ensure order preserved, no message dropped or duplicated within the page
    assertEquals("msg1", response.getItems().get(0).getId());
    assertEquals("msg2", response.getItems().get(1).getId());
  }

  @Test
  void getConversationHistory_returnsLastPage_withNoMoreDataFlag() {
    Message m1 = buildMessage("msg1", Instant.parse("2026-07-27T10:00:00Z"));

    when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation));
    when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq("conv1"), any(Pageable.class)))
        .thenReturn(List.of(m1)); // fewer results than limit -> no more pages

    PagedResponse<MessageResponse> response =
        messageService.getConversationHistory("conv1", "alice", null, 5);

    assertEquals(1, response.getItems().size());
    assertFalse(response.isHasMore());
    assertNull(response.getNextCursor());
  }

  @Test
  void getConversationHistory_throwsException_whenUserNotPartOfConversation() {
    when(conversationRepository.findById("conv1")).thenReturn(Optional.of(conversation));

    M2MException exception = assertThrows(M2MException.class,
        () -> messageService.getConversationHistory("conv1", "stranger", null, 10));

    assertEquals("You are not authorized to view this conversation", exception.getMessage());
    verify(messageRepository, never()).findByConversationIdOrderByCreatedAtDesc(any(), any());
  }

  @Test
  void getConversationHistory_throwsException_whenConversationDoesNotExist() {
    when(conversationRepository.findById("missingConv")).thenReturn(Optional.empty());

    assertThrows(M2MException.class,
        () -> messageService.getConversationHistory("missingConv", "alice", null, 10));
  }

  private Message buildMessage(String id, Instant createdAt) {
    return Message.builder()
        .id(id)
        .conversationId("conv1")
        .senderId("alice")
        .recipientId("bob")
        .content("test content " + id)
        .createdAt(createdAt)
        .build();
  }

}
