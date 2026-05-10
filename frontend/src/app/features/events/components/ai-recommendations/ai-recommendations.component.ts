import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  EventRecommendationResponse,
  RecommendedSlot
} from '../../../../models/events/event-recommendation.model';

@Component({
  selector: 'app-ai-recommendations',
  templateUrl: './ai-recommendations.component.html',
  styleUrls: ['./ai-recommendations.component.css']
})
export class AiRecommendationsComponent {
  @Input() result: EventRecommendationResponse | null = null;
  @Input() loading = false;
  @Input() error = '';
  @Output() useSlot = new EventEmitter<RecommendedSlot>();

  get statusClass(): string {
    const label = (this.result?.originalEngagement ?? '').toLowerCase();
    if (label.includes('high')) return 'status-high';
    if (label.includes('medium')) return 'status-medium';
    return 'status-low';
  }

  getCardTone(index: number): 'best' | 'medium' | 'poor' {
    if (index === 0) return 'best';
    if (index === 1) return 'medium';
    return 'poor';
  }

  onUse(slot: RecommendedSlot): void {
    this.useSlot.emit(slot);
  }
}
