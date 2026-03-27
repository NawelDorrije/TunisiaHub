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
  showAddForm: boolean = false;
  searchAddress: string = '';
  newRestaurant = {
    name: '',
    address: '',
    email: '',
    phoneNum: ''
  };
  isSubmitting: boolean = false;

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

  getFilteredRestaurants(): any[] {
    if (!this.searchAddress.trim()) {
      return this.restaurants;
    }
    const searchTerm = this.searchAddress.toLowerCase();
    return this.restaurants.filter(restaurant => 
      restaurant.address.toLowerCase().includes(searchTerm)
    );
  }

  openAddForm(): void {
    this.showAddForm = true;
    this.newRestaurant = { name: '', address: '', email: '', phoneNum: '' };
  }

  closeAddForm(): void {
    this.showAddForm = false;
    this.newRestaurant = { name: '', address: '', email: '', phoneNum: '' };
  }

  submitAddRestaurant(): void {
    if (!this.newRestaurant.name || !this.newRestaurant.address || !this.newRestaurant.email || !this.newRestaurant.phoneNum) {
      alert('Please fill in all fields');
      return;
    }

    this.isSubmitting = true;
    this.api.addRestaurant(this.newRestaurant).subscribe(
      data => {
        console.log('Restaurant added:', data);
        this.restaurants.push(data);
        this.closeAddForm();
        this.isSubmitting = false;
        alert('Restaurant added successfully!');
      },
      err => {
        console.error('Error adding restaurant:', err);
        this.isSubmitting = false;
        alert('Error adding restaurant. Please try again.');
      }
    );
  }
}
