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

@NgModule({
  declarations: [
    TrendyPlacesComponent,
    LieuListComponent,
    LieuDetailComponent,
    AdminLieuComponent,
    AdminActiviteComponent
  ],
  imports: [
  CommonModule,
  FormsModule,
  RouterModule,  // ← ajouter
  TrendyPlacesRoutingModule
]
})
export class TrendyPlacesModule {}