package com.dhbw.pawsitters.service.admin;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.user.Role;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
public class AdminService {

    @Autowired
    private AppUserRepository userRepository;

    /** Throws if the caller isn't an admin. Used by every /api/admin endpoint. */
    public void requireAdmin(Long requesterId) {
        if (requesterId == null) {
            throw new RuntimeException("Missing requesterId");
        }
        AppUser user = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        if (user.getRoles() == null || !user.getRoles().contains(Role.ADMIN)) {
            throw new RuntimeException("Admin role required");
        }
    }

    @Transactional
    public AppUser setRoles(Long requesterId, Long targetUserId, Set<Role> roles) {
        requireAdmin(requesterId);
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("A user must have at least one role");
        }
        AppUser target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
        target.setRoles(EnumSet.copyOf(roles));
        return userRepository.save(target);
    }
}
