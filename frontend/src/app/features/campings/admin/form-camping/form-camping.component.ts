import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors
} from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';
import { CampingService } from '../../../../services/campings/camping.service';
import { Camping } from '../../../../models/campings/camping';

@Component({
  selector: 'app-form-camping',
  templateUrl: './form-camping.component.html',
  styleUrls: ['./form-camping.component.css']
})
export class FormCampingComponent implements OnInit {

  campingForm!: FormGroup;

  isEditMode = false;

  campingId?: number;

  // liste des types affichés en minuscule
  campingTypes = ['tent', 'caravan', 'bungalow'];

  constructor(
    private fb: FormBuilder,
    private campingService: CampingService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.initForm();

    const id = this.route.snapshot.params['id'];

    if (id) {

      this.isEditMode = true;

      this.campingId = Number(id);

      this.loadCamping(this.campingId);

    }

  }

  initForm() {

    this.campingForm = this.fb.group({

      name: [
        '',
        [
          Validators.required,
          Validators.minLength(3)
        ]
      ],

      location: [
        '',
        Validators.required
      ],

      campingType: [
        '',
        Validators.required
      ],

      price: [
        1,
        [
          Validators.required,
          Validators.min(1)
        ]
      ],

      description: [
        '',
        [
          Validators.required,
          Validators.minLength(10)
        ]
      ],

      startDate: [
        '',
        Validators.required
      ],

      endDate: [
        '',
        Validators.required
      ],

      photos: [[]],

      spots: [[]]

    },
    {
      validators: this.dateValidator
    });

  }

  // validation End Date >= Start Date

  dateValidator(group: AbstractControl): ValidationErrors | null {

    const start = group.get('startDate')?.value;

    const end = group.get('endDate')?.value;

    if (!start || !end) return null;

    return end < start
      ? { endDateBeforeStart: true }
      : null;

  }

  loadCamping(id: number) {

    this.campingService.getCampingById(id).subscribe({

      next: (camping) => {

        this.campingForm.patchValue({

          ...camping,

          // convertir en minuscule pour l'affichage
          campingType: camping.campingType.toLowerCase()

        });

      },

      error: (err) => {

        console.error(err);

        alert("Failed to load camping");

      }

    });

  }

  onSubmit() {

    if (this.campingForm.invalid) {

      this.campingForm.markAllAsTouched();

      return;

    }

    const formValue = this.campingForm.value;

    const camping: Camping = {

      ...formValue,

      // convertir en MAJUSCULE pour la base
      campingType: formValue.campingType.toUpperCase(),

      id: this.campingId

    };

    if (this.isEditMode) {

      this.updateCamping(camping);

    } else {

      this.createCamping(camping);

    }

  }

  createCamping(camping: Camping) {

    this.campingService.createCamping(camping).subscribe({

      next: () => {

        alert("Camping created successfully");

        this.router.navigate(['/campings/admin']);

      },

      error: (err) => {

        console.error(err);

        alert("Error creating camping");

      }

    });

  }

  updateCamping(camping: Camping) {

    this.campingService.updateCamping(camping).subscribe({

      next: () => {

        alert("Camping updated successfully");

        this.router.navigate(['/campings/admin']);

      },

      error: (err) => {

        console.error(err);

        alert("Error updating camping");

      }

    });

  }

}
