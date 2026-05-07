import { User } from '../users/user';
import { ReviewType } from './review-type.enum';

export interface Review {
  id?: number;
  user?: User;
  reviewType: ReviewType;
  targetId: number;           // product id or shop id
  rating: number;             // 1 to 5
  comment: string;
  createdAt?: string;
  deleted?: boolean;
  deletedAt?: string;
  averageRating?: number;
}

export interface CreateReviewRequest {
  rating: number;
  comment: string;
}

export interface UpdateReviewRequest {
  rating: number;
  comment: string;
}

export interface ReviewEligibilityResponse {
  reviews: Review[];
  canWriteReview: boolean;
  userReview: Review | null;
}