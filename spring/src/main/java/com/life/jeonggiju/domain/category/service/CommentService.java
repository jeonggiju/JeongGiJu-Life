package com.life.jeonggiju.domain.category.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.life.jeonggiju.domain.category.dto.FindCommentResponse;
import com.life.jeonggiju.domain.category.dto.UpdateCommentRequest;
import com.life.jeonggiju.domain.category.entity.Category;
import com.life.jeonggiju.domain.category.entity.Comment;
import com.life.jeonggiju.domain.category.exception.CategoryNotFoundException;
import com.life.jeonggiju.domain.category.exception.CommentDeleteForbiddenException;
import com.life.jeonggiju.domain.category.exception.CommentNotFoundException;
import com.life.jeonggiju.domain.category.exception.CommentUpdateForbiddenException;
import com.life.jeonggiju.domain.category.exception.EmptyCommentContentException;
import com.life.jeonggiju.domain.category.exception.ParentCommentMismatchException;
import com.life.jeonggiju.domain.category.repository.CategoryRepository;
import com.life.jeonggiju.domain.category.repository.CommentRepository;
import com.life.jeonggiju.domain.notification.dto.NotificationCreatedDto;
import com.life.jeonggiju.domain.notification.entity.NotificationType;
import com.life.jeonggiju.domain.notification.service.NotificationService;
import com.life.jeonggiju.domain.user.dto.CreateCommentRequest;
import com.life.jeonggiju.domain.user.entity.User;
import com.life.jeonggiju.domain.user.exception.UserNotFoundException;
import com.life.jeonggiju.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	private final NotificationService notificationService;

	@Transactional(readOnly = true)
	public List<FindCommentResponse> find(UUID userId, UUID categoryId) {

		boolean isAuthenticated = (userId != null);
		boolean isCategoryOwner = isAuthenticated && categoryRepository.existsByIdAndUserId(categoryId, userId);

		List<Comment> comments = commentRepository.findByCategoryId(categoryId);

		Map<UUID, FindCommentResponse> dtoMap = new LinkedHashMap<>();

		for (Comment comment : comments) {
			UUID parentId = (comment.getParent() == null) ? null : comment.getParent().getId();

			boolean mine = isAuthenticated && comment.getUser().getId().equals(userId);
			boolean canDelete = mine || isCategoryOwner;
			boolean canUpdate = mine;
			boolean isUpdated = comment.getUpdatedAt() != null;

			FindCommentResponse dto = FindCommentResponse.builder()
				.commentId(comment.getId())
				.authorEmail(comment.getUser().getEmail())
				.comment(comment.getComment())
				.createdAt(comment.getCreatedAt())
				.parentId(parentId)
				.children(new ArrayList<>())
				.mine(mine)
				.canDelete(canDelete)
				.canUpdate(canUpdate)
				.isUpdated(isUpdated)
				.build();

			dtoMap.put(comment.getId(), dto);
		}

		List<FindCommentResponse> roots = new ArrayList<>();
		for (FindCommentResponse dto : dtoMap.values()) {
			if (dto.getParentId() == null)
				roots.add(dto);
			else {
				FindCommentResponse parent = dtoMap.get(dto.getParentId());
				if (parent == null)
					roots.add(dto);
				else
					parent.getChildren().add(dto);
			}
		}

		return roots;
	}

	@Transactional
	public UUID createComment(CreateCommentRequest dto, UUID userId) {
		if (dto.getComment() == null || dto.getComment().isBlank()) {
			throw new EmptyCommentContentException();
		}

		Category category = categoryRepository.findById(dto.getCategoryId())
			.orElseThrow(() -> CategoryNotFoundException.withId(dto.getCategoryId()));
		User user = userRepository.findById(userId)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));

		Comment saved = commentRepository.save(
			Comment.builder()
				.comment(dto.getComment())
				.category(category)
				.user(user)
				.parent(null)
				.build()
		);

		NotificationCreatedDto notificationCreatedDto = NotificationCreatedDto.builder()
			.receiverId(category.getUser().getId())
			.senderId(user.getId())
			.data(Map.of("categoryTitle", category.getTitle(), "senderEmail", user.getEmail(), "comment",
				saved.getComment()))
			.type(NotificationType.COMMENT)
			.build();
		notificationService.notify(notificationCreatedDto);

		return saved.getId();
	}

	@Transactional
	public UUID createReply(UUID categoryId, UUID parentId, UUID userId, String content) {
		if (content == null || content.isBlank()) {
			throw new EmptyCommentContentException();
		}

		Category category = categoryRepository.findById(categoryId)
			.orElseThrow(() -> CategoryNotFoundException.withId(categoryId));
		User user = userRepository.findById(userId)
			.orElseThrow(() -> UserNotFoundException.withUserId(userId));
		Comment parent = commentRepository.findById(parentId)
			.orElseThrow(() -> CommentNotFoundException.withId(parentId));

		if (!parent.getCategory().getId().equals(categoryId)) {
			throw ParentCommentMismatchException.withIds(categoryId, parentId);
		}

		Comment saved = commentRepository.save(
			Comment.builder()
				.comment(content)
				.category(category)
				.user(user)
				.parent(parent)
				.build()
		);

		NotificationCreatedDto notificationCreatedDto = NotificationCreatedDto.builder()
			.receiverId(parent.getUser().getId())
			.senderId(user.getId())
			.data(Map.of("categoryTitle", category.getTitle(), "myComment", content, "senderEmail", user.getEmail(),
				"comment", saved.getComment()))
			.type(NotificationType.REPLY)
			.build();
		notificationService.notify(notificationCreatedDto);

		return saved.getId();
	}

	@Transactional
	public void delete(UUID commentId, UUID requestUserId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> CommentNotFoundException.withId(commentId));

		UUID authorId = comment.getUser().getId();
		UUID categoryOwnerId = comment.getCategory().getUser().getId();

		boolean canDelete = authorId.equals(requestUserId) || categoryOwnerId.equals(requestUserId);
		if (!canDelete) {
			throw CommentDeleteForbiddenException.withIds(commentId, requestUserId);
		}

		commentRepository.delete(comment);
	}

	@Transactional
	public void update(UpdateCommentRequest dto, UUID requestUserId) {
		Comment comment = commentRepository.findById(dto.getCommentId())
			.orElseThrow(() -> CommentNotFoundException.withId(dto.getCommentId()));

		if (!comment.getUser().getId().equals(requestUserId)) {
			throw CommentUpdateForbiddenException.withIds(dto.getCommentId(), requestUserId);
		}

		if (dto.getComment() == null || dto.getComment().isBlank()) {
			throw new EmptyCommentContentException();
		}

		comment.setComment(dto.getComment());
	}

}

