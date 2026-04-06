import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import * as L from 'leaflet';
import { CampingService } from '../../../../../../services/campings/camping.service';

@Component({
  selector: 'app-camping-form',
  templateUrl: './camping-form.component.html',
  styleUrls: ['./camping-form.component.css']
})
export class CampingFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  campingId: number | null = null;
  selectedPhotos: File[] = [];
  previewUrls: string[] = [];
  private map!: L.Map;
  private marker!: L.Marker;
  submitting = false;

  governorates = [
    'Tunis','Ariana','Ben Arous','Manouba','Nabeul','Zaghouan',
    'Bizerte','Béja','Jendouba','Kef','Siliana','Sousse',
    'Monastir','Mahdia','Sfax','Kairouan','Kasserine','Sidi Bouzid',
    'Gabès','Medenine','Tataouine','Gafsa','Tozeur','Kebili'
  ];

  constructor(
    private fb: FormBuilder,
    private campingService: CampingService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
    this.campingId = this.route.snapshot.params['id'] ? +this.route.snapshot.params['id'] : null;
    this.isEdit = !!this.campingId;
    if (this.isEdit) this.loadCamping();
    setTimeout(() => this.initMap(), 100);
  }

  buildForm(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      address: ['', Validators.required],
      governorate: ['', Validators.required],
      latitude: [null, Validators.required],
      longitude: [null, Validators.required],
      maxCapacity: [null, [Validators.required, Validators.min(1)]],
      price: [null, [Validators.required, Validators.min(0)]],
      description: [''],
      rules: [''],
      checkInTime: [''],
      checkOutTime: [''],
      startDate: [''],
      endDate: [''],
      status: ['PENDING'],
      ownerId: [1] // Replace with auth service
    });
  }

  initMap(): void {
    this.map = L.map('picker-map', {
      center: [33.8869, 9.5375],
      zoom: 6
    });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      this.form.patchValue({ latitude: lat.toFixed(6), longitude: lng.toFixed(6) });
      if (this.marker) this.map.removeLayer(this.marker);
      this.marker = L.marker([lat, lng]).addTo(this.map);
    });

    // If editing and coords exist, show marker
    const lat = this.form.get('latitude')?.value;
    const lng = this.form.get('longitude')?.value;
    if (lat && lng) {
      this.marker = L.marker([lat, lng]).addTo(this.map);
      this.map.setView([lat, lng], 10);
    }
  }

  loadCamping(): void {
    this.campingService.getCampingById(this.campingId!).subscribe(data => {
      this.form.patchValue(data);
      if (data.photos) this.previewUrls = data.photos;
      if (data.latitude && data.longitude) {
        if (this.marker) this.map.removeLayer(this.marker);
        this.marker = L.marker([data.latitude, data.longitude]).addTo(this.map);
        this.map.setView([data.latitude, data.longitude], 10);
      }
    });
  }

  onPhotosSelected(event: Event): void {
    const files = (event.target as HTMLInputElement).files;
    if (!files) return;
    this.selectedPhotos = Array.from(files);
    this.previewUrls = this.selectedPhotos.map(f => URL.createObjectURL(f));
  }

  removePhoto(index: number): void {
    this.previewUrls.splice(index, 1);
    this.selectedPhotos.splice(index, 1);
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    const formData = this.campingService.buildFormData(this.form.value, this.selectedPhotos);

    const req = this.isEdit
      ? this.campingService.updateCamping(this.campingId!, formData)
      : this.campingService.createCamping(formData);

    req.subscribe({
      next: () => { this.submitting = false; this.router.navigate(['/camping/backoffice/owner']); },
      error: () => { this.submitting = false; }
    });
  }

  get f() { return this.form.controls; }
}
