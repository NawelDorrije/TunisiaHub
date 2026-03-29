import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrendyPlacesRoutingModule } from './trendy-places-routing.module';
import { TrendyPlacesComponent } from './trendy-places.component';
import { LieuListComponent } from './lieu-list/lieu-list.component';
import { LieuDetailComponent } from './lieu-detail/lieu-detail.component';

@NgModule({
  declarations: [
    TrendyPlacesComponent,
    LieuListComponent,
    LieuDetailComponent
  ],
  imports: [
    CommonModule,
    TrendyPlacesRoutingModule
  ]
})
export class TrendyPlacesModule {}