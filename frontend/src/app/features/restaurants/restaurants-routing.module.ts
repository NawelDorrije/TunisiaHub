import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminGuard } from '../auth/admin.guard';
import { AdminRestaurantDashboardComponent } from './admin-restaurant-dashboard/admin-restaurant-dashboard.component';
import { AdminManageRestaurantsComponent } from './admin-manage-restaurants/admin-manage-restaurants.component';
import { AdminRestaurantReservationsComponent } from './admin-restaurant-reservations/admin-restaurant-reservations.component';
import { RestaurantsComponent } from './restaurants.component';
import { FloorPlanDesignerComponent } from './admin/floor-plan-designer/floor-plan-designer.component';
import { MyReservationsComponent } from './client/my-reservations/my-reservations.component';

const routes: Routes = [
  { path: '', component: RestaurantsComponent },
  { path: 'my-reservations', component: MyReservationsComponent },
  {
    path: 'dashboard',
    component: AdminRestaurantDashboardComponent,
    canActivate: [AdminGuard],
  },
  {
    path: 'manage',
    component: AdminManageRestaurantsComponent,
    canActivate: [AdminGuard],
  },
  {
    path: 'floor-plan/:id',
    component: FloorPlanDesignerComponent,
    canActivate: [AdminGuard],
  },
  {
    path: 'reservations',
    component: AdminRestaurantReservationsComponent,
    canActivate: [AdminGuard],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RestaurantsRoutingModule { }
