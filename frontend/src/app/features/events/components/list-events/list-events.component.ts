import { Component, OnInit } from '@angular/core';
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

  filters: string[] = ['All', 'Music', 'Sport', 'Tech', 'Art', 'Conference'];

  constructor(private eventService: EventService) {}

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

  setView(mode: 'cards' | 'list'): void {
    this.viewMode = mode;
  }

  setFilter(filter: string): void {
    this.activeFilter = filter;
  }

  get filteredEvents(): Event[] {
    return this.events.filter(event => {
      const matchType = this.activeFilter === 'All' || event.type === this.activeFilter;
      const matchSearch = !this.searchQuery ||
        event.title.toLowerCase().includes(this.searchQuery.toLowerCase());
      return matchType && matchSearch;
    });
  }

  get totalRevenue(): number {
    return this.events.reduce((sum, e) => sum + (e.price || 0), 0);
  }

  get activeEventsCount(): number {
    return this.events.filter(e => e.status === 'Active').length;
  }
}