import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CarpoolingRoutingModule } from './carpooling-routing.module';
import { CarpoolingHomeComponent } from './carpooling-home/carpooling-home.component';
import { PublishTripComponent } from './publish-trip/publish-trip.component';
import { MyTripsComponent } from './my-trips/my-trips.component';
import { TripDetailsComponent } from './trip-details/trip-details.component';
import { EditTripComponent } from './edit-trip/edit-trip.component';
import { SearchRidesComponent } from './search-rides/search-rides.component';
import { BookTripComponent } from './book-trip/book-trip.component';
import { MyBookingsComponent } from './my-bookings/my-bookings.component';
import { PassengerListComponent } from './passenger-list/passenger-list.component';
import { ReportComplaintComponent } from './report-complaint/report-complaint.component';
import { CarpoolingSearchBarComponent } from './shared/carpooling-search-bar/carpooling-search-bar.component';
import { ReviewDriverComponent } from './review-driver/review-driver.component';
import { AdminCarpoolingComponent } from './admin-carpooling/admin-carpooling.component';

@NgModule({
  declarations: [
    CarpoolingHomeComponent,
    PublishTripComponent,
    MyTripsComponent,
    TripDetailsComponent,
    EditTripComponent,
    SearchRidesComponent,
    BookTripComponent,
    MyBookingsComponent,
    PassengerListComponent,
    ReportComplaintComponent,
    CarpoolingSearchBarComponent,
    ReviewDriverComponent,
    AdminCarpoolingComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    CarpoolingRoutingModule,
  ],
})
export class CarpoolingModule {}
