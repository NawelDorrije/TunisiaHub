export type OnlinePaymentMethod = 'CREDIT_CARD' | 'PAYPAL' | 'BANK_TRANSFER';
export type ReceptionPaymentMethod = 'CASH' | 'CARD_AT_RECEPTION';
export type PaymentMethod = OnlinePaymentMethod | ReceptionPaymentMethod;
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';

export interface Payment {
  id?: number;
  reservationId: number;
  reservationSummary?: string;

  /** Full reservation total */
  amount?: number;

  /** Amount paid online as deposit */
  depositAmount?: number;

  /** Remaining balance to settle at reception */
  remainingAmount?: number;

  /** Configured minimum deposit percentage (e.g. 30) */
  minimumDepositPercent?: number;

  /** Online payment method used for the deposit */
  method: OnlinePaymentMethod;

  /** How the remaining balance will be settled at reception */
  remainingPaymentMethod?: ReceptionPaymentMethod;

  status?: PaymentStatus;
  transactionRef?: string;
  clientId?: number;
  clientEmail?: string;
  clientName?: string;
  createdAt?: string;
  paidAt?: string;

  /** Base64-encoded PNG of the check-in QR code */
  qrCodeBase64?: string;
}

export interface QRScanResult {
  valid: boolean;
  message: string;

  clientId?: number;
  clientName?: string;
  clientEmail?: string;

  reservationId?: number;
  reservationStatus?: string;
  checkIn?: string;
  checkOut?: string;
  numberOfGuests?: number;

  campingId?: number;
  campingName?: string;
  spotId?: number;
  spotName?: string;

  totalAmount?: number;
  depositPaid?: number;
  remainingDue?: number;
  transactionRef?: string;
  remainingPaymentMethod?: string;
}
