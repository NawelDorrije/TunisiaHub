import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../../../models/souvenirs-shops/product.model';
import { Review, CreateReviewRequest, UpdateReviewRequest, ReviewEligibilityResponse } from '../../../../../models/souvenirs-shops/review.model';
import { ProductService } from '../../../../../services/souvenirs-shops/product.service';
import { CartService } from '../../../../../services/souvenirs-shops/cart.service';
import { ReviewService } from '../../../../../services/souvenirs-shops/review.service';
import { OrderService } from '../../../../../services/souvenirs-shops/order.service';
import { AuthService } from '../../../../auth/services/auth.service';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  reviews: Review[] = [];
  isLoading = true;
  errorMessage = '';
  selectedImageUrl: string | null = null;

  quantity = 1;
  cartMessage = '';

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
    private productService: ProductService,
    private cartService: CartService,
    private reviewService: ReviewService,
    private orderService: OrderService,
    public authService: AuthService
  ) {}

  get canManageProducts(): boolean {
    return this.authService.isAdmin() || this.authService.isOwner();
  }

  get canAddToCart(): boolean {
    return this.authService.isClient() && !!this.product && this.product.stockQuantity > 0;
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id || isNaN(id)) {
      this.errorMessage = 'Invalid product id.';
      this.isLoading = false;
      return;
    }

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        this.product = product;        this.checkReviewPermissions(id);        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load product details. Please try again later.';
        this.isLoading = false;
      }
    });
  }

  editProduct(): void {
    if (this.product?.id) {
      this.router.navigate(['/products', this.product.id, 'edit']);
    }
  }

  addToCart(): void {
    if (!this.product || !this.canAddToCart) return;

    this.cartService.addProduct(this.product, this.quantity || 1);
    this.cartMessage = 'Added to cart successfully.';
    setTimeout(() => this.cartMessage = '', 3000);
  }

  backToProductList(event?: Event): void {
    event?.preventDefault();
    if (this.product?.shop?.id) {
      this.router.navigate(['/products', 'shop', this.product.shop.id]);
      return;
    }
    this.router.navigate(['/products']);
  }

  openImageViewer(url?: string): void {
    if (!url) {
      return;
    }
    this.selectedImageUrl = url;
  }

  closeImageViewer(): void {
    this.selectedImageUrl = null;
  }
  increaseQty(): void {
  if (this.quantity < (this.product?.stockQuantity || 1)) {
    this.quantity++;
  }
}

decreaseQty(): void {
  if (this.quantity > 1) {
    this.quantity--;
  }
}

  // Review methods
  private checkReviewPermissions(productId: number): void {
    // For owners/admins, load reviews directly (read-only)
    if (this.authService.isAdmin() || this.authService.isOwner()) {
      this.canSeeReviews = true;
      this.canWriteReview = false;
      this.loadReviewsWithEligibility(productId);
      return;
    }

    // For clients, use the combined API call
    if (this.authService.isClient()) {
      this.loadReviewsWithEligibility(productId);
    } else {
      this.canSeeReviews = false;
      this.canWriteReview = false;
    }
  }

  private loadReviewsWithEligibility(productId: number): void {
    this.reviewService.getReviewsWithEligibilityByProduct(productId).subscribe({
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

  openReviewForm(): void {
    this.showReviewForm = true;
  }

  closeReviewForm(): void {
    this.showReviewForm = false;
    this.newReviewRating = 5;
    this.newReviewComment = '';
  }

  submitReview(): void {
    if (!this.product?.id || this.isSubmittingReview || !this.canWriteReview || this.hasUserReview) {
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

    this.reviewService.createReviewForProduct(this.product.id, reviewRequest).subscribe({
      next: (review) => {
        this.reviews.unshift(review);
        this.closeReviewForm();
        this.isSubmittingReview = false;

        // Reload product to get updated averageRating
        if (this.product?.id) {
          this.productService.getProductById(this.product.id).subscribe({
            next: (updatedProduct) => {
              this.product = updatedProduct;
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
        const index = this.reviews.findIndex(r => r.id === updatedReview.id);
        if (index !== -1) {
          this.reviews[index] = updatedReview;
        }
        this.cancelEditReview();
        this.isUpdatingReview = false;

        // Reload product to get updated averageRating
        if (this.product?.id) {
          this.productService.getProductById(this.product.id).subscribe({
            next: (updatedProduct) => {
              this.product = updatedProduct;
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
        this.reviews = this.reviews.filter(r => r.id !== review.id);
        this.userReview = null;
        this.hasUserReview = false;

        // Reload product to get updated averageRating
        if (this.product?.id) {
          this.productService.getProductById(this.product.id).subscribe({
            next: (updatedProduct) => {
              this.product = updatedProduct;
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
}
