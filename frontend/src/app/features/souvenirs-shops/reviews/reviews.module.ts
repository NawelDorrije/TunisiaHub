import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ReviewsRoutingModule } from './reviews-routing.module';
import { ReviewListComponent } from './pages/review-list/review-list.component';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    ReviewListComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReviewsRoutingModule
  ]
})
export class ReviewsModule { }
