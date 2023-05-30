package com.bitchat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bitchat.model.Messages;

@Repository
public interface MessagesRepository extends JpaRepository<Messages, UUID> {
	
	public List<Messages> findBySenderUserIDInAndReceiverUserIDInOrderByDateAsc(List<UUID> senderUserIDs, List<UUID> receiverUserIDs);	
	public List<Messages> findByReceiverUserIDOrderByDateAsc(UUID senderUserID);	
	
	@Modifying
	@Transactional
	@Query("update Messages messages set messages.seen = 1 where messages.senderUserID = ?1 and  messages.receiverUserID = ?2")
	int updateSeenStatus(UUID senderUserID, UUID receiverUserID);

}
