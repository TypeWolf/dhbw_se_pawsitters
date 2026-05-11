package com.dhbw.pawsitters.service.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SittingRequestService {

    @Autowired
    private SittingRequestRepository requestRepository;

    @Autowired
    private AppUserService userService;

    public List<SittingRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public List<SittingRequest> getOpenRequests() {
        return requestRepository.findByStatus(SittingRequest.RequestStatus.PENDING);
    }

    public List<SittingRequest> getRequestsByRequester(Long requesterId) {
        return requestRepository.findByRequesterId(requesterId);
    }

    public List<SittingRequest> getRequestsBySitter(Long sitterId) {
        return requestRepository.findBySitterId(sitterId);
    }

    public SittingRequest createRequest(SittingRequest request) {
        request.setStatus(SittingRequest.RequestStatus.PENDING);
        return requestRepository.save(request);
    }

    @Transactional
    public SittingRequest acceptRequest(Long requestId, Long sitterId) {
        SittingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getRequester().getId().equals(sitterId)) {
            throw new RuntimeException("Owner cannot accept their own sitting request");
        }

        AppUser sitter = userService.getUserById(sitterId);

        if (request.getStatus() != SittingRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        request.setSitter(sitter);
        request.setStatus(SittingRequest.RequestStatus.ACCEPTED);
        return requestRepository.save(request);
    }

    @Transactional
    public SittingRequest cancelRequest(Long requestId, Long userId) {
        SittingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Only the requester can cancel this request");
        }
        if (request.getStatus() != SittingRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Only PENDING requests can be cancelled");
        }

        request.setStatus(SittingRequest.RequestStatus.CANCELLED);
        return requestRepository.save(request);
    }

    public void deleteRequest(Long id) {
        requestRepository.deleteById(id);
    }
}
