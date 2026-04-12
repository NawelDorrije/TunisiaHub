import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Accommodation } from '../../../models/accommodations/accommodation.model';
import { PriceRecommendation } from '../../../models/accommodations/price-recommendation.model';
import { DescriptionRecommendation } from '../../../models/accommodations/description-recommendation.model';
import { RecommendationResponse } from '../../../models/accommodations/recommendation.model';
@Injectable({
  providedIn: 'root'
})
export class AccommodationService {

  private baseUrl = 'http://localhost:8089/api/accommodations';

  constructor(private http: HttpClient) {}

  getAllAccommodations(): Observable<Accommodation[]> {
    return this.http.get<Accommodation[]>(`${this.baseUrl}/getAll`);
  }

  getAccommodationById(id: number): Observable<Accommodation> {
    return this.http.get<Accommodation>(`${this.baseUrl}/get/${id}`);
  }

  addAccommodation(accommodation: Accommodation): Observable<Accommodation> {
    return this.http.post<Accommodation>(`${this.baseUrl}/add`, accommodation);
  }

  updateAccommodation(id: number, accommodation: Accommodation): Observable<Accommodation> {
    return this.http.put<Accommodation>(`${this.baseUrl}/update/${id}`, accommodation);
  }

  deleteAccommodation(id: number): Observable<string> {
    return this.http.delete(`${this.baseUrl}/delete/${id}`, { responseType: 'text' });
  }
  suggestPrice(type: string, adresse: string, capacite: number): Observable<PriceRecommendation> {
  return this.http.post<PriceRecommendation>('http://localhost:8089/api/ai/suggest-price', {
    type,
    adresse,
    capacite
  });
}
  generateDescription(
  title: string,
  type: string,
  adresse: string,
  capacite: number,
  price: number
): Observable<DescriptionRecommendation> {
  return this.http.post<DescriptionRecommendation>(
    'http://localhost:8089/api/ai/generate-description',
    { title, type, adresse, capacite, price }
  );
}
  trackView(accommodationId: number): Observable<void> {
  return this.http.post<void>(
    `http://localhost:8089/api/history/track/${accommodationId}`, {}
  );
}

getRecommendations(): Observable<RecommendationResponse> {
  return this.http.get<RecommendationResponse>(
    'http://localhost:8089/api/ai/recommendations'
  );
}
}