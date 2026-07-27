package com.parush.messaging_service.repository;

import com.parush.messaging_service.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

  @Query("{ 'participantIds': { '$all': [?0, ?1] } }")
  Optional<Conversation> findByParticipants(String participantOne, String participantTwo);

  List<Conversation> findByParticipantIdsContainingOrderByLastMessageAtDesc(String userId);
}
