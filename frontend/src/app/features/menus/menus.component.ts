import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-menus',
  templateUrl: './menus.component.html',
  styleUrls: ['./menus.component.css']
})
export class MenusComponent implements OnInit {
  menus: any[] = [];
  selectedMenu: any = null;
  selectedMenuItems: any[] = [];
  restaurantId: number | null = null;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get restaurantId from query params or route params
    this.route.queryParams.subscribe(params => {
      if (params['restaurantId']) {
        this.restaurantId = params['restaurantId'];
        this.loadMenusByRestaurant();
      }
    });
  }

  loadMenusByRestaurant(): void {
    if (this.restaurantId) {
      this.api.getMenusByRestaurantId(this.restaurantId).subscribe(
        data => this.menus = data,
        err => console.error('Error fetching menus:', err)
      );
    }
  }

  selectMenu(menu: any): void {
    this.selectedMenu = menu;
    this.selectedMenuItems = [];
    // Fetch menu items when menu is selected
    this.api.getMenuItemsByMenuId(menu.id).subscribe(
      (data: any) => {
        this.selectedMenuItems = data;
        console.log('Menu items:', data);
      },
      (err: any) => console.error('Error fetching menu items:', err)
    );
  }

  clearSelection(): void {
    this.selectedMenu = null;
    this.selectedMenuItems = [];
  }

  getMenuIcon(menuType: string): string {
    // Return different icons based on menu type
    const iconMap: { [key: string]: string } = {
      'appetizers': 'bi-egg-fried',
      'mains': 'bi-cup-hot',
      'desserts': 'bi-cake2',
      'drinks': 'bi-cup',
      'beverages': 'bi-cup',
      'soups': 'bi-cup-hot',
      'salads': 'bi-leaf',
      'breakfast': 'bi-sunrise',
      'lunch': 'bi-sun',
      'dinner': 'bi-moon-stars',
    };
    return iconMap[menuType?.toLowerCase()] || 'bi-bookmark';
  }
}
