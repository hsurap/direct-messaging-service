package com.parush.messaging_service.service;

import com.parush.messaging_service.dto.request.SendMessageRequest;
import com.parush.messaging_service.dto.response.MessageResponse;

public interface MessageService {

  MessageResponse sendMessage(SendMessageRequest request);

}
