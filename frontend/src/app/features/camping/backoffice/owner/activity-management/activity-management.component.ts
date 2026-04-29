import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Camping } from '../../../../../models/campings/camping';
import { Activity } from '../../../../../models/campings/activity';
import { ActivityService } from '../../../../../services/campings/activity.service';
import { CampingService } from '../../../../../services/campings/camping.service';

@Component({
  selector: 'app-activity-management',
  templateUrl: './activity-management.component.html',
  styleUrls: ['./activity-management.component.css'],
})
export class ActivityManagementComponent implements OnInit, OnDestroy {
  campingId!: number;
  camping: Camping | null = null;
  activities: Activity[] = [];
  loading = true;
  saving  = false;

  /** 'list' = show activity list | 'form' = show create/edit form */
  view: 'list' | 'form' = 'list';

  editTarget: Activity | null = null;
  deleteTarget: Activity | null = null;
  deleting = false;

  form!: FormGroup;
  successMsg: string | null = null;
  errorMsg:   string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private activityService: ActivityService,
    private campingService: CampingService,
  ) {}

  ngOnInit(): void {
    this.campingId = +this.route.snapshot.params['campingId'];
    this.campingService.getCampingById(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: c => (this.camping = c) });
    this.buildForm();
    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      name:            ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description:     ['', Validators.maxLength(500)],
      price:           [null, [Validators.required, Validators.min(0)]],
      duration:        [null, [Validators.min(0.5)]],
      maxParticipants: [null, [Validators.min(1)]],
      active:          [true],
    });
  }

  load(): void {
    this.loading = true;
    this.activityService.getActivitiesByCamping(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: d  => { this.activities = d; this.loading = false; },
        error: () => { this.loading = false; this.errorMsg = 'Failed to load activities.'; },
      });
  }

  openCreate(): void {
    this.editTarget = null;
    this.form.reset({ active: true });
    this.view = 'form';
    this.errorMsg = null;
  }

  openEdit(a: Activity): void {
    this.editTarget = a;
    this.form.reset();
    this.form.patchValue({
      name:            a.name,
      description:     a.description ?? '',
      price:           a.price,
      duration:        a.duration ?? null,
      maxParticipants: (a as any).maxParticipants ?? null,
      active:          a.active ?? true,
    });
    this.view = 'form';
    this.errorMsg = null;
  }

  closeForm(): void {
    this.view = 'list';
    this.editTarget = null;
    this.form.reset({ active: true });
    this.errorMsg = null;
  }

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.saving = true;
    this.errorMsg = null;

    const payload: Activity = {
      ...this.form.value,
      campingId: this.campingId,
    };

    const req$ = this.editTarget
      ? this.activityService.updateActivity(this.editTarget.id!, payload)
      : this.activityService.createActivity(payload);

    req$.pipe(takeUntil(this.destroy$)).subscribe({
      next: (saved) => {
        if (this.editTarget) {
          const i = this.activities.findIndex(a => a.id === saved.id);
          if (i >= 0) this.activities[i] = saved;
        } else {
          this.activities.push(saved);
        }
        this.saving = false;
        this.closeForm();
        this.flash('success', this.editTarget ? 'Activity updated successfully.' : 'Activity created successfully.');
      },
      error: () => {
        this.saving = false;
        this.errorMsg = 'Failed to save activity. Please check all fields and try again.';
      },
    });
  }

  confirmDelete(a: Activity): void { this.deleteTarget = a; }
  cancelDelete(): void             { this.deleteTarget = null; }

  executeDelete(): void {
    if (!this.deleteTarget) return;
    this.deleting = true;
    this.activityService.deleteActivity(this.deleteTarget.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.activities = this.activities.filter(a => a.id !== this.deleteTarget!.id);
          this.deleteTarget = null;
          this.deleting = false;
          this.flash('success', 'Activity deleted successfully.');
        },
        error: () => {
          this.deleting = false;
          this.deleteTarget = null;
          this.flash('error', 'Delete failed. Please try again.');
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/camping/backoffice/owner']);
  }

  /** Convenience accessors */
  f(n: string): AbstractControl { return this.form.get(n)!; }
  invalid(n: string): boolean   { return this.f(n).invalid && (this.f(n).dirty || this.f(n).touched); }

  get activeCount():   number { return this.activities.filter(a => a.active).length; }
  get inactiveCount(): number { return this.activities.filter(a => !a.active).length; }

  get descRemaining(): number {
    const val: string = this.f('description').value ?? '';
    return 500 - val.length;
  }

  private flash(type: 'success' | 'error', msg: string): void {
    if (type === 'success') { this.successMsg = msg; this.errorMsg   = null; }
    else                    { this.errorMsg   = msg; this.successMsg = null; }
    setTimeout(() => { this.successMsg = null; this.errorMsg = null; }, 5000);
  }
}
