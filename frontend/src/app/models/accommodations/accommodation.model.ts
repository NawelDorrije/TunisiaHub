export interface Accommodation {
  id?: number;
  title: string;
  description: string;
  adresse: string;
  type: string;
  price: number;
  capacite: number;
  photos: string[];
    latitude?: number;
  longitude?: number;
}