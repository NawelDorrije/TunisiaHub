import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../../../models/events/event.model';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.css']
})
export class EventDetailsComponent implements OnInit {

  event!: Event;

  map: any;
  marker: any;
  L: any;

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];

    this.eventService.getEventById(id).subscribe({
      next: (data) => {
        this.event = data;

        // charger map après data
        setTimeout(() => {
          if (isPlatformBrowser(this.platformId)) {
            this.loadMap();
          }
        }, 0);
      }
    });
  }

  // =========================
  // MAP
  // =========================
  async loadMap() {
    this.L = await import('leaflet');

    delete this.L.Icon.Default.prototype._getIconUrl;

    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
      iconUrl: 'assets/leaflet/marker-icon.png',
      shadowUrl: 'assets/leaflet/marker-shadow.png',
    });

    this.map = this.L.map('map').setView(
      [this.event.latitude, this.event.longitude],
      13
    );

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
      .addTo(this.map);

    this.marker = this.L.marker([
      this.event.latitude,
      this.event.longitude
    ]).addTo(this.map);
  }
}