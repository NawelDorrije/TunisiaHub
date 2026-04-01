import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Camping } from '../../../../models/campings/camping';
import { CampingService } from '../../../../services/campings/camping.service';

@Component({
  selector: 'app-list-camping',
  templateUrl: './list-camping.component.html',
  styleUrls: ['./list-camping.component.css']
})
export class ListCampingComponent implements OnInit {

  campings: Camping[] = [];
  loading = true;

  // URL backend
  BASE_URL = "http://localhost:8089";

  constructor(
    private campingService: CampingService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCampings();
  }

  loadCampings() {

    this.campingService
      .getAllCampings()
      .subscribe({

        next: (data) => {

          this.campings = data;

          this.loading = false;

        },

        error: (err) => {

          console.error(err);

          this.loading = false;

          alert("Failed to load campings.");

        }

      });

  }

  // GET FIRST IMAGE
  getFirstPhoto(
    camping: Camping
  ): string {

    if (
      camping.photos &&
      camping.photos.length > 0
    ) {

      return this.BASE_URL +
             "/" +
             camping.photos[0];

    }

    return "assets/no-image.png";

  }

  deleteCamping(id?: number) {

    if (!id) return;

    if (!confirm(
      "Are you sure you want to delete this camping?"
    )) return;

    this.campingService
      .deleteCamping(id)
      .subscribe({

        next: () => {

          alert(
            "Camping deleted successfully."
          );

          this.loadCampings();

        },

        error: (err) => {

          console.error(err);

          alert(
            "Error deleting camping."
          );

        }

      });

  }

  addCamping() {

    this.router.navigate([
      '/campings/admin/add-camping'
    ]);

  }

  editCamping(id?: number) {

    if (!id) return;

    this.router.navigate([
      '/campings/admin/edit-camping',
      id
    ]);

  }

  manageSpots(id?: number) {

    if (!id) return;

    this.router.navigate([
      '/campings/admin/details',
      id
    ]);

  }

}
