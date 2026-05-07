package org.example.backend_tunisiahub.Entities.Camping.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Returned to reception staff after scanning a client's check-in QR code.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRScanResultDTO {

    boolean valid;
    String message;

    // Client info
    Long   clientId;
    String clientName;
    String clientEmail;

    // Reservation info
    Long              reservationId;
    ReservationStatus reservationStatus;
    LocalDate         checkIn;
    LocalDate         checkOut;
    Integer           numberOfGuests;

    // Camping / spot
    Long   campingId;
    String campingName;
    Long   spotId;
    String spotName;

    // Payment info
    BigDecimal totalAmount;
    BigDecimal depositPaid;
    BigDecimal remainingDue;
    String     transactionRef;
    String     remainingPaymentMethod;
}