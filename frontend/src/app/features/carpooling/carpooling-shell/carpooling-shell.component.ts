import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-carpooling-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './carpooling-shell.component.html',
  styleUrl: './carpooling-shell.component.css',
})
export class CarpoolingShellComponent {
  protected readonly tabs = [
    { label: 'Find a ride', path: 'trips' },
    { label: 'My trips', path: 'my-trips' },
    { label: 'Publish a trip', path: 'trips/new' },
  ];
}
