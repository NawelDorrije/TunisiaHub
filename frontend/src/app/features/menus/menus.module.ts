import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
    MenusRoutingModule,
    FormsModule
  ]
})
export class MenusModule { }
