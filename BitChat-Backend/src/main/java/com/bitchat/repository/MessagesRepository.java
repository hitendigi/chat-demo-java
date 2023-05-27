package com.bitchat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bitchat.model.Message;
import com.bitchat.model.Messages;

@Repository
public interface MessagesRepository extends JpaRepository<Messages, UUID> {
	
	public List<Messages> findBySenderUserIDInAndReceiverUserIDInOrderByDateAsc(List<UUID> senderUserIDs, List<UUID> receiverUserIDs);	
	public List<Messages> findByReceiverUserIDAndSeen(UUID senderUserID, int seen);	
	
	@Modifying
	@Transactional
	@Query("update Messages messages set messages.seen = 1 where messages.senderUserID = ?1 and  messages.receiverUserID = ?2")
	int updateSeenStatus(UUID senderUserID, UUID receiverUserID);

    /*public List<Message> findAllByReceiverUsername(String receiver);

    public List<Message> findAllBySenderUsername(String receiver);

    @Query(value = "select * from message m where(m.date < :date) and "
            + "(m.receiver_username=:receiver) "
            + "order by m.date desc limit :limit", nativeQuery = true)
    public List<Message> fetchMessages(@Param("limit") int limit, @Param("receiver") String receiver, @Param("date") long date);

    @Query(value = "select * from message m where (m.date < :date) and "
            + "((m.sender_username=:sender and m.receiver_username=:receiver) or "
            + "(m.sender_username=:receiver and m.receiver_username=:sender)) "
            + "order by m.date desc limit :limit", nativeQuery = true)
    public List<Message> fetchMessages(@Param("limit") int limit, @Param("sender") String sender, @Param("receiver") String receiver, @Param("date") long date);
*/
    @Override
    public default void delete(Messages msg) {
        deleteById(msg.getId());
    }
}
