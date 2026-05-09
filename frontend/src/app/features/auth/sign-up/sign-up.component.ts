import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
<<<<<<< HEAD
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
=======
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { UserRole } from '../../../models/auth/auth.model';
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
<<<<<<< HEAD
  styleUrl: './sign-up.component.css'
=======
  styleUrls: ['./sign-up.component.css']
>>>>>>> origin/feature/integrated-app-event
})
export class SignUpComponent {

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

<<<<<<< HEAD
  signUpForm = new FormGroup({
    nom: new FormControl('', Validators.required),
    prenom: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

=======
  returnUrl: string | null;

  signUpForm = new FormGroup({
    nom: new FormControl('', Validators.required),

    prenom: new FormControl('', Validators.required),

    email: new FormControl('', [
      Validators.required,
      Validators.email
    ]),

    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6)
    ]),

    role: new FormControl<UserRole>(
      'CLIENT',
      Validators.required
    )
  });

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl =
      this.route.snapshot.queryParamMap.get('returnUrl');
  }

>>>>>>> origin/feature/integrated-app-event
  get f() {
    return this.signUpForm.controls;
  }

<<<<<<< HEAD
  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
=======
  onSubmit(): void {

>>>>>>> origin/feature/integrated-app-event
    if (this.signUpForm.invalid) {
      this.signUpForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register({
<<<<<<< HEAD
      nom: this.signUpForm.value.nom!,
      prenom: this.signUpForm.value.prenom!,
      email: this.signUpForm.value.email!,
      password: this.signUpForm.value.password!
    }).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/accommodations/explore']);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Registration failed. Please try again.';
=======

      nom: this.signUpForm.value.nom!,

      prenom: this.signUpForm.value.prenom!,

      email: this.signUpForm.value.email!,

      password: this.signUpForm.value.password!,

      role: this.signUpForm.value.role as UserRole

    }).subscribe({

      next: (response) => {

        this.isLoading = false;

        // 1️⃣ returnUrl priority
        if (
          this.returnUrl &&
          this.returnUrl.startsWith('/')
        ) {
          this.router.navigateByUrl(this.returnUrl);
          return;
        }

        // 2️⃣ role-based navigation
        if (response.role === 'ADMIN') {

          this.router.navigate(['/events']);

        } else if (response.role === 'OWNER') {

          this.router.navigate(['/home']);

        } else {

          this.router.navigate(['/events/user/events']);
        }
      },

      error: (err) => {

        this.errorMessage =
          err.error ||
          'Registration failed. Please try again.';

>>>>>>> origin/feature/integrated-app-event
        this.isLoading = false;
      }
    });
  }
}