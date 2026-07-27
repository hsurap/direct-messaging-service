package com.parush.messaging_service.controller;

import com.parush.messaging_service.dto.response.ConversationResponse;
import com.parush.messaging_service.service.ConversationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

  private static final Logger log = LoggerFactory.getLogger(ConversationController.class);

  private final ConversationService conversationService;

  public ConversationController(ConversationService conversationService) {
    this.conversationService = conversationService;
  }

  @GetMapping("/{userId}/conversations")
  public ResponseEntity<List<ConversationResponse>> getConversations(@PathVariable final String userId) {
    log.info("Received request to list conversations for user {}", userId);
    final List<ConversationResponse> conversations = conversationService.getConversationsForUser(userId);
    return ResponseEntity.ok(conversations);
  }

}
