import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event as AppEvent } from '../../../../models/events/event.model';

@Component({
  selector: 'app-list-events',
  templateUrl: './list-events.component.html',
  styleUrls: ['./list-events.component.css']
})
export class ListEventsComponent implements OnInit {
  private readonly backendBaseUrl = 'http://localhost:8089';

  private readonly fallbackCardImage =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 500"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#10233f"/><stop offset="1" stop-color="#2f5bea"/></linearGradient></defs><rect width="800" height="500" fill="url(#g)"/><text x="50%" y="52%" dominant-baseline="middle" text-anchor="middle" fill="#ffffff" font-family="Arial,sans-serif" font-size="34" opacity="0.92">Event Image</text></svg>');
  events: AppEvent[] = [];
  viewMode: 'cards' | 'list' = 'cards';
  activeFilter: string = 'All';
  searchQuery: string = '';

filters: string[] = ['All', 'SPORT', 'FESTIVAL', 'CONFERENCE', 'COMPETITION'];

  constructor(
    private eventService: EventService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.events = data;
        console.log('Events loaded:', data);
      },
      error: (err) => {
        console.error('Error loading events', err);
      }
    });
  }

  

  deleteEvent(id: number): void {
    this.eventService.deleteEvent(id).subscribe(() => {
      this.loadEvents();
    });
  }

  /* ================= SEARCH ================= */
  onSearch(): void {
    if (!this.searchQuery.trim()) {
      this.loadEvents();
      return;
    }

    this.eventService.searchEvents(this.searchQuery).subscribe(data => {
      this.events = data;
    });
  }

  /* ================= FILTER ================= */
  setFilter(filter: string): void {
    this.activeFilter = filter;

    if (filter === 'All') {
      this.loadEvents();
      return;
    }

    this.eventService.filterByType(filter).subscribe(data => {
      this.events = data;
    });
  }

  /* ================= VIEW ================= */
  setView(mode: 'cards' | 'list'): void {
    this.viewMode = mode;
  }

  goToDetails(id: number | undefined): void {
    if (!id) {
      return;
    }

    this.router.navigate(['/events/details', id], {
      state: { returnUrl: '/events' }
    });
  }

  /* ================= DISPLAY ================= */
  get filteredEvents(): AppEvent[] {
    return this.events;
  }

  get completedEventsCount(): number {
  return this.events.filter(e => e.status === 'COMPLETED').length;
}

get activeEventsCount(): number {
  return this.events.filter(e => e.status === 'OPEN').length;
}

  getCardImageSrc(image?: string): string {
    const candidate = (image ?? '').trim();
    if (!candidate) {
      return this.fallbackCardImage;
    }

    const slashNormalized = candidate.replace(/\\/g, '/');
    const lower = slashNormalized.toLowerCase();
    const uploadsIdx = lower.indexOf('/uploads/');

    if (uploadsIdx >= 0) {
      return `${this.backendBaseUrl}${slashNormalized.substring(uploadsIdx)}`;
    }

    if (slashNormalized.startsWith('http://') || slashNormalized.startsWith('https://') || slashNormalized.startsWith('data:')) {
      return slashNormalized;
    }

    if (slashNormalized.startsWith('//localhost') || slashNormalized.startsWith('//127.0.0.1')) {
      return `http:${slashNormalized}`;
    }

    if (lower.startsWith('//uploads/')) {
      return `${this.backendBaseUrl}${slashNormalized.substring(1)}`;
    }

    if (slashNormalized.startsWith('/')) {
      return `${this.backendBaseUrl}${slashNormalized}`;
    }

    return `${this.backendBaseUrl}/${slashNormalized}`;
  }

  onCardImageError(event: globalThis.Event): void {
    const imageElement = event.target as HTMLImageElement | null;
    if (!imageElement) {
      return;
    }

    if (imageElement.src === this.fallbackCardImage) {
      return;
    }

    imageElement.src = this.fallbackCardImage;
  }
}
