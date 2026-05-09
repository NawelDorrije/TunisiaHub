import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
<<<<<<< HEAD
=======
import { PriceRecommendation } from '../../../../models/accommodations/price-recommendation.model';
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-add-accommodation',
  templateUrl: './add-accommodation.component.html',
  styleUrls: ['./add-accommodation.component.css']
})
export class AddAccommodationComponent {

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  accommodationTypes: string[] = ['Villa', 'Apartment', 'Hostel', 'Hotel', 'Chalet'];

  addForm = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
    description: new FormControl('', [Validators.required, Validators.minLength(10)]),
    adresse: new FormControl('', Validators.required),
    type: new FormControl('', Validators.required),
    price: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    capacite: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
<<<<<<< HEAD
    photos: new FormControl('')
=======
    photos: new FormControl(''),
    latitude: new FormControl<number | null>(null),
    longitude: new FormControl<number | null>(null)
>>>>>>> origin/feature/integrated-app-event
  });

  constructor(
    private accommodationService: AccommodationService,
    private router: Router
  ) {}

  get f() {
    return this.addForm.controls;
  }

  onSubmit(): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const photosValue = this.addForm.value.photos;
    const accommodation = {
      ...this.addForm.value,
      photos: photosValue
        ? photosValue.split(',').map((p: string) => p.trim()).filter((p: string) => p !== '')
        : []
    };

    this.accommodationService.addAccommodation(accommodation as any).subscribe({
      next: () => {
        this.successMessage = 'Accommodation added successfully!';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/accommodations/admin']), 1500);
      },
      error: () => {
        this.errorMessage = 'Failed to add accommodation. Please try again.';
        this.isLoading = false;
      }
    });
  }
<<<<<<< HEAD
=======
  onLocationSelected(event: { lat: number; lng: number }): void {
  this.addForm.patchValue({
    latitude: event.lat,
    longitude: event.lng
  });
}
>>>>>>> origin/feature/integrated-app-event

  goBack(): void {
    this.router.navigate(['/accommodations/admin']);
  }
<<<<<<< HEAD
=======
  isPriceLoading = false;
  priceRecommendation: PriceRecommendation | null = null;
  priceError = '';

getSuggestedPrice(): void {
  const type = this.addForm.value.type;
  const adresse = this.addForm.value.adresse;
  const capacite = this.addForm.value.capacite;

  if (!type || !adresse || !capacite) {
    this.priceError = 'Please fill type, address and capacity first.';
    return;
  }
  this.isPriceLoading = true;
  this.priceError = '';
  this.priceRecommendation = null;
  this.accommodationService.suggestPrice(type, adresse, capacite).subscribe({
    next: (data) => {
      this.priceRecommendation = data;
      this.addForm.patchValue({ price: data.recommended });
      this.isPriceLoading = false;
    },
    error: () => {
      this.priceError = 'Failed to get price suggestion.';
      this.isPriceLoading = false;
    }
  });
}
isDescriptionLoading = false;
descriptionError = '';

generateDescription(): void {
  const title = this.addForm.value.title;
  const type = this.addForm.value.type;
  const adresse = this.addForm.value.adresse;
  const capacite = this.addForm.value.capacite;
  const price = this.addForm.value.price;

  if (!title || !type || !adresse || !capacite || !price) {
    this.descriptionError = 'Please fill title, type, address, capacity and price first.';
    return;
  }

  this.isDescriptionLoading = true;
  this.descriptionError = '';

  this.accommodationService.generateDescription(
    title, type, adresse, capacite, price
  ).subscribe({
    next: (data) => {
      this.addForm.patchValue({ description: data.description });
      this.isDescriptionLoading = false;
    },
    error: () => {
      this.descriptionError = 'Failed to generate description.';
      this.isDescriptionLoading = false;
    }
  });
}
>>>>>>> origin/feature/integrated-app-event
}