import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';

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
    return this.http.post<unknown>(`${this.eventsApiUrl}/upload-image`, formData).pipe(
      map((response) => this.normalizeUploadResponse(response))
    );
  }

  private normalizeUploadResponse(response: unknown): EventImageUploadResponse {
    const payload = this.unwrapPayload(response);
    const imageUrl =
      this.toText(payload['imageUrl']) ??
      this.toText(payload['image']) ??
      this.toText(payload['url']) ??
      '';

    const aiDescription =
      this.toText(payload['aiDescription']) ??
      this.toText(payload['description']) ??
      this.toText(payload['generatedDescription']) ??
      '';

    return { imageUrl, aiDescription };
  }

  private unwrapPayload(response: unknown): Record<string, unknown> {
    if (!this.isRecord(response)) {
      return {};
    }

    const nestedData = response['data'];
    if (this.isRecord(nestedData)) {
      return nestedData;
    }

    return response;
  }

  private isRecord(value: unknown): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
  }

  private toText(value: unknown): string | null {
    return typeof value === 'string' && value.trim().length > 0 ? value.trim() : null;
  }
}
