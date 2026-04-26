import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../../services/api.service';

@Component({
  selector: 'app-check-in',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="checkin-container">
      <div class="checkin-card shadow">
        <div *ngIf="loading" class="spinner-section">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <h4 class="mt-3">Processing your check-in...</h4>
          <p class="text-muted">Please wait a moment while we verify your reservation.</p>
        </div>
        
        <div *ngIf="!loading && success" class="result-section fade-in">
          <div class="icon-circle bg-success text-white">
            <i class="bi bi-check-lg"></i>
          </div>
          <h2 class="mt-4 mb-3">Welcome!</h2>
          <p class="lead">Your reservation is now <strong>{{ reservation?.status }}</strong> ✅</p>
          <div class="reservation-details" *ngIf="reservation">
            <div class="detail-item">
              <span class="label">Restaurant:</span>
              <span class="value">{{ reservation.restaurant?.name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">Date:</span>
              <span class="value">{{ formatDateTime(reservation.dateTime) }}</span>
            </div>
          </div>
          <button class="btn btn-primary w-100 mt-4" (click)="goToHome()">Enjoy your meal!</button>
        </div>

        <div *ngIf="!loading && !success" class="result-section fade-in">
          <div class="icon-circle bg-danger text-white">
            <i class="bi bi-x-lg"></i>
          </div>
          <h2 class="mt-4 mb-3">Check-in Failed</h2>
          <p class="text-muted mb-4">{{ errorMessage }}</p>
          <button class="btn btn-outline-secondary w-100" (click)="goToHome()">Back to Home</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .checkin-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
      padding: 20px;
    }
    .checkin-card {
      background: white;
      border-radius: 20px;
      padding: 40px;
      width: 100%;
      max-width: 450px;
      text-align: center;
      border: none;
    }
    .spinner-section {
      padding: 20px 0;
    }
    .icon-circle {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto;
      font-size: 40px;
    }
    .fade-in {
      animation: fadeIn 0.5s ease-in;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .reservation-details {
      background: #f8f9fa;
      border-radius: 12px;
      padding: 20px;
      margin-top: 20px;
      text-align: left;
    }
    .detail-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
    }
    .detail-item:last-child { margin-bottom: 0; }
    .label { color: #6c757d; font-weight: 500; }
    .value { font-weight: 600; color: #212529; }
    .btn-primary {
      background: #d4a574;
      border: none;
      padding: 12px;
      font-weight: 600;
      border-radius: 10px;
    }
    .btn-primary:hover { background: #c39463; }
  `]
})
export class CheckInComponent implements OnInit {
  loading = true;
  success = false;
  errorMessage = '';
  reservation: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService
  ) {}

  ngOnInit(): void {
    // Read token from query params: /checkin?token=XYZ
    const token = this.route.snapshot.queryParamMap.get('token');
    
    if (token) {
      this.api.checkInByToken(token).subscribe({
        next: (data) => {
          this.reservation = data;
          this.success = true;
          this.loading = false;
        },
        error: (err) => {
          console.error('Check-in error:', err);
          
          if (err.status === 400 && (err.error?.includes('already') || (typeof err.error === 'string' && err.error.includes('already')))) {
             this.errorMessage = 'You have already checked in! ✅';
             this.success = true; 
             this.loading = false;
             // If the error response contains the reservation object despite the 400
             if (err.error?.id) this.reservation = err.error;
          } else {
             this.success = false;
             this.loading = false;
             this.errorMessage = err?.error?.message || err?.error || 'Invalid or expired check-in token.';
          }
        }
      });
    } else {
      this.loading = false;
      this.errorMessage = 'Missing check-in token. Please scan the QR code from your PDF.';
    }
  }

  formatDateTime(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      return `${dt[0]}-${dt[1]}-${dt[2]} ${dt[3]}:${String(dt[4]).padStart(2, '0')}`;
    }
    return String(dt).replace('T', ' ').slice(0, 16);
  }

  goToHome(): void {
    this.router.navigate(['/']);
  }
}
