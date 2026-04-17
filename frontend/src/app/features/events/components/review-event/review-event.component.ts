import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-review-event',
  templateUrl: './review-event.component.html',
  styleUrls: ['./review-event.component.css']
})
export class ReviewEventComponent implements OnInit {

  reservationId!: number;

  comment = '';
  rating = 0;

  hoverRating = 0;

  successMessage = '';
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.reservationId = Number(this.route.snapshot.paramMap.get('reservationId'));
  }

  setRating(star: number) {
    this.rating = star;
  }

  submitReview() {

    const userId = this.authService.getUserId();

    if (!this.comment.trim()) {
      this.errorMessage = "Comment cannot be empty";
      return;
    }

    if (this.rating === 0) {
      this.errorMessage = "Please select rating";
      return;
    }

    this.eventService.addReview(
      userId,
      this.reservationId,
      this.comment,
      this.rating
    ).subscribe({

      next: () => {
        this.successMessage = "Review added successfully!";
        this.errorMessage = '';

        setTimeout(() => {
          this.router.navigate(['/events/user/events']);
        }, 1500);
      },

      error: (err) => {
        this.errorMessage = err.error?.message || "Error";
      }
    });
  }
}