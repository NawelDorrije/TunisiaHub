import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Lieu, ActiviteLieu } from '../../../models/trendy-places/lieu.model';
import { TrendyPlacesService } from '../../../services/trendy-places.service';

@Component({
  selector: 'app-lieu-detail',
  templateUrl: './lieu-detail.component.html',
  styleUrls: ['./lieu-detail.component.css']
})
export class LieuDetailComponent implements OnInit {
  lieu: Lieu | null = null;
  activites: ActiviteLieu[] = [];
  loading: boolean = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private trendyService: TrendyPlacesService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.trendyService.getLieuById(id).subscribe({
      next: (data) => {
        this.lieu = data;
        this.activites = data.activites || [];
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

 getImageUrl(image: string): string {
  if (!image) {
    return '/assets/images/lieux/default.jpg';
  }
  if (image.startsWith('http')) {
    return image;
  }
  return `/assets/images/lieux/${image}`;
}

onImageError(event: Event): void {
  const img = event.target as HTMLImageElement;
  img.src = '/assets/images/lieux/default.jpg';
}

  goBack(): void {
    this.router.navigate(['/trendy-places']);
  }
}