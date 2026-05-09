<<<<<<< HEAD
=======
// auth.model.ts

export type UserRole = 'ADMIN' | 'CLIENT' | 'OWNER';

>>>>>>> origin/feature/integrated-app-event
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
<<<<<<< HEAD
}

export interface AuthResponse {
  token: string;
  role: string;
  email: string;
  nom: string;
  prenom: string;
  id: number;
=======
  role: UserRole;
}

export interface AuthResponse {

  id: number;

  token: string;

  role: UserRole;

  email: string;

  nom: string;

  prenom: string;

  userId?: number;

  user?: {
    id: number;
    email: string;
    role: UserRole;
  };
>>>>>>> origin/feature/integrated-app-event
}