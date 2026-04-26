import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { RestaurantsRoutingModule } from './restaurants-routing.module';
import { AdminManageRestaurantsComponent } from './admin-manage-restaurants/admin-manage-restaurants.component';
import { AdminRestaurantDashboardComponent } from './admin-restaurant-dashboard/admin-restaurant-dashboard.component';
import { AdminRestaurantReservationsComponent } from './admin-restaurant-reservations/admin-restaurant-reservations.component';
import { RestaurantListComponent } from './restaurant-list/restaurant-list.component';
import { RestaurantsComponent } from './restaurants.component';
import { AiSearchBarComponent } from '../../core/components/ai-search-bar/ai-search-bar.component';
import { FloorPlanCanvasComponent } from './shared/floor-plan-canvas/floor-plan-canvas.component';
import { FloorPlanDesignerComponent } from './admin/floor-plan-designer/floor-plan-designer.component';
import { TablePickerComponent } from './client/table-picker/table-picker.component';
import { MyReservationsComponent } from './client/my-reservations/my-reservations.component';

@NgModule({
  declarations: [
    RestaurantListComponent,
    RestaurantsComponent,
    AdminRestaurantReservationsComponent,
    AdminRestaurantDashboardComponent,
    AdminManageRestaurantsComponent,
    FloorPlanCanvasComponent,
    FloorPlanDesignerComponent,
    TablePickerComponent,
    MyReservationsComponent,
  ],
  imports: [
    CommonModule,
    RouterModule,
    RestaurantsRoutingModule,
    FormsModule,
    AiSearchBarComponent
  ]
})
export class RestaurantsModule { }
