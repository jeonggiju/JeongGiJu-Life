package com.life.jeonggiju.domain.friendship.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
import lombok.NoArgsConstructor;

@Entity
@Table(name = "friendship",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_requester_receiver",
		columnNames = {"requester_id", "receiver_id"}
	)
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Friendship {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "requester_id", nullable = false)
	private User requester;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "receiver_id", nullable = false)
	private User receiver;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FriendshipStatus status;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime updatedAt;

	public static Friendship of(User requester, User receiver) {
		return Friendship.builder()
			.requester(requester)
			.receiver(receiver)
			.status(FriendshipStatus.PENDING)
			.build();
	}

	public void accept() {
		this.status = FriendshipStatus.ACCEPTED;
		this.updatedAt = LocalDateTime.now();
	}

	public void reject() {
		this.status = FriendshipStatus.REJECTED;
		this.updatedAt = LocalDateTime.now();
	}

	public void block() {
		this.status = FriendshipStatus.BLOCKED;
		this.updatedAt = LocalDateTime.now();
	}
}
