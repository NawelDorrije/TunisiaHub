import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminListAccommodationComponent } from './components/admin-list-accommodation/admin-list-accommodation.component';
import { UserListAccommodationComponent } from './components/user-list-accommodation/user-list-accommodation.component';
import { AddAccommodationComponent } from './components/add-accommodation/add-accommodation.component';
import { EditAccommodationComponent } from './components/edit-accommodation/edit-accommodation.component';
import { DetailsAccommodationComponent } from './components/details-accommodation/details-accommodation.component';
<<<<<<< HEAD
import { AdminGuard } from '../auth/admin.guard';

const routes: Routes = [
   { path: 'explore', component: UserListAccommodationComponent },                          
=======
import { ManagerGuard } from '../auth/manager.guard';
import { AdminGuard } from '../auth/admin.guard';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { AccommodationStatisticsComponent } from './components/accommodation-statistics/accommodation-statistics.component';

const routes: Routes = [
  { path: 'dashboard', component: AdminDashboardComponent, canActivate: [ManagerGuard] },
  { path: 'explore', component: UserListAccommodationComponent },                          
>>>>>>> origin/feature/integrated-app-event
  { path: 'detail/:id', component: DetailsAccommodationComponent },                        
  { path: 'admin', component: AdminListAccommodationComponent, canActivate: [AdminGuard] },
  { path: 'add', component: AddAccommodationComponent, canActivate: [AdminGuard] },        
  { path: 'edit/:id', component: EditAccommodationComponent, canActivate: [AdminGuard] },  
<<<<<<< HEAD
  { path: '', redirectTo: 'explore', pathMatch: 'full' }
=======

  { path: '', redirectTo: 'explore', pathMatch: 'full' },
  
  { 
  path: 'statistics', 
  component: AccommodationStatisticsComponent, 
  canActivate: [AdminGuard] 
}

>>>>>>> origin/feature/integrated-app-event
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccommodationsRoutingModule { }