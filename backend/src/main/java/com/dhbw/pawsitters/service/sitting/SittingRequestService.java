package com.dhbw.pawsitters.service.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SittingRequestService {

    @Autowired
    private UnitOfWork unitOfWork;

    @Autowired
    private AppUserService userService;

    public List<SittingRequest> getAllRequests() {
        return unitOfWork.getAll(SittingRequest.class);
    }

    public SittingRequest createRequest(SittingRequest request) {
        request.setStatus(SittingRequest.RequestStatus.PENDING);
        return unitOfWork.save(request);
    }

    @Transactional
    public SittingRequest acceptRequest(Long requestId, Long sitterId) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);
        
        if (request.getRequester().getId().equals(sitterId)) {
            throw new RuntimeException("Owner cannot accept their own sitting request");
        }

        AppUser sitter = userService.getUserById(sitterId);

        if (request.getStatus() != SittingRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        request.setSitter(sitter);
        request.setStatus(SittingRequest.RequestStatus.ACCEPTED);
        return unitOfWork.save(request);
    }

    public void deleteRequest(Long id) {
        unitOfWork.delete(SittingRequest.class, id);
    }
}
