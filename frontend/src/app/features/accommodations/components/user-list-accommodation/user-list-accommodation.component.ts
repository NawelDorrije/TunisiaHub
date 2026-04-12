import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-user-list-accommodation',
  templateUrl: './user-list-accommodation.component.html',
  styleUrls: ['./user-list-accommodation.component.css']
})
export class UserListAccommodationComponent implements OnInit {

  allAccommodations: Accommodation[] = [];
  filteredAccommodations: Accommodation[] = [];
  errorMessage: string = '';
  isLoading: boolean = true;

  // Filters
  searchKeyword: string = '';
  selectedType: string = '';
  maxPrice: number | null = null;
  minCapacity: number | null = null;
  sortOrder: string = '';

  // Available types
  accommodationTypes: string[] = ['Villa', 'Apartment', 'Hostel', 'Hotel', 'Chalet'];

  // Sidebar toggle for mobile
  showFilters: boolean = false;

  constructor(
    private accommodationService: AccommodationService,
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
            next: (acc) => this.recommendations.push(acc),
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
    this.isLoading = true;
    this.accommodationService.getAllAccommodations().subscribe({
      next: (data) => {
        this.allAccommodations = data;
        this.filteredAccommodations = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load accommodations.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
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

    // Filter by type
    if (this.selectedType) {
      result = result.filter(a => a.type === this.selectedType);
    }

    // Filter by max price
    if (this.maxPrice !== null && this.maxPrice > 0) {
      result = result.filter(a => a.price <= this.maxPrice!);
    }

    // Filter by min capacity
    if (this.minCapacity !== null && this.minCapacity > 0) {
      result = result.filter(a => a.capacite >= this.minCapacity!);
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
    this.maxPrice = null;
    this.minCapacity = null;
    this.sortOrder = '';
    this.filteredAccommodations = [...this.allAccommodations];
  }

  get activeFiltersCount(): number {
    let count = 0;
    if (this.searchKeyword.trim()) count++;
    if (this.selectedType) count++;
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
  
}