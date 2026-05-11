package com.dhbw.pawsitters.controller.sitting;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.service.sitting.SittingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class SittingRequestController {

    @Autowired
    private SittingRequestService requestService;

    @GetMapping
    public List<SittingRequest> getOpenRequests() {
        return requestService.getOpenRequests();
    }

    @GetMapping("/all")
    public List<SittingRequest> getAllRequests() {
        return requestService.getAllRequests();
    }

    @GetMapping("/mine")
    public List<SittingRequest> getMyRequests(@RequestParam Long userId) {
        return requestService.getRequestsByRequester(userId);
    }

    @GetMapping("/booked")
    public List<SittingRequest> getBookedRequests(@RequestParam Long sitterId) {
        return requestService.getRequestsBySitter(sitterId);
    }

    @PostMapping
    public SittingRequest createRequest(@RequestBody SittingRequest request) {
        return requestService.createRequest(request);
    }

    @PutMapping("/{id}/accept")
    public SittingRequest acceptRequest(@PathVariable Long id, @RequestParam Long sitterId) {
        return requestService.acceptRequest(id, sitterId);
    }

    @PutMapping("/{id}/cancel")
    public SittingRequest cancelRequest(@PathVariable Long id, @RequestParam Long userId) {
        return requestService.cancelRequest(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
    }
}
