export type TripStatus = 'ACTIVE' | 'CANCELED' | 'COMPLETED';
export type BookingStatus = 'CONFIRMED' | 'PENDING' | 'CANCELED';
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
  durationMinutes?: number;
  pricePerSeat: number;
  seatsTotal: number;
  seatsAvailable?: number;
  ownerUserId: number;
  ownerFullName?: string;
  driverRatingAverage?: number;
  driverReviewsCount?: number;
  bookingMode?: string;
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

export interface ReservationQuote {
  seatsRequested: number;
  driverAmount: number;
  serviceFee: number;
  totalAmount: number;
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

export interface DriverReview {
  id: number;
  reservationId: number;
  rating: number;
  comment: string;
  date: string;
  reviewerName?: string;
}

export interface DriverReviewSummary {
  averageRating: number;
  reviewsCount: number;
}

export interface TripSearchFilters {
  departure?: string;
  destination?: string;
  dateFrom?: string;
  dateTo?: string;
  seatsNeeded?: number;
  minDriverRating?: number;
  status?: string;
  bookingMode?: string;
  minPrice?: number;
  maxPrice?: number;
  durationMax?: number;
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
