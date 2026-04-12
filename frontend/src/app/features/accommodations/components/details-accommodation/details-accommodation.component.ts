import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';
import { AuthService } from '../../../auth/services/auth.service';

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

  // Recommendations
  recommendations: Accommodation[] = [];
  recommendationReasoning: string = '';
  isLoadingRecommendations: boolean = false;

  constructor(
    private accommodationService: AccommodationService,
    private route: ActivatedRoute,
    private router: Router,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAccommodation(id);
  }

  loadAccommodation(id: number): void {
    this.accommodationService.getAccommodationById(id).subscribe({
      next: (data) => {
        this.accommodation = data;
        this.selectedPhoto = data.photos?.[0] || 'assets/images/placeholder.jpg';
        this.isLoading = false;

        // Track view if logged in
        if (this.authService.isLoggedIn()) {
          this.accommodationService.trackView(id).subscribe();
          this.loadRecommendations();
        }
      },
      error: () => {
        this.errorMessage = 'Accommodation not found.';
        this.isLoading = false;
      }
    });
  }

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

  selectPhoto(photo: string): void {
    this.selectedPhoto = photo;
  }

  goBack(): void {
    this.router.navigate(['/accommodations']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/accommodations/edit', id]);
  }

  goToDetail(id: number): void {
    this.router.navigate(['/accommodations/detail', id]);
  }
}