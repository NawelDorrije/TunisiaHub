import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OrderItem {
  id: number;
  productName: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  user?: {
    id: number;
    nom: string;
    prenom: string;
    email: string;
  };
  shop?: {
    id: number;
    name: string;
  };
  items?: OrderItem[];
  totalAmount: number;
  status: string;
  createdAt: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminOrderService {
  private apiUrl = 'http://localhost:8089/api/souvenir-shops/orders';

  constructor(private http: HttpClient) { }

  getAllOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl);
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${id}`);
  }

  getOrderItems(id: number): Observable<OrderItem[]> {
    return this.http.get<OrderItem[]>(`${this.apiUrl}/${id}/items`);
  }

  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/${id}/status`, { status });
  }

  deleteOrder(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
