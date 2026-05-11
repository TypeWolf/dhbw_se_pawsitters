package com.dhbw.pawsitters.config;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.pet.PetRepository;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            AppUserRepository userRepository,
            PetRepository petRepository,
            SittingRequestRepository requestRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Create Users
            AppUser user1 = AppUser.builder()
                    .firstName("Alice")
                    .lastName("Smith")
                    .email("alice@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .phoneNumber("0123456789")
                    .build();

            AppUser user2 = AppUser.builder()
                    .firstName("Bob")
                    .lastName("Jones")
                    .email("bob@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .phoneNumber("0987654321")
                    .build();

            userRepository.saveAll(List.of(user1, user2));

            // 2. Create Pets
            Pet pet1 = Pet.builder()
                    .name("Buddy")
                    .species("Dog")
                    .breed("Golden Retriever")
                    .age(3)
                    .owner(user1)
                    .build();

            Pet pet2 = Pet.builder()
                    .name("Mittens")
                    .species("Cat")
                    .breed("Persian")
                    .age(2)
                    .owner(user1)
                    .build();

            Pet pet3 = Pet.builder()
                    .name("Rocky")
                    .species("Dog")
                    .breed("Bulldog")
                    .age(5)
                    .owner(user2)
                    .build();

            petRepository.saveAll(List.of(pet1, pet2, pet3));

            // 3. Create Sitting Requests
            SittingRequest request1 = SittingRequest.builder()
                    .pet(pet1)
                    .requester(user1)
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(4))
                    .status(SittingRequest.RequestStatus.PENDING)
                    .build();

            SittingRequest request2 = SittingRequest.builder()
                    .pet(pet3)
                    .requester(user2)
                    .startTime(LocalDateTime.now().plusDays(2))
                    .endTime(LocalDateTime.now().plusDays(2).plusHours(2))
                    .status(SittingRequest.RequestStatus.PENDING)
                    .build();

            requestRepository.saveAll(List.of(request1, request2));

            System.out.println("Data initialization complete: 2 Users, 3 Pets, 2 Requests created.");
        };
    }
}
