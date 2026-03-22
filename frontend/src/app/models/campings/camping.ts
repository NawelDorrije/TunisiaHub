import { Spot } from "./spot";

export interface Camping {
  id?: number;

  name: string;

  location: string;

  campingType: string;

  price: number;

  description: string;

  startDate: string;

  endDate: string;

  photos: string[];

  spots: Spot[];
}
