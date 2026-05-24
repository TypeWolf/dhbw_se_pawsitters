package com.dhbw.pawsitters.repository.pet;

import com.dhbw.pawsitters.model.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwnerIdOrderByNameAsc(Long ownerId);
    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);
}
