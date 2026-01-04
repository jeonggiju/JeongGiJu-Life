package com.life.jeonggiju.domain.friend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.life.jeonggiju.domain.friend.entity.Friend;
import com.life.jeonggiju.domain.friend.entity.FriendStatus;


@Repository
public interface FriendRepository extends JpaRepository<Friend, UUID> {

	@EntityGraph(attributePaths = {"requester", "addressee"})
	@Query("""
	  select f
	  from Friend f
	  where f.status = 'ACCEPTED'
		and (f.requester.id = :userId or f.addressee.id = :userId)
	  order by f.createdAt desc	
	""")
	List<Friend> findAllAcceptedOf(@Param("userId") UUID userId);

	Optional<Friend> findByIdAndAddressee_IdAndStatus(UUID id, UUID addresseeId, FriendStatus status);

	@EntityGraph(attributePaths = {"requester", "addressee"})
	List<Friend> findByRequester_IdAndStatus(UUID userId, FriendStatus status); // 내가 보낸..

	@EntityGraph(attributePaths = {"requester", "addressee"})
	List<Friend> findByAddressee_IdAndStatus(UUID userId, FriendStatus status); // 내가 받은 ..

	@Query("""
        select case when count(f) > 0 then true else false end
        from Friend f
        where (
            (f.requester.id = :a and f.addressee.id = :b)
            or
            (f.requester.id = :b and f.addressee.id = :a)
        )
        and f.status = 'ACCEPTED'
    """)
	boolean existsAcceptedBetween(@Param("a") UUID requesterId, @Param("b") UUID addressesId);
	boolean existsByRequester_IdAndAddressee_IdAndStatusIn(UUID requesterId, UUID addressesId, List<FriendStatus> status);

	Optional<Friend> findByRequester_IdAndAddressee_IdAndStatus(UUID requesterId, UUID addresseeId ,FriendStatus status);

	@Query("""
        select f from Friend f
        where (
            (f.requester.id = :a and f.addressee.id = :b)
            or
            (f.requester.id = :b and f.addressee.id = :a)
        )
    """)
	Optional<Friend> findAnyRelationBetween(@Param("a") UUID a, @Param("b") UUID b);

	@Query("""
    select f from Friend f
    where f.status = :status
      and (
        (f.requester.id = :a and f.addressee.id = :b)
        or
        (f.requester.id = :b and f.addressee.id = :a)
      )
	""")
	Optional<Friend> findBetweenByStatus(@Param("a") UUID a, @Param("b") UUID b,@Param("status") FriendStatus status);
}
