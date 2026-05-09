import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';
<<<<<<< HEAD
=======
import { AuthService } from '../../../auth/services/auth.service';
import { ReviewService } from '../../services/review.service';
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-details-accommodation',
  templateUrl: './details-accommodation.component.html',
  styleUrls: ['./details-accommodation.component.css']
})
export class DetailsAccommodationComponent implements OnInit {

  accommodation!: Accommodation;
  errorMessage: string = '';
  isLoading: boolean = true;
  selectedPhoto: string = '';

<<<<<<< HEAD
  constructor(
    private accommodationService: AccommodationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAccommodation(id);
  }

  loadAccommodation(id: number): void {
=======
  // Recommendations
  recommendations: Accommodation[] = [];
  recommendationReasoning: string = '';
  isLoadingRecommendations: boolean = false;
  currentAccommodationId: number | null = null;

  constructor(
    private accommodationService: AccommodationService,
    private reviewService: ReviewService,
    private route: ActivatedRoute,
    private router: Router,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      if (!id || id <= 0 || id === this.currentAccommodationId) return;

      this.currentAccommodationId = id;
      this.loadAccommodation(id);
    });
  }

  loadAccommodation(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.recommendations = [];

>>>>>>> origin/feature/integrated-app-event
    this.accommodationService.getAccommodationById(id).subscribe({
      next: (data) => {
        this.accommodation = data;
        this.selectedPhoto = data.photos?.[0] || 'assets/images/placeholder.jpg';
<<<<<<< HEAD
        this.isLoading = false;
=======
        this.attachAverageRating(this.accommodation);
        this.isLoading = false;

        // Track view if logged in
        if (this.authService.isLoggedIn()) {
          this.accommodationService.trackView(id).subscribe();
          this.loadRecommendations();
        }
>>>>>>> origin/feature/integrated-app-event
      },
      error: () => {
        this.errorMessage = 'Accommodation not found.';
        this.isLoading = false;
      }
    });
  }

<<<<<<< HEAD
=======
  loadRecommendations(): void {
    this.isLoadingRecommendations = true;
    this.accommodationService.getRecommendations().subscribe({
      next: (data) => {
        if (data.recommended_ids?.length > 0) {
          this.recommendationReasoning = data.reasoning;
          // Fetch each recommended accommodation
          const fetches = data.recommended_ids
            .filter(rid => rid !== this.accommodation.id)
            .slice(0, 3);

          this.recommendations = [];
          fetches.forEach(rid => {
            this.accommodationService.getAccommodationById(rid).subscribe({
              next: (acc) => this.recommendations.push(acc),
              error: () => {}
            });
          });
        }
        this.isLoadingRecommendations = false;
      },
      error: () => {
        this.isLoadingRecommendations = false;
      }
    });
  }

>>>>>>> origin/feature/integrated-app-event
  selectPhoto(photo: string): void {
    this.selectedPhoto = photo;
  }

  goBack(): void {
    this.router.navigate(['/accommodations']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/accommodations/edit', id]);
  }
<<<<<<< HEAD
=======

  goToDetail(id: number): void {
    this.router.navigate(['/accommodations/detail', id]);
  }

  goToSignUpForReservation(): void {
    const returnUrl = `/accommodations/detail/${this.accommodation.id}`;
    this.router.navigate(['/auth/sign-up'], { queryParams: { returnUrl } });
  }

  private attachAverageRating(accommodation: Accommodation): void {
    if (!accommodation.id) return;

    this.reviewService.getReviewsByAccommodation(accommodation.id).subscribe({
      next: (reviews) => {
        if (!reviews.length) {
          accommodation.averageRating = 0;
          return;
        }
        const sum = reviews.reduce((total, r) => total + r.rating, 0);
        accommodation.averageRating = Math.round((sum / reviews.length) * 10) / 10;
      },
      error: () => {
        accommodation.averageRating = accommodation.averageRating ?? 0;
      }
    });
  }
>>>>>>> origin/feature/integrated-app-event
}