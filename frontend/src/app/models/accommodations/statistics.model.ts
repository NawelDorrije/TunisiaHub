export interface AccommodationStats {
  totalAccommodations: number;
  totalReservations: number;
  totalRevenue: number;
  averageRating: number;
  totalReviews: number;
  accommodationsByType: { [key: string]: number };
  confirmedReservations: number;
  cancelledReservations: number;
  under100: number;
  between100and200: number;
  between200and300: number;
  above300: number;
  capacity1to2: number;
  capacity3to5: number;
  capacity6to10: number;
  capacityAbove10: number;
  topProfitableAccommodations: any[];
  topReservedAccommodations: any[];
}