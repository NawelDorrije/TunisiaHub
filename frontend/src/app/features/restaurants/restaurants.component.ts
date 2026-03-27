import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-restaurants',
  templateUrl: './restaurants.component.html',
  styleUrls: ['./restaurants.component.css']
})
export class RestaurantsComponent implements OnInit {
  restaurants: any[] = [];
  selectedRestaurant: any = null;
  showDetailsModal: boolean = false;

  constructor(
    private api: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.api.getRestaurants().subscribe(
      data => this.restaurants = data,
      err => console.error(err)
    );
  }

  viewDetails(restaurant: any): void {
    this.selectedRestaurant = restaurant;
    this.showDetailsModal = true;
  }

  viewMenus(restaurant: any): void {
    // Navigate to menus page with restaurantId as query parameter
    this.router.navigate(['/menus'], { queryParams: { restaurantId: restaurant.id } });
  }

  closeDetailsModal(): void {
    this.showDetailsModal = false;
    this.selectedRestaurant = null;
  }
}
