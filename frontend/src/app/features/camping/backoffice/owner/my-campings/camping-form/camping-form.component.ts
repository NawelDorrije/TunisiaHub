import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import * as L from 'leaflet';
import { CampingService } from '../../../../../../services/campings/camping.service';

function noWhitespaceOnly(control: AbstractControl): ValidationErrors | null {
  const val = control.value as string;
  if (val && val.trim().length === 0) return { whitespaceOnly: true };
  return null;
}

function checkOutAfterCheckIn(group: AbstractControl): ValidationErrors | null {
  const ci = group.get('checkInTime')?.value as string;
  const co = group.get('checkOutTime')?.value as string;
  if (ci && co && co <= ci) return { checkOutBeforeCheckIn: true };
  return null;
}

function endAfterStart(group: AbstractControl): ValidationErrors | null {
  const s = group.get('startDate')?.value as string;
  const e = group.get('endDate')?.value as string;
  if (s && e && e < s) return { endBeforeStart: true };
  return null;
}

const GOVERNORATE_BOUNDS: Record<string, [number, number, number, number]> = {
  'Tunis':       [36.72, 36.92, 10.09, 10.30],
  'Ariana':      [36.83, 36.99, 10.10, 10.28],
  'Ben Arous':   [36.65, 36.82, 10.17, 10.47],
  'Manouba':     [36.75, 36.90, 9.90,  10.15],
  'Nabeul':      [36.30, 36.88, 10.43, 11.10],
  'Zaghouan':    [36.16, 36.60, 9.80,  10.20],
  'Bizerte':     [37.10, 37.55, 9.45,  10.05],
  'Béja':        [36.60, 37.05, 8.80,  9.40],
  'Jendouba':    [36.45, 37.00, 8.35,  9.00],
  'Kef':         [35.90, 36.65, 8.40,  9.10],
  'Siliana':     [35.85, 36.40, 9.00,  9.70],
  'Sousse':      [35.65, 36.24, 10.30, 10.80],
  'Monastir':    [35.60, 35.90, 10.55, 11.05],
  'Mahdia':      [35.10, 35.65, 10.55, 11.20],
  'Sfax':        [34.40, 35.20, 10.15, 11.00],
  'Kairouan':    [35.30, 35.95, 9.60,  10.25],
  'Kasserine':   [34.85, 35.70, 8.10,  9.00],
  'Sidi Bouzid': [34.60, 35.30, 9.10,  10.00],
  'Gabès':       [33.60, 34.20, 9.50,  10.30],
  'Medenine':    [32.80, 33.65, 9.95,  11.05],
  'Tataouine':   [30.90, 33.10, 8.90,  10.40],
  'Gafsa':       [34.10, 34.80, 7.90,  8.90],
  'Tozeur':      [33.50, 34.10, 7.70,  8.80],
  'Kebili':      [32.50, 34.00, 8.50,  9.70],
};

export interface PostCreationAction {
  label: string;
  icon: string;
  route: string;
  description: string;
}

@Component({
  selector: 'app-camping-form',
  templateUrl: './camping-form.component.html',
  styleUrls: ['./camping-form.component.css']
})
export class CampingFormComponent implements OnInit, OnDestroy {

  form!: FormGroup;
  isEdit = false;
  campingId: number | null = null;
  selectedPhotos: File[] = [];
  previewUrls: string[] = [];
  submitting = false;
  successMsg = '';
  errorMsg   = '';

  // ── THE FIX: these two control the modal ─────────────────────────────────
  showPostModal    = false;
  createdCampingId: number | null = null;
  // ─────────────────────────────────────────────────────────────────────────

  private map!: L.Map;
  private marker!: L.Marker;
  geocoding = false;

  readonly MAX_PHOTOS   = 10;
  readonly MAX_PHOTO_MB = 5;
  photoError = '';

  governorates = Object.keys(GOVERNORATE_BOUNDS);

  postActions: PostCreationAction[] = [
    { label: 'Add Spots',      icon: '⛺', route: 'spots',      description: 'Define individual camping emplacements' },
    { label: 'Add Equipment',  icon: '🎒', route: 'equipment',  description: 'List rental equipment available on site' },
    { label: 'Add Activities', icon: '🏕',  route: 'activities', description: 'Showcase activities guests can enjoy'    },
  ];

  constructor(
    private fb:      FormBuilder,
    private svc:     CampingService,
    private route:   ActivatedRoute,
    private router:  Router,
    private cdr:     ChangeDetectorRef,   // ← KEY
    private zone:    NgZone               // ← KEY
  ) {}

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    this.buildForm();
    this.campingId = this.route.snapshot.params['id'] ? +this.route.snapshot.params['id'] : null;
    this.isEdit    = !!this.campingId;
    if (this.isEdit) this.loadCamping();
    setTimeout(() => this.initMap(), 150);

    this.form.get('governorate')!.valueChanges.subscribe((g: string) => this.zoomToGovernorate(g));
  }

  ngOnDestroy(): void {
    if (this.map) this.map.remove();
  }

  // ── Form ──────────────────────────────────────────────────────────────────
  buildForm(): void {
    this.form = this.fb.group({
      name:          ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100), noWhitespaceOnly]],
      address:       ['', [Validators.required, Validators.minLength(5), Validators.maxLength(255)]],
      governorate:   ['', Validators.required],
      latitude:      [null, [Validators.required, Validators.min(30), Validators.max(38)]],
      longitude:     [null, [Validators.required, Validators.min(7),  Validators.max(12)]],
      numberOfSpots: [null, [Validators.min(1), Validators.max(1000)]],
      maxCapacity:   [null, [Validators.required, Validators.min(1), Validators.max(5000)]],
      price:         [null, [Validators.required, Validators.min(0), Validators.max(10000)]],
      description:   ['', Validators.maxLength(2000)],
      rules:         ['', Validators.maxLength(1000)],
      checkInTime:   [''],
      checkOutTime:  [''],
      startDate:     [''],
      endDate:       [''],
      status:        ['PENDING', Validators.required],
      ownerId:       [2, [Validators.required, Validators.min(1)]],
    }, { validators: [checkOutAfterCheckIn, endAfterStart] });
  }

  // ── Map ───────────────────────────────────────────────────────────────────
  initMap(): void {
    this.map = L.map('picker-map', { center: [33.8869, 9.5375], zoom: 6 });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    // Leaflet events fire OUTSIDE Angular zone — wrap with zone.run()
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.zone.run(() => this.handleMapClick(e));
    });

    const lat = this.form.get('latitude')?.value;
    const lng = this.form.get('longitude')?.value;
    if (lat && lng) { this.placeMarker(lat, lng); this.map.setView([lat, lng], 10); }
  }

  zoomToGovernorate(gov: string): void {
    const b = GOVERNORATE_BOUNDS[gov];
    if (!b || !this.map) return;
    this.map.fitBounds([[b[0], b[2]], [b[1], b[3]]], { padding: [20, 20] });
  }

  handleMapClick(e: L.LeafletMouseEvent): void {
    const { lat, lng } = e.latlng;
    this.placeMarker(lat, lng);
    this.form.patchValue({
      latitude:  parseFloat(lat.toFixed(6)),
      longitude: parseFloat(lng.toFixed(6))
    });
    this.reverseGeocode(lat, lng);
  }

  placeMarker(lat: number, lng: number): void {
    if (this.marker) this.map.removeLayer(this.marker);
    this.marker = L.marker([lat, lng]).addTo(this.map);
  }

  reverseGeocode(lat: number, lng: number): void {
    this.geocoding = true;
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`, {
      headers: { 'Accept-Language': 'fr' }
    })
      .then(r => r.json())
      .then((d: any) => {
        const a = d.address ?? {};
        const parts = [a.road, a.suburb, a.town ?? a.city ?? a.village, a.state].filter(Boolean);
        const address = parts.join(', ') || d.display_name || '';
        // fetch also runs outside zone
        this.zone.run(() => {
          this.form.patchValue({ address });
          this.geocoding = false;
          this.cdr.markForCheck();
        });
      })
      .catch(() => this.zone.run(() => { this.geocoding = false; }));
  }

  // ── Load existing camping (edit mode) ─────────────────────────────────────
  loadCamping(): void {
    this.svc.getCampingById(this.campingId!).subscribe({
      next: d => {
        this.form.patchValue(d);
        if (d.photos?.length) this.previewUrls = [...d.photos];
        if (d.latitude && d.longitude && this.map) {
          this.placeMarker(d.latitude, d.longitude);
          this.map.setView([d.latitude, d.longitude], 10);
        }
      },
      error: () => { this.errorMsg = 'Could not load camping. Please try again.'; }
    });
  }

  // ── Photos ────────────────────────────────────────────────────────────────
  onPhotosSelected(event: Event): void {
    const files = Array.from((event.target as HTMLInputElement).files ?? []);
    this.photoError = '';
    const valid: File[] = [];
    for (const f of files) {
      if (!['image/jpeg','image/jpg','image/png','image/webp'].includes(f.type)) {
        this.photoError = 'Only JPG, PNG, WEBP images accepted.'; continue;
      }
      if (f.size > this.MAX_PHOTO_MB * 1024 * 1024) {
        this.photoError = `Max ${this.MAX_PHOTO_MB} MB per image.`; continue;
      }
      valid.push(f);
    }
    const room = this.MAX_PHOTOS - this.selectedPhotos.length;
    if (valid.length > room) { this.photoError = `Max ${this.MAX_PHOTOS} photos.`; valid.splice(room); }
    valid.forEach(f => { this.selectedPhotos.push(f); this.previewUrls.push(URL.createObjectURL(f)); });
  }

  removePhoto(i: number): void {
    URL.revokeObjectURL(this.previewUrls[i]);
    this.previewUrls.splice(i, 1);
    this.selectedPhotos.splice(i, 1);
  }

  // ── Submit ────────────────────────────────────────────────────────────────
  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); this.errorMsg = 'Please fix the errors below.'; return; }
    this.submitting = true;
    this.successMsg = '';
    this.errorMsg   = '';

    const fd  = this.svc.buildFormData(this.form.value, this.selectedPhotos);
    const req = this.isEdit
      ? this.svc.updateCamping(this.campingId!, fd)
      : this.svc.createCamping(fd);

    req.subscribe({
      next: (result: any) => {
        // ── THE CRITICAL FIX ──────────────────────────────────────────────
        // HttpClient normally runs inside Angular zone, but interceptors or
        // certain RxJS operators can break that. We always wrap with zone.run()
        // to guarantee change detection fires, then call detectChanges().
        this.zone.run(() => {
          this.submitting = false;

          if (this.isEdit) {
            this.successMsg = 'Camping updated successfully!';
            setTimeout(() => this.router.navigate(['/camping/backoffice/owner/my-campings']), 1500);
          } else {
            // Robustly extract the id regardless of backend shape
            this.createdCampingId =
              (result?.id        != null ? +result.id        : null) ??
              (result?.campingId != null ? +result.campingId : null) ??
              (typeof result === 'number' ? result            : null);

            console.log('[CampingForm] server result:', result, '| extracted id:', this.createdCampingId);

            this.showPostModal = true;   // ← triggers *ngIf in template
            this.cdr.detectChanges();    // ← forces view update immediately
          }
        });
        // ─────────────────────────────────────────────────────────────────
      },
      error: (err: any) => {
        this.zone.run(() => {
          this.submitting = false;
          this.errorMsg   = 'An error occurred. Please try again.';
          console.error('[CampingForm] error:', err);
          this.cdr.detectChanges();
        });
      }
    });
  }

  // ── Post-creation modal actions ───────────────────────────────────────────
  goToSection(actionRoute: string): void {
    this.showPostModal = false;
    if (this.createdCampingId) {
      this.router.navigate(['/camping/backoffice/owner', this.createdCampingId, actionRoute]);
    } else {
      // id not captured — still navigate away
      this.router.navigate(['/camping/backoffice/owner/my-campings']);
    }
  }

  skipModal(): void {
    this.showPostModal = false;
    this.router.navigate(['/camping/backoffice/owner/my-campings']);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  get f() { return this.form.controls; }
}
