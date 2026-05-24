package com.dhbw.pawsitters.repository.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SittingRequestRepository extends JpaRepository<SittingRequest, Long> {
    @Query("""
            select request
            from SittingRequest request
            where request.requester.id = :userId or request.sitter.id = :userId
            order by request.startTime desc
            """)
    List<SittingRequest> findMine(@Param("userId") Long userId);

    @Query("""
            select request
            from SittingRequest request
            where request.status = com.dhbw.pawsitters.model.sitting.SittingRequest.RequestStatus.PENDING
              and request.requester.id <> :userId
            order by request.startTime asc
            """)
    List<SittingRequest> findAvailableForUser(@Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from SittingRequest request where request.id = :id")
    Optional<SittingRequest> findByIdForUpdate(@Param("id") Long id);
}
