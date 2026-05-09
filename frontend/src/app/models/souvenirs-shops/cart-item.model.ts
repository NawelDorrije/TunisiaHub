import { Product } from './product.model';
import { Shop } from './shop.model';

export interface CartItem {
  product: Product;
  shop: Partial<Shop>;
  quantity: number;
}
