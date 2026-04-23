export type UserRole = 'ADMIN' | 'CLIENT' | 'OWNER';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface AuthResponse {
  token: string;
  role: UserRole;
  email: string;
  nom: string;
  prenom: string;
  userId?: number;
}