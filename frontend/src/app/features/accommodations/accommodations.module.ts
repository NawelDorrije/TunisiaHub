import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AddAccommodationComponent } from './components/add-accommodation/add-accommodation.component';
import { EditAccommodationComponent } from './components/edit-accommodation/edit-accommodation.component';
import { DetailsAccommodationComponent } from './components/details-accommodation/details-accommodation.component';
import { provideHttpClient } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminListAccommodationComponent } from './components/admin-list-accommodation/admin-list-accommodation.component';
import { UserListAccommodationComponent } from './components/user-list-accommodation/user-list-accommodation.component';
import { AccommodationsRoutingModule } from './accommodations-routing.module';
import { ReviewListComponent } from './components/review-list/review-list.component';
import { ReviewItemComponent } from './components/review-item/review-item.component';
import { ReviewFormComponent } from './components/review-form/review-form.component';
import { MapPickerComponent } from './components/map-picker/map-picker.component';
import { MapWeatherComponent } from './components/map-weather/map-weather.component';
import { ReservationFormComponent } from './components/reservation-form/reservation-form.component';
import { FormsModule } from '@angular/forms';
import { FeedbackPopupComponent } from './components/feedback-popup/feedback-popup.component';
import { AccommodationStatisticsComponent } from './components/accommodation-statistics/accommodation-statistics.component';
import { NgApexchartsModule } from 'ng-apexcharts';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';



@NgModule({
  declarations: [
    AddAccommodationComponent,
    EditAccommodationComponent,
    DetailsAccommodationComponent,
    AdminListAccommodationComponent,
    UserListAccommodationComponent,
    ReviewListComponent,
    ReviewItemComponent,
    ReviewFormComponent,
    MapPickerComponent,
    MapWeatherComponent,
    ReservationFormComponent,
    FeedbackPopupComponent,
    AccommodationStatisticsComponent,
    AdminDashboardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    FormsModule,
    AccommodationsRoutingModule,
    NgApexchartsModule
  ],
  providers:[
    provideHttpClient()
  ]
})
export class AccommodationsModule { }
