export interface Reservation {
  id?: number;

  // ================= USER =================
  userId: number;
  userName?: string;

  // ================= CAMPING =================
  spotId?: number;
  spotName?: string;
  campingName?: string;

  activityIds?: number[];
  activityNames?: string[];

  checkIn?: string;
  checkOut?: string;
  numberOfGuests?: number;

  // ================= ACCOMMODATION (OLD SYSTEM) =================
  accommodationId?: number;
  accommodationName?: string;
  startDate?: string;
  endDate?: string;

  // ================= CARPOOL / TRIP =================
  tripId?: number;
  tripName?: string;

  // ================= PRICING =================
  totalPrice?: number;

  // ================= STATUS =================
  status?:
    | 'PENDING'
    | 'PAID'
    | 'CONFIRMED'
    | 'CANCELLED'
    | 'ACTIVE'
    | 'COMPLETED';

  // ================= META =================
  notes?: string;
  createdAt?: string;
  updatedAt?: string;

  // ================= REMINDER SYSTEM =================
  reminderSentAt?: string;
  reminderStatus?: string;
  reminderError?: string;

  // ================= TYPE (legacy support) =================
  type?: 'CAMPING' | 'ACCOMMODATION' | 'TRIP';
}