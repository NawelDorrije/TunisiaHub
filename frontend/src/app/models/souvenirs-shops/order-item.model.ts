import { Order } from './order.model';
import { Product } from './product.model';

export interface OrderItem {
  id?: number;
  order?: Order;
  product?: Product;
  quantity: number;
  unitPrice: number;
  createdAt?: string;
}