import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { ActivatedRoute } from '@angular/router';
import { AuthService } from '../auth/services/auth.service';

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
  showAddMenuForm: boolean = false;
  newMenu: any = {
    name: '',
    type: '',
    restaurantId: null
  };
  menuTypes: string[] = [];
  isSubmitting: boolean = false;
  showAddItemForm: boolean = false;
  newMenuItem: any = {
    name: '',
    ingredients: '',
    description: '',
    menu_id: null
  };
  isSubmittingItem: boolean = false;
  showEditMenuForm: boolean = false;
  editMenu: any = { id: null, name: '', type: '', restaurant_id: null as number | null };
  isSubmittingMenuEdit: boolean = false;
  showEditItemForm: boolean = false;
  editMenuItem: any = {
    id: null,
    name: '',
    ingredients: '',
    description: '',
    menu_id: null as number | null,
  };
  isSubmittingItemEdit: boolean = false;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  ngOnInit(): void {
    // Load menu types from backend
    this.api.getMenuTypes().subscribe(
      (data: any) => {
        console.log('Menu types from backend:', data);
        this.menuTypes = data;
        if (this.menuTypes.length > 0) {
          this.newMenu.type = this.menuTypes[0];
        }
      },
      (err: any) => console.error('Error fetching menu types:', err)
    );

    // Get restaurantId from query params or route params
    this.route.queryParams.subscribe(params => {
      if (params['restaurantId']) {
        this.restaurantId = parseInt(params['restaurantId'], 10);
        console.log('✅ Restaurant ID:', this.restaurantId);
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

  openAddMenuForm(): void {
    if (!this.isAdmin) return;
    this.showAddMenuForm = true;
    this.newMenu = { name: '', type: 'mains', restaurantId: this.restaurantId as number };
  }

  closeAddMenuForm(): void {
    this.showAddMenuForm = false;
    this.newMenu = { name: '', type: 'mains', restaurantId: null };
  }

  submitAddMenu(): void {
    if (!this.isAdmin) return;
    if (!this.newMenu.name || !this.newMenu.type) {
      alert('Please fill in all fields');
      return;
    }

    if (!this.restaurantId) {
      alert('Error: Restaurant ID missing');
      return;
    }

    this.isSubmitting = true;
    const menuData = {
      name: this.newMenu.name,
      type: this.newMenu.type,
      restaurant_id: this.restaurantId
    };

    console.log('Sending menu data:', menuData);

    this.api.addMenu(menuData).subscribe(
      (data: any) => {
        console.log('Menu added:', data);
        this.closeAddMenuForm();
        this.isSubmitting = false;
        this.loadMenusByRestaurant();
        alert('Menu added successfully!');
      },
      (err: any) => {
        console.error('Error adding menu:', err);
        this.isSubmitting = false;
        alert('Error adding menu. Please try again.');
      }
    );
  }

  openAddItemForm(): void {
    if (!this.isAdmin) return;
    if (!this.selectedMenu) {
      alert('Please select a menu first');
      return;
    }
    this.showAddItemForm = true;
    this.newMenuItem = {
      name: '',
      ingredients: '',
      description: '',
      menu_id: this.selectedMenu.id
    };
  }

  closeAddItemForm(): void {
    this.showAddItemForm = false;
    this.newMenuItem = {
      name: '',
      ingredients: '',
      description: '',
      menu_id: null
    };
  }

  submitAddItem(): void {
    if (!this.isAdmin) return;
    if (!this.newMenuItem.name || !this.newMenuItem.ingredients || !this.newMenuItem.description) {
      alert('Please fill in all fields');
      return;
    }

    if (!this.selectedMenu?.id) {
      alert('Error: Menu ID missing');
      return;
    }

    this.isSubmittingItem = true;
    const itemData = {
      name: this.newMenuItem.name,
      ingredients: this.newMenuItem.ingredients,
      description: this.newMenuItem.description,
      menu_id: this.selectedMenu.id
    };

    console.log('Sending menu item data:', itemData);

    this.api.addMenuItem(itemData).subscribe(
      (data: any) => {
        console.log('✅ Menu item added:', data);
        this.selectedMenuItems.push(data);
        this.closeAddItemForm();
        this.isSubmittingItem = false;
        alert('Item added successfully!');
      },
      (err: any) => {
        console.error('❌ Error adding item:', err);
        this.isSubmittingItem = false;
        alert('Error adding item. Please try again.');
      }
    );
  }

  openEditMenuForm(event: Event, menu: any): void {
    event.stopPropagation();
    if (!this.isAdmin) return;
    this.editMenu = {
      id: menu.id,
      name: menu.name ?? '',
      type: menu.type ?? '',
      restaurant_id: this.restaurantId,
    };
    this.showEditMenuForm = true;
  }

  closeEditMenuForm(): void {
    this.showEditMenuForm = false;
    this.editMenu = { id: null, name: '', type: '', restaurant_id: null };
    this.isSubmittingMenuEdit = false;
  }

  submitEditMenu(): void {
    if (!this.isAdmin || this.editMenu.id == null) return;
    if (!this.editMenu.name || !this.editMenu.type || !this.restaurantId) {
      alert('Please fill in all fields');
      return;
    }
    this.isSubmittingMenuEdit = true;
    const payload = {
      id: this.editMenu.id,
      name: this.editMenu.name,
      type: this.editMenu.type,
      restaurant_id: this.restaurantId,
    };
    this.api.updateMenu(payload).subscribe({
      next: (data: any) => {
        const idx = this.menus.findIndex((m) => m.id === data.id);
        if (idx !== -1) this.menus[idx] = data;
        if (this.selectedMenu?.id === data.id) this.selectedMenu = { ...this.selectedMenu, ...data };
        this.closeEditMenuForm();
        alert('Menu updated successfully!');
      },
      error: (err: any) => {
        console.error('Error updating menu:', err);
        this.isSubmittingMenuEdit = false;
        alert('Error updating menu. Please try again.');
      },
    });
  }

  confirmDeleteMenu(event: Event, menu: any): void {
    event.stopPropagation();
    if (!this.isAdmin) return;
    if (!confirm(`Delete menu "${menu.name}"? Items in this menu may also be removed.`)) return;
    this.api.deleteMenu(menu.id).subscribe({
      next: () => {
        this.menus = this.menus.filter((m) => m.id !== menu.id);
        if (this.selectedMenu?.id === menu.id) this.clearSelection();
        alert('Menu deleted.');
      },
      error: (err: any) => {
        console.error('Error deleting menu:', err);
        alert('Error deleting menu. Please try again.');
      },
    });
  }

  openEditItemForm(event: Event, item: any): void {
    event.stopPropagation();
    if (!this.isAdmin) return;
    if (!this.selectedMenu?.id) return;
    this.editMenuItem = {
      id: item.id,
      name: item.name ?? '',
      ingredients: item.ingredients ?? '',
      description: item.description ?? '',
      menu_id: item.menu_id ?? this.selectedMenu.id,
    };
    this.showEditItemForm = true;
  }

  closeEditItemForm(): void {
    this.showEditItemForm = false;
    this.editMenuItem = {
      id: null,
      name: '',
      ingredients: '',
      description: '',
      menu_id: null,
    };
    this.isSubmittingItemEdit = false;
  }

  submitEditMenuItem(): void {
    if (!this.isAdmin || this.editMenuItem.id == null) return;
    if (
      !this.editMenuItem.name ||
      !this.editMenuItem.ingredients ||
      !this.editMenuItem.description ||
      !this.selectedMenu?.id
    ) {
      alert('Please fill in all fields');
      return;
    }
    this.isSubmittingItemEdit = true;
    const payload = {
      id: this.editMenuItem.id,
      name: this.editMenuItem.name,
      ingredients: this.editMenuItem.ingredients,
      description: this.editMenuItem.description,
      menu_id: this.selectedMenu.id,
    };
    this.api.updateMenuItem(payload).subscribe({
      next: (data: any) => {
        const idx = this.selectedMenuItems.findIndex((i) => i.id === data.id);
        if (idx !== -1) this.selectedMenuItems[idx] = data;
        this.closeEditItemForm();
        alert('Item updated successfully!');
      },
      error: (err: any) => {
        console.error('Error updating item:', err);
        this.isSubmittingItemEdit = false;
        alert('Error updating item. Please try again.');
      },
    });
  }

  confirmDeleteMenuItem(event: Event, item: any): void {
    event.stopPropagation();
    if (!this.isAdmin) return;
    if (!confirm(`Delete "${item.name}"?`)) return;
    this.api.deleteMenuItem(item.id).subscribe({
      next: () => {
        this.selectedMenuItems = this.selectedMenuItems.filter((i) => i.id !== item.id);
        alert('Item deleted.');
      },
      error: (err: any) => {
        console.error('Error deleting item:', err);
        alert('Error deleting item. Please try again.');
      },
    });
  }
}
