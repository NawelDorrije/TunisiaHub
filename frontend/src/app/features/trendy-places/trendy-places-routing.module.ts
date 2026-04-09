import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LieuListComponent } from './lieu-list/lieu-list.component';
import { LieuDetailComponent } from './lieu-detail/lieu-detail.component';
import { AdminLieuComponent } from './admin/admin-lieu/admin-lieu.component';
import { AdminActiviteComponent } from './admin/admin-activite/admin-activite.component';
import { MesReservationsComponent } from './mes-reservations/mes-reservations.component';
import { PaiementReservationComponent } from './paiement-reservation/paiement-reservation.component';
import { AdminReservationsComponent } from './admin/admin-reservations/admin-reservations.component';

const routes: Routes = [
  { path: '', component: LieuListComponent },
  { path: 'mes-reservations', component: MesReservationsComponent },
  { path: 'paiement/:id', component: PaiementReservationComponent },
  { path: 'paiement-tranche/:id', component: PaiementReservationComponent }, // ← AJOUTE
  { path: 'admin/lieux', component: AdminLieuComponent },
  { path: 'admin/activites', component: AdminActiviteComponent },
  { path: 'admin/reservations', component: AdminReservationsComponent }, // ← AJOUTE
  { path: ':id', component: LieuDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TrendyPlacesRoutingModule {}