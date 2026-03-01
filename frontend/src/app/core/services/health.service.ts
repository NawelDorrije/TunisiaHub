import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HealthStatus } from '../../shared/models/health-status.model';

@Injectable({ providedIn: 'root' })
export class HealthService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = `${environment.apiUrl}/health`;

  fetchHealth(): Observable<HealthStatus> {
    return this.http.get<HealthStatus>(this.endpoint);
  }
}
