package com.dhbw.pawsitters.service.admin;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.user.Role;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
public class AdminService {

    @Autowired
    private UnitOfWork unitOfWork;

    /** Throws if the caller isn't an admin. Used by every /api/admin endpoint. */
    public void requireAdmin(Long requesterId) {
        if (requesterId == null) {
            throw new RuntimeException("Missing requesterId");
        }
        AppUser user = unitOfWork.getById(AppUser.class, requesterId);
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
        AppUser target = unitOfWork.getById(AppUser.class, targetUserId);
        target.setRoles(EnumSet.copyOf(roles));
        return unitOfWork.save(target);
    }
}
