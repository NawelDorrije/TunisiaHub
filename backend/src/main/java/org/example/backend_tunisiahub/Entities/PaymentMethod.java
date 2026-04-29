package org.example.backend_tunisiahub.Entities;

public enum PaymentMethod {
    /** Online payment methods (for deposit) */
    CREDIT_CARD,
    PAYPAL,
    BANK_TRANSFER,

    /** On-site payment methods (for remaining balance at camping reception) */
    CASH,
    CARD_AT_RECEPTION
}
