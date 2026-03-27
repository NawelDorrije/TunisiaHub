import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private BASE_URL = 'http://localhost:8089';

  constructor(private http: HttpClient) { }

  getCampings(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/campings`);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/auth/login`, credentials);
  }

  signup(user: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/auth/signup`, user);
  }

  getRestaurants(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/restaurants`);
  }

  getRestaurantById(id: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/restaurants/${id}`);
  }

  addRestaurant(restaurant: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/restaurants/add`, restaurant);
  }

  updateRestaurant(id: number, restaurant: any): Observable<any> {
    return this.http.put(`${this.BASE_URL}/api/restaurants/update/${id}`, restaurant);
  }

  deleteRestaurant(id: number): Observable<any> {
    return this.http.delete(`${this.BASE_URL}/api/restaurants/delete/${id}`);
  }

  getMenusByRestaurantId(restaurantId: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/menus/by-restaurant/${restaurantId}`);
  }

  getMenuItemsByMenuId(menuId: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/menu-items/by-menu/${menuId}`);
  }
}

