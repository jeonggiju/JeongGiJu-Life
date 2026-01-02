package com.life.jeonggiju.domain.friend.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.life.jeonggiju.domain.common.entity.BaseEntity;
import com.life.jeonggiju.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@Table(
	name = "friends",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
	}
)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Friend extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "addressee_id", nullable = false)
	private User addressee;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	private FriendStatus status;

	protected Friend() {}

	public void changeStatus(FriendStatus status) {
		this.status = status;
	}
}
