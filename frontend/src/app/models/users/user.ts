export interface User {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  motDePasse?: string;
  role: 'ADMIN' | 'CLIENT' | 'OWNER';
}