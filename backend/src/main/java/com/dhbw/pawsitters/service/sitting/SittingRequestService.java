package com.dhbw.pawsitters.service.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import com.dhbw.pawsitters.service.payment.PaymentService;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SittingRequestService {

    @Autowired
    private UnitOfWork unitOfWork;

    @Autowired
    private AppUserService userService;

    @Autowired
    private PaymentService paymentService;

    public List<SittingRequest> getAllRequests() {
        return unitOfWork.getAll(SittingRequest.class);
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

    @Transactional
    public SittingRequest createRequest(SittingRequest request) {
        request.setStatus(SittingRequest.RequestStatus.PENDING);
        return unitOfWork.save(request);
        SittingRequest saved = requestRepository.save(request);

        BigDecimal price = saved.getPriceOffered();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            // Will throw "Insufficient funds" if the owner's wallet doesn't cover it,
            // rolling back the request creation thanks to @Transactional.
            paymentService.hold(saved.getId(), saved.getRequester().getId(), price);
        }
        return saved;
    }

    @Transactional
    public SittingRequest acceptRequest(Long requestId, Long sitterId) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);
        
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
        return unitOfWork.save(request);
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

        paymentService.findHeldForRequest(requestId)
                .ifPresent(p -> paymentService.refund(p.getId()));

        request.setStatus(SittingRequest.RequestStatus.CANCELLED);
        return requestRepository.save(request);
    }

    @Transactional
    public SittingRequest completeRequest(Long requestId, Long userId) {
        SittingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Only the requester can confirm completion");
        }
        if (request.getStatus() != SittingRequest.RequestStatus.ACCEPTED) {
            throw new RuntimeException("Only ACCEPTED requests can be marked completed");
        }

        paymentService.findHeldForRequest(requestId)
                .ifPresent(p -> paymentService.release(p.getId()));

        request.setStatus(SittingRequest.RequestStatus.COMPLETED);
        return requestRepository.save(request);
    }

    public void deleteRequest(Long id) {
        unitOfWork.delete(SittingRequest.class, id);
    }
}
