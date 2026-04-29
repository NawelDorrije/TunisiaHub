import { Component, ChangeDetectionStrategy, HostListener, OnInit } from '@angular/core';
import { map, Observable, combineLatest } from 'rxjs';
import { CartService } from '../../services/souvenirs-shops/cart.service';
import { Router } from '@angular/router';
import { AuthService, UserState } from '../../features/auth/services/auth.service';

interface HeaderState {
  user: UserState | null;
  cartCount: number;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent implements OnInit {

  vm$: Observable<HeaderState>;
  isScrolled = false;

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {
    const cartCount$ = this.cartService.items$.pipe(
      map((items) => items.reduce((total, item) => total + item.quantity, 0))
    );

    this.vm$ = combineLatest([
      this.authService.user$,
      cartCount$
    ]).pipe(
      map(([user, cartCount]) => ({ user, cartCount }))
    );
  }

  ngOnInit(): void {}

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled = window.scrollY > 20;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/sign-in']);
  }
}