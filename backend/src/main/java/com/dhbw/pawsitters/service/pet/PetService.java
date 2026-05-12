package com.dhbw.pawsitters.service.pet;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

    @Autowired
    private UnitOfWork unitOfWork;

    public List<Pet> getAllPets() {
        return unitOfWork.getAll(Pet.class);
    }

    public Pet createPet(Pet pet) {
        return unitOfWork.save(pet);
    }

    public Pet getPetById(Long id) {
        return unitOfWork.getById(Pet.class, id);
    }
}
