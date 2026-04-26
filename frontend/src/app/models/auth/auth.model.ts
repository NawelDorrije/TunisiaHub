export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nom: string;
  prenom: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  role: string;
  email: string;
  nom: string;
  prenom: string;
  id: number;
}