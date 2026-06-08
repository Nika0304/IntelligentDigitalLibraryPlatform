package com.library.model;

public enum ChallengeType {
    READ_FROM_CATEGORY,   // X cărți dintr-o categorie
    READ_FROM_AUTHOR,     // X cărți de la un autor
    READ_DIGITAL,         // X cărți digitale
    READ_PHYSICAL,        // X cărți fizice (rezervări)
    WRITE_REVIEWS,        // X recenzii scrise
    ANY_READ              // X cărți de orice fel
}