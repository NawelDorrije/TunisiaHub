import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-campings',
  templateUrl: './campings.component.html',
  styleUrls: ['./campings.component.css']
})
export class CampingsComponent implements OnInit {
  campings: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getCampings().subscribe(
      data => this.campings = data,
      err => console.error(err)
    );
  }
}
