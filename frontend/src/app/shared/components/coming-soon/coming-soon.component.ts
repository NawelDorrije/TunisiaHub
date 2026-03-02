import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-coming-soon',
  standalone: true,
  templateUrl: './coming-soon.component.html',
  styleUrl: './coming-soon.component.css',
})
export class ComingSoonComponent {
  private readonly route = inject(ActivatedRoute);

  protected get moduleName(): string {
    return this.route.snapshot.data['module'] ?? 'Module';
  }
}
