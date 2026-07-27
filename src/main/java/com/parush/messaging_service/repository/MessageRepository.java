package com.parush.messaging_service.repository;

import com.parush.messaging_service.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

  List<Message> findByConversationIdOrderByCreatedAtDesc(final String conversationId, final Pageable pageable);

  List<Message> findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
      final String conversationId, final Instant cursorCreatedAt, final Pageable pageable);

}
