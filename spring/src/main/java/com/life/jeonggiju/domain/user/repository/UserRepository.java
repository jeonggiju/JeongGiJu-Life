package com.life.jeonggiju.domain.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.life.jeonggiju.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);

	Optional<User> findByUsernameAndEmail(String username, String email);

	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%')) " +
		"OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<User> searchByNicknameOrEmail(@Param("query") String query);
}
