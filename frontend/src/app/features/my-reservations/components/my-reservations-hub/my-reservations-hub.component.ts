import { Component } from '@angular/core';
import { Router } from '@angular/router';

interface ReservationCategory {
  title: string;
  description: string;
  icon: string;
  link: string;
  color: string;
}

@Component({
  selector: 'app-my-reservations-hub',
  templateUrl: './my-reservations-hub.component.html',
  styleUrls: ['./my-reservations-hub.component.css']
})
export class MyReservationsHubComponent {

  categories: ReservationCategory[] = [
    {
      title: 'Accommodations',
      description: 'View and manage your accommodation bookings.',
      icon: 'bi bi-house-heart',
      link: '/my-reservations/accommodations',
      color: 'primary'
    },
    {
      title: 'Campings',
      description: 'View and manage your camping reservations.',
      icon: 'bi bi-tree',
      link: '/my-reservations/campings',
      color: 'success'
    },
    {
      title: 'Events',
      description: 'View and manage your event bookings.',
      icon: 'bi bi-calendar-event',
      link: '/my-reservations/events',
      color: 'warning'
    },
    {
      title: 'Restaurants',
      description: 'View and manage your restaurant reservations.',
      icon: 'bi bi-egg-fried',
      link: '/my-reservations/restaurants',
      color: 'danger'
    },
    {
      title: 'Carpooling',
      description: 'View and manage your carpooling trips.',
      icon: 'bi bi-car-front',
      link: '/carpooling',
      color: 'info'
    }
  ];

  constructor(private router: Router) {}

  navigate(link: string): void {
    this.router.navigate([link]);
  }
}