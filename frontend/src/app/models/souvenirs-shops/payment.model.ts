import { Order } from './order.model';
import { PaymentStatus } from './payment-status.enum';
import { PaymentMethod } from './payment-method.enum';

export interface Payment {
  id?: number;
  order?: Order;
  status: PaymentStatus;
  method: PaymentMethod;
  amount: number;
  transactionReference?: string;
  createdAt?: string;
}