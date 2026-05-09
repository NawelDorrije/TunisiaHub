import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../../../models/accommodations/accommodation.model';
<<<<<<< HEAD
=======
import { PriceRecommendation } from '../../../../models/accommodations/price-recommendation.model';
>>>>>>> origin/feature/integrated-app-event

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
<<<<<<< HEAD
          photos: data.photos?.join(', ') // array → comma separated string
=======
          photos: data.photos?.join(', '),
          latitude: data.latitude ?? null,
          longitude: data.longitude ?? null // array → comma separated string
>>>>>>> origin/feature/integrated-app-event
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
<<<<<<< HEAD
=======
  onLocationSelected(event: { lat: number; lng: number }): void {
  this.editForm.patchValue({
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
  const type = this.editForm.value.type;
  const adresse = this.editForm.value.adresse;
  const capacite = this.editForm.value.capacite;

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
      this.editForm.patchValue({ price: data.recommended });
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
  const title = this.editForm.value.title;
  const type = this.editForm.value.type;
  const adresse = this.editForm.value.adresse;
  const capacite = this.editForm.value.capacite;
  const price = this.editForm.value.price;

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
      this.editForm.patchValue({ description: data.description });
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