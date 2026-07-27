package com.parush.messaging_service.controller;

import com.parush.messaging_service.dto.request.SendMessageRequest;
import com.parush.messaging_service.dto.response.MessageResponse;
import com.parush.messaging_service.dto.response.PagedResponse;
import com.parush.messaging_service.service.MessageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/messages")
public class MessageController {


  private static final Logger log = LoggerFactory.getLogger(MessageController.class);

  private final MessageService messageService;

  public MessageController(final MessageService messageService) {
    this.messageService = messageService;
  }

  @PostMapping
  public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
    log.info("Received send-message request: sender={}, recipient={}", request.getSenderId(), request.getRecipientId());
    MessageResponse response = messageService.sendMessage(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/conversations/{conversationId}")
  public ResponseEntity<PagedResponse<MessageResponse>> getConversationHistory(
      @PathVariable String conversationId,
      @RequestParam String requestingUserId,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {

    log.info("Received history request for conversation {} by user {}", conversationId, requestingUserId);
    PagedResponse<MessageResponse> response = messageService.getConversationHistory(conversationId, requestingUserId, cursor, limit);
    return ResponseEntity.ok(response);
  }

}
