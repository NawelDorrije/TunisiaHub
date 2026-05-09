import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  AfterViewInit
} from '@angular/core';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http';

interface WeatherDay {
  date: string;
  maxTemp: number;
  minTemp: number;
  weatherCode: number;
}

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
  selector: 'app-map-weather',
  templateUrl: './map-weather.component.html',
  styleUrls: ['./map-weather.component.css']
})
export class MapWeatherComponent implements OnChanges, AfterViewInit {

  @Input() latitude!: number;
  @Input() longitude!: number;
  @Input() locationName!: string;

  private map!: L.Map;
  private marker!: L.Marker;
  private mapInitialized = false;

  forecast: WeatherDay[] = [];
  isLoadingWeather = true;
  weatherError = '';

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    if (this.latitude && this.longitude) {
      this.initMap();
      this.loadWeather();
      this.mapInitialized = true;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.mapInitialized && this.latitude && this.longitude) {
      this.updateMap();
      this.loadWeather();
    }
  }

  private initMap(): void {
    this.map = L.map('map-display', {
      center: [this.latitude, this.longitude],
      zoom: 13,
      zoomControl: true,
      scrollWheelZoom: false
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(this.map);

    this.marker = L.marker([this.latitude, this.longitude])
      .addTo(this.map)
      .bindPopup(`<b>${this.locationName}</b>`)
      .openPopup();
  }

  private updateMap(): void {
    if (this.map) {
      this.map.setView([this.latitude, this.longitude], 13);
      if (this.marker) {
        this.marker.setLatLng([this.latitude, this.longitude]);
        this.marker.bindPopup(`<b>${this.locationName}</b>`).openPopup();
      }
    }
  }

  private loadWeather(): void {
    this.isLoadingWeather = true;
    this.weatherError = '';

    const url = `https://api.open-meteo.com/v1/forecast` +
      `?latitude=${this.latitude}` +
      `&longitude=${this.longitude}` +
      `&daily=temperature_2m_max,temperature_2m_min,weather_code` +
      `&forecast_days=5` +
      `&timezone=auto`;

    this.http.get<any>(url).subscribe({
      next: (data) => {
        this.forecast = data.daily.time.map((date: string, i: number) => ({
          date,
          maxTemp: Math.round(data.daily.temperature_2m_max[i]),
          minTemp: Math.round(data.daily.temperature_2m_min[i]),
          weatherCode: data.daily.weather_code[i]
        }));
        this.isLoadingWeather = false;
      },
      error: () => {
        this.weatherError = 'Failed to load weather data.';
        this.isLoadingWeather = false;
      }
    });
  }

  getWeatherEmoji(code: number): string {
    if (code === 0) return '☀️';
    if (code <= 3) return '⛅';
    if (code <= 49) return '🌫️';
    if (code <= 67) return '🌧️';
    if (code <= 77) return '❄️';
    if (code <= 82) return '🌦️';
    if (code <= 99) return '⛈️';
    return '🌡️';
  }

  getWeatherLabel(code: number): string {
    if (code === 0) return 'Clear sky';
    if (code <= 3) return 'Partly cloudy';
    if (code <= 49) return 'Foggy';
    if (code <= 67) return 'Rainy';
    if (code <= 77) return 'Snowy';
    if (code <= 82) return 'Showers';
    if (code <= 99) return 'Thunderstorm';
    return 'Unknown';
  }

  getDayName(dateStr: string): string {
    const date = new Date(dateStr);
    const today = new Date();
    const tomorrow = new Date();
    tomorrow.setDate(today.getDate() + 1);

    if (date.toDateString() === today.toDateString()) return 'Today';
    if (date.toDateString() === tomorrow.toDateString()) return 'Tomorrow';
    return date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
  }

  openInMaps(): void {
    window.open(
      `https://www.openstreetmap.org/?mlat=${this.latitude}&mlon=${this.longitude}&zoom=15`,
      '_blank'
    );
  }
}