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



}
