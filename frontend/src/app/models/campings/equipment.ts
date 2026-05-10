export interface Equipment {
  id?: number;
  name: string;
  description?: string;
  quantity: number;
  available: boolean;
  condition: 'GOOD' | 'DAMAGED' | 'UNDER_REPAIR';
  spotId: number;
  spotName?: string;
}
