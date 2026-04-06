export interface Reservation {
  id?: number;
  userId: number;
  userName?: string;
  spotId: number;
  spotName?: string;
  campingName?: string;
  activityIds?: number[];
  activityNames?: string[];
  checkIn: string;
  checkOut: string;
  numberOfGuests: number;
  totalPrice?: number;
  status?: 'PENDING' | 'PAID' | 'CONFIRMED' | 'CANCELLED' | 'ACTIVE' | 'COMPLETED';
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}
