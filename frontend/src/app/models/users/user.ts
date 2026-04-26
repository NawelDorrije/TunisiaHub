import { Reservation } from "../shared-reservation/reservation";

export interface User {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  role: 'ADMIN' | 'USER' |'OWNER';
  reservations?: Reservation[];
}
