import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminListAccommodationComponent } from './components/admin-list-accommodation/admin-list-accommodation.component';
import { UserListAccommodationComponent } from './components/user-list-accommodation/user-list-accommodation.component';
import { AddAccommodationComponent } from './components/add-accommodation/add-accommodation.component';
import { EditAccommodationComponent } from './components/edit-accommodation/edit-accommodation.component';
import { DetailsAccommodationComponent } from './components/details-accommodation/details-accommodation.component';
import { ManagerGuard } from '../auth/manager.guard';
import { AdminGuard } from '../auth/admin.guard';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AccommodationStatisticsComponent } from './components/accommodation-statistics/accommodation-statistics.component';

const routes: Routes = [
  { path: 'dashboard', component: AdminDashboardComponent, canActivate: [ManagerGuard] },
  { path: 'explore', component: UserListAccommodationComponent },                          
  { path: 'detail/:id', component: DetailsAccommodationComponent },                        
  { path: 'admin', component: AdminListAccommodationComponent, canActivate: [AdminGuard] },
  { path: 'add', component: AddAccommodationComponent, canActivate: [AdminGuard] },        
  { path: 'edit/:id', component: EditAccommodationComponent, canActivate: [AdminGuard] },  
  { path: '', redirectTo: 'explore', pathMatch: 'full' },
  { 
  path: 'statistics', 
  component: AccommodationStatisticsComponent, 
  canActivate: [AdminGuard] 
}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccommodationsRoutingModule { }