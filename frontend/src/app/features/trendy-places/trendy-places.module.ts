import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';  
import { TrendyPlacesRoutingModule } from './trendy-places-routing.module';
import { LieuListComponent } from './lieu-list/lieu-list.component';
import { LieuDetailComponent } from './lieu-detail/lieu-detail.component';
import { AdminLieuComponent } from './admin/admin-lieu/admin-lieu.component';
import { AdminActiviteComponent } from './admin/admin-activite/admin-activite.component';
import { TrendyPlacesComponent } from './trendy-places.component';
import { MesReservationsComponent } from './mes-reservations/mes-reservations.component';
import { PaiementReservationComponent } from './paiement-reservation/paiement-reservation.component';
import { AdminReservationsComponent } from './admin/admin-reservations/admin-reservations.component';
import { FactureComponent } from './facture/facture.component';
import { VerifyBilletComponent } from './verify-billet/verify-billet.component';

@NgModule({
  declarations: [
    TrendyPlacesComponent,
    LieuListComponent,
    LieuDetailComponent,
    AdminLieuComponent,
    AdminActiviteComponent,
    MesReservationsComponent,
    PaiementReservationComponent,
    AdminReservationsComponent,
    FactureComponent,
    VerifyBilletComponent
  ],
  imports: [
  CommonModule,
  FormsModule,
  RouterModule,  // ← ajouter
  TrendyPlacesRoutingModule
]
})
export class TrendyPlacesModule {}