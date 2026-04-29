export interface Activity {
  id?: number;
  name: string;
  description?: string;
  price: number;
  duration?: number;
  active?: boolean;
  campingId: number;
  campingName?: string;
  spotId?: number;
  spotName?: string;
}
