import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  Observable, BehaviorSubject, of, tap, catchError, Subject, forkJoin, map
} from 'rxjs';

export interface PricingResponse {
  spotId: number;
  checkIn: string;
  basePrice: number;
  maxPrice: number | null;       // owner cap, may be null
  dynamicPrice: number;
  multiplier: number;
  pricingActive: boolean;
   reason?: string;
}

export interface PricingAudit {
  id: number;
  spotId: number;
  basePrice: number;
  dynamicPrice: number;
  rawMultiplier: number;
  reason: string;
  weatherScore: number;
  occupancyRate: number;
  demandIndex: number;
  localEventNearby: boolean;
  dayOfWeek: string;
  daysUntilCheckIn: number;
  createdAt: string;
  bookingConfirmed: boolean;
}

/** Combined result used by the popup */
export interface PricingPopupData {
  pricing: PricingResponse;
  audit: PricingAudit | null;
}

export interface PriceLevel {
  label: string;
  icon: string;
  colorClass: string;
  description: string;
  badgeClass: string;
}

export interface SpotPricingState {
  spotId: number;
  loading: boolean;
  pricing: PricingResponse | null;
  error: boolean;
  priceChanged: boolean;
}

@Injectable({ providedIn: 'root' })
export class DynamicPricingService {

  private readonly API = 'http://localhost:8089/api/pricing';

  private priceCache = new Map<string, PricingResponse>();
  private spotStates = new Map<number, BehaviorSubject<SpotPricingState>>();

  private globalLoadingSubject = new BehaviorSubject<boolean>(false);
  loading$ = this.globalLoadingSubject.asObservable();

  private priceUpdated$ = new Subject<{ spotId: number; oldPrice: number; newPrice: number }>();
  priceUpdated = this.priceUpdated$.asObservable();

  constructor(private http: HttpClient) {}

  // ── State management ───────────────────────────────────────────────

  getSpotState$(spotId: number): Observable<SpotPricingState> {
    if (!this.spotStates.has(spotId)) {
      this.spotStates.set(spotId, new BehaviorSubject<SpotPricingState>({
        spotId, loading: false, pricing: null, error: false, priceChanged: false
      }));
    }
    return this.spotStates.get(spotId)!.asObservable();
  }

  private patchState(spotId: number, patch: Partial<SpotPricingState>): void {
    const subject = this.spotStates.get(spotId);
    if (subject) subject.next({ ...subject.getValue(), ...patch });
  }

  // ── Core API calls ─────────────────────────────────────────────────

  getEffectivePrice(spotId: number, checkIn: string): Observable<PricingResponse> {
    const key = `${spotId}_${checkIn}`;
    if (!this.spotStates.has(spotId)) this.getSpotState$(spotId);
    if (this.priceCache.has(key)) return of(this.priceCache.get(key)!);

    const prev = this.spotStates.get(spotId)?.getValue();
    this.patchState(spotId, { loading: true, error: false });
    this.globalLoadingSubject.next(true);

    const params = new HttpParams().set('checkIn', checkIn);
    return this.http.get<PricingResponse>(`${this.API}/spots/${spotId}/price`, { params }).pipe(
      tap(res => {
        const oldPrice = prev?.pricing?.dynamicPrice ?? prev?.pricing?.basePrice ?? null;
        this.priceCache.set(key, res);
        this.patchState(spotId, {
          loading: false, pricing: res, error: false,
          priceChanged: oldPrice !== null && oldPrice !== res.dynamicPrice
        });
        this.globalLoadingSubject.next(false);
        if (oldPrice !== null && oldPrice !== res.dynamicPrice) {
          this.priceUpdated$.next({ spotId, oldPrice, newPrice: res.dynamicPrice });
        }
      }),
      catchError(err => {
        this.patchState(spotId, { loading: false, error: true });
        this.globalLoadingSubject.next(false);
        throw err;
      })
    );
  }

  /** Fetch latest audit record for explanation fields */
  getLatestAudit(spotId: number): Observable<PricingAudit | null> {
    return this.http.get<PricingAudit>(`${this.API}/spots/${spotId}/latest-audit`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Fetch price + latest audit in parallel.
   * Use this in the spot-form popup after creation.
   */
  getPricingWithExplanation(spotId: number, checkIn: string): Observable<PricingPopupData> {
    // Bypass cache for fresh post-creation data
    this.priceCache.delete(`${spotId}_${checkIn}`);

    return forkJoin({
      pricing: this.getEffectivePrice(spotId, checkIn),
      audit: this.getLatestAudit(spotId)
    });
  }

  getSpotStateSnapshot(spotId: number): SpotPricingState {
    if (!this.spotStates.has(spotId)) this.getSpotState$(spotId);
    return this.spotStates.get(spotId)?.getValue() ?? {
      spotId, loading: false, pricing: null, error: false, priceChanged: false
    };
  }

  refreshAllSpots(spotIds: number[], checkIn: string): void {
    spotIds.forEach(id => this.priceCache.delete(`${id}_${checkIn}`));
    spotIds.forEach(id => this.getEffectivePrice(id, checkIn).subscribe({ error: () => {} }));
  }

  validatePriceBeforeBooking(spotId: number, checkIn: string): Observable<PricingResponse> {
    this.priceCache.delete(`${spotId}_${checkIn}`);
    return this.getEffectivePrice(spotId, checkIn);
  }

  getPricingHistory(spotId: number): Observable<PricingAudit[]> {
    return this.http.get<PricingAudit[]>(`${this.API}/spots/${spotId}/history`);
  }

  repriceSpot(spotId: number, checkIn?: string): Observable<any> {
    let params = new HttpParams();
    if (checkIn) params = params.set('checkIn', checkIn);
    return this.http.post(`${this.API}/spots/${spotId}/reprice`, null, { params });
  }

  markBookingConfirmed(spotId: number): Observable<any> {
    return this.http.post(`${this.API}/spots/${spotId}/booking-confirmed`, null);
  }

  clearCache(): void { this.priceCache.clear(); }

  // ── Display helpers ────────────────────────────────────────────────

getPriceLevel(multiplier: number): PriceLevel {
  if (multiplier >= 1.4)  return { label: 'Peak Demand',  icon: '🔥', colorClass: 'level-hot',      badgeClass: 'badge--peak',     description: 'High occupancy + local event' };
  if (multiplier >= 1.2)  return { label: 'High Demand',  icon: '📈', colorClass: 'level-high',     badgeClass: 'badge--high',     description: 'Weekend or seasonal pressure' };
  if (multiplier >= 1.05) return { label: 'Moderate',     icon: '☀️', colorClass: 'level-moderate', badgeClass: 'badge--moderate', description: 'Slightly above standard' };
  return                         { label: 'Standard',     icon: '⚖️', colorClass: 'level-standard', badgeClass: 'badge--standard', description: 'Balanced signals' };
}

  getPriceChangePercent(basePrice: number, dynamicPrice: number): number {
    if (!basePrice) return 0;
    return Math.round(((dynamicPrice - basePrice) / basePrice) * 100);
  }

  getEffectiveDisplayPrice(pricing: PricingResponse | null, basePrice: number): number {
    return pricing?.dynamicPrice ?? basePrice;
  }

  getSpotCardVariant(multiplier: number | undefined): string {
    if (!multiplier) return '';
    if (multiplier <= 0.9) return 'spot-card--deal';
    if (multiplier >= 1.4) return 'spot-card--peak';
    return '';
  }
  runFullRepricing(): Observable<any> {
  return this.http.post(`${this.API}/run`, {}).pipe(
    tap(res => {
      console.log('🔄 Full repricing triggered:', res);
    }),
    catchError(err => {
      console.error('❌ Repricing failed', err);
      throw err;
    })
  );
}
}
