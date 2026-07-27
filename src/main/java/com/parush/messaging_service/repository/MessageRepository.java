package com.parush.messaging_service.repository;

import com.parush.messaging_service.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {
}
