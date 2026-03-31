import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ShopsRoutingModule } from './shops-routing.module';
import { ShopListComponent } from './pages/shop-list/shop-list.component';
import { ShopDetailComponent } from './pages/shop-detail/shop-detail.component';
import { ShopFormComponent } from './pages/shop-form/shop-form.component';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';


@NgModule({
  declarations: [
    ShopListComponent,
    ShopDetailComponent,
    ShopFormComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    ShopsRoutingModule
  ]
})
export class ShopsModule { }
