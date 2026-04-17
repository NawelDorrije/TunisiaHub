import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { Event } from '../../../../models/events/event.model';
import { Router } from '@angular/router';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-list-events-user',
  templateUrl: './list-events-user.component.html',
  styleUrls: ['./list-events-user.component.css']
})
export class ListEventsUserComponent implements OnInit {

  events: Event[] = [];
  searchQuery: string = '';

  // 🔥 NEW
  showPopup = false;
  errorMessage = '';

  constructor(
    private eventService: EventService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.eventService.getAllEvents().subscribe({
      next: (data) => this.events = data,
      error: (err) => console.error(err)
    });
  }

  get filteredEvents(): Event[] {
    return this.events.filter(e =>
      e.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      e.lieu.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  goToDetails(id: number) {
    this.router.navigate(['/events/details', id]);
  }

  reserveEvent(event: Event) {

  const userId = this.authService.getUserId(); // 🔥 dynamique

  if (event.status === 'COMPLETED') {
    this.errorMessage = "❌ Event is FULL";
    this.showPopup = true;
    return;
  }

  this.eventService.reserveEvent(userId, event.id!).subscribe({
    next: () => {
      this.router.navigate(['/events/reserve', event.id]);
    },
    error: (err) => {
      this.errorMessage = err.error?.message || "User is already reserved";
      this.showPopup = true;
    }
  });
}

  closePopup() {
    this.showPopup = false;
  }
  
}