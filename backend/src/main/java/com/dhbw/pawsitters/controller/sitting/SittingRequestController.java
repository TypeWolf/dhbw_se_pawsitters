package com.dhbw.pawsitters.controller.sitting;

import com.dhbw.pawsitters.dto.sitting.SittingRequestCreateRequest;
import com.dhbw.pawsitters.dto.sitting.SittingRequestResponse;
import com.dhbw.pawsitters.mapper.ApiMapper;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.sitting.SittingRequestService;
import com.dhbw.pawsitters.service.user.AppUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class SittingRequestController {

    private final SittingRequestService requestService;
    private final AppUserService userService;
    private final ApiMapper mapper;

    public SittingRequestController(SittingRequestService requestService, AppUserService userService, ApiMapper mapper) {
        this.requestService = requestService;
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<SittingRequestResponse> getMine(Authentication authentication) {
        AppUser user = userService.getCurrentUser(authentication);
        return requestService.getRequestsForUser(user).stream()
                .map(mapper::toSittingRequestResponse)
                .toList();
    }

    @GetMapping("/mine")
    public List<SittingRequestResponse> getMyRequests(Authentication authentication) {
        return getMine(authentication);
    }

    @GetMapping("/available")
    public List<SittingRequestResponse> getAvailable(Authentication authentication) {
        AppUser user = userService.getCurrentUser(authentication);
        return requestService.getAvailableRequests(user).stream()
                .map(mapper::toSittingRequestResponse)
                .toList();
    }

    @PostMapping
    public SittingRequestResponse createRequest(
            @Valid @RequestBody SittingRequestCreateRequest request,
            Authentication authentication
    ) {
        AppUser requester = userService.getCurrentUser(authentication);
        return mapper.toSittingRequestResponse(requestService.createRequest(request, requester));
    }

    @PutMapping("/{id}/accept")
    public SittingRequestResponse acceptRequest(@PathVariable Long id, Authentication authentication) {
        AppUser sitter = userService.getCurrentUser(authentication);
        return mapper.toSittingRequestResponse(requestService.acceptRequest(id, sitter));
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable Long id, Authentication authentication) {
        AppUser requester = userService.getCurrentUser(authentication);
        requestService.deleteRequest(id, requester);
    }
}
