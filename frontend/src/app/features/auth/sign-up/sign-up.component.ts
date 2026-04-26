import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.css'
})
export class SignUpComponent {

  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;
  returnUrl: string | null;

  signUpForm = new FormGroup({
    nom: new FormControl('', Validators.required),
    prenom: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)])
  });

  get f() {
    return this.signUpForm.controls;
  }

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
  }

  onSubmit(): void {
    if (this.signUpForm.invalid) {
      this.signUpForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register({
      nom: this.signUpForm.value.nom!,
      prenom: this.signUpForm.value.prenom!,
      email: this.signUpForm.value.email!,
      password: this.signUpForm.value.password!
    }).subscribe({
      next: () => {
        this.isLoading = false;
        if (this.returnUrl && this.returnUrl.startsWith('/')) {
          this.router.navigateByUrl(this.returnUrl);
          return;
        }
        this.router.navigate(['/accommodations/explore']);
      },
      error: (err) => {
        this.errorMessage = err.error || 'Registration failed. Please try again.';
        this.isLoading = false;
      }
    });
  }
}