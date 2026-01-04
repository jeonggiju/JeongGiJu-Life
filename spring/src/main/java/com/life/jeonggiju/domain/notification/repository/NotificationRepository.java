package com.life.jeonggiju.domain.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.life.jeonggiju.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>{

	@Query("""
		select n
		from Notification n
		where n.receiver.id = :userId
		and n.read = false
	""")
	List<Notification> findUnreadByReceiverId(UUID userId);

	@Query("""
		select count(n)
		from Notification n
		where n.receiver.id = :userId
		and n.read = false
	""")
	int countUnreadByReceiverId(UUID userId);

	@Modifying
	@Query("""
		update Notification n
		set n.read = true
		where n.id in :ids
		  and n.receiver.id = :receiverId
		  and n.read = false
	""")
	int markAsRead(@Param("ids") List<UUID> ids, @Param("receiverId") UUID receiverId);

}
