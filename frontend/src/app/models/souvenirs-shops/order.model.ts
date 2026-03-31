import { User } from '../users/user';
import { Shop } from '../souvenirs-shops/shop.model';
import { OrderItem } from '../souvenirs-shops/order-item.model';
import { Payment } from '../souvenirs-shops/payment.model';
import { OrderStatus } from './order-status.enum';

export interface Order {
  id?: number;
  user?: User;
  shop?: Shop;
  totalAmount: number;
  status: OrderStatus;
  createdAt?: string;
  orderItems?: OrderItem[];
  payment?: Payment;
}