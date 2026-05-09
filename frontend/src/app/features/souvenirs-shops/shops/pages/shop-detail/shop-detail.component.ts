import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Shop } from '../../../../../models/souvenirs-shops/shop.model';
import { Product } from '../../../../../models/souvenirs-shops/product.model';
import { Review, CreateReviewRequest, UpdateReviewRequest, ReviewEligibilityResponse } from '../../../../../models/souvenirs-shops/review.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';
import { CartService } from '../../../../../services/souvenirs-shops/cart.service';
import { ReviewService } from '../../../../../services/souvenirs-shops/review.service';
import { OrderService } from '../../../../../services/souvenirs-shops/order.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-shop-detail',
  templateUrl: './shop-detail.component.html',
  styleUrls: ['./shop-detail.component.css']
})
export class ShopDetailComponent implements OnInit {
  shop: Shop | null = null;
  reviews: Review[] = [];
  productCount = 0;
  orderCount = 0;
  isLoading = true;
  isProductsLoading = true;
  isDeleting = false;
  errorMessage = '';
  selectedImageUrl: string | null = null;

  // Review permissions
  canSeeReviews = false;
  canWriteReview = false;
  hasUserReview = false;
  userReview: Review | null = null;

  // Review form
  showReviewForm = false;
  newReviewRating = 5;
  newReviewComment = '';
  isSubmittingReview = false;

  // Edit review
  editingReview: Review | null = null;
  editRating = 5;
  editComment = '';
  isUpdatingReview = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private shopService: ShopService,
    private cartService: CartService,
    private reviewService: ReviewService,
    private orderService: OrderService,
    public authService: AuthService
  ) {}

  get canManageShop(): boolean {
    return this.authService.isOwner();
  }

  get canAddToCart(): boolean {
    return this.authService.isClient() || this.authService.isAdmin();
  }

  addToCart(product: Product): void {
    if (!product?.id || !this.canAddToCart || product.stockQuantity <= 0) {
      return;
    }

    this.cartService.addProduct(product, 1);
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id || isNaN(id)) {
      this.errorMessage = 'Invalid shop id.';
      this.isLoading = false;
      return;
    }

    this.shopService.getShopById(id).subscribe({
      next: (shop) => {
        this.shop = shop;
        this.loadProductCount(id);
        if (this.authService.isOwner() || this.authService.isAdmin()) {
          this.loadOrderCount(id);
        }
        this.checkReviewPermissions(id);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load shop details. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  private loadProductCount(shopId: number): void {
    this.shopService.getProductsByShop(shopId).subscribe({
      next: (products) => {
        this.productCount = products.length;
        if (this.shop) {
          this.shop.products = products;
        }
        this.isProductsLoading = false;
      },
      error: () => {
        this.productCount = 0;
        this.isProductsLoading = false;
      }
    });
  }

  private loadShopProducts(shopId: number): void {
    this.shopService.getProductsByShop(shopId).subscribe({
      next: (products) => {
        if (this.shop) {
          this.shop.products = products;
        }
      },
      error: () => {
        // Keep existing product state if loading fails
      }
    });
  }

  private loadOrderCount(shopId: number): void {
    this.shopService.getOrdersByShop(shopId).subscribe({
      next: (orders) => {
        this.orderCount = orders.length;
      },
      error: () => {
        this.orderCount = this.shop?.orders?.length ?? 0;
      }
    });
  }

  private checkReviewPermissions(shopId: number): void {
    if (this.authService.isOwner()) {
      this.canSeeReviews = true;
      this.canWriteReview = false;
      this.loadReviewsWithEligibility(shopId);
      return;
    }

    if (this.authService.isClient() || this.authService.isAdmin()) {
      this.loadReviewsWithEligibility(shopId);
    } else {
      this.canSeeReviews = false;
      this.canWriteReview = false;
    }
  }

  private loadReviewsWithEligibility(shopId: number): void {
    this.reviewService.getReviewsWithEligibilityByShop(shopId).subscribe({
      next: (response: ReviewEligibilityResponse) => {
        this.reviews = response.reviews;
        this.canWriteReview = response.canWriteReview;
        this.canSeeReviews = true; // Reviews are always visible to eligible users
        this.userReview = response.userReview;
        this.hasUserReview = this.userReview !== null;
      },
      error: () => {
        this.reviews = [];
        this.canWriteReview = false;
        this.canSeeReviews = false;
        this.userReview = null;
        this.hasUserReview = false;
      }
    });
  }

  viewOrders(): void {
    this.router.navigate(['/orders']);
  }

  editShop(): void {
    if (this.shop?.id) {
      this.router.navigate(['/shops', this.shop.id, 'edit']);
    }
  }

  deleteShop(): void {
    if (!this.shop?.id || this.isDeleting) {
      return;
    }

    const confirmed = window.confirm('Delete this shop and all its data? This action cannot be undone.');
    if (!confirmed) {
      return;
    }

    this.isDeleting = true;
    this.shopService.deleteShop(this.shop.id).subscribe({
      next: () => {
        this.router.navigate(['/shops']);
      },
      error: () => {
        this.errorMessage = 'Unable to delete shop. Please try again.';
        this.isDeleting = false;
      }
    });
  }

  viewProducts(): void {
    if (this.shop?.id) {
      this.router.navigate(['/products', 'shop', this.shop.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/shops']);
  }

  // Review methods
  openReviewForm(): void {
    this.showReviewForm = true;
  }

  closeReviewForm(): void {
    this.showReviewForm = false;
    this.newReviewRating = 5;
    this.newReviewComment = '';
  }

  submitReview(): void {
    if (!this.shop?.id || this.isSubmittingReview || !this.canWriteReview || this.hasUserReview) {
      return;
    }

    if (!this.newReviewComment.trim()) {
      alert('Please enter a comment for your review.');
      return;
    }

    this.isSubmittingReview = true;
    const reviewRequest: CreateReviewRequest = {
      rating: this.newReviewRating,
      comment: this.newReviewComment.trim()
    };

    this.reviewService.createReviewForShop(this.shop.id, reviewRequest).subscribe({
      next: (review) => {
        this.reviews.unshift(review); // Add to beginning of list
        this.closeReviewForm();
        this.isSubmittingReview = false;

        // Reload shop to get updated averageRating
        if (this.shop?.id) {
          this.shopService.getShopById(this.shop.id).subscribe({
            next: (updatedShop) => {
              this.shop = updatedShop;
            }
          });
        }
      },
      error: (error) => {
        console.error('Error creating review:', error);
        alert('Failed to submit review. Please try again.');
        this.isSubmittingReview = false;
      }
    });
  }

  // Check if current user can edit/delete a review
  canManageReview(review: Review): boolean {
    const currentUserEmail = this.authService.getEmail();
    return currentUserEmail !== null && currentUserEmail === review.user?.email;
  }

  startEditReview(review: Review): void {
    this.editingReview = review;
    this.editRating = review.rating;
    this.editComment = review.comment;
  }

  cancelEditReview(): void {
    this.editingReview = null;
    this.editRating = 5;
    this.editComment = '';
  }

  updateReview(): void {
    if (!this.editingReview?.id || this.isUpdatingReview) {
      return;
    }

    if (!this.editComment.trim()) {
      alert('Please enter a comment for your review.');
      return;
    }

    this.isUpdatingReview = true;
    const updateRequest: UpdateReviewRequest = {
      rating: this.editRating,
      comment: this.editComment.trim()
    };

    this.reviewService.updateReview(this.editingReview.id, updateRequest).subscribe({
      next: (updatedReview) => {
        // Update the review in the list
        const index = this.reviews.findIndex(r => r.id === updatedReview.id);
        if (index !== -1) {
          this.reviews[index] = updatedReview;
        }
        this.cancelEditReview();
        this.isUpdatingReview = false;
        // Reload shop to get updated averageRating
        if (this.shop?.id) {
          this.shopService.getShopById(this.shop.id).subscribe({
            next: (updatedShop) => {
              this.shop = updatedShop;
            }
          });
        }
      },
      error: (error) => {
        console.error('Error updating review:', error);
        alert('Failed to update review. Please try again.');
        this.isUpdatingReview = false;
      }
    });
  }

  deleteReview(review: Review): void {
    if (!review.id) {
      return;
    }

    const confirmed = confirm('Are you sure you want to delete this review? This action cannot be undone.');
    if (!confirmed) {
      return;
    }

    this.reviewService.deleteReview(review.id).subscribe({
      next: () => {
        // Remove from the list
        this.reviews = this.reviews.filter(r => r.id !== review.id);
        // Reload shop to get updated averageRating
        if (this.shop?.id) {
          this.shopService.getShopById(this.shop.id).subscribe({
            next: (updatedShop) => {
              this.shop = updatedShop;
            }
          });
        }
      },
      error: (error) => {
        console.error('Error deleting review:', error);
        alert('Failed to delete review. Please try again.');
      }
    });
  }

  openImageViewer(url: string): void {
    this.selectedImageUrl = url;
  }

  closeImageViewer(): void {
    this.selectedImageUrl = null;
  }
  
}



