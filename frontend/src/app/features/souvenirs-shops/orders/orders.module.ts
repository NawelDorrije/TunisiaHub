import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OrdersRoutingModule } from './orders-routing.module';
import { OrderDetailComponent } from './pages/order-detail/order-detail.component';
import { OrderListComponent } from './pages/order-list/order-list.component';
import { OrderSuccessComponent } from './pages/order-success/order-success.component';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    OrderDetailComponent,
    OrderListComponent,
    OrderSuccessComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    OrdersRoutingModule
  ]
})
export class OrdersModule { }
