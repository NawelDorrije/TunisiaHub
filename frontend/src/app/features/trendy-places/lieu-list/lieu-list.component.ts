import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Lieu } from '../../../models/trendy-places/lieu.model';
import { TrendyPlacesService } from '../../../services/trendy-places.service';

@Component({
  selector: 'app-lieu-list',
  templateUrl: './lieu-list.component.html',
  styleUrls: ['./lieu-list.component.css']
})
export class LieuListComponent implements OnInit {
  lieux: Lieu[] = [];
  filteredLieux: Lieu[] = [];
  searchTerm = '';
  selectedType = '';
  selectedVille = '';
  loading = true;
  types: string[] = [];
  villes: string[] = [];

  // Comparateur
  selectedForCompare: Lieu[] = [];
  showCompareBar = false;

  constructor(private trendyService: TrendyPlacesService, private router: Router) {}

  ngOnInit(): void {
    this.trendyService.getAllLieux().subscribe({
      next: (data) => {
        this.lieux = data;
        this.filteredLieux = data;
        this.types = [...new Set(data.map(l => l.type))];
        this.villes = [...new Set(data.map(l => l.ville))];
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  filter(): void {
    this.filteredLieux = this.lieux.filter(lieu => {
      const matchSearch = lieu.nom.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                         lieu.description.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchType = this.selectedType ? lieu.type === this.selectedType : true;
      const matchVille = this.selectedVille ? lieu.ville === this.selectedVille : true;
      return matchSearch && matchType && matchVille;
    });
  }

  onSearchChange(value: string): void { this.searchTerm = value; this.filter(); }
  onTypeChange(value: string): void { this.selectedType = value; this.filter(); }
  onVilleChange(value: string): void { this.selectedVille = value; this.filter(); }
  goToDetail(id: number): void { this.router.navigate(['/trendy-places', id]); }

  // ===== COMPARATEUR =====
  toggleCompare(event: Event, lieu: Lieu): void {
    event.stopPropagation();
    const idx = this.selectedForCompare.findIndex(l => l.id === lieu.id);
    if (idx > -1) {
      this.selectedForCompare.splice(idx, 1);
    } else {
      if (this.selectedForCompare.length >= 3) return;
      this.selectedForCompare.push(lieu);
    }
    this.showCompareBar = this.selectedForCompare.length >= 1;
  }

  isSelectedForCompare(lieu: Lieu): boolean {
    return this.selectedForCompare.some(l => l.id === lieu.id);
  }

  lancerComparaison(): void {
    const ids = this.selectedForCompare.map(l => l.id).join(',');
    this.router.navigate(['/trendy-places/comparer'], { queryParams: { ids } });
  }

  clearCompare(): void {
    this.selectedForCompare = [];
    this.showCompareBar = false;
  }

  getImageUrl(image: string): string {
    if (!image) return '/assets/images/lieux/default.jpg';
    if (image.startsWith('http')) return image;
    return `/assets/images/lieux/${image}`;
  }

  onImageError(event: Event): void {
    (event.target as HTMLImageElement).src = '/assets/images/lieux/default.jpg';
  }

  getTypeIcon(type: string): string {
    const icons: {[key: string]: string} = {
      'Culturel': '🏛️', 'Naturel': '🌿', 'Sportif': '⚽',
      'Gastronomique': '🍽️', 'Historique': '🏰', 'Artistique': '🎨'
    };
    return icons[type] || '📍';
  }
}