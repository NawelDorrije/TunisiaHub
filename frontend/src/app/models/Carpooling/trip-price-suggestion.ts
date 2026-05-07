export interface TripPriceSuggestion {
  suggestedPrice: number;
  basePrice: number;
  minHistoricalPrice: number;
  maxHistoricalPrice: number;
  similarTripsCount: number;
  holidayAdjusted: boolean;
  holidayName?: string;
  message: string;
}
