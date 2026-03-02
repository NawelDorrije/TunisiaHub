export type UserRole = 'DRIVER' | 'PASSENGER' | 'ADMIN';

export type TripStatus = 'PLANNED' | 'FULL' | 'CANCELLED';

export interface Trip {
  id: number;
  driverId: number;
  departurePoint: string;
  destination: string;
  departureDateTime: string;
  price: number | null;
  seatsTotal: number;
  seatsAvailable: number;
  status: TripStatus;
  createdAt: string;
  updatedAt: string;
}

export interface TripCreateRequest {
  departurePoint: string;
  destination: string;
  departureDateTime: string;
  price: number | null;
  seatsTotal: number;
}

export interface TripUpdateRequest {
  departurePoint: string;
  destination: string;
  departureDateTime: string;
  price: number | null;
  seatsTotal: number;
}

export interface TripSearchParams {
  departurePoint?: string;
  destination?: string;
  date?: string;
}
