export interface Review {
  id?: number;
  rating: number;
  comment: string;
  reviewDate?: string;
  user?: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
}