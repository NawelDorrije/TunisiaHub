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
import { AdminDashboardComponent } from './admin-dashboard/admin-dashboard.component';
import { AllBookingsComponent } from './all-bookings/all-bookings.component';
import { ComplaintsManagementComponent } from './complaints-management/complaints-management.component';
import { UsersSummaryComponent } from './users-summary/users-summary.component';

const routes: Routes = [
  { path: '', component: CarpoolingHomeComponent },
  { path: 'publish', component: PublishTripComponent },
  { path: 'my-trips', component: MyTripsComponent },
  { path: 'trip/:id', component: TripDetailsComponent },
  { path: 'trip/:id/edit', component: PublishTripComponent },
  { path: 'trip/:id/book', component: BookTripComponent },
  { path: 'trip/:id/passengers', component: PassengerListComponent },
  { path: 'search-rides', component: SearchRidesComponent },
  { path: 'my-bookings', component: MyBookingsComponent },
  { path: 'report-complaint', component: ReportComplaintComponent },
  { path: 'admin', component: AdminDashboardComponent },
  { path: 'admin/bookings', component: AllBookingsComponent },
  { path: 'admin/complaints', component: ComplaintsManagementComponent },
  { path: 'admin/users', component: UsersSummaryComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class CarpoolingRoutingModule {}
