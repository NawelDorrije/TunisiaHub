<<<<<<< HEAD
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../features/auth/services/auth.service';
=======
import {
  Component,
  ChangeDetectionStrategy,
  HostListener,
  OnInit,
} from '@angular/core';
import { map, Observable, combineLatest } from 'rxjs';
import { CartService } from '../../services/souvenirs-shops/cart.service';
import { Router } from '@angular/router';
import {
  AuthService,
  UserState,
} from '../../features/auth/services/auth.service';

interface HeaderState {
  user: UserState | null;
  cartCount: number;
}
>>>>>>> origin/feature/integrated-app-event

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
<<<<<<< HEAD
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/sign-in']);
  }
}
=======
  styleUrls: ['./header.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent implements OnInit {
  vm$: Observable<HeaderState>;
  isScrolled = false;

  constructor(
    private cartService: CartService,
    public authService: AuthService,
    private router: Router,
  ) {
    const cartCount$ = this.cartService.items$.pipe(
      map((items) => items.reduce((total, item) => total + item.quantity, 0)),
    );

    this.vm$ = combineLatest([this.authService.user$, cartCount$]).pipe(
      map(([user, cartCount]) => ({ user, cartCount })),
    );
  }

  ngOnInit(): void {}

  @HostListener('window:scroll', [])
  onWindowScroll() {
    this.isScrolled = window.scrollY > 20;
  }

  // Helper pour simplifier le template
  // Dans le TS
  get currentRole(): string | null {
    return this.authService.getRole();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/sign-in']);
  }

  get eventsLink(): string {
    if (this.authService.isAdmin()) {
      return '/events';
    }
    return '/events/user/events';
  }
  isCarpoolingPage(): boolean {
    return this.router.url.startsWith('/carpooling');
  }
}
>>>>>>> origin/feature/integrated-app-event
