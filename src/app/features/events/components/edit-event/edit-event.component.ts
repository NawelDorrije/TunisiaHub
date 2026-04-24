import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event, EventType } from '../../../../models/events/event.model';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-edit-event',
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.css']
})
export class EditEventComponent implements OnInit {

  eventId!: number;
  eventTypes = Object.values(EventType);

  successMessage = '';
  errorMessage = '';

  map: any;
  marker: any;
  L: any;

  editForm = new FormGroup({
    id: new FormControl(),
    title: new FormControl('', Validators.required),
    description: new FormControl('', Validators.required),
    startDate: new FormControl('', Validators.required),
    endDate: new FormControl('', Validators.required),
    price: new FormControl(0, Validators.required),
    capacity: new FormControl(0, Validators.required),
    status: new FormControl('', Validators.required),
    type: new FormControl('', Validators.required),
    image: new FormControl(''),
    latitude: new FormControl(0),
    longitude: new FormControl(0),
    lieu: new FormControl('')
  });

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // =========================
  // INIT
  // =========================
  ngOnInit(): void {
    this.eventId = this.route.snapshot.params['id'];

    this.loadEvent();

    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        this.loadMap();
      }, 0);
    }
  }

  // =========================
  // LOAD EVENT DATA
  // =========================
  loadEvent() {
    this.eventService.getEventById(this.eventId).subscribe({
      next: (data) => {
        this.editForm.patchValue(data);

        // attendre map puis placer marker
        setTimeout(() => {
          if (this.map && data.latitude && data.longitude) {
            this.map.setView([data.latitude, data.longitude], 13);
            this.setMarker(data.latitude, data.longitude);
          }
        }, 500);
      },
      error: () => {
        this.errorMessage = 'Error loading event';
      }
    });
  }

  // =========================
  // LOAD MAP
  // =========================
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
      const lat = e.latlng.lat;
      const lng = e.latlng.lng;

      this.setMarker(lat, lng);
      this.getPlaceName(lat, lng);
    });
  }

  // =========================
  // MARKER
  // =========================
  setMarker(lat: number, lng: number) {

    if (this.marker) {
      this.map.removeLayer(this.marker);
    }

    this.marker = this.L.marker([lat, lng]).addTo(this.map);

    this.editForm.patchValue({
      latitude: lat,
      longitude: lng
    });
  }

  // =========================
  // REVERSE GEOCODING
  // =========================
  async getPlaceName(lat: number, lng: number) {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`
    );

    const data = await res.json();

    this.editForm.patchValue({
      lieu: data.display_name
    });
  }

  // =========================
  // IMAGE UPDATE
  // =========================
  onFileSelected(event: any) {
    const file = event.target.files[0];

    if (file) {
      const reader = new FileReader();

      reader.onload = () => {
        this.editForm.patchValue({
          image: reader.result as string
        });
      };

      reader.readAsDataURL(file);
    }
  }

  // =========================
// SEARCH LOCATION
// =========================
async searchLocation(query: string) {
  if (!query) return;

  const res = await fetch(
    `https://nominatim.openstreetmap.org/search?format=json&q=${query}`
  );

  const data = await res.json();

  if (data.length > 0) {
    const lat = parseFloat(data[0].lat);
    const lon = parseFloat(data[0].lon);

    this.map.setView([lat, lon], 13);
    this.setMarker(lat, lon);

    this.editForm.patchValue({
      lieu: data[0].display_name
    });
  } else {
    alert("Location not found");
  }
}
  // =========================
  // SUBMIT
  // =========================
  onSubmit() {
    this.eventService.updateEvent(this.editForm.value as Event).subscribe({
      next: () => {
        this.successMessage = 'Event updated successfully!';
        setTimeout(() => this.router.navigate(['/events']), 1500);
      },
      error: () => {
        this.errorMessage = 'Error updating event';
      }
    });
  }
  
  // =========================
// CANCEL
// =========================
onCancel() {
  this.router.navigate(['/events']);
}
}
