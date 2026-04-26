package org.example.backend_tunisiahub.Entities.Camping.Enums;

public enum ReservationStatus {
    PENDING,      // created, awaiting payment
    PAID,         // payment received
    CONFIRMED,    // owner confirmed
    CANCELLED,    // cancelled by user or owner
    ACTIVE,       // guest checked in
    COMPLETED
}
