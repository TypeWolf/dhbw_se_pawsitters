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

    @Autowired
    private com.dhbw.pawsitters.service.rating.RatingService ratingService;

    public List<SittingRequest> getAllRequests() {
        List<SittingRequest> requests = unitOfWork.getAll(SittingRequest.class);
        requests.forEach(this::populateRatings);
        return requests;
    }

    public List<SittingRequest> getOpenRequests() {
        List<SittingRequest> requests = unitOfWork.getByProperty(SittingRequest.class, "status", SittingRequest.RequestStatus.PENDING);
        requests.forEach(this::populateRatings);
        return requests;
    }

    public List<SittingRequest> getRequestsByRequester(Long requesterId) {
        List<SittingRequest> requests = unitOfWork.getByProperty(SittingRequest.class, "requester.id", requesterId);
        requests.forEach(this::populateRatings);
        return requests;
    }

    public List<SittingRequest> getRequestsBySitter(Long sitterId) {
        List<SittingRequest> requests = unitOfWork.getByProperty(SittingRequest.class, "sitter.id", sitterId);
        requests.forEach(this::populateRatings);
        return requests;
    }

    private void populateRatings(SittingRequest request) {
        if (request.getSitter() != null) {
            ratingService.populateAverageRating(request.getSitter());
        }
        if (request.getRequester() != null) {
            ratingService.populateAverageRating(request.getRequester());
        }
    }

    @Transactional
    public SittingRequest createRequest(SittingRequest request) {
        request.setStatus(SittingRequest.RequestStatus.PENDING);
        SittingRequest saved = unitOfWork.save(request);

        BigDecimal price = saved.getPriceOffered();
        if (price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            paymentService.hold(saved.getId(), saved.getRequester().getId(), price);
        }
        return saved;
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

    @Transactional
    public SittingRequest cancelRequest(Long requestId, Long userId) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Only the requester can cancel this request");
        }
        if (request.getStatus() != SittingRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Only PENDING requests can be cancelled");
        }

        paymentService.findHeldForRequest(requestId)
                .ifPresent(p -> paymentService.refund(p.getId()));

        request.setStatus(SittingRequest.RequestStatus.CANCELLED);
        return unitOfWork.save(request);
    }

    @Transactional
    public SittingRequest completeRequest(Long requestId, Long userId) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);

        if (!request.getRequester().getId().equals(userId)) {
            throw new RuntimeException("Only the requester can confirm completion");
        }
        if (request.getStatus() != SittingRequest.RequestStatus.ACCEPTED) {
            throw new RuntimeException("Only ACCEPTED requests can be marked completed");
        }

        paymentService.findHeldForRequest(requestId)
                .ifPresent(p -> paymentService.release(p.getId()));

        request.setStatus(SittingRequest.RequestStatus.COMPLETED);
        return unitOfWork.save(request);
    }

    public void deleteRequest(Long id) {
        unitOfWork.delete(SittingRequest.class, id);
    }
}
