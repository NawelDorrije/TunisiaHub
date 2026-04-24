import { Component, OnInit } from '@angular/core';
import { ComplaintStatus, ComplaintWithContext } from '../models';
import { CarpoolingDataService } from '../services/carpooling-data.service';

@Component({
  selector: 'app-complaints-management',
  templateUrl: './complaints-management.component.html',
  styleUrls: ['./complaints-management.component.css'],
})
export class ComplaintsManagementComponent implements OnInit {
  complaints: ComplaintWithContext[] = [];

  constructor(private readonly dataService: CarpoolingDataService) {}

  ngOnInit(): void {
    this.reload();
  }

  updateStatus(complaintId: number, status: ComplaintStatus): void {
    this.dataService.updateComplaintStatus(complaintId, status).subscribe({
      next: () => this.reload(),
      error: () => this.reload(),
    });
  }

  private reload(): void {
    this.dataService.getComplaintsWithContext().subscribe({
      next: (complaints) => {
        this.complaints = complaints;
      },
      error: () => {
        this.complaints = [];
      },
    });
  }
}
