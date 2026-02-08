package com.life.jeonggiju.domain.chat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.life.jeonggiju.domain.chat.entity.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

	@Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.createdAt DESC")
	List<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(@Param("chatRoomId") UUID chatRoomId, Pageable pageable);

	@Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId ORDER BY cm.createdAt ASC")
	List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(@Param("chatRoomId") UUID chatRoomId);

	@Modifying
	@Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chatRoom.id = :chatRoomId " +
		"AND cm.sender.id != :userId AND cm.isRead = false")
	int markAsReadByChatRoomIdAndUserId(@Param("chatRoomId") UUID chatRoomId, @Param("userId") UUID userId);

	@Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id = :chatRoomId " +
		"AND cm.sender.id != :userId AND cm.isRead = false")
	int countUnreadMessages(@Param("chatRoomId") UUID chatRoomId, @Param("userId") UUID userId);

	@Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatRoom.id IN " +
		"(SELECT cr.id FROM ChatRoom cr WHERE cr.user1.id = :userId OR cr.user2.id = :userId) " +
		"AND cm.sender.id != :userId AND cm.isRead = false")
	int countTotalUnreadMessages(@Param("userId") UUID userId);
}
