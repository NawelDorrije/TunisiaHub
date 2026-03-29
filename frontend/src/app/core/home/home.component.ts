import { Component } from '@angular/core';

interface ServiceCard {
  title: string;
  description: string;
  icon: string;
  link: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  services: ServiceCard[] = [
    { title: 'Campings', description: 'Find the best camping spots across Tunisia.', icon: 'bi bi-tree', link: '/campings' },
    { title: 'Events', description: 'Discover amazing events near you.', icon: 'bi bi-calendar-event', link: '/events' },
    { title: 'Trendy Places', description: 'Explorez les lieux les plus tendance de Tunisie.', icon: 'bi bi-geo-alt', link: '/trendy-places' },
    { title: 'Shoppings', description: 'Explore top shopping destinations.', icon: 'bi bi-cart4', link: '/shoppings' },
    { title: 'Carpooling', description: 'Share rides and save money.', icon: 'bi bi-car-front', link: '/carpooling' },
    { title: 'Restaurants', description: 'Find the best restaurants in town.', icon: 'bi bi-egg-fried', link: '/restaurants' },
    { title: 'Tours', description: 'Book guided tours and experiences.', icon: 'bi bi-compass', link: '/tours' },
    { title: 'Hotels', description: 'Discover hotels and stays for your trip.', icon: 'bi bi-building', link: '/hotels' },
  ];
}
