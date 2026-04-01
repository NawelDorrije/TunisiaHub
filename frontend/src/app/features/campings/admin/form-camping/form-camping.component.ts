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

  BASE_URL = "http://localhost:8089";

  campingTypes = [
    'tent',
    'caravan',
    'bungalow'
  ];

  photosPreview: string[] = [];

  selectedFiles: File[] = [];

  existingPhotos: string[] = [];

  constructor(
    private fb: FormBuilder,
    private campingService: CampingService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.initForm();

    const id =
      this.route.snapshot.params['id'];

    if (id) {

      this.isEditMode = true;

      this.campingId = Number(id);

      this.loadCamping(this.campingId);

    }

  }

initForm() {

  this.campingForm =
    this.fb.group({

      name: [
        '',
        [
          Validators.required,
          Validators.minLength(3)
        ]
      ],

      location: [
        '',
        [
          Validators.required
        ]
      ],

      campingType: [
        '',
        [
          Validators.required
        ]
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
        [
          Validators.required
        ]
      ],

      endDate: [
        '',
        [
          Validators.required
        ]
      ]

    },
    {
      validators: this.dateValidator
    });

}

  dateValidator(
  group: AbstractControl
): ValidationErrors | null {

  const start =
    group.get('startDate')?.value;

  const end =
    group.get('endDate')?.value;

  if (!start || !end)
    return null;

  if (end < start) {

    group
      .get('endDate')
      ?.setErrors({
        endDateBeforeStart: true
      });

    return {
      endDateBeforeStart: true
    };

  }

  return null;

}

  loadCamping(id: number) {

  this.campingService
    .getCampingById(id)
    .subscribe({

      next: camping => {

        console.log("Photos:", camping.photos);

        this.campingForm.patchValue({

          ...camping,

          campingType:
            camping.campingType.toLowerCase()

        });

        this.existingPhotos =
          camping.photos || [];

        this.photosPreview =
          this.existingPhotos.map(
            (p: string) => {

              return "http://localhost:8089/" + p;

            }
          );

      }

    });

}

  onFilesSelected(event: any) {

    const files: FileList =
      event.target.files;

    if (!files)
      return;

    for (let i = 0; i < files.length; i++) {

      const file = files[i];

      this.selectedFiles.push(file);

      const reader = new FileReader();

      reader.onload =
        (e: any) => {

          this.photosPreview.push(
            e.target.result
          );

        };

      reader.readAsDataURL(file);

    }

  }

  removeImage(index: number) {

    this.photosPreview.splice(index, 1);

    if (index < this.selectedFiles.length) {

      this.selectedFiles.splice(index, 1);

    }
    else {

      const existingIndex =
        index - this.selectedFiles.length;

      this.existingPhotos.splice(
        existingIndex,
        1
      );

    }

  }

  onSubmit() {

    if (this.campingForm.invalid) {

      this.campingForm
        .markAllAsTouched();

      return;

    }

    const formValue =
      this.campingForm.value;

    const camping: Camping = {

      ...formValue,

      campingType:
        formValue.campingType.toUpperCase(),

      id: this.campingId,

      photos: this.existingPhotos

    };

    const formData =
      new FormData();

    formData.append(

      "camping",

      new Blob(
        [JSON.stringify(camping)],
        { type: "application/json" }
      )

    );

    this.selectedFiles
      .forEach(file => {

        formData.append(
          "files",
          file
        );

      });

    if (this.isEditMode)

      this.updateCamping(formData);

    else

      this.createCamping(formData);

  }

  createCamping(
    formData: FormData
  ) {

    this.campingService
      .createCamping(formData)
      .subscribe({

        next: () => {

          alert(
            "Camping created successfully"
          );

          this.router.navigate(
            ['/campings/admin']
          );

        }

      });

  }

  updateCamping(
    formData: FormData
  ) {

    this.campingService
      .updateCamping(formData)
      .subscribe({

        next: () => {

          alert(
            "Camping updated successfully"
          );

          this.router.navigate(
            ['/campings/admin']
          );

        }

      });

  }

}
