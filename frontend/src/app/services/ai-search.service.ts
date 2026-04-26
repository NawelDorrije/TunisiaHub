import { Injectable, signal, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, finalize, of, tap } from 'rxjs';
import { AiSearchRequest, AiSearchResponse } from '../models/ai-search/ai-search.model';

@Injectable({
  providedIn: 'root'
})
export class AiSearchService {
  private http = inject(HttpClient);
  // Base URL consistent with ApiService
  private readonly BASE_URL = 'http://localhost:8089';

  // Signals for state management
  results = signal<AiSearchResponse | null>(null);
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);

  /**
   * Performs an AI-powered search call.
   * @param query The search query string.
   * @param latitude Optional latitude for location-based search.
   * @param longitude Optional longitude for location-based search.
   */
  search(query: string, latitude?: number, longitude?: number): Observable<AiSearchResponse | null> {
    if (!query.trim()) {
      return of(null);
    }

    const payload: AiSearchRequest = { query, latitude, longitude };
    
    // Reset state before starting the search
    this.isLoading.set(true);
    this.error.set(null);

    return this.http.post<AiSearchResponse>(`${this.BASE_URL}/api/ai/search`, payload).pipe(
      tap((response: AiSearchResponse) => {
        this.results.set(response);
      }),
      catchError((err: HttpErrorResponse) => {
        const errorMessage = err.error?.message || 'An unexpected error occurred during the AI search.';
        this.error.set(errorMessage);
        console.error('AI Search Error:', err);
        return of(null);
      }),
      finalize(() => {
        this.isLoading.set(false);
      })
    );
  }

  /**
   * Resets the entire search state.
   */
  clear(): void {
    this.results.set(null);
    this.isLoading.set(false);
    this.error.set(null);
  }
}
