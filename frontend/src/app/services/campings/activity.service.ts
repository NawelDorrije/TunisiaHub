import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Activity } from '../../models/campings/activity';

@Injectable({ providedIn: 'root' })
export class ActivityService {
  private API_URL = 'http://localhost:8089/api/activities';
  constructor(private http: HttpClient) {}

  getAllActivities(): Observable<Activity[]> {
    return this.http.get<Activity[]>(this.API_URL);
  }
  getActivityById(id: number): Observable<Activity> {
    return this.http.get<Activity>(`${this.API_URL}/${id}`);
  }
  getActivitiesByCamping(campingId: number): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.API_URL}/camping/${campingId}`);
  }
  getActivitiesBySpot(spotId: number): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.API_URL}/spot/${spotId}`);
  }
  createActivity(activity: Activity): Observable<Activity> {
    return this.http.post<Activity>(this.API_URL, activity);
  }
  updateActivity(id: number, activity: Activity): Observable<Activity> {
    return this.http.put<Activity>(`${this.API_URL}/${id}`, activity);
  }
  deleteActivity(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}
