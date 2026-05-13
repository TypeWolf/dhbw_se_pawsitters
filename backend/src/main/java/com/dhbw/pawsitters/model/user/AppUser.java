package com.dhbw.pawsitters.model.user;

import com.dhbw.pawsitters.model.pet.Pet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String phoneNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = EnumSet.of(Role.PET_OWNER, Role.SITTER);

    // ---------- Saved card (mock) ----------
    // Stored on the account so request-new can auto-charge it as a wallet fallback.
    // CVC is NEVER persisted. The cardholder name is write-only over the API
    // (storage only — we won't echo it back in responses since the user already knows it).
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String cardholderName;

    private String cardLast4;

    private String cardExpiry;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Pet> pets;
}
