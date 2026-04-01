import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SpotService } from '../../../../services/campings/spot.service';
import { CampingService } from '../../../../services/campings/camping.service';
import { Spot } from '../../../../models/campings/spot';
import { Camping } from '../../../../models/campings/camping';

@Component({
  selector: 'app-list-spot',
  templateUrl: './list-spot.component.html',
  styleUrls: ['./list-spot.component.css']
})
export class ListSpotComponent implements OnInit {

  campingId!: number;

  camping!: Camping;

  spots: Spot[] = [];

  showForm = false;

  isEditMode = false;

  selectedSpotId?: number;

  newSpot: Spot = {
    number: 0,
    size: 0,
    availability: true,
    price: 0,
    maxCapacity: 1
  };

  constructor(
    private route: ActivatedRoute,
    private spotService: SpotService,
    private campingService: CampingService
  ) {}

  ngOnInit(): void {

    this.campingId = Number(
      this.route.snapshot.paramMap.get('campingId')
    );

    this.loadCamping();

  }

  loadCamping() {

    this.campingService
      .getCampingById(this.campingId)
      .subscribe({

        next: (camping) => {

          this.camping = camping;

          this.spots = camping.spots;

        },

        error: () => {
          alert("Failed to load camping");
        }

      });

  }

  openAddForm() {

    this.showForm = true;

    this.isEditMode = false;

    this.newSpot = {
      number: 0,
      size: 0,
      availability: true,
      price: 0,
      maxCapacity: 1,
      campingId :  this.campingId
    };

  }

  openEditForm(spot: Spot) {

    this.showForm = true;

    this.isEditMode = true;

    this.selectedSpotId = spot.id;

    this.newSpot = { ...spot };

  }

  deleteSpot(id: number) {

    if (!confirm("Delete this spot?")) return;

    this.spotService
      .deleteSpot(id)
      .subscribe({

        next: () => {

          alert("Spot deleted");

          this.loadCamping();

        }

      });

  }

}
