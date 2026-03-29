import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AccommodationService } from '../../services/accommodation.service';

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
    photos: new FormControl('')
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

  goBack(): void {
    this.router.navigate(['/accommodations/admin']);
  }
}