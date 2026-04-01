import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Camping } from '../../models/campings/camping';

@Injectable({
  providedIn: 'root'
})
export class CampingService {

  private BASE_URL = 'http://localhost:8089';
  private API_URL = this.BASE_URL + '/api/campings';

  constructor(private http: HttpClient) { }

  // GET all campings
  getAllCampings(): Observable<Camping[]> {
    return this.http.get<Camping[]>(this.API_URL);
  }

  // GET camping by id
  getCampingById(id: number): Observable<Camping> {
    return this.http.get<Camping>(`${this.API_URL}/${id}`);
  }

  // CREATE camping
   createCamping(
    formData: FormData
  ) {

    return this.http.post(
      this.API_URL,
      formData
    );

  }

  // UPDATE camping
 updateCamping(
    formData: FormData
  ) {

    return this.http.put(
      this.API_URL,
      formData
    );

  }

  // DELETE camping
  deleteCamping(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

}
