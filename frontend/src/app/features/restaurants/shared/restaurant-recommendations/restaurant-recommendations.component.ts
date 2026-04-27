import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { ApiService } from '../../../../services/api.service';

@Component({
  selector: 'app-restaurant-recommendations',
  templateUrl: './restaurant-recommendations.component.html',
  styleUrls: ['./restaurant-recommendations.component.css']
})
export class RestaurantRecommendationsComponent implements OnInit, OnChanges {
  @Input() allRestaurants: any[] = [];
  @Output() onView = new EventEmitter<any>();
  @Output() onBook = new EventEmitter<any>();

  loading = true;
  recommendationData: any = null;
  error: string | null = null;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.fetchRecommendations();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['allRestaurants'] && this.allRestaurants?.length) {
      // allRestaurants updated - re-run enrichment to merge pictures/address
      this.enrichWithImages();
    }
  }

  fetchRecommendations(): void {
    this.loading = true;
    this.apiService.getRestaurantRecommendations().subscribe({
      next: (response) => {
        console.log('[Recommendations] API response:', response);
        this.recommendationData = response;
        this.enrichWithImages();
        this.loading = false;
      },
      error: (err) => {
        console.error('[Recommendations] Failed to load:', err);
        this.error = "Unable to load personalized recommendations at this time.";
        this.loading = false;
      }
    });
  }

  enrichWithImages(): void {
    if (!this.recommendationData?.restaurants) return;

    // Backend DTO field is 'image'; local restaurant list has 'picture'
    // We prefer the local picture (served correctly), falling back to backend 'image'
    this.recommendationData.restaurants = this.recommendationData.restaurants
      .filter((rec: any) => rec && rec.id)  // Drop any entry with no valid ID
      .map((rec: any) => {
        const fullRes = this.allRestaurants.find(r => r.id === rec.id);
        const resolvedPicture = fullRes?.picture || rec.image || null;
        console.log(`[Recommendations] id=${rec.id} name=${rec.name} localPicture=${fullRes?.picture} backendImage=${rec.image} resolved=${resolvedPicture}`);
        return {
          ...rec,
          picture: resolvedPicture,
          address: fullRes?.address || ''
        };
      });

    console.log('[Recommendations] Final list after enrichment:', this.recommendationData.restaurants);
  }

  getImageUrl(path: string): string {
    return this.apiService.getImageUrl(path);
  }

  onViewClick(restaurant: any): void {
    // Emit the full restaurant object if found, otherwise the recommendation item
    const fullRes = this.allRestaurants.find(r => r.id === restaurant.id) || restaurant;
    this.onView.emit(fullRes);
  }

  onBookClick(restaurant: any): void {
    const fullRes = this.allRestaurants.find(r => r.id === restaurant.id) || restaurant;
    this.onBook.emit(fullRes);
  }
}
