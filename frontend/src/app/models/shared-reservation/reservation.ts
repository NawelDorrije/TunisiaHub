export type ReservationStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELLED';

export interface Reservation {

  id?: number;

  startDateCamping: string;

  endDateCamping: string;

  numberOfPeopleCamping: number;

  totalPriceCamping: number;

  statusCamping: ReservationStatus;

  userId: number;

  spotId: number;

}
