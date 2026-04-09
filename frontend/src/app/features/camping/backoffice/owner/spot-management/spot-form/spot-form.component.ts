import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SpotService } from '../../../../../../services/campings/spot.service';
import { Spot } from '../../../../../../models/campings/spot';

export type SpotType = 'TENT' | 'BUNGALOW' | 'CARAVAN' | 'GLAMPING' | 'CHALET';

@Component({
  selector: 'app-spot-form',
  templateUrl: './spot-form.component.html',
  styleUrls: ['./spot-form.component.css'],
})
export class SpotFormComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  campingId!: number;
  spotId: number | null = null;
  isEdit = false;
  loading = false;
  saving  = false;
  successMsg: string | null = null;
  errorMsg:   string | null = null;

  spotTypes: SpotType[] = ['TENT','BUNGALOW','CARAVAN','GLAMPING','CHALET'];
  viewTypes = ['Sea','Mountain','Forest','Desert','Countryside','Lake'];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private spotService: SpotService,
  ) {}

  ngOnInit(): void {
    this.campingId = +this.route.snapshot.params['campingId'];
    const sid = this.route.snapshot.params['spotId'];
    if (sid) { this.isEdit = true; this.spotId = +sid; }
    this.buildForm();
    if (this.isEdit) this.loadSpot();
  }
  ngOnDestroy(): void { this.destroy$.next(); this.destroy$.complete(); }

  private buildForm(): void {
    this.form = this.fb.group({
      name:                ['', [Validators.required, Validators.minLength(2)]],
      type:                ['TENT', Validators.required],
      description:         [''],
      basePrice:           [null, [Validators.required, Validators.min(0)]],
      capacity:            [null, [Validators.required, Validators.min(1)]],
      viewType:            [''],
      hasShade:            [false],
      hasElectricity:      [false],
      hasWater:            [false],
      accessibleForDisabled:[false],
      active:              [true],
    });
  }

  private loadSpot(): void {
    this.loading = true;
    this.spotService.getSpotById(this.spotId!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: s => { this.form.patchValue(s); this.loading = false; },
        error: () => { this.loading = false; this.errorMsg = 'Failed to load spot.'; },
      });
  }

submit(): void {
  this.form.markAllAsTouched();
  if (this.form.invalid) return;

  this.saving = true;
  this.errorMsg = null;
  this.successMsg = null;

  // 1. Create FormData
  const formData = new FormData();

  // 2. Append all form values (except files for now)
  const formValue = this.form.value;

  Object.keys(formValue).forEach(key => {
    const value = formValue[key];

    if (value !== null && value !== undefined) {
      if (key === 'basePrice' || key === 'capacity') {
        formData.append(key, value.toString());   // numbers as string
      } else if (typeof value === 'boolean') {
        formData.append(key, value.toString());
      } else {
        formData.append(key, value);
      }
    }
  });

  // 3. Add campingId (important!)
  formData.append('campingId', this.campingId.toString());

  // TODO: If you have file upload (image), append it here, e.g.:
  // if (this.selectedFile) {
  //   formData.append('image', this.selectedFile, this.selectedFile.name);
  // }

  const req$ = this.isEdit
    ? this.spotService.updateSpot(this.spotId!, formData)
    : this.spotService.createSpot(formData);

  req$.pipe(takeUntil(this.destroy$)).subscribe({
    next: () => {
      this.saving = false;
      this.successMsg = this.isEdit ? 'Spot updated successfully!' : 'Spot created successfully!';
      setTimeout(() => {
        this.router.navigate(['/owner', this.campingId, 'spots']);
      }, 1500);
    },
    error: (err) => {
      this.saving = false;
      this.errorMsg = err?.error?.message || 'Failed to save the spot.';
    },
  });
}
  f(n: string): AbstractControl { return this.form.get(n)!; }
  invalid(n: string): boolean { return this.f(n).invalid && this.f(n).touched; }
}
