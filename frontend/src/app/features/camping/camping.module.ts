import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { CampingRoutingModule } from './camping-routing.module';

// Import child modules
import { FrontofficeModule } from './frontoffice/frontoffice.module';
import { BackofficeModule } from './backoffice/backoffice.module';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    CampingRoutingModule,
    FrontofficeModule,
    BackofficeModule
  ],
  exports: [
    FrontofficeModule, // Export them to make components/services usable elsewhere
    BackofficeModule
  ]
})
export class CampingModule {}
