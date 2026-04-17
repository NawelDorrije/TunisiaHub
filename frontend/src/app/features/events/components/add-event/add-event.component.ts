import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { EventType } from '../../../../models/events/event.model';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-add-event',
  templateUrl: './add-event.component.html',
  styleUrls: ['./add-event.component.css']
})
export class AddEventComponent implements OnInit {

  map: any;
  marker: any;
  L: any;

  errorMessage = '';
  successMessage = '';
  isLoading = false;

  eventTypes = Object.values(EventType);
  existingTitles: string[] = [];

  constructor(
    private eventService: EventService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // =====================
  ngOnInit(): void {
    this.loadExistingEvents();

    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => this.loadMap(), 0);
    }
  }

  // =====================
  loadExistingEvents() {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {

        this.existingTitles = data.map(e =>
          e.title ? e.title.toLowerCase() : ''
        );

        console.log("Existing titles loaded:", this.existingTitles);

        // 🔥 IMPORTANT FIX
        this.addForm.get('title')?.updateValueAndValidity();
      }
    });
  }

  // =====================
  uniqueTitleValidator = (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;

    return this.existingTitles.includes(control.value.toLowerCase())
      ? { titleExists: true }
      : null;
  };

  // =====================
  dateValidator(group: AbstractControl): ValidationErrors | null {
    const start = group.get('startDate')?.value;
    const end = group.get('endDate')?.value;

    if (!start || !end) return null;

    return new Date(start) < new Date(end)
      ? null
      : { dateInvalid: true };
  }

  // =====================
  addForm = new FormGroup({
    title: new FormControl('', [
      Validators.required,
      Validators.minLength(3),
      this.uniqueTitleValidator.bind(this) // 🔥 IMPORTANT FIX
    ]),
    description: new FormControl('', [Validators.required, Validators.minLength(10)]),
    status: new FormControl('', Validators.required),
    type: new FormControl('', Validators.required),
    price: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    capacity: new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    startDate: new FormControl('', Validators.required),
    endDate: new FormControl('', Validators.required),
    lieu: new FormControl('', Validators.required),
    latitude: new FormControl<number | null>(null),
    longitude: new FormControl<number | null>(null),
    image: new FormControl('', Validators.required)
  }, { validators: this.dateValidator });

  get f() {
    return this.addForm.controls;
  }

  // =====================
  onSubmit() {

    console.log("FORM STATUS:", this.addForm.status);
    console.log("FORM VALUE:", this.addForm.value);

    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      this.errorMessage = "Form is invalid";
      return;
    }

    this.isLoading = true;

    this.eventService.addEvent(this.addForm.value as any).subscribe({
      next: (res) => {

        console.log("EVENT CREATED:", res);

        this.successMessage = 'Event created successfully!';
        this.isLoading = false;

        setTimeout(() => {
          this.router.navigate(['/events']); // ✅ LIST PAGE
        }, 800);
      },
      error: (err) => {
        console.error("ERROR:", err);
        this.errorMessage = 'Error while creating event';
        this.isLoading = false;
      }
    });
  }

  // =====================
  onCancel() {
    console.log("Cancel clicked");
    this.router.navigate(['/events']); // ✅ BACK TO LIST
  }

  // =====================
  async loadMap() {

    this.L = await import('leaflet');

    delete this.L.Icon.Default.prototype._getIconUrl;

    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
      iconUrl: 'assets/leaflet/marker-icon.png',
      shadowUrl: 'assets/leaflet/marker-shadow.png',
    });

    this.map = this.L.map('map').setView([36.8065, 10.1815], 13);

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    this.map.on('click', (e: any) => {
      this.setMarker(e.latlng.lat, e.latlng.lng);
      this.getPlaceName(e.latlng.lat, e.latlng.lng);
    });
  }

  setMarker(lat: number, lng: number) {

    if (this.marker) {
      this.map.removeLayer(this.marker);
    }

    this.marker = this.L.marker([lat, lng]).addTo(this.map);

    this.addForm.patchValue({
      latitude: lat,
      longitude: lng
    });
  }

  searchLocation(query: string) {
    if (!query) return;

    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${query}`)
      .then(res => res.json())
      .then(data => {

        if (data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lon = parseFloat(data[0].lon);

          this.map.setView([lat, lon], 13);
          this.setMarker(lat, lon);

          this.addForm.patchValue({
            lieu: data[0].display_name
          });
        } else {
          this.errorMessage = "Location not found";
        }
      });
  }

  getPlaceName(lat: number, lng: number) {
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`)
      .then(res => res.json())
      .then(data => {
        this.addForm.patchValue({
          lieu: data.display_name
        });
      });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];

    if (file) {
      const reader = new FileReader();

      reader.onload = () => {
        this.addForm.patchValue({
          image: reader.result as string
        });
      };

      reader.readAsDataURL(file);
    }
  }
}