import { Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { debounceTime, finalize, merge, Subscription } from 'rxjs';
import { EventType } from '../../../../models/events/event.model';
import { EventService } from '../../services/event.service';
import { EventImageAiService, EventImageUploadResponse } from '../../services/event-image-ai.service';
import {
  EventRecommendationResponse,
  RecommendedSlot
} from '../../../../models/events/event-recommendation.model';

@Component({
  selector: 'app-add-event',
  templateUrl: './add-event.component.html',
  styleUrls: ['./add-event.component.css']
})
export class AddEventComponent implements OnInit, OnDestroy {
  private static readonly BACKEND_BASE_URL = 'http://localhost:8089';
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
  private recommendationSub?: Subscription;
  private recommendationFormWatcherSub?: Subscription;

  recommendationLoading = false;
  recommendationError = '';
  recommendationResult: EventRecommendationResponse | null = null;

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

    this.recommendationFormWatcherSub = merge(
      this.addForm.controls.startDate.valueChanges,
      this.addForm.controls.type.valueChanges
    )
      .pipe(debounceTime(350))
      .subscribe(() => {
        this.fetchRecommendations();
      });
  }

  ngOnDestroy(): void {
    this.recommendationSub?.unsubscribe();
    this.recommendationFormWatcherSub?.unsubscribe();
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
      error: (err: HttpErrorResponse) => {
        this.errorMessage = this.extractCreateEventError(err);
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

  applyRecommendedSlot(slot: RecommendedSlot): void {
    const newStart = new Date(`${slot.date}T${slot.time}`);
    const currentStartRaw = this.addForm.controls.startDate.value;
    const currentEndRaw = this.addForm.controls.endDate.value;

    let durationMs = 2 * 60 * 60 * 1000;
    if (currentStartRaw && currentEndRaw) {
      const currentStart = new Date(currentStartRaw);
      const currentEnd = new Date(currentEndRaw);
      if (!Number.isNaN(currentStart.getTime()) && !Number.isNaN(currentEnd.getTime()) && currentEnd > currentStart) {
        durationMs = currentEnd.getTime() - currentStart.getTime();
      }
    }

    const patch: { startDate: string; endDate?: string } = {
      startDate: this.toDateTimeLocalInput(newStart)
    };

    if (currentEndRaw) {
      patch.endDate = this.toDateTimeLocalInput(new Date(newStart.getTime() + durationMs));
    }

    this.addForm.patchValue(patch);
  }

  private fetchRecommendations(): void {
    const startDateValue = this.addForm.controls.startDate.value;
    const type = this.addForm.controls.type.value;

    if (!startDateValue || !type) {
      this.recommendationResult = null;
      this.recommendationError = '';
      this.recommendationLoading = false;
      return;
    }

    const [date, timeRaw] = startDateValue.split('T');
    if (!date || !timeRaw) {
      this.recommendationResult = null;
      this.recommendationError = '';
      this.recommendationLoading = false;
      return;
    }

    const selectedDateTime = new Date(startDateValue);
    if (Number.isNaN(selectedDateTime.getTime()) || selectedDateTime <= new Date()) {
      this.recommendationResult = null;
      this.recommendationLoading = false;
      this.recommendationError = 'Choose a future start date/time to get AI suggestions.';
      return;
    }

    this.recommendationSub?.unsubscribe();
    this.recommendationLoading = true;
    this.recommendationError = '';

    this.recommendationSub = this.eventService.getEventRecommendations({
      date,
      time: timeRaw.slice(0, 5),
      type
    }).subscribe({
      next: (res) => {
        this.recommendationResult = res;
        this.recommendationLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.recommendationLoading = false;
        this.recommendationResult = null;
        this.recommendationError = this.extractRecommendationError(err);
      }
    });
  }

  private extractCreateEventError(err: HttpErrorResponse): string {
    if (err?.status === 403) {
      return 'Acces refuse (403). Connectez-vous avec un compte autorise, puis reessayez.';
    }

    const backendMessage = err?.error?.message || err?.error?.error || err?.message;
    if (typeof backendMessage === 'string' && backendMessage.trim().length > 0) {
      return `Erreur lors de la creation de l evenement: ${backendMessage}`;
    }

    return 'Erreur lors de la creation de l evenement.';
  }

  private extractRecommendationError(err: HttpErrorResponse): string {
    if (err?.status === 403) {
      return 'AI recommendations refusees (403). Reconnectez-vous puis reessayez.';
    }

    const backendMessage = err?.error?.message || err?.error?.error || err?.message;
    if (typeof backendMessage === 'string' && backendMessage.trim().length > 0) {
      return `Unable to load AI recommendations: ${backendMessage}`;
    }

    return 'Unable to load AI recommendations for the selected slot.';
  }

  private toDateTimeLocalInput(date: Date): string {
    const shifted = new Date(date.getTime() - (date.getTimezoneOffset() * 60000));
    return shifted.toISOString().slice(0, 16);
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
          if (error?.status === 0) {
            this.errorMessage = 'Impossible de contacter le backend (status 0). Verifiez que Spring Boot tourne sur http://localhost:8089 et que CORS autorise votre frontend.';
            this.addForm.patchValue({ image: '' });
            return;
          }

          if (error?.status === 403) {
            this.errorMessage = 'Acces refuse (403) pendant upload IA. Verifiez que vous etes connecte en ADMIN et que CORS autorise votre domaine frontend.';
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

    const normalizedImageUrl = this.toAbsoluteImageUrl(response.imageUrl);
    const cleanedDescription = this.cleanAiDescription(response.aiDescription);
    const description = cleanedDescription || previousDescription;

    this.addForm.patchValue({
      image: normalizedImageUrl,
      description
    });

    this.addForm.get('image')?.markAsDirty();
    this.addForm.get('description')?.markAsDirty();
    this.descriptionLength = description.length;
    this.trySwitchPreviewToRemote(normalizedImageUrl);
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

  private trySwitchPreviewToRemote(remoteUrl: string): void {
    const preloader = new Image();
    preloader.onload = () => {
      // Remote image is reachable: release temporary blob and keep final URL.
      this.releasePreviewUrl();
      this.imagePreviewUrl = remoteUrl;
    };
    preloader.onerror = () => {
      // Keep local preview so the image stays visible even if remote URL is temporarily unavailable.
      this.imagePreviewUrl = this.localPreviewObjectUrl || remoteUrl;
    };
    preloader.src = this.withCacheBuster(remoteUrl);
  }

  private toAbsoluteImageUrl(imageUrl: string): string {
    const trimmed = imageUrl.trim();
    if (!trimmed) {
      return '';
    }

    const slashNormalized = trimmed.replace(/\\/g, '/');
    const lower = slashNormalized.toLowerCase();
    const uploadsIdx = lower.indexOf('/uploads/');

    if (uploadsIdx >= 0) {
      return `${AddEventComponent.BACKEND_BASE_URL}${slashNormalized.substring(uploadsIdx)}`;
    }

    if (slashNormalized.startsWith('http://') || slashNormalized.startsWith('https://')) {
      return slashNormalized;
    }

    if (slashNormalized.startsWith('//localhost') || slashNormalized.startsWith('//127.0.0.1')) {
      return `http:${slashNormalized}`;
    }

    if (lower.startsWith('//uploads/')) {
      return `${AddEventComponent.BACKEND_BASE_URL}${slashNormalized.substring(1)}`;
    }

    if (lower.startsWith('uploads/')) {
      return `${AddEventComponent.BACKEND_BASE_URL}/${slashNormalized}`;
    }

    if (slashNormalized.startsWith('/')) {
      return `${AddEventComponent.BACKEND_BASE_URL}${slashNormalized}`;
    }

    return `${AddEventComponent.BACKEND_BASE_URL}/${slashNormalized}`;
  }

  private withCacheBuster(url: string): string {
    const separator = url.includes('?') ? '&' : '?';
    return `${url}${separator}t=${Date.now()}`;
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
