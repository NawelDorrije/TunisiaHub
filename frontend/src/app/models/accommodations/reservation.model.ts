export interface ReservationRequest {
  startDate: string;
  endDate: string;
}

export interface ReservationResponse {
  id: number;
  startDate: string;
  endDate: string;
  totalPrice: number;
  status: string;
  type: string;
}

export interface ReservedDateRange {
  startDate: string;
  endDate: string;
}