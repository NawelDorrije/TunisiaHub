import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EquipmentService } from '../../../../../../services/campings/equipment.service';
import { SpotService } from '../../../../../../services/campings/spot.service';
import { Spot } from '../../../../../../models/campings/spot';

@Component({
  selector: 'app-equipmennt-form',
  templateUrl: './equipmennt-form.component.html',
  styleUrls: ['./equipmennt-form.component.css']
})
export class EquipmenntFormComponent implements OnInit, OnDestroy {

  form!: FormGroup;
  campingId!: number;
  isEditMode = false;
  equipmentId?: number;
  isSubmitting = false;
  errorMessage = '';

  spots: Spot[] = [];   // List of spots to assign equipment

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private equipmentService: EquipmentService,
    private spotService: SpotService
  ) {}

  ngOnInit(): void {
    this.campingId = Number(this.route.snapshot.paramMap.get('campingId'));
    this.equipmentId = Number(this.route.snapshot.paramMap.get('equipmentId')) || undefined;
    this.isEditMode = !!this.equipmentId;

    this.buildForm();
    this.loadSpots();

    if (this.isEditMode && this.equipmentId) {
      this.loadEquipmentData();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      name:        ['', [Validators.required, Validators.maxLength(100)]],
      description: [''],
      quantity:    [1, [Validators.required, Validators.min(1)]],
      available:   [true],
      condition:   ['GOOD', Validators.required],
      spotId:      [null, Validators.required],
      campingId:   [this.campingId]   // hidden
    });
  }

  get f(): { [key: string]: AbstractControl } {
    return this.form.controls;
  }

  private loadSpots(): void {
    this.spotService.getSpotsByCamping(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => this.spots = data,
        error: () => this.errorMessage = 'Failed to load spots list.'
      });
  }

  private loadEquipmentData(): void {
    this.equipmentService.getEquipmentById(this.equipmentId!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (eq) => {
          this.form.patchValue({
            name: eq.name,
            description: eq.description || '',
            quantity: eq.quantity,
            available: eq.available,
            condition: eq.condition,
            spotId: eq.spotId
          });
        },
        error: () => this.errorMessage = 'Failed to load equipment data.'
      });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const payload = this.form.value;

    const request$ = this.isEditMode && this.equipmentId
      ? this.equipmentService.updateEquipment(this.equipmentId, payload)
      : this.equipmentService.createEquipment(payload);

    request$.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.router.navigate(['/camping/backoffice/owner/', this.campingId, 'equipment']);
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'An error occurred while saving. Please try again.';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/camping/backoffice/owner/', this.campingId, 'equipment']);
  }
}
