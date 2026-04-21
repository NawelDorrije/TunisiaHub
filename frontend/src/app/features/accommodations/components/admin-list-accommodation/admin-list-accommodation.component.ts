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

  accommodations: Accommodation[] = [];
  errorMessage: string = '';
  successMessage: string = '';
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
  goToStatistics(): void {
  this.router.navigate(['/accommodations/statistics']);
}

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
  }
}