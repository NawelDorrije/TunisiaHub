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

  errorMessage      = '';
  successMessage    = '';
  isLoading         = false;
  isSearching       = false;       // spinner bouton recherche
  descriptionLength = 0;           // compteur caractères description
  selectedLocationName = '';       // badge affiché sur la carte

  eventTypes     = Object.values(EventType);
  existingTitles: string[] = [];
  minDate: string = '';

  constructor(
    private eventService: EventService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
  this.loadExistingEvents();

  // DATE MIN = AUJOURD'HUI
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());

  this.minDate = now.toISOString().slice(0, 16);

  if (isPlatformBrowser(this.platformId)) {
    setTimeout(() => this.loadMap(), 0);
  }
}

  // ── Événements existants ─────────────────────────────────────────────────
  loadExistingEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.existingTitles = data.map(e => e.title ? e.title.toLowerCase() : '');
        this.addForm.get('title')?.updateValueAndValidity();
      }
    });
  }

  // ── Validators ────────────────────────────────────────────────────────────
  uniqueTitleValidator = (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null;
    return this.existingTitles.includes(control.value.toLowerCase())
      ? { titleExists: true }
      : null;
  };

  dateValidator(group: AbstractControl): ValidationErrors | null {
  const start = group.get('startDate')?.value;
  const end   = group.get('endDate')?.value;

  if (!start || !end) return null;

  const now = new Date();

  if (new Date(start) < now) {
    return { startInPast: true };
  }

  if (new Date(start) >= new Date(end)) {
    return { dateInvalid: true };
  }

  return null;
}

  // ── Formulaire ────────────────────────────────────────────────────────────
  addForm = new FormGroup({
    title:       new FormControl('', [
      Validators.required,
      Validators.minLength(3),
      this.uniqueTitleValidator.bind(this)
    ]),
    description: new FormControl('', [Validators.required, Validators.minLength(10)]),
    status:      new FormControl('OPEN', Validators.required),
    type:        new FormControl('', Validators.required),
    price:       new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    capacity:    new FormControl<number | null>(null, [Validators.required, Validators.min(1)]),
    startDate:   new FormControl('', Validators.required),
    endDate:     new FormControl('', Validators.required),
    lieu:        new FormControl('', Validators.required),
    latitude:    new FormControl<number | null>(null),
    longitude:   new FormControl<number | null>(null),
    image:       new FormControl('', Validators.required)
  }, { validators: this.dateValidator });

  get f() { return this.addForm.controls; }

  // ── Submit ────────────────────────────────────────────────────────────────
  onSubmit(): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      this.errorMessage = 'Veuillez corriger les erreurs avant de soumettre.';
      return;
    }
    this.isLoading    = true;
    this.errorMessage = '';

    this.eventService.addEvent(this.addForm.value as any).subscribe({
      next: () => {
        this.successMessage = 'Événement créé avec succès !';
        this.isLoading      = false;
        setTimeout(() => this.router.navigate(['/events']), 800);
      },
      error: () => {
        this.errorMessage = 'Erreur lors de la création de l\'événement.';
        this.isLoading    = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/events']);
  }

  // ── Compteur description ─────────────────────────────────────────────────
  updateCharCount(event: Event): void {
    this.descriptionLength = (event.target as HTMLTextAreaElement).value.length;
  }

  // ── MAP ───────────────────────────────────────────────────────────────────
  async loadMap(): Promise<void> {
    this.L = await import('leaflet');

    // Fix icônes avec Webpack
    delete (this.L.Icon.Default.prototype as any)._getIconUrl;
    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
      iconUrl:       'assets/leaflet/marker-icon.png',
      shadowUrl:     'assets/leaflet/marker-shadow.png',
    });

    this.map = this.L.map('map', { zoomControl: true }).setView([36.8065, 10.1815], 13);

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap'
    }).addTo(this.map);

    // Clic carte → marqueur + géocodage inverse
    this.map.on('click', (e: any) => {
      this.setMarker(e.latlng.lat, e.latlng.lng);
      this.getPlaceName(e.latlng.lat, e.latlng.lng);
    });
  }

  /** Icône personnalisée : point bleu avec halo blanc */
  private customIcon(): any {
    return this.L.divIcon({
      className: '',
      html: `<div style="
        width:18px;height:18px;
        background:#185fa5;
        border:2.5px solid #ffffff;
        border-radius:50%;
        box-shadow:0 2px 8px rgba(24,95,165,.45);
      "></div>`,
      iconSize:   [18, 18],
      iconAnchor: [9, 9],
    });
  }

  setMarker(lat: number, lng: number): void {
    if (this.marker) this.map.removeLayer(this.marker);
    this.marker = this.L.marker([lat, lng], { icon: this.customIcon() }).addTo(this.map);
    this.addForm.patchValue({ latitude: lat, longitude: lng });
  }

  // ── Recherche d'adresse (Nominatim) ──────────────────────────────────────
  searchLocation(query: string): void {
    if (!query.trim()) return;

    this.isSearching  = true;
    this.errorMessage = '';

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`;

    fetch(url, { headers: { 'Accept-Language': 'fr' } })
      .then(res => res.json())
      .then(data => {
        this.isSearching = false;

        if (data && data.length > 0) {
          const lat  = parseFloat(data[0].lat);
          const lon  = parseFloat(data[0].lon);
          const name = data[0].display_name.split(',').slice(0, 3).join(', ');

          this.map.flyTo([lat, lon], 14, { duration: 1 });   // animation fluide
          this.setMarker(lat, lon);
          this.addForm.patchValue({ lieu: name });
          this.selectedLocationName = name;
        } else {
          this.errorMessage = 'Aucun résultat trouvé pour cette adresse.';
        }
      })
      .catch(() => {
        this.isSearching  = false;
        this.errorMessage = 'Erreur lors de la recherche. Veuillez réessayer.';
      });
  }

  // ── Géocodage inverse (clic carte) ───────────────────────────────────────
  getPlaceName(lat: number, lng: number): void {
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`, {
      headers: { 'Accept-Language': 'fr' }
    })
      .then(res => res.json())
      .then(data => {
        const name = data.display_name
          ? data.display_name.split(',').slice(0, 3).join(', ')
          : `${lat.toFixed(5)}, ${lng.toFixed(5)}`;

        this.addForm.patchValue({ lieu: name });
        this.selectedLocationName = name;
      })
      .catch(() => {
        const fallback = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
        this.addForm.patchValue({ lieu: fallback });
        this.selectedLocationName = fallback;
      });
  }

  // ── Upload image ──────────────────────────────────────────────────────────
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'L\'image ne doit pas dépasser 5 Mo.';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.addForm.patchValue({ image: reader.result as string });
    };
    reader.readAsDataURL(file);
  }
}