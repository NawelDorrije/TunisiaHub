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
}