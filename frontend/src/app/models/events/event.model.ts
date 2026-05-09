export enum EventType {
  SPORT = 'SPORT',
  FESTIVAL = 'FESTIVAL',
  CONFERENCE = 'CONFERENCE',
  COMPETITION = 'COMPETITION'
}
export interface Event {
    id?: number;

  title: string;

  description: string;

  startDate: string;   // LocalDateTime -> string côté Angular

  endDate: string;     // LocalDateTime -> string

  price: number;

  capacity: number;

  status: string;

  image?: string;

  type: EventType;

  latitude: number;

  longitude: number;
  lieu: string;

  // relation backend (optionnel côté frontend)
  reservations?: any[];
}
