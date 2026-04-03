import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';

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
    this.accommodationService.getAccommodationById(id).subscribe({
      next: (data) => {
        this.accommodation = data;
        this.selectedPhoto = data.photos?.[0] || 'assets/images/placeholder.jpg';
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Accommodation not found.';
        this.isLoading = false;
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
}