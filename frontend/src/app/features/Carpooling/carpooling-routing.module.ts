import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CarpoolingHomeComponent } from './carpooling-home/carpooling-home.component';
import { PublishTripComponent } from './publish-trip/publish-trip.component';
import { MyTripsComponent } from './my-trips/my-trips.component';
import { TripDetailsComponent } from './trip-details/trip-details.component';
import { SearchRidesComponent } from './search-rides/search-rides.component';
import { BookTripComponent } from './book-trip/book-trip.component';
import { MyBookingsComponent } from './my-bookings/my-bookings.component';
import { PassengerListComponent } from './passenger-list/passenger-list.component';
import { ReportComplaintComponent } from './report-complaint/report-complaint.component';

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

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CarpoolingRoutingModule {}
