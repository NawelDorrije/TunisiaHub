import { Review } from './review.model';

export interface Product {
  id?: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  photoUrl?: string;
  createdAt?: string;
  category?: string;
  shop?: { id: number; name?: string };
  reviews?: Review[];
  averageRating?: number;
}