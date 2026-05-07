export interface FeedbackRequest {
  rating: number;
  comment: string;
}

export interface FeedbackResponse {
  id: number;
  rating: number;
  comment: string;
  submittedAt: string;
}