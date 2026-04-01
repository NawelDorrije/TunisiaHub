import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CampingService } from '../../../../services/campings/camping.service';
import { SpotService } from '../../../../services/campings/spot.service';
import { Camping } from '../../../../models/campings/camping';
import { Spot } from '../../../../models/campings/spot';
import * as bootstrap from 'bootstrap';
import { Router } from '@angular/router';

@Component({
  selector: 'app-details-camping',
  templateUrl: './details-camping.component.html',
  styleUrls: ['./details-camping.component.css']
})
export class DetailsCampingComponent implements OnInit {

  BASE_URL = 'http://localhost:8089';

  campingId!: number;
  camping!: Camping;
  spots: Spot[] = [];

  isEditMode = false;
  selectedSpotId?: number;

  spotForm: Spot = {
    number: 0,
    size: 0,
    availability: true,
    price: 0,
    maxCapacity: 1
  };

  @ViewChild('spotModal') spotModalRef!: ElementRef;
  private modalInstance: bootstrap.Modal | null = null;

  constructor(
    private route: ActivatedRoute,
    private campingService: CampingService,
    private spotService: SpotService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.campingId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCamping();
  }

  ngAfterViewInit(): void {
    this.modalInstance = new bootstrap.Modal(
      this.spotModalRef.nativeElement
    );
  }

  loadCamping() {
    this.campingService.getCampingById(this.campingId).subscribe({

      next: (camping) => {

        console.log("Camping:", camping);
        console.log("Photos:", camping.photos);

        this.camping = camping;
        this.spots = camping.spots || [];

      },

      error: () => alert("Erreur lors du chargement du camping")

    });
  }

  getImageUrl(photo: string): string {

    if (!photo) {
      return 'assets/default-camping.jpg';
    }

    return `${this.BASE_URL}/${photo}`;
  }

  openAddForm() {

    this.isEditMode = false;

    this.selectedSpotId = undefined;

    this.spotForm = {
      number: 0,
      size: 0,
      availability: true,
      price: 0,
      maxCapacity: 1
    };

    this.showModal();

  }

  openEditForm(spot: Spot) {

    this.isEditMode = true;

    this.selectedSpotId = spot.id;

    this.spotForm = { ...spot };

    this.showModal();

  }

  private showModal() {

    if (this.modalInstance) {
      this.modalInstance.show();
    }

  }

  closeModal() {

    if (this.modalInstance) {
      this.modalInstance.hide();
    }

  }

  saveSpot() {

    this.spotForm.campingId = this.campingId;

    if (this.isEditMode && this.selectedSpotId) {

      this.spotService.updateSpot(
        this.selectedSpotId,
        this.spotForm
      ).subscribe({

        next: () => {

          alert("Spot modifié avec succès");

          this.closeModal();

          this.loadCamping();

        },

        error: () => alert("Erreur lors de la modification")

      });

    } else {

      this.spotService.createSpot(
        this.spotForm
      ).subscribe({

        next: () => {

          alert("Spot ajouté avec succès");

          this.closeModal();

          this.loadCamping();

        },

        error: () => alert("Erreur lors de l'ajout")

      });

    }

  }

  deleteSpot(id?: number) {

    if (!id || !confirm("Voulez-vous vraiment supprimer ce spot ?")) {
      return;
    }

    this.spotService.deleteSpot(id).subscribe({

      next: () => {

        alert("Spot supprimé");

        this.loadCamping();

      }

    });

  }

  goBack() {

    this.router.navigate(['/campings']);

  }

}
