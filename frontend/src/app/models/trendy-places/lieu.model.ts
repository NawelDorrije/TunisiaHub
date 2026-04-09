export interface ActiviteLieu {
  id: number;
  nomActivite: string;
  description: string;
  prix: number;
  duree: number;
  capaciteMax: number;
  disponible: boolean;
  dateEvenement?: string; // ← NOUVEAU
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