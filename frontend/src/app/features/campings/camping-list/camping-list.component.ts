import { Component, OnInit, OnDestroy } from '@angular/core';
import { Camping } from '../../../models/campings/camping';
import { CampingService } from '../../../services/campings/camping.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-camping-list',
  templateUrl: './camping-list.component.html',
  styleUrls: ['./camping-list.component.css']
})
export class CampingListComponent implements OnInit, OnDestroy { // <-- renommer ici
  campings: Camping[] = [];
  loading = true;

  BASE_URL = "http://localhost:8089";

  currentImageIndex: number[] = [];
  intervals: any[] = [];

  constructor(
    private campingService: CampingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCampings();
  }

  ngOnDestroy(): void {
    this.intervals.forEach(interval => clearInterval(interval));
  }

  loadCampings(): void {
    this.campingService.getAllCampings().subscribe({
      next: (data) => {
        this.campings = data;
        this.loading = false;
        this.currentImageIndex = this.campings.map(() => 0);
        this.startSlideshow();
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.loading = false;
      }
    });
  }

  startSlideshow(): void {
    this.campings.forEach((camping, i) => {
      if (camping.photos && camping.photos.length > 1) {
        const interval = setInterval(() => {
          this.currentImageIndex[i] =
            (this.currentImageIndex[i] + 1) % camping.photos.length;
        }, 3000);
        this.intervals.push(interval);
      }
    });
  }

  getCurrentPhoto(camping: Camping, index: number): string {
    if (camping.photos && camping.photos.length > 0) {
      return `${this.BASE_URL}/${camping.photos[this.currentImageIndex[index]]}`;
    }
    return 'assets/no-image.png';
  }

  addCamping() {
    this.router.navigate(['/campings/admin/add-camping']);
  }

  editCamping(id?: number) {
    if (!id) return;
    this.router.navigate(['/campings/admin/edit-camping', id]);
  }

  deleteCamping(id?: number) {
    if (!id) return;
    if (!confirm("Are you sure you want to delete this camping?")) return;

    this.campingService.deleteCamping(id).subscribe({
      next: () => {
        alert("Camping deleted successfully.");
        this.loadCampings();
      },
      error: (err) => {
        console.error(err);
        alert("Error deleting camping.");
      }
    });
  }

  manageSpots(id?: number) {
    if (!id) return;
    this.router.navigate(['/campings/admin/details', id]);
  }

  // Aller à l'image suivante
nextImage(index: number, totalImages: number) {
  this.currentImageIndex[index] =
    (this.currentImageIndex[index] + 1) % totalImages;
}

// Aller à l'image précédente
prevImage(index: number, totalImages: number) {
  this.currentImageIndex[index] =
    (this.currentImageIndex[index] - 1 + totalImages) % totalImages;
}
}
