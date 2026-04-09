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
  searchTerm: string = '';
  selectedType: string = '';
  selectedVille: string = '';
  loading: boolean = true;

  types: string[] = [];
  villes: string[] = [];

  constructor(
    private trendyService: TrendyPlacesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.trendyService.getAllLieux().subscribe({
      next: (data) => {
        this.lieux = data;
        this.filteredLieux = data;
        this.types = [...new Set(data.map(l => l.type))];
        this.villes = [...new Set(data.map(l => l.ville))];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
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

  onSearchChange(value: string): void {
    this.searchTerm = value;
    this.filter();
  }

  onTypeChange(value: string): void {
    this.selectedType = value;
    this.filter();
  }

  onVilleChange(value: string): void {
    this.selectedVille = value;
    this.filter();
  }

  goToDetail(id: number): void {
    this.router.navigate(['/trendy-places', id]);
  }

getImageUrl(image: string): string {
  if (!image) return '/assets/images/lieux/default.jpg';
  if (image.startsWith('http')) return image;  // ← ✅ gère http://localhost:8089/...
  return `/assets/images/lieux/${image}`;       // ← ✅ gère 1.jpg, 2.jpg...
}

onImageError(event: Event): void {
  const img = event.target as HTMLImageElement;
  img.src = '/assets/images/lieux/default.jpg';
}

  getTypeIcon(type: string): string {
    const icons: {[key: string]: string} = {
      'Culturel': '🏛️',
      'Naturel': '🌿',
      'Sportif': '⚽',
      'Gastronomique': '🍽️',
      'Historique': '🏰',
      'Artistique': '🎨'
    };
    return icons[type] || '📍';
  }
}