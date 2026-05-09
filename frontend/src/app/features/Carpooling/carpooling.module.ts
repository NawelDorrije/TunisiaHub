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
<<<<<<< HEAD
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AllBookingsComponent } from './all-bookings/all-bookings.component';
import { ComplaintsManagementComponent } from './complaints-management/complaints-management.component';
import { UsersSummaryComponent } from './users-summary/users-summary.component';
import { VehicleManagementComponent } from './vehicle-management/vehicle-management.component';
=======
import { CarpoolingSearchBarComponent } from './shared/carpooling-search-bar/carpooling-search-bar.component';
import { ReviewDriverComponent } from './review-driver/review-driver.component';
import { AdminCarpoolingComponent } from './admin-carpooling/admin-carpooling.component';
>>>>>>> origin/feature/integrated-app-event

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
<<<<<<< HEAD
    AdminDashboardComponent,
    AllBookingsComponent,
    ComplaintsManagementComponent,
    UsersSummaryComponent,
    VehicleManagementComponent,
=======
    CarpoolingSearchBarComponent,
    ReviewDriverComponent,
    AdminCarpoolingComponent,
>>>>>>> origin/feature/integrated-app-event
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    CarpoolingRoutingModule,
  ],
})
export class CarpoolingModule {}
