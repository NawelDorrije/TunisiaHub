import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MenusRoutingModule } from './menus-routing.module';
import { MenusComponent } from './menus.component';
import { MenuListComponent } from './menu-list/menu-list.component';


@NgModule({
  declarations: [
    MenusComponent,
    MenuListComponent
  ],
  imports: [
    CommonModule,
    MenusRoutingModule
  ]
})
export class MenusModule { }
