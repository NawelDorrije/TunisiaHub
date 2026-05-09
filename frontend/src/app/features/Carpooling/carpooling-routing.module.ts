import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CarpoolingHomeComponent } from './carpooling-home/carpooling-home.component';
import { PublishTripComponent } from './publish-trip/publish-trip.component';
import { MyTripsComponent } from './my-trips/my-trips.component';
import { TripDetailsComponent } from './trip-details/trip-details.component';
<<<<<<< HEAD
import { EditTripComponent } from './edit-trip/edit-trip.component';
=======
>>>>>>> origin/feature/integrated-app-event
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

const routes: Routes = [
  { path: '', component: CarpoolingHomeComponent },
  { path: 'publish', component: PublishTripComponent },
  { path: 'vehicles', component: VehicleManagementComponent },
  { path: 'my-trips', component: MyTripsComponent },
  { path: 'trip/:id', component: TripDetailsComponent },
  { path: 'trip/:id/edit', component: EditTripComponent },
  { path: 'trip/:id/book', component: BookTripComponent },
  { path: 'trip/:id/passengers', component: PassengerListComponent },
  { path: 'search-rides', component: SearchRidesComponent },
  { path: 'my-bookings', component: MyBookingsComponent },
  { path: 'report-complaint', component: ReportComplaintComponent },
  { path: 'admin', component: AdminDashboardComponent },
  { path: 'admin/bookings', component: AllBookingsComponent },
  { path: 'admin/complaints', component: ComplaintsManagementComponent },
  { path: 'admin/users', component: UsersSummaryComponent },
=======

import { ReviewDriverComponent } from './review-driver/review-driver.component';
import { AdminCarpoolingComponent } from './admin-carpooling/admin-carpooling.component';
import { AuthGuard } from '../auth/auth.guard';
import { AdminGuard } from '../auth/admin.guard';

const routes: Routes = [
  { path: '', component: CarpoolingHomeComponent },
  { path: 'publish', component: PublishTripComponent, canActivate: [AuthGuard] },
  { path: 'my-trips', component: MyTripsComponent, canActivate: [AuthGuard] },
  { path: 'trip/:id', component: TripDetailsComponent },
  { path: 'trip/:id/edit', component: PublishTripComponent, canActivate: [AuthGuard] },
  { path: 'trip/:id/book', component: BookTripComponent, canActivate: [AuthGuard] },
  { path: 'trip/:id/passengers', component: PassengerListComponent, canActivate: [AuthGuard] },
  { path: 'search-rides', component: SearchRidesComponent },
  { path: 'my-bookings', component: MyBookingsComponent, canActivate: [AuthGuard] },
  { path: 'report-complaint', component: ReportComplaintComponent, canActivate: [AuthGuard] },
  { path: 'review-driver', component: ReviewDriverComponent, canActivate: [AuthGuard] },
  { path: 'admin', component: AdminCarpoolingComponent, canActivate: [AdminGuard] },

>>>>>>> origin/feature/integrated-app-event
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CarpoolingRoutingModule {}
