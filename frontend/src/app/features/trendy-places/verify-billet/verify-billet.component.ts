import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TrendyPlacesService } from '../../../services/trendy-places.service';

@Component({
  selector: 'app-verify-billet',
  templateUrl: './verify-billet.component.html',
  styleUrls: ['./verify-billet.component.css']
})
export class VerifyBilletComponent implements OnInit {
  result: any = null;
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private trendyService: TrendyPlacesService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token')!;
    this.trendyService.verifierBillet(token).subscribe({
      next: (res) => { this.result = res; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}