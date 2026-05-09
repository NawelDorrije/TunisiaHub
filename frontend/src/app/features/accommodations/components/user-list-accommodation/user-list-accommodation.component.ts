import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';
<<<<<<< HEAD
=======
import { AuthService } from '../../../auth/services/auth.service';
import { ReviewService } from '../../services/review.service';
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-user-list-accommodation',
  templateUrl: './user-list-accommodation.component.html',
  styleUrls: ['./user-list-accommodation.component.css']
})
export class UserListAccommodationComponent implements OnInit {

<<<<<<< HEAD
  accommodations: Accommodation[] = [];
  errorMessage: string = '';
  isLoading: boolean = true;

  constructor(
    private accommodationService: AccommodationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAccommodations();
  }

  loadAccommodations(): void {
    this.isLoading = true;
    this.accommodationService.getAllAccommodations().subscribe({
      next: (data) => {
        this.accommodations = data;
=======
  allAccommodations: Accommodation[] = [];
  filteredAccommodations: Accommodation[] = [];
  errorMessage: string = '';
  isLoading: boolean = true;

  // Filters
  searchKeyword: string = '';
  selectedType: string = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  minCapacity: number | null = null;
  sortOrder: string = '';

  // Available types
  accommodationTypes: string[] = ['Villa', 'Apartment', 'Hostel', 'Hotel', 'Chalet'];

  // Sidebar toggle for mobile
  showFilters: boolean = false;

  constructor(
    private accommodationService: AccommodationService,
    private reviewService: ReviewService,
    private router: Router,
    public authService: AuthService
  ) {}

 // Add these properties
recommendations: Accommodation[] = [];
recommendationReasoning: string = '';
isLoadingRecommendations: boolean = false;

// Add in ngOnInit
ngOnInit(): void {
  this.loadAccommodations();
  if (this.authService.isLoggedIn()) {
    this.loadRecommendations();
  }
}

loadRecommendations(): void {
  this.isLoadingRecommendations = true;
  this.accommodationService.getRecommendations().subscribe({
    next: (data) => {
      if (data.recommended_ids?.length > 0) {
        this.recommendationReasoning = data.reasoning;
        data.recommended_ids.slice(0, 3).forEach(rid => {
          this.accommodationService.getAccommodationById(rid).subscribe({
            next: (acc) => {
              this.recommendations.push(acc);
              this.attachAverageRating(acc);
            },
            error: () => {}
          });
        });
      }
      this.isLoadingRecommendations = false;
    },
    error: () => this.isLoadingRecommendations = false
  });
}
  loadAccommodations(): void {
    this.fetchFilteredAccommodations();
  }

  fetchFilteredAccommodations(): void {
    this.isLoading = true;
    this.accommodationService.getFilteredAccommodations(
      this.selectedType || undefined,
      this.minPrice ?? undefined,
      this.maxPrice ?? undefined,
      this.minCapacity ?? undefined
    ).subscribe({
      next: (data) => {
        this.allAccommodations = data;
        this.allAccommodations.forEach(acc => this.attachAverageRating(acc));
        this.applyClientSideSearchAndSort();
>>>>>>> origin/feature/integrated-app-event
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load accommodations.';
        this.isLoading = false;
      }
    });
  }

<<<<<<< HEAD
  goToDetail(id: number): void {
    this.router.navigate(['/accommodations/detail', id]);
  }
=======
  applyFilters(): void {
    this.fetchFilteredAccommodations();
  }

  applyClientSideSearchAndSort(): void {
    let result = [...this.allAccommodations];

    // Search by keyword
    if (this.searchKeyword.trim()) {
      const keyword = this.searchKeyword.toLowerCase();
      result = result.filter(a =>
        a.title.toLowerCase().includes(keyword) ||
        a.adresse.toLowerCase().includes(keyword) ||
        a.description?.toLowerCase().includes(keyword)
      );
    }

    // Sort by price
    if (this.sortOrder === 'asc') {
      result.sort((a, b) => a.price - b.price);
    } else if (this.sortOrder === 'desc') {
      result.sort((a, b) => b.price - a.price);
    }

    this.filteredAccommodations = result;
  }

  resetFilters(): void {
    this.searchKeyword = '';
    this.selectedType = '';
    this.minPrice = null;
    this.maxPrice = null;
    this.minCapacity = null;
    this.sortOrder = '';
    this.loadAccommodations();
  }

  get activeFiltersCount(): number {
    let count = 0;
    if (this.searchKeyword.trim()) count++;
    if (this.selectedType) count++;
    if (this.minPrice) count++;
    if (this.maxPrice) count++;
    if (this.minCapacity) count++;
    if (this.sortOrder) count++;
    return count;
  }

  goToDetail(id: number): void {
    this.router.navigate(['/accommodations/detail', id]);
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
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