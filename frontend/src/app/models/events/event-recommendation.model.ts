export interface EventRecommendationRequest {
  date: string;
  time: string;
  type: string;
}

export interface RecommendedSlot {
  date: string;
  time: string;
  score: number;
  engagement: string;
  reason: string;
  improvementPercent: number;
}

export interface EventRecommendationResponse {
  originalScore: number;
  originalEngagement: string;
  originalReason: string;
  recommendations: RecommendedSlot[];
}
