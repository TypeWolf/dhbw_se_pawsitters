package com.dhbw.pawsitters.repository.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SittingRequestRepository extends JpaRepository<SittingRequest, Long> {
}
