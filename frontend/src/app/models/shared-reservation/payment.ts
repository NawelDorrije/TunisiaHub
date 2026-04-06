export interface Payment {
  id?: number;
  reservationId: number;
  reservationSummary?: string;
  amount?: number;
  method: 'CREDIT_CARD' | 'PAYPAL' | 'BANK_TRANSFER' | 'CASH';
  status?: 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
  transactionRef?: string;
  clientId?: number;
  createdAt?: string;
  paidAt?: string;
}
