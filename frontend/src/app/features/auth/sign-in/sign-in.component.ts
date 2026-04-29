import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { OnboardingTourService } from '../../../core/services/onboarding-tour.service';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css']
})
export class SignInComponent {

  errorMessage: string = '';
  isLoading: boolean = false;
  returnUrl: string | null;

  signInForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

  get f() {
    return this.signInForm.controls;
  }

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private tourService: OnboardingTourService
  ) {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
  }

  onSubmit(): void {
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
      next: (response) => {
        this.isLoading = false;
        if (this.returnUrl && this.returnUrl.startsWith('/')) {
          this.router.navigateByUrl(this.returnUrl);
          return;
        }
        if (response.role === 'ADMIN') {
          this.router.navigate(['/accommodations/dashboard']);
        } else if (response.role === 'OWNER') {
          this.tourService.startIfNeeded();
          this.router.navigate(['/owner-dashboard']);
        } else {
          this.router.navigate(['/home']);
        }
      },
      error: () => {
        this.errorMessage = 'Invalid email or password.';
        this.isLoading = false;
      }
    });
  }
}


