import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';

@Component({
  selector: 'app-user-list-accommodation',
  templateUrl: './user-list-accommodation.component.html',
  styleUrls: ['./user-list-accommodation.component.css']
})
export class UserListAccommodationComponent implements OnInit {

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
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load accommodations.';
        this.isLoading = false;
      }
    });
  }

  goToDetail(id: number): void {
    this.router.navigate(['/accommodations/detail', id]);
  }
}