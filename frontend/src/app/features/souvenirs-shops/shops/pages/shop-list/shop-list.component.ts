import { Component, OnInit } from '@angular/core';
import { Shop } from '../../../../../models/souvenirs-shops/shop.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';

@Component({
  selector: 'app-shop-list',
  templateUrl: './shop-list.component.html',
  styleUrl: './shop-list.component.css'
})
export class ShopListComponent implements OnInit {
  shops: Shop[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(private shopService: ShopService) {}

  ngOnInit(): void {
    this.shopService.getAllShops().subscribe({
      next: (data: any) => {
        this.shops = data;
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Failed to load shops. Please try again.';
        this.isLoading = false;
      }
    });
  }
}