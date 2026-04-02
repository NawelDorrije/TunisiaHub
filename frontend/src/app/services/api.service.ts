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
    return this.http.get(`${this.BASE_URL}/api/restaurants/get/${id}`);
  }

  addRestaurant(restaurant: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/restaurants/add`, restaurant);
  }

  updateRestaurant(restaurant: any): Observable<any> {
    return this.http.put(`${this.BASE_URL}/api/restaurants/update`, restaurant);
  }

  deleteRestaurant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/api/restaurants/delete/${id}`);
  }

  getMenusByRestaurantId(restaurantId: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/menus/by-restaurant/${restaurantId}`);
  }

  getMenuItemsByMenuId(menuId: number): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/menu-items/by-menu/${menuId}`);
  }

  addMenu(menu: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/menus/add`, menu);
  }

  updateMenu(menu: any): Observable<any> {
    return this.http.put(`${this.BASE_URL}/api/menus/update`, menu);
  }

  deleteMenu(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/api/menus/delete/${id}`);
  }

  addMenuItem(menuItem: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/menu-items/add`, menuItem);
  }

  updateMenuItem(menuItem: any): Observable<any> {
    return this.http.put(`${this.BASE_URL}/api/menu-items/update`, menuItem);
  }

  deleteMenuItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/api/menu-items/delete/${id}`);
  }

  getMenuTypes(): Observable<any> {
    return this.http.get(`${this.BASE_URL}/api/menus/types`);
  }

  createReservation(reservation: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/api/reservations`, reservation);
  }

  getAllReservations(): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/api/reservations`);
  }

  confirmReservation(reservationId: number, tableIds: number[]): Observable<any> {
    return this.http.patch(
      `${this.BASE_URL}/api/reservations/${reservationId}/confirm`,
      { tableIds },
    );
  }

  cancelReservationById(reservationId: number): Observable<any> {
    return this.http.patch(
      `${this.BASE_URL}/api/reservations/${reservationId}/cancel`,
      {},
    );
  }

  getTablesByRestaurant(
    restaurantId: number,
    status?: string,
  ): Observable<any[]> {
    let url = `${this.BASE_URL}/api/restaurant-tables/by-restaurant/${restaurantId}`;
    if (status) {
      url += `?status=${encodeURIComponent(status)}`;
    }
    return this.http.get<any[]>(url);
  }
}

