import { Component } from '@angular/core';
import { map, Observable } from 'rxjs';
import { CartService } from '../../services/souvenirs-shops/cart.service';
import { Router } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'] 
})
export class HeaderComponent {

  cartCount$: Observable<number>;

  constructor(
    private cartService: CartService,
    public authService: AuthService,
    private router: Router
  ) {
    this.cartCount$ = this.cartService.items$.pipe(
      map((items) => items.reduce((total, item) => total + item.quantity, 0))
    );
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/sign-in']);
  }
}