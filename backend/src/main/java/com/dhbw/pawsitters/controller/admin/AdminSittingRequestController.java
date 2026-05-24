package com.dhbw.pawsitters.controller.admin;

import com.dhbw.pawsitters.dto.sitting.SittingRequestResponse;
import com.dhbw.pawsitters.mapper.ApiMapper;
import com.dhbw.pawsitters.service.sitting.SittingRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/requests")
public class AdminSittingRequestController {

    private final SittingRequestService requestService;
    private final ApiMapper mapper;

    public AdminSittingRequestController(SittingRequestService requestService, ApiMapper mapper) {
        this.requestService = requestService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<SittingRequestResponse> getAllRequests() {
        return requestService.getAllRequests().stream()
                .map(mapper::toSittingRequestResponse)
                .toList();
    }
}
