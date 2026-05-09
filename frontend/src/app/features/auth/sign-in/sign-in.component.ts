import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
<<<<<<< HEAD
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
=======
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { OnboardingTourService } from '../../../core/services/onboarding-tour.service';
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
<<<<<<< HEAD
  styleUrl: './sign-in.component.css'
=======
  styleUrls: ['./sign-in.component.css']
>>>>>>> origin/feature/integrated-app-event
})
export class SignInComponent {

  errorMessage: string = '';
  isLoading: boolean = false;
<<<<<<< HEAD

  signInForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

=======
  returnUrl: string | null;

  signInForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.email
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6)
    ])
  });

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private tourService: OnboardingTourService
  ) {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
  }

>>>>>>> origin/feature/integrated-app-event
  get f() {
    return this.signInForm.controls;
  }

<<<<<<< HEAD
  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
=======
  onSubmit(): void {

>>>>>>> origin/feature/integrated-app-event
    if (this.signInForm.invalid) {
      this.signInForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login({
      email: this.signInForm.value.email!,
      password: this.signInForm.value.password!
    }).subscribe({
<<<<<<< HEAD
      next: () => {
        this.isLoading = false;
        if (this.authService.isAdmin()) {
          this.router.navigate(['/accommodations/admin']);
        } else {
          this.router.navigate(['/accommodations/explore']);
        }
      },
=======

      next: (response) => {

        this.isLoading = false;

        // ✅ Priorité au returnUrl
        if (this.returnUrl && this.returnUrl.startsWith('/')) {
          this.router.navigateByUrl(this.returnUrl);
          return;
        }

        // ✅ Navigation selon le rôle
        if (response.role === 'ADMIN') {

          // ancienne route conservée
          this.router.navigate(['/events']);

          // si vous voulez utiliser dashboard plus tard :
          // this.router.navigate(['/accommodations/dashboard']);

        } else if (response.role === 'OWNER') {

          this.router.navigate(['/home']);

        } else {

          // ancienne route utilisateur conservée
          this.router.navigate(['/events/user/events']);

          // alternative :
          // this.router.navigate(['/home']);
        }

        // ✅ Tour optionnel
        // this.tourService.startTour();

      },

>>>>>>> origin/feature/integrated-app-event
      error: () => {
        this.errorMessage = 'Invalid email or password.';
        this.isLoading = false;
      }
<<<<<<< HEAD
=======

>>>>>>> origin/feature/integrated-app-event
    });
  }
}