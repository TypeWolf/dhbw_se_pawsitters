package com.dhbw.pawsitters.repository.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SittingRequestRepository extends JpaRepository<SittingRequest, Long> {
    List<SittingRequest> findByRequesterId(Long requesterId);
    List<SittingRequest> findByStatus(SittingRequest.RequestStatus status);
    List<SittingRequest> findBySitterId(Long sitterId);
}
