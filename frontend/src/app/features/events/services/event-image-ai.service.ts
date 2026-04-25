import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EventImageUploadResponse {
  imageUrl: string;
  aiDescription: string;
}

@Injectable({
  providedIn: 'root'
})
export class EventImageAiService {
  private readonly eventsApiUrl = 'http://localhost:8089/api/events';

  constructor(private http: HttpClient) {}

  uploadImageAndGenerateDescription(file: File): Observable<EventImageUploadResponse> {
    const formData = new FormData();
    formData.append('image', file);
    return this.http.post<EventImageUploadResponse>(`${this.eventsApiUrl}/upload-image`, formData);
  }
}
