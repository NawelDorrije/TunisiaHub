import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../../../models/souvenirs-shops/product.model';
import { ProductService } from '../../../../../services/souvenirs-shops/product.service';
import { ImageService } from '../../../../../services/image.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit {
  product: Product = {
    name: '',
    description: '',
    price: 0,
    stockQuantity: 0
  };

  shopId: number | null = null;
  isEditMode = false;
  isLoading = false;
  errorMessage = '';
  selectedFile: File | null = null;
  photoUrl = '';
  isUploadingImage = false;
  isGeneratingDescription = false;
  uploadError = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private imageService: ImageService
  ) {}

  ngOnInit(): void {
    const shopParam = this.route.snapshot.paramMap.get('shopId');
    const idParam = this.route.snapshot.paramMap.get('id');

    if (shopParam) {
      this.shopId = Number(shopParam);
    }

    if (idParam) {
      this.isEditMode = true;
      const id = Number(idParam);
      if (!isNaN(id)) {
        this.loadProduct(id);
      } else {
        this.errorMessage = 'Invalid product id.';
      }
    }
  }

  private loadProduct(id: number): void {
    this.isLoading = true;
    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;
        this.shopId = product.shop?.id ?? this.shopId;
        this.photoUrl = product.photoUrl ?? '';
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load product data.';
        this.isLoading = false;
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadError = '';
    }
  }

  uploadProductImage(): void {
    if (!this.selectedFile) {
      return;
    }
    this.generateProductDescription();
  }

  private generateProductDescription(): void {
    if (!this.selectedFile) {
      return;
    }

    this.isUploadingImage = true;
    this.isGeneratingDescription = true;
    this.uploadError = '';

    // Pass optional fields to help with description generation
    this.imageService.describeProductImage(
      this.selectedFile,
      this.product.name || undefined,
      this.product.shop?.name || undefined,
      this.product.price || undefined
    ).subscribe({
      next: (response) => {
        // Always fill both from the response
        this.product.photoUrl = response.imageUrl;
        this.photoUrl = response.imageUrl;
        this.product.description = response.suggestedDescription;
        this.selectedFile = null;
      },
      error: (err) => {
        console.error(err);
        this.uploadError = 'Failed to process image. Please try again.';
        // Still allow manual upload fallback if needed
      },
      complete: () => {
        this.isUploadingImage = false;
        this.isGeneratingDescription = false;
      }
    });
  }

  save(form: NgForm): void {
    if (form.invalid) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    const resolvedShopId = this.shopId ?? this.product.shop?.id;
    if (!resolvedShopId) {
      this.errorMessage = 'Shop ID is required.';
      return;
    }

    const payload: any = {
      ...this.product,
      shop: {
        id: resolvedShopId
      },
      photoUrl: this.product.photoUrl
    };

    if (this.isEditMode && this.product.id) {
      payload.id = this.product.id;
      this.productService.updateProduct(payload).subscribe({
        next: (updated) => {
          this.router.navigate(['/products', updated.id]);
        },
        error: () => {
          this.errorMessage = 'Failed to update product. Please try again.';
        }
      });
      return;
    }

    this.productService.addProduct(payload).subscribe({
      next: (created) => {
        this.router.navigate(['/products', created.id]);
      },
      error: () => {
        this.errorMessage = 'Failed to create product. Please try again.';
      }
    });
  }

  cancel(): void {
    if (this.isEditMode && this.product.id) {
      this.router.navigate(['/products', this.product.id]);
      return;
    }

    if (this.shopId !== null) {
      this.router.navigate(['/products', 'shop', this.shopId]);
      return;
    }

    this.router.navigate(['/products']);
  }
}



