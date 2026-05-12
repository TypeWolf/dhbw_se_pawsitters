package com.dhbw.pawsitters.controller.admin;

import com.dhbw.pawsitters.model.payment.Payment;
import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.user.Role;
import com.dhbw.pawsitters.repository.payment.PaymentRepository;
import com.dhbw.pawsitters.service.admin.AdminService;
import com.dhbw.pawsitters.service.pet.PetService;
import com.dhbw.pawsitters.service.sitting.SittingRequestService;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AppUserService userService;

    @Autowired
    private PetService petService;

    @Autowired
    private SittingRequestService requestService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/users")
    public List<AppUser> users(@RequestParam Long requesterId) {
        adminService.requireAdmin(requesterId);
        return userService.getAllUsers();
    }

    @GetMapping("/pets")
    public List<Pet> pets(@RequestParam Long requesterId) {
        adminService.requireAdmin(requesterId);
        return petService.getAllPets();
    }

    @GetMapping("/requests")
    public List<SittingRequest> requests(@RequestParam Long requesterId) {
        adminService.requireAdmin(requesterId);
        return requestService.getAllRequests();
    }

    @GetMapping("/payments")
    public List<Payment> payments(@RequestParam Long requesterId) {
        adminService.requireAdmin(requesterId);
        return paymentRepository.findAll();
    }

    @PutMapping("/users/{id}/roles")
    public AppUser setRoles(@PathVariable Long id,
                            @RequestParam Long requesterId,
                            @RequestBody Set<Role> roles) {
        return adminService.setRoles(requesterId, id, roles);
    }
}
