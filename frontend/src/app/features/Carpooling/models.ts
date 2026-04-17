export type TripStatus = 'ACTIVE' | 'CANCELED' | 'COMPLETED';
export type BookingStatus = 'CONFIRMED' | 'CANCELED';
export type ComplaintStatus = 'OPEN' | 'REVIEWED' | 'CLOSED';

export interface CarpoolUser {
  id: number;
  fullName: string;
  email: string;
  isAdmin?: boolean;
}

export interface Trip {
  id: number;
  departure: string;
  destination: string;
  departureDateTime: string;
  pricePerSeat: number;
  seatsTotal: number;
  seatsAvailable: number;
  ownerUserId: number;
  vehicleInfo?: string;
  meetingPoint?: string;
  status: TripStatus;
}

export interface Booking {
  id: number;
  tripId: number;
  passengerUserId: number;
  seatsBooked: number;
  totalPrice: number;
  bookingDate: string;
  status: BookingStatus;
}

export interface Complaint {
  id: number;
  tripId: number;
  bookingId?: number;
  reporterUserId: number;
  description: string;
  createdAt: string;
  status: ComplaintStatus;
}

export interface TripSearchFilters {
  departure?: string;
  destination?: string;
  date?: string;
  seatsNeeded?: number;
}

export interface AdminStats {
  totalTrips: number;
  totalBookings: number;
  totalComplaints: number;
  activeUsers: number;
}

export interface BookingWithContext {
  booking: Booking;
  trip?: Trip;
  passenger?: CarpoolUser;
  driver?: CarpoolUser;
}

export interface ComplaintWithContext {
  complaint: Complaint;
  trip?: Trip;
  reporter?: CarpoolUser;
}
