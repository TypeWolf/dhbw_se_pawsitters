package com.dhbw.pawsitters.service.sitting;

import com.dhbw.pawsitters.dto.sitting.SittingRequestCreateRequest;
import com.dhbw.pawsitters.exception.BadRequestException;
import com.dhbw.pawsitters.exception.ConflictException;
import com.dhbw.pawsitters.exception.ForbiddenException;
import com.dhbw.pawsitters.exception.NotFoundException;
import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.pet.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SittingRequestService {

    private static final Logger log = LoggerFactory.getLogger(SittingRequestService.class);

    private final SittingRequestRepository requestRepository;
    private final PetService petService;

    public SittingRequestService(SittingRequestRepository requestRepository, PetService petService) {
        this.requestRepository = requestRepository;
        this.petService = petService;
    }

    @Transactional(readOnly = true)
    public List<SittingRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SittingRequest> getRequestsForUser(AppUser user) {
        return requestRepository.findMine(user.getId());
    }

    @Transactional(readOnly = true)
    public List<SittingRequest> getAvailableRequests(AppUser user) {
        return requestRepository.findAvailableForUser(user.getId());
    }

    @Transactional
    public SittingRequest createRequest(SittingRequestCreateRequest request, AppUser requester) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("INVALID_TIME_RANGE", "End time must be after start time.");
        }

        Pet pet = petService.getOwnedPet(request.petId(), requester);
        SittingRequest sittingRequest = SittingRequest.builder()
                .pet(pet)
                .requester(requester)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(SittingRequest.RequestStatus.PENDING)
                .build();

        SittingRequest saved = requestRepository.save(sittingRequest);
        log.info("User id={} created sitting request id={}", requester.getId(), saved.getId());
        return saved;
    }

    @Transactional
    public SittingRequest acceptRequest(Long requestId, AppUser sitter) {
        SittingRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new NotFoundException("REQUEST_NOT_FOUND", "Sitting request was not found."));
        
        if (request.getRequester().getId().equals(sitter.getId())) {
            throw new ForbiddenException("CANNOT_ACCEPT_OWN_REQUEST", "Owners cannot accept their own sitting request.");
        }

        if (request.getStatus() != SittingRequest.RequestStatus.PENDING) {
            throw new ConflictException("REQUEST_NOT_PENDING", "This request is no longer available.");
        }

        request.setSitter(sitter);
        request.setStatus(SittingRequest.RequestStatus.ACCEPTED);
        SittingRequest saved = requestRepository.save(request);
        log.info("User id={} accepted sitting request id={}", sitter.getId(), requestId);
        return saved;
    }

    @Transactional
    public void deleteRequest(Long id, AppUser requester) {
        SittingRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("REQUEST_NOT_FOUND", "Sitting request was not found."));

        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new ForbiddenException("REQUEST_DELETE_FORBIDDEN", "Only the requester can delete this request.");
        }

        requestRepository.delete(request);
        log.info("User id={} deleted sitting request id={}", requester.getId(), id);
    }
}
