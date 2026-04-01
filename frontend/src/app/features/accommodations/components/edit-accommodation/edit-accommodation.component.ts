import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';

@Component({
  selector: 'app-edit-accommodation',
  templateUrl: './edit-accommodation.component.html',
  styleUrls: ['./edit-accommodation.component.css']
})
export class EditAccommodationComponent implements OnInit {

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  accommodation!: Accommodation;
  accommodationId!: number;

  accommodationTypes: string[] = ['Villa', 'Apartment', 'Hostel', 'Hotel', 'Chalet'];

  editForm = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
    description: new FormControl('', [Validators.required, Validators.minLength(10)]),
    adresse: new FormControl('', Validators.required),
    type: new FormControl('', Validators.required),
    price: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    capacite: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    photos: new FormControl(''),
    latitude: new FormControl<number | null>(null),
    longitude: new FormControl<number | null>(null)
  });

  constructor(
    private accommodationService: AccommodationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.accommodationId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadAccommodation();
  }

  get f() {
    return this.editForm.controls;
  }

  loadAccommodation(): void {
    this.isLoading = true;
    this.accommodationService.getAccommodationById(this.accommodationId).subscribe({
      next: (data) => {
        this.accommodation = data;
        this.editForm.patchValue({
          title: data.title,
          description: data.description,
          adresse: data.adresse,
          type: data.type,
          price: data.price,
          capacite: data.capacite,
          photos: data.photos?.join(', '),
          latitude: data.latitude ?? null,
          longitude: data.longitude ?? null // array → comma separated string
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Accommodation not found.';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const photosValue = this.editForm.value.photos;
    const updated = {
      ...this.editForm.value,
      photos: photosValue
        ? photosValue.split(',').map((p: string) => p.trim()).filter((p: string) => p !== '')
        : []
    };

    this.accommodationService.updateAccommodation(this.accommodationId, updated as any).subscribe({
      next: () => {
        this.successMessage = 'Accommodation updated successfully!';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/accommodations/admin']), 1500);
      },
      error: () => {
        this.errorMessage = 'Failed to update accommodation. Please try again.';
        this.isLoading = false;
      }
    });
  }
  onLocationSelected(event: { lat: number; lng: number }): void {
  this.editForm.patchValue({
    latitude: event.lat,
    longitude: event.lng
  });
}

  goBack(): void {
    this.router.navigate(['/accommodations/admin']);
  }
}