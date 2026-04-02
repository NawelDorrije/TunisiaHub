import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.css'
})
export class SignInComponent {

  errorMessage: string = '';
  isLoading: boolean = false;

  signInForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

  get f() {
    return this.signInForm.controls;
  }

  constructor(private authService: AuthService, private router: Router) {}

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
      next: () => {
        this.isLoading = false;
        if (this.authService.isAdmin()) {
          this.router.navigate(['/accommodations/admin']);
        } else {
          this.router.navigate(['/accommodations/explore']);
        }
      },
      error: () => {
        this.errorMessage = 'Invalid email or password.';
        this.isLoading = false;
      }
    });
  }
}