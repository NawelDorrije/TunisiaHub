export interface AiEnhancedRestaurant {
  id: number;
  name: string;
  address: string;
  cuisine: string;
  phoneNum: string;
  picture?: string;
  email: string;
  // AI fields
  matchScore?: number; // 0-100
  matchReason?: string; // Short "Why it matches" text
  suggestedTime?: string; // Optional time slot (e.g., "8:00 PM")
}
