import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import * as L from 'leaflet';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Camping } from '../../../../models/campings/camping';
import { CampingService } from '../../../../services/campings/camping.service';

@Component({
  selector: 'app-camping-map',
  templateUrl: './camping-map.component.html',
  styleUrls: ['./camping-map.component.css']
})
export class CampingMapComponent implements OnInit, AfterViewInit, OnDestroy {
  private map!: L.Map;
  campings: Camping[] = [];
  filteredCampings: Camping[] = [];
  selectedCamping: Camping | null = null;
  loading = true;
  error = '';
  searchKeyword = '';
  private markers: L.Marker[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    private campingService: CampingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCampings();
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  private initMap(): void {
    if (this.map) return;
    this.map = L.map('camping-map', {
      center: [33.8869, 9.5375],
      zoom: 7,
      zoomControl: false,
      attributionControl: true
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(this.map);

    L.control.zoom({ position: 'bottomright' }).addTo(this.map);

    // Close panel on map click
    this.map.on('click', () => { this.selectedCamping = null; });
  }

  private loadCampings(): void {
    this.loading = true;
    this.campingService.getAllCampings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.campings = data.filter(c => c.status === 'ACTIVE' && c.latitude && c.longitude);
          this.filteredCampings = [...this.campings];
          this.loading = false;
          // Wait for map init
          setTimeout(() => this.renderMarkers(), 100);
        },
        error: () => {
          this.error = 'Could not load campings.';
          this.loading = false;
        }
      });
  }

  private renderMarkers(): void {
    if (!this.map) return;
    // Clear existing
    this.markers.forEach(m => this.map.removeLayer(m));
    this.markers = [];

    this.filteredCampings.forEach(camping => {
      const icon = L.divIcon({
        className: '',
        html: `<div class="map-marker" title="${camping.name}">
                 <div class="marker-pin">🏕</div>
                 <div class="marker-label">${camping.name}</div>
               </div>`,
        iconSize: [40, 52],
        iconAnchor: [20, 52],
        popupAnchor: [0, -52]
      });

      const marker = L.marker([camping.latitude, camping.longitude], { icon })
        .addTo(this.map);

      marker.on('click', (e) => {
        L.DomEvent.stopPropagation(e);
        this.selectedCamping = camping;
        this.map.panTo([camping.latitude, camping.longitude]);
      });

      this.markers.push(marker);
    });

    // Fit bounds if multiple markers
    if (this.filteredCampings.length > 1) {
      const group = L.featureGroup(this.markers);
      this.map.fitBounds(group.getBounds().pad(0.1));
    }
  }

  filterCampings(): void {
    const kw = this.searchKeyword.toLowerCase();
    this.filteredCampings = this.campings.filter(c =>
      !kw || c.name.toLowerCase().includes(kw) || c.governorate.toLowerCase().includes(kw)
    );
    this.renderMarkers();
  }

  focusCamping(camping: Camping): void {
    this.selectedCamping = camping;
    if (camping.latitude && camping.longitude) {
      this.map.setView([camping.latitude, camping.longitude], 12);
    }
  }

  goToDetail(): void {
    if (this.selectedCamping?.id) {
      this.router.navigate(['/camping', this.selectedCamping.id]);
    }
  }

  goToReserve(): void {
    if (this.selectedCamping?.id) {
      this.router.navigate(['/camping', this.selectedCamping.id, 'reserve']);
    }
  }

  closePanel(): void {
    this.selectedCamping = null;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.map) this.map.remove();
  }
}
