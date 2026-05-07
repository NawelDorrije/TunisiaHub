export interface ActiviteLieu {
  id?: number;
  nomActivite: string;
  description?: string;
  prix?: number;
  duree?: number;
  capaciteMax?: number;
  placesReservees?: number;  // ← NOUVEAU
  disponible?: boolean;
  dateEvenement?: string;
  lieu?: Lieu;
}

export interface Lieu {
  id: number;
  nom: string;
  description: string;
  type: string;
  ville: string;
  image: string;
  latitude: number;
  longitude: number;
  horaires: string;
  activites?: ActiviteLieu[];
}