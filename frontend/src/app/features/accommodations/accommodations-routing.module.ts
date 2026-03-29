import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminListAccommodationComponent } from './components/admin-list-accommodation/admin-list-accommodation.component';
import { UserListAccommodationComponent } from './components/user-list-accommodation/user-list-accommodation.component';
import { AddAccommodationComponent } from './components/add-accommodation/add-accommodation.component';
import { EditAccommodationComponent } from './components/edit-accommodation/edit-accommodation.component';
import { DetailsAccommodationComponent } from './components/details-accommodation/details-accommodation.component';

const routes: Routes = [
  { path: 'admin', component: AdminListAccommodationComponent },
  { path: 'explore', component: UserListAccommodationComponent },
  { path: 'add', component: AddAccommodationComponent },
  { path: 'edit/:id', component: EditAccommodationComponent },
  { path: 'detail/:id', component: DetailsAccommodationComponent },
  { path: '', redirectTo: 'explore', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccommodationsRoutingModule { }