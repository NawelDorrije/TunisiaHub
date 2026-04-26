import { Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { EventType } from '../../../../models/events/event.model';
import { EventService } from '../../services/event.service';
import { EventImageAiService, EventImageUploadResponse } from '../../services/event-image-ai.service';

@Component({
  selector: 'app-add-event',
  templateUrl: './add-event.component.html',
  styleUrls: ['./add-event.component.css']
})
export class AddEventComponent implements OnInit, OnDestroy {
  map: any;
  marker: any;
  L: any;

  errorMessage = '';
  successMessage = '';
  isLoading = false;
  isSearching = false;
  isGeneratingDescription = false;
  descriptionLength = 0;
  selectedLocationName = '';
  imagePreviewUrl: string | null = null;
  private localPreviewObjectUrl: string | null = null;

  eventTypes = Object.values(EventType);
  existingTitles: string[] = [];
  minDate = '';

  addForm = new FormGroup({
    title: new FormControl('', [
      Validators.required,
      Validators.minLength(3),
      this.uniqueTitleValidator.bind(this)
    ]),
    description: new FormControl('', [Validators.required, Validators.minLength(10)]),
    status: new FormControl('OPEN', Validators.required),
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

  constructor(
    private eventService: EventService,
    private eventImageAiService: EventImageAiService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  get f() {
    return this.addForm.controls;
  }

  ngOnInit(): void {
    this.loadExistingEvents();
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    this.minDate = now.toISOString().slice(0, 16);

    if (isPlatformBrowser(this.platformId)) {
      setTimeout(() => this.loadMap(), 0);
    }
  }

  ngOnDestroy(): void {
    this.releasePreviewUrl();
  }

  loadExistingEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.existingTitles = data.map((e) => e.title ? e.title.toLowerCase() : '');
        this.addForm.get('title')?.updateValueAndValidity();
      }
    });
  }

  uniqueTitleValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) {
      return null;
    }
    return this.existingTitles.includes(control.value.toLowerCase()) ? { titleExists: true } : null;
  }

  dateValidator(group: AbstractControl): ValidationErrors | null {
    const start = group.get('startDate')?.value;
    const end = group.get('endDate')?.value;
    if (!start || !end) {
      return null;
    }
    const now = new Date();
    if (new Date(start) < now) {
      return { startInPast: true };
    }
    if (new Date(start) >= new Date(end)) {
      return { dateInvalid: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      this.errorMessage = 'Veuillez corriger les erreurs avant de soumettre.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.eventService.addEvent(this.addForm.value as any).subscribe({
      next: () => {
        this.successMessage = 'Evenement cree avec succes.';
        this.isLoading = false;
        setTimeout(() => this.router.navigate(['/events']), 800);
      },
      error: () => {
        this.errorMessage = 'Erreur lors de la creation de l evenement.';
        this.isLoading = false;
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/events']);
  }

  updateCharCount(event: Event): void {
    this.descriptionLength = (event.target as HTMLTextAreaElement).value.length;
  }

  async loadMap(): Promise<void> {
    this.L = await import('leaflet');

    delete (this.L.Icon.Default.prototype as any)._getIconUrl;
    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
      iconUrl: 'assets/leaflet/marker-icon.png',
      shadowUrl: 'assets/leaflet/marker-shadow.png'
    });

    this.map = this.L.map('map', { zoomControl: true }).setView([36.8065, 10.1815], 13);
    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: 'OpenStreetMap'
    }).addTo(this.map);

    this.map.on('click', (e: any) => {
      this.setMarker(e.latlng.lat, e.latlng.lng);
      this.getPlaceName(e.latlng.lat, e.latlng.lng);
    });
  }

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
      iconSize: [18, 18],
      iconAnchor: [9, 9]
    });
  }

  setMarker(lat: number, lng: number): void {
    if (this.marker) {
      this.map.removeLayer(this.marker);
    }
    this.marker = this.L.marker([lat, lng], { icon: this.customIcon() }).addTo(this.map);
    this.addForm.patchValue({ latitude: lat, longitude: lng });
  }

  searchLocation(query: string): void {
    if (!query.trim()) {
      return;
    }

    this.isSearching = true;
    this.errorMessage = '';
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1`;

    fetch(url, { headers: { 'Accept-Language': 'fr' } })
      .then((res) => res.json())
      .then((data) => {
        this.isSearching = false;
        if (data && data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lon = parseFloat(data[0].lon);
          const name = data[0].display_name.split(',').slice(0, 3).join(', ');
          this.map.flyTo([lat, lon], 14, { duration: 1 });
          this.setMarker(lat, lon);
          this.addForm.patchValue({ lieu: name });
          this.selectedLocationName = name;
        } else {
          this.errorMessage = 'Aucun resultat trouve pour cette adresse.';
        }
      })
      .catch(() => {
        this.isSearching = false;
        this.errorMessage = 'Erreur lors de la recherche.';
      });
  }

  getPlaceName(lat: number, lng: number): void {
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`, {
      headers: { 'Accept-Language': 'fr' }
    })
      .then((res) => res.json())
      .then((data) => {
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

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Veuillez selectionner une image valide.';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'L image ne doit pas depasser 5 Mo.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    const previousDescription = this.addForm.controls.description.value ?? '';
    this.setLocalPreviewUrl(file);
    this.isGeneratingDescription = true;

    this.eventImageAiService.uploadImageAndGenerateDescription(file)
      .pipe(finalize(() => {
        this.isGeneratingDescription = false;
      }))
      .subscribe({
        next: (response) => {
          this.applyGeneratedContent(response, previousDescription);
        },
        error: (error) => {
          if (error?.status === 403) {
            this.errorMessage = 'Acces refuse (403) pendant upload IA. Verifiez CORS/backend puis reconnectez-vous.';
            this.addForm.patchValue({ image: '' });
            return;
          }

          const backendMessage = error?.error?.message || error?.message;
          this.errorMessage = backendMessage
            ? `Erreur upload image ou generation IA: ${backendMessage}`
            : 'Erreur upload image ou generation IA.';
          this.addForm.patchValue({ image: '' });
        }
      });
  }

  private applyGeneratedContent(response: EventImageUploadResponse, previousDescription: string): void {
    if (!response.imageUrl) {
      this.errorMessage = 'Image non generee correctement. Reessayez avec une autre image.';
      this.addForm.patchValue({ image: '' });
      return;
    }

    const cleanedDescription = this.cleanAiDescription(response.aiDescription);
    const description = cleanedDescription || previousDescription;

    this.addForm.patchValue({
      image: response.imageUrl,
      description
    });

    this.addForm.get('image')?.markAsDirty();
    this.addForm.get('description')?.markAsDirty();
    this.descriptionLength = description.length;
    this.imagePreviewUrl = response.imageUrl;
  }

  private cleanAiDescription(description: string | null | undefined): string {
    if (!description) {
      return '';
    }

    return description
      .replace(/\*\*/g, '')
      .replace(/\s+/g, ' ')
      .trim();
  }

  private setLocalPreviewUrl(file: File): void {
    this.releasePreviewUrl();
    this.localPreviewObjectUrl = URL.createObjectURL(file);
    this.imagePreviewUrl = this.localPreviewObjectUrl;
  }

  private releasePreviewUrl(): void {
    if (!this.localPreviewObjectUrl) {
      return;
    }
    URL.revokeObjectURL(this.localPreviewObjectUrl);
    this.localPreviewObjectUrl = null;
    this.imagePreviewUrl = null;
  }
}
