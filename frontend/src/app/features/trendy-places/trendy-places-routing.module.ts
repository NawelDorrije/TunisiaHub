import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TrendyPlacesComponent } from './trendy-places.component';
import { LieuListComponent } from './lieu-list/lieu-list.component';
import { LieuDetailComponent } from './lieu-detail/lieu-detail.component';

const routes: Routes = [
  { path: '', component: LieuListComponent },
  { path: ':id', component: LieuDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TrendyPlacesRoutingModule {}