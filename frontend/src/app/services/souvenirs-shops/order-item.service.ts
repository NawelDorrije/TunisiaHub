import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderItem } from '../../models/souvenirs-shops/order-item.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrderItemService {

  private apiUrl = `${environment.apiUrl}/order-items`;

  constructor(private http: HttpClient) {}

  getAllOrderItems(): Observable<OrderItem[]> { return this.http.get<OrderItem[]>(`${this.apiUrl}`); }
  getOrderItemById(id: number): Observable<OrderItem> { return this.http.get<OrderItem>(`${this.apiUrl}/${id}`); }
  getOrderItemsByOrder(orderId: number): Observable<OrderItem[]> { return this.http.get<OrderItem[]>(`${this.apiUrl}/order/${orderId}`); }

  addOrderItem(item: OrderItem): Observable<OrderItem> { return this.http.post<OrderItem>(`${this.apiUrl}`, item); }
  updateOrderItem(item: OrderItem): Observable<OrderItem> { return this.http.put<OrderItem>(`${this.apiUrl}`, item); }
  deleteOrderItem(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}