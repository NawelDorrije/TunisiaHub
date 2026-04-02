import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../../models/souvenirs-shops/order.model';
import { OrderItem } from '../../models/souvenirs-shops/order-item.model';
import { Payment } from '../../models/souvenirs-shops/payment.model';
import { environment } from '../../environments/environment';

export interface CreateOrdersRequest {
  items: Array<{ productId: number; quantity: number }>;
}

@Injectable({ providedIn: 'root' })
export class OrderService {

  private apiUrl = `${environment.apiUrl}/orders`;

  constructor(private http: HttpClient) {}

  getAllOrders(): Observable<Order[]> { return this.http.get<Order[]>(`${this.apiUrl}`); }
  getOrderById(id: number): Observable<Order> { return this.http.get<Order>(`${this.apiUrl}/${id}`); }
  getOrdersByUser(userId: number): Observable<Order[]> { return this.http.get<Order[]>(`${this.apiUrl}/user/${userId}`); }
  getOrdersByShop(shopId: number): Observable<Order[]> { return this.http.get<Order[]>(`${this.apiUrl}/shop/${shopId}`); }
  getOrderItems(orderId: number): Observable<OrderItem[]> { return this.http.get<OrderItem[]>(`${this.apiUrl}/${orderId}/items`); }
  getOrderPayments(orderId: number): Observable<Payment[]> { return this.http.get<Payment[]>(`${this.apiUrl}/${orderId}/payments`); }

  createOrders(request: CreateOrdersRequest): Observable<Order[]> {
    return this.http.post<Order[]>(`${this.apiUrl}`, request);
  }

  updateOrderStatus(orderId: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${orderId}/status`, { status });
  }

  addOrder(order: Order): Observable<Order> { return this.http.post<Order>(`${this.apiUrl}`, order); }
  updateOrder(order: Order): Observable<Order> { return this.http.put<Order>(`${this.apiUrl}`, order); }
  deleteOrder(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}