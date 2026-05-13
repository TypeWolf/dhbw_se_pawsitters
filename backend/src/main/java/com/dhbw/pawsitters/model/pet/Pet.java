package com.dhbw.pawsitters.model.pet;

import com.dhbw.pawsitters.model.user.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    private String breed;

    /** Free-text integer age, kept for backwards compatibility. New form uses ageCategory. */
    private Integer age;

    @Enumerated(EnumType.STRING)
    private AgeCategory ageCategory;

    // ---------- Health ----------
    @Column(length = 500)
    private String allergies;

    @Column(length = 500)
    private String medications;

    @Builder.Default
    private boolean vaccinated = false;

    // ---------- Behaviour ----------
    @Builder.Default
    private boolean canGoOutside = false;

    @Builder.Default
    private boolean aggressive = false;

    @Column(length = 500)
    private String aggressiveContext;

    @Builder.Default
    private boolean goodWithStrangers = false;

    @Builder.Default
    private boolean goodWithOtherAnimals = false;

    @Builder.Default
    private boolean houseTrained = false;

    // ---------- Care ----------
    @Column(length = 500)
    private String careNotes;

    // ---------- Relationships ----------
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;
}
