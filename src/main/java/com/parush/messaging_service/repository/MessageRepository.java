package com.parush.messaging_service.repository;

import com.parush.messaging_service.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

  List<Message> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);

  List<Message> findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
      String conversationId, Instant cursorCreatedAt, Pageable pageable);

}
