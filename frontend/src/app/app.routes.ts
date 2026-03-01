import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { HealthComponent } from './features/health/health.component';

export const routes: Routes = [
	{ path: '', component: HomeComponent, pathMatch: 'full' },
	{ path: 'health', component: HealthComponent },
	{ path: '**', redirectTo: '' }
];
