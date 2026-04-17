export interface Spot {
  id?: number;
  campingId: number;
  campingName?: string;
  name: string;
  type: 'TENT' | 'CARAVAN' | 'BUNGALOW' | 'TREEHOUSE' | 'GLAMPING' | 'MOBILE_HOME';
  capacity: number;
  area?: number;
  description?: string;
  basePrice: number;
  status: 'LIBRE' | 'OCCUPE' | 'MAINTENANCE' | 'HORS_SERVICE';
  positionX?: number;
  positionY?: number;
  viewType?: 'SEA' | 'LAKE' | 'MOUNTAIN' | 'FOREST' | 'STANDARD';
  hasShade: boolean;
  accessibleForDisabled: boolean;
  active: boolean;
  photos?: string[];
  createdAt?: string;
   dynamicPrice?: number;      // AI-computed price (may be null if not yet priced)
  lastPricedAt?: string;
   multiplier?: number;

}
