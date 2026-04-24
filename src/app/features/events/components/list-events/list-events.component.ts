import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../../../models/events/event.model';

@Component({
  selector: 'app-list-events',
  templateUrl: './list-events.component.html',
  styleUrls: ['./list-events.component.css']
})
export class ListEventsComponent implements OnInit {

  events: Event[] = [];
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
  get filteredEvents(): Event[] {
    return this.events;
  }

  get completedEventsCount(): number {
  return this.events.filter(e => e.status === 'COMPLETED').length;
}

get activeEventsCount(): number {
  return this.events.filter(e => e.status === 'OPEN').length;
}
}
