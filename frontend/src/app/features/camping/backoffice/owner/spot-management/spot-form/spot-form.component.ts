import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SpotService } from '../../../../../../services/campings/spot.service';
import { DynamicPricingService, PricingResponse } from '../../../../../../services/campings/dynamic-pricing.service';

declare const L: any; // Leaflet

@Component({
  selector: 'app-spot-form',
  templateUrl: './spot-form.component.html',
  styleUrls: ['./spot-form.component.css'],
})
export class SpotFormComponent implements OnInit, AfterViewInit, OnDestroy {

  form!: FormGroup;
  campingId!: number;
  isEditMode = false;
  spotId?: number;
  isSubmitting = false;
  errorMessage = '';
  selectedPhotos: File[] = [];
  previewUrls: string[] = [];
  showPricingPopup = false;
pricingData: PricingResponse | null = null;

  // Map state
  private map: any;
  private marker: any;
  mapReady = false;

  private destroy$ = new Subject<void>();

  spotTypes = [
    { value: 'TENT',        label: 'Tent',        icon: '⛺' },
    { value: 'CARAVAN',     label: 'Caravan',     icon: '🚐' },
    { value: 'BUNGALOW',    label: 'Bungalow',    icon: '🏠' },
    { value: 'TREEHOUSE',   label: 'Treehouse',   icon: '🌲' },
    { value: 'GLAMPING',    label: 'Glamping',    icon: '✨' },
    { value: 'MOBILE_HOME', label: 'Mobile Home', icon: '🏡' },
  ];
  statusOptions = [
    { value: 'LIBRE',       label: 'Available' },
    { value: 'OCCUPE',      label: 'Occupied' },
    { value: 'MAINTENANCE', label: 'Maintenance' },
    { value: 'HORS_SERVICE',label: 'Out of service' },
  ];
  viewTypes = [
    { value: 'SEA',      label: 'Sea view' },
    { value: 'LAKE',     label: 'Lake view' },
    { value: 'MOUNTAIN', label: 'Mountain view' },
    { value: 'FOREST',   label: 'Forest view' },
    { value: 'STANDARD', label: 'Standard' },
  ];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private spotService: SpotService,
    private pricingService: DynamicPricingService,

  ) {}

  ngOnInit(): void {
    this.campingId = Number(this.route.snapshot.paramMap.get('campingId'));
    this.spotId    = Number(this.route.snapshot.paramMap.get('spotId')) || undefined;
    this.isEditMode = !!this.spotId;

    this.buildForm();

    if (this.isEditMode && this.spotId) {
      this.spotService.getSpotById(this.spotId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: spot => {
            this.form.patchValue(spot);
            if (spot.photos?.length) {
              this.previewUrls = [...spot.photos];
            }
            // Place marker if coords exist
            if (spot.positionX != null && spot.positionY != null) {
              this.placeMarker(Number(spot.positionX), Number(spot.positionY));
            }
          },
          error: () => this.errorMessage = 'Failed to load spot data.'
        });
    }
  }

  ngAfterViewInit(): void {
    this.loadLeaflet();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.map) this.map.remove();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      campingId:             [this.campingId, Validators.required],
      name:                  ['', [Validators.required, Validators.maxLength(50)]],
      type:                  [null, Validators.required],
      capacity:              [null, [Validators.required, Validators.min(1)]],
      area:                  [null, Validators.min(0.01)],
      description:           [''],
      basePrice:             [null, [Validators.required, Validators.min(0.01)]],
      status:                ['LIBRE', Validators.required],
      positionX:             [null],
      positionY:             [null],
      viewType:              [null],
      hasShade:              [false],
      accessibleForDisabled: [false],
      active:                [true],
    });
  }

  get f(): { [key: string]: AbstractControl } {
    return this.form.controls;
  }

  // ── Map ───────────────────────────────────────────────

  private loadLeaflet(): void {
    // Dynamically load Leaflet CSS + JS if not already present
    if (!(window as any).L) {
      const css = document.createElement('link');
      css.rel = 'stylesheet';
      css.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(css);

      const script = document.createElement('script');
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      script.onload = () => this.initMap();
      document.head.appendChild(script);
    } else {
      this.initMap();
    }
  }

  private initMap(): void {
    const L = (window as any).L;

    this.map = L.map('tunisia-map', {
      center: [33.8869, 9.5375], // Center of Tunisia
      zoom: 6,
      zoomControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 18,
    }).addTo(this.map);

    // Click handler
    this.map.on('click', (e: any) => {
      const lat = parseFloat(e.latlng.lat.toFixed(4));
      const lng = parseFloat(e.latlng.lng.toFixed(4));
      this.form.patchValue({ positionX: lat, positionY: lng });
      this.placeMarker(lat, lng);
    });

    this.mapReady = true;

    // If edit mode coords already patched before map ready
    const x = this.form.value.positionX;
    const y = this.form.value.positionY;
    if (x && y) this.placeMarker(x, y);
  }

  private placeMarker(lat: number, lng: number): void {
    if (!this.map) return;
    const L = (window as any).L;

    const icon = L.divIcon({
      className: '',
      html: `<div style="
        width:28px;height:28px;background:#2d5016;border:3px solid #fff;
        border-radius:50% 50% 50% 0;transform:rotate(-45deg);
        box-shadow:0 2px 8px rgba(0,0,0,.35);
      "></div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 28],
    });

    if (this.marker) this.marker.remove();
    this.marker = L.marker([lat, lng], { icon }).addTo(this.map);
    this.map.setView([lat, lng], Math.max(this.map.getZoom(), 9));
  }

  clearLocation(): void {
    this.form.patchValue({ positionX: null, positionY: null });
    if (this.marker) { this.marker.remove(); this.marker = null; }
  }

  // ── Photos ────────────────────────────────────────────

  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;
    const files = Array.from(input.files);
    this.selectedPhotos.push(...files);
    files.forEach(file => {
      const reader = new FileReader();
      reader.onload = e => this.previewUrls.push(e.target?.result as string);
      reader.readAsDataURL(file);
    });
    input.value = '';
  }

  removePhoto(index: number): void {
    this.previewUrls.splice(index, 1);
    this.selectedPhotos.splice(index, 1);
  }

  // ── Submit ────────────────────────────────────────────

   onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = '';

    const formData = this.spotService.buildFormData(this.form.value, this.selectedPhotos);

    const request$ = this.isEditMode && this.spotId
      ? this.spotService.updateSpot(this.spotId, formData)
      : this.spotService.createSpot(formData);

    request$.pipe(takeUntil(this.destroy$)).subscribe({
      next: (spot) => {
        this.isSubmitting = false;

        // Safely extract the ID (handle both 'id' and possible 'spotId')
        const newSpotId = spot?.id || spot?.id;

        if (!this.isEditMode && newSpotId) {
          const checkIn = new Date().toISOString().split('T')[0];

          this.pricingService.getEffectivePrice(newSpotId, checkIn)
            .pipe(takeUntil(this.destroy$))
            .subscribe({
              next: (pricing) => {
                this.pricingData = pricing;
                this.showPricingPopup = true;
              },
              error: () => {
                // Still show popup even if pricing fails
                this.showPricingPopup = true;
              }
            });
        } else {
          // For edit mode or if ID is missing, redirect directly
          this.router.navigate(['/camping/backoffice/owner/', this.campingId, 'spots']);
        }
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'An error occurred. Please try again.';
      }
    });
  }

closePricingPopup(): void {
  this.showPricingPopup = false;
  this.router.navigate(['/camping/backoffice/owner/', this.campingId, 'spots']);
}

getPriceChangePercent(): number {
  if (!this.pricingData) return 0;
  return this.pricingService.getPriceChangePercent(this.pricingData.basePrice, this.pricingData.dynamicPrice);
}

getPriceLevel() {
  if (!this.pricingData) return null;
  return this.pricingService.getPriceLevel(this.pricingData.multiplier);
}
  goBack(): void {
    this.router.navigate(['/camping/backoffice/owner/', this.campingId, 'spots']);
  }
  goToSpots(): void {
  this.router.navigate([
    '/camping/backoffice/owner/',
    this.campingId,
    'spots'
  ]);
}
}
