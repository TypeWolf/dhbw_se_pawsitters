package com.dhbw.pawsitters.model.pet;

/**
 * Age groups for pets. Owners pick a category (intuitive) instead of a numeric age
 * that means different things for different species. Mapped to {@link Pet#ageCategory}.
 */
public enum AgeCategory {
    PUPPY,   // 0-1
    YOUNG,   // 1-3
    ADULT,   // 3-8
    SENIOR   // 8+
}
