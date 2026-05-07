import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TrendyPlacesService } from '../../../services/trendy-places.service';
import { Lieu } from '../../../models/trendy-places/lieu.model';

@Component({
  selector: 'app-comparateur-lieux',
  templateUrl: './comparateur-lieux.component.html',
  styleUrls: ['./comparateur-lieux.component.css']
})
export class ComparateurLieuxComponent implements OnInit {
  lieux: Lieu[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private service: TrendyPlacesService
  ) {}

  ngOnInit(): void {
    const ids = this.route.snapshot.queryParamMap.get('ids')?.split(',').map(Number) || [];
    let loaded = 0;
    ids.forEach(id => {
      this.service.getLieuById(id).subscribe({
        next: (lieu) => {
          this.lieux.push(lieu);
          loaded++;
          if (loaded === ids.length) this.loading = false;
        },
        error: () => { loaded++; if (loaded === ids.length) this.loading = false; }
      });
    });
  }

  getImageUrl(image: string): string {
    if (!image) return '/assets/images/lieux/default.jpg';
    if (image.startsWith('http')) return image;
    return `/assets/images/lieux/${image}`;
  }

  getTotalActivites(lieu: Lieu): number {
    return lieu.activites?.length || 0;
  }

  getPrixMin(lieu: Lieu): number {
    const activites = lieu.activites || [];
    if (activites.length === 0) return 0;
    return Math.min(...activites.map(a => a.prix || 0));
  }

  getPrixMax(lieu: Lieu): number {
    const activites = lieu.activites || [];
    if (activites.length === 0) return 0;
    return Math.max(...activites.map(a => a.prix || 0));
  }

  getTotalPlaces(lieu: Lieu): number {
    return lieu.activites?.reduce((sum, a) => sum + (a.capaciteMax || 0), 0) || 0;
  }

  getPlacesDisponibles(lieu: Lieu): number {
    return lieu.activites?.reduce((sum, a) =>
      sum + Math.max(0, (a.capaciteMax || 0) - (a.placesReservees || 0)), 0) || 0;
  }

  getTauxRemplissage(lieu: Lieu): number {
    const total = this.getTotalPlaces(lieu);
    if (total === 0) return 0;
    const reservees = lieu.activites?.reduce((sum, a) => sum + (a.placesReservees || 0), 0) || 0;
    return Math.round((reservees / total) * 100);
  }

  getActivitesDisponibles(lieu: Lieu): number {
    return lieu.activites?.filter(a => a.disponible).length || 0;
  }

  getBestActivite(lieu: Lieu): string {
    const actives = lieu.activites?.filter(a => a.disponible) || [];
    if (actives.length === 0) return '—';
    return actives[0].nomActivite;
  }

  getWinner(stat: string): number {
    if (this.lieux.length === 0) return -1;
    let values: number[] = [];
    if (stat === 'activites') values = this.lieux.map(l => this.getTotalActivites(l));
    if (stat === 'places') values = this.lieux.map(l => this.getPlacesDisponibles(l));
    if (stat === 'prix') values = this.lieux.map(l => this.getPrixMin(l));
    const best = stat === 'prix' ? Math.min(...values) : Math.max(...values);
    return values.indexOf(best);
  }

  goBack(): void { this.router.navigate(['/trendy-places']); }
  goToLieu(id: number): void { this.router.navigate(['/trendy-places', id]); }

  getTypeIcon(type: string): string {
    const icons: {[key: string]: string} = {
      'Culturel': '🏛️', 'Naturel': '🌿', 'Sportif': '⚽',
      'Gastronomique': '🍽️', 'Historique': '🏰', 'Artistique': '🎨'
    };
    return icons[type] || '📍';
  }
}