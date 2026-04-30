import { Component } from '@angular/core';

type AdminCard = {
  title: string;
  description: string;
  icon: string;
  route: string;
  cta: string;
};

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent {
  cards: AdminCard[] = [
    {
      title: 'Carpooling',
      description: 'Monitor rides, review reports, and manage transport activity.',
      icon: 'bi bi-car-front-fill',
      route: '/carpooling',
      cta: 'Open Carpooling'
    },
    {
      title: 'Campings',
      description: 'Manage camping spots, updates, and operational visibility.',
      icon: 'bi bi-tree-fill',
      route: '/camping/backoffice/admin',
      cta: 'Open Campings'
    },
    {
      title: 'Accommodations',
      description: 'Manage listings, edits, approvals, and reservation visibility.',
      icon: 'bi bi-building-fill',
      route: '/accommodations/admin',
      cta: 'Open Accommodation Admin'
    },
    {
      title: 'Restaurants',
      description: 'Central place for restaurant catalog, status, and quality controls.',
      icon: 'bi bi-cup-hot-fill',
      route: '/restaurants',
      cta: 'Open Restaurants'
    },
    {
      title: 'Events',
      description: 'Oversee event lifecycle, moderation, and publication health.',
      icon: 'bi bi-calendar-event-fill',
      route: '/events',
      cta: 'Open Events'
    },
    {
      title: 'Trendy Places',
      description: 'Curate and highlight trending destinations across Tunisia.',
      icon: 'bi bi-stars',
      route: '/trendy-places/admin/lieux',
      cta: 'Open Trendy Places'
    },
    {
      title: 'Shops',
      description: 'Manage souvenir shops, orders, and promotional content.',
      icon: 'bi bi-shop',
      route: '/admin-dashboard',
      cta: 'Open Shop Management'
    }
  ];
}
