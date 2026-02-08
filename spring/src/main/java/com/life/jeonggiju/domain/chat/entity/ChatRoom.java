package com.life.jeonggiju.domain.chat.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.life.jeonggiju.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "chat_room",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_chat_room_users",
		columnNames = {"user1_id", "user2_id"}
	)
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

	@Id
	@GeneratedValue(generator = "UUID")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user1_id", nullable = false)
	private User user1;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user2_id", nullable = false)
	private User user2;

	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	@ToString.Exclude
	private List<ChatMessage> messages = new ArrayList<>();

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column
	private LocalDateTime lastMessageAt;

	public static ChatRoom of(User user1, User user2) {
		return ChatRoom.builder()
			.user1(user1)
			.user2(user2)
			.build();
	}

	public void updateLastMessageAt() {
		this.lastMessageAt = LocalDateTime.now();
	}

	public boolean containsUser(User user) {
		return user1.getId().equals(user.getId()) || user2.getId().equals(user.getId());
	}

	public User getOtherUser(User user) {
		if (user1.getId().equals(user.getId())) {
			return user2;
		}
		return user1;
	}
}
