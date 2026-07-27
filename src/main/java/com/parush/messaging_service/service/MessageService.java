package com.parush.messaging_service.service;

import com.parush.messaging_service.dto.request.SendMessageRequest;
import com.parush.messaging_service.dto.response.MessageResponse;
import com.parush.messaging_service.dto.response.PagedResponse;

public interface MessageService {

  MessageResponse sendMessage(final SendMessageRequest request);

  PagedResponse<MessageResponse> getConversationHistory(String conversationId, String requestingUserId, String cursor, int limit);

}
