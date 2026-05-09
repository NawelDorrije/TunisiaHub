export interface Camping {
<<<<<<< HEAD
=======
  id?: number;
  name: string;
  address: string;
  governorate: string;
  latitude: number;
  longitude: number;
  averageRating?: number;
  numberOfSpots?: number;
  maxCapacity: number;
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
  rules?: string;
  checkInTime?: string;
  checkOutTime?: string;
  ownerId: number;
  ownerName?: string;

  description?: string;
  startDate?: string;
  endDate?: string;
  photos?: string[];
  createdAt?: string;
  updatedAt?: string;
>>>>>>> origin/feature/integrated-app-event
}
