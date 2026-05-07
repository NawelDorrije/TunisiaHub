import {
  Component,
  EventEmitter,
  Input,
  AfterViewInit,
  Output,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import * as L from 'leaflet';

// Fix Leaflet default marker icon issue with Angular
const iconDefault = L.icon({
  iconUrl: 'assets/marker-icon.png',
  shadowUrl: 'assets/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-map-picker',
  templateUrl: './map-picker.component.html',
  styleUrls: ['./map-picker.component.css']
})
export class MapPickerComponent implements AfterViewInit, OnChanges {

  @Input() latitude: number | null = null;
  @Input() longitude: number | null = null;
  @Output() locationSelected = new EventEmitter<{ lat: number; lng: number }>();

  private map!: L.Map;
  private marker!: L.Marker;

  // Default center → Tunisia
  private defaultLat = 33.8869;
  private defaultLng = 9.5375;
  private defaultZoom = 6;

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.map && (changes['latitude'] || changes['longitude'])) {
      if (this.latitude && this.longitude) {
        this.placeMarker(this.latitude, this.longitude);
        this.map.setView([this.latitude, this.longitude], 13);
      }
    }
  }

  private initMap(): void {
    this.map = L.map('map-picker', {
      center: [
        this.latitude ?? this.defaultLat,
        this.longitude ?? this.defaultLng
      ],
      zoom: this.latitude ? 13 : this.defaultZoom
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(this.map);

    // If editing — show existing marker
    if (this.latitude && this.longitude) {
      this.placeMarker(this.latitude, this.longitude);
    }

    // Click to place marker
    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.placeMarker(e.latlng.lat, e.latlng.lng);
      this.locationSelected.emit({ lat: e.latlng.lat, lng: e.latlng.lng });
    });
  }

  private placeMarker(lat: number, lng: number): void {
    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
    } else {
      this.marker = L.marker([lat, lng], { draggable: true }).addTo(this.map);

      // Drag marker to update location
      this.marker.on('dragend', () => {
        const pos = this.marker.getLatLng();
        this.locationSelected.emit({ lat: pos.lat, lng: pos.lng });
      });
    }

    this.marker.bindPopup(`
      <b>Selected location</b><br>
      Lat: ${lat.toFixed(5)}<br>
      Lng: ${lng.toFixed(5)}
    `).openPopup();
  }
}