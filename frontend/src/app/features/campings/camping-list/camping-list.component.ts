import { Component, OnInit } from '@angular/core';
import { Camping } from '../../../models/campings/camping';
import { CampingService } from '../../../services/campings/camping.service';

@Component({
  selector: 'app-camping-list',
  templateUrl: './camping-list.component.html',
  styleUrls: ['./camping-list.component.css']
})
export class CampingListComponent implements OnInit {

  campings: Camping[] = [];

  loading: boolean = true;

  constructor(private campingService: CampingService) {}

  ngOnInit(): void {
    this.loadCampings();
  }

  loadCampings(): void {
    this.campingService.getAllCampings().subscribe({
      next: (data) => {
        this.campings = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur:', err);
        this.loading = false;
      }
    });
  }

}
