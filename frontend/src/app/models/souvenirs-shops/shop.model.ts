import { Product } from '../souvenirs-shops/product.model';
import { Order } from '../souvenirs-shops/order.model';
import { Review } from '../souvenirs-shops/review.model';
import { ShopCategory } from './shop-category.enum';

export interface ShopOwner {
  id: number;
  prenom?: string;
  nom?: string;
  email?: string;
}

export interface Shop {
  id?: number;
  name: string;
  description: string;
  category?: ShopCategory;
  city?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  photoUrl?: string;
  createdAt?: string;
  owner?: ShopOwner;
  products?: Product[];
  orders?: Order[];
  reviews?: Review[];
  averageRating?: number;
}

export interface NearbyShopResponse extends Shop {
  distanceKm: number;
}