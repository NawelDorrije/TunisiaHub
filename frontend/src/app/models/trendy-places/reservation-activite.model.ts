export interface ReservationActivite {
  id?: number;
  dateReservation?: string;
  nombrePersonnes: number;
  prixTotal?: number;
  statut?: string;
  activite?: {
    id: number;
    nomActivite: string;
    prix: number;
    lieu?: { nom: string; ville: string };
  };
  user?: { id: number; nom: string; prenom: string };
}