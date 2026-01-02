package com.life.jeonggiju.domain.notification.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.life.jeonggiju.domain.notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>{
}
