import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../../models/souvenirs-shops/product.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private apiUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {}

  getAllProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  getProductsByShop(shopId: number): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/shop/${shopId}`);
  }

  getReviewsByProduct(productId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${productId}/reviews`);
  }

  addProduct(product: Product): Observable<Product> {
    return this.http.post<Product>(`${this.apiUrl}`, product);
  }

  updateProduct(product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}`, product);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}