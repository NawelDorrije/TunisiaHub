import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { HealthService } from '../../core/services/health.service';
import { HealthStatus } from '../../shared/models/health-status.model';
import { finalize, take } from 'rxjs/operators';

@Component({
  selector: 'app-health',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './health.component.html',
  styleUrl: './health.component.scss'
})
export class HealthComponent {
  private readonly healthService = inject(HealthService);

  protected readonly loading = signal(false);
  protected readonly payload = signal<HealthStatus | null>(null);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.refresh();
  }

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.payload.set(null);

    this.healthService
      .fetchHealth()
      .pipe(take(1), finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => this.payload.set(response),
        error: () => this.error.set('Unable to reach backend health endpoint')
      });
  }
}
