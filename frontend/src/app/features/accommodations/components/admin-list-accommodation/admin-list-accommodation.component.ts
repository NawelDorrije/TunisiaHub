import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';

@Component({
  selector: 'app-admin-list-accommodation',
  templateUrl: './admin-list-accommodation.component.html',
  styleUrls: ['./admin-list-accommodation.component.css']
})
export class AdminListAccommodationComponent implements OnInit {

<<<<<<< HEAD
  accommodations: Accommodation[] = [];
=======
  allAccommodations: Accommodation[] = [];
  filteredAccommodations: Accommodation[] = [];
>>>>>>> origin/feature/integrated-app-event
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = true;

<<<<<<< HEAD
=======
  // Filters
  searchKeyword: string = '';
  selectedType: string = '';
  minPrice: number | null = null;
  maxPrice: number | null = null;
  minCapacity: number | null = null;
  sortOrder: string = '';
  showFilters: boolean = false;
  showDeleteConfirm = false;
  pendingDeleteAccommodationId: number | null = null;

  // Available types
  accommodationTypes: string[] = ['Villa', 'Apartment', 'Hostel', 'Hotel', 'Chalet'];

>>>>>>> origin/feature/integrated-app-event
  constructor(
    private accommodationService: AccommodationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAccommodations();
  }

  loadAccommodations(): void {
<<<<<<< HEAD
    this.isLoading = true;
    this.accommodationService.getAllAccommodations().subscribe({
      next: (data) => {
        this.accommodations = data;
=======
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

  goToAdd(): void {
    this.router.navigate(['/accommodations/add']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/accommodations/edit', id]);
  }

  goToDetail(id: number): void {
    this.router.navigate(['/accommodations/detail', id]);
  }
<<<<<<< HEAD

  deleteAccommodation(id: number): void {
    if (confirm('Are you sure you want to delete this accommodation?')) {
      this.accommodationService.deleteAccommodation(id).subscribe({
        next: () => {
          this.successMessage = 'Accommodation deleted successfully.';
          this.errorMessage = '';
          this.accommodations = this.accommodations.filter(a => a.id !== id);
        },
        error: () => {
          this.errorMessage = 'Failed to delete accommodation.';
          this.successMessage = '';
        }
      });
    }
=======
  goToStatistics(): void {
  this.router.navigate(['/accommodations/statistics']);
}

  applyFilters(): void {
    this.fetchFilteredAccommodations();
  }

  applyClientSideSearchAndSort(): void {
    let result = [...this.allAccommodations];

    if (this.searchKeyword.trim()) {
      const keyword = this.searchKeyword.toLowerCase();
      result = result.filter(a =>
        a.title.toLowerCase().includes(keyword) ||
        a.adresse.toLowerCase().includes(keyword) ||
        a.description?.toLowerCase().includes(keyword)
      );
    }

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

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  deleteAccommodation(id: number): void {
    this.pendingDeleteAccommodationId = id;
    this.showDeleteConfirm = true;
  }

  confirmDeleteAccommodation(): void {
    if (!this.pendingDeleteAccommodationId) return;

    const id = this.pendingDeleteAccommodationId;
    this.accommodationService.deleteAccommodation(id).subscribe({
      next: () => {
        this.successMessage = 'Accommodation deleted successfully.';
        this.errorMessage = '';
        this.allAccommodations = this.allAccommodations.filter(a => a.id !== id);
        this.filteredAccommodations = this.filteredAccommodations.filter(a => a.id !== id);
        this.closeDeleteModal();
      },
      error: () => {
        this.errorMessage = 'Failed to delete accommodation.';
        this.successMessage = '';
        this.closeDeleteModal();
      }
    });
  }

  closeDeleteModal(): void {
    this.showDeleteConfirm = false;
    this.pendingDeleteAccommodationId = null;
>>>>>>> origin/feature/integrated-app-event
  }
}