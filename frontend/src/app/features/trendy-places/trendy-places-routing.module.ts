import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LieuListComponent } from './lieu-list/lieu-list.component';
import { LieuDetailComponent } from './lieu-detail/lieu-detail.component';
import { AdminLieuComponent } from './admin/admin-lieu/admin-lieu.component';
import { AdminActiviteComponent } from './admin/admin-activite/admin-activite.component';

const routes: Routes = [
  { path: '', component: LieuListComponent },
  { path: 'admin/lieux', component: AdminLieuComponent },
  { path: 'admin/activites', component: AdminActiviteComponent },
  { path: ':id', component: LieuDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TrendyPlacesRoutingModule {}