import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private BASE_URL = environment.apiUrlBase;
  constructor(private http: HttpClient) { }

  getImageUrl(path: string): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    const cleanPath = path.startsWith('/') ? path : '/' + path;
    return this.BASE_URL + cleanPath;
  }

  uploadRestaurantPicture(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(this.BASE_URL + '/api/uploads/restaurants-picture', formData);
  }

  uploadMenuItemPicture(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(this.BASE_URL + '/api/uploads/menu-items-picture', formData);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/auth/login', credentials);
  }

  signup(user: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/auth/register', user);
  }

  getCampings(): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/campings');
  }

  getRestaurants(): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/restaurants');
  }

  getRestaurantCuisines(): Observable<any[]> {
    return this.http.get<any[]>(this.BASE_URL + '/api/restaurants/cuisines');
  }

  getRestaurantById(id: number): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/restaurants/get/' + id);
  }

  addRestaurant(restaurant: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/restaurants/add', restaurant);
  }

  updateRestaurant(restaurant: any): Observable<any> {
    return this.http.put(this.BASE_URL + '/api/restaurants/update', restaurant);
  }

  deleteRestaurant(id: number): Observable<void> {
    return this.http.delete<void>(this.BASE_URL + '/api/restaurants/delete/' + id);
  }

  getMenusByRestaurantId(restaurantId: number): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/menus/by-restaurant/' + restaurantId);
  }

  getMenuItemsByMenuId(menuId: number): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/menu-items/by-menu/' + menuId);
  }

  addMenu(menu: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/menus/add', menu);
  }

  updateMenu(menu: any): Observable<any> {
    return this.http.put(this.BASE_URL + '/api/menus/update', menu);
  }

  deleteMenu(id: number): Observable<void> {
    return this.http.delete<void>(this.BASE_URL + '/api/menus/delete/' + id);
  }

  addMenuItem(menuItem: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/menu-items/add', menuItem);
  }

  updateMenuItem(menuItem: any): Observable<any> {
    return this.http.put(this.BASE_URL + '/api/menu-items/update', menuItem);
  }

  deleteMenuItem(id: number): Observable<void> {
    return this.http.delete<void>(this.BASE_URL + '/api/menu-items/delete/' + id);
  }

  getMenuTypes(): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/menus/types');
  }

  createReservation(reservation: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/reservations', reservation);
  }

  getAllReservations(): Observable<any[]> {
    return this.http.get<any[]>(this.BASE_URL + '/api/reservations');
  }

  confirmReservation(reservationId: number, tableIds: number[]): Observable<any> {
    return this.http.patch(this.BASE_URL + '/api/reservations/' + reservationId + '/confirm', { tableIds });
  }

  cancelReservationById(reservationId: number): Observable<any> {
    return this.http.patch(this.BASE_URL + '/api/reservations/' + reservationId + '/cancel', {});
  }

  getAiReservationSuggestion(restaurantId: number, date: string): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/reservations/ai-suggestions', {
      params: { restaurantId: restaurantId.toString(), date }
    });
  }

  getTablesByRestaurant(restaurantId: number, status?: string, dateTime?: string, partySize?: number): Observable<any[]> {
    let url = this.BASE_URL + '/api/restaurant-tables/by-restaurant/' + restaurantId;
    let params: string[] = [];
    if (status) params.push('status=' + encodeURIComponent(status));
    if (dateTime) params.push('dateTime=' + encodeURIComponent(dateTime));
    if (partySize) params.push('partySize=' + partySize);
    if (params.length > 0) url += '?' + params.join('&');
    return this.http.get<any[]>(url);
  }

  addRestaurantTable(payload: any): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/restaurant-tables/add', payload);
  }

  updateRestaurantTable(payload: any): Observable<any> {
    return this.http.put(this.BASE_URL + '/api/restaurant-tables/update', payload);
  }

  getRestaurantTableStatuses(): Observable<string[]> {
    return this.http.get<string[]>(this.BASE_URL + '/api/restaurant-tables/statuses');
  }

  getFloorPlan(restaurantId: number): Observable<any> {
    return this.http.get(this.BASE_URL + '/api/floor-plans/' + restaurantId);
  }

  updateTableLayout(restaurantId: number, layout: any): Observable<any> {
    return this.http.put(this.BASE_URL + '/api/restaurant-tables/bulk/' + restaurantId, layout);
  }

  getReservationsByUser(userId: number): Observable<any[]> {
    return this.http.get<any[]>(this.BASE_URL + '/api/reservations/user/' + userId);
  }

  checkInByToken(token: string): Observable<any> {
    return this.http.post(this.BASE_URL + '/api/reservations/checkin?token=' + encodeURIComponent(token), {});
  }
}
