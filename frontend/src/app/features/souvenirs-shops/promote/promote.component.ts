import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PromotionService, GeneratePromotionRequest } from '../../../services/promotion.service';
import { PromoCardOverlayService, ShopData } from '../../../services/promo-card-overlay.service';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-promote',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './promote.component.html',
  styleUrls: ['./promote.component.css']
})
export class PromoteComponent implements OnInit {
  @Input() isVisible = false;
  @Input() targetType: 'shop' | 'product' = 'shop';
  @Input() targetId?: number;
  @Input() shopData?: ShopData;
  @Input() ownerShops: any[] = [];
  @Output() close = new EventEmitter<void>();

  activeTab: 'caption' | 'image' = 'caption';

  // Selection state
  selectedShopId?: number;
  selectedProductId?: number | null = null;
  selectedShop?: any;
  selectedProduct?: any;

  // Available products for the selected shop
  availableProducts: any[] = [];

  // Enhanced caption options
  captionStyle = 'Professional';
  captionLength = 'Medium';
  captionEmojis = true;

  // Enhanced image options
  imageSize = '1080x1080';
  imageQuality = 'Standard';
  imageStyle = 'Photographic';

  // Caption form data
  captionLanguage = 'English';
  captionPlatform = 'Instagram';
  captionTone = 'Friendly';

  // Image form data
  imageMood = 'Luxury';
  imageColorTheme = 'Warm';
  imageFocus = 'Full shop atmosphere';

  // Generated data
  generatedCaption = '';
  generatedImageUrl = '';
  compositedImageUrl = '';

  // Loading states
  isGeneratingCaption = false;
  isGeneratingImage = false;
  isCompositingImage = false;

  // UI states
  copyButtonText = 'Copy';
  showCopyFeedback = false;

  // Dropdown options
  languages = ['Arabic', 'French', 'English'];
  platforms = ['Instagram', 'Facebook', 'WhatsApp'];
  tones = ['Luxurious', 'Friendly', 'Urgent', 'Professional', 'Casual', 'Exciting'];
  styles = ['Professional', 'Casual', 'Creative', 'Formal', 'Playful'];
  lengths = ['Short', 'Medium', 'Long'];
  moods = ['Luxury', 'Bold', 'Minimal', 'Artistic', 'Modern', 'Vintage'];
  colorThemes = ['Warm', 'Dark', 'Light', 'Vibrant', 'Pastel', 'Monochrome'];
  focuses = ['Full shop atmosphere', 'Specific product closeup', 'Lifestyle scene', 'Detail shot'];
  imageSizes = ['1080x1080', '1920x1080', '1080x1920', '1600x1600'];
  qualities = ['Standard', 'High', 'Ultra'];
  imageStyles = ['Photographic', 'Artistic', 'Illustration', '3D Render', 'Minimalist'];

  constructor(
    private promotionService: PromotionService,
    private promoCardOverlayService: PromoCardOverlayService
  ) {}

  ngOnInit(): void {
    if (this.ownerShops && this.ownerShops.length > 0) {
      this.selectedShopId = this.ownerShops[0].id;
      this.selectedShop = this.ownerShops[0];
      this.onShopChange(); // Load products for the first shop
    } else if (this.shopData) {
      this.selectedShop = this.shopData;
      this.selectedShopId = this.shopData.id;
      this.onShopChange();
    }
  }

  closePanel(): void {
    this.isVisible = false;
    this.close.emit();
  }

  // When shop is changed → load its products
  onShopChange(): void {
    this.selectedShop = this.ownerShops?.find(shop => shop.id == this.selectedShopId);

    // Reset product selection
    this.selectedProductId = null;
    this.selectedProduct = undefined;

    // Load products for selected shop
    if (this.selectedShop?.products && Array.isArray(this.selectedShop.products)) {
      this.availableProducts = [...this.selectedShop.products];
    } else {
      this.availableProducts = [];
    }
  }

  // When product is selected
  onProductChange(): void {
    if (this.selectedProductId) {
      this.selectedProduct = this.availableProducts.find(
        (product: any) => product.id == this.selectedProductId
      );
      this.imageFocus = 'Specific product closeup'; // Auto-set better focus for products
    } else {
      this.selectedProduct = undefined;
      this.imageFocus = 'Full shop atmosphere';
    }
  }

  // Getter for template
  get productOptions(): any[] {
    return this.availableProducts;
  }

  // Caption Generation
  generateCaption(): void {
    if (this.isGeneratingCaption) return;

    const shopId = this.selectedShopId || (this.targetType === 'shop' ? this.targetId : undefined);
    const productId = this.selectedProductId || (this.targetType === 'product' ? this.targetId : undefined);

    if (!shopId) {
      alert('Please select a shop to promote');
      return;
    }

    const request: GeneratePromotionRequest = {
      shopId,
      productId,
      language: this.captionLanguage,
      platform: this.captionPlatform,
      tone: this.captionTone,
      colorTheme: this.captionStyle,
      mood: this.captionLength
    };

    this.isGeneratingCaption = true;
    this.generatedCaption = '';

    this.promotionService.generateCaption(request)
      .pipe(catchError(error => {
        console.error('Error generating caption:', error);
        this.generatedCaption = 'Failed to generate caption. Please try again.';
        this.isGeneratingCaption = false;
        return of(null);
      }))
      .subscribe((response: any) => {
        if (response?.caption) {
          this.generatedCaption = response.caption;
        }
        this.isGeneratingCaption = false;
      });
  }

  copyCaption(): void {
    if (!this.generatedCaption) return;

    navigator.clipboard.writeText(this.generatedCaption).then(() => {
      this.copyButtonText = '✓ Copied!';
      this.showCopyFeedback = true;
      setTimeout(() => {
        this.copyButtonText = 'Copy';
        this.showCopyFeedback = false;
      }, 2000);
    });
  }

  regenerateCaption(): void {
    this.generateCaption();
  }

  // Image Generation
  generateImage(): void {
    if (this.isGeneratingImage) return;

    const shopId = this.selectedShopId || (this.targetType === 'shop' ? this.targetId : undefined);
    const productId = this.selectedProductId || (this.targetType === 'product' ? this.targetId : undefined);

    if (!shopId) {
      alert('Please select a shop to promote');
      return;
    }

    const request: GeneratePromotionRequest = {
      shopId,
      productId,
      language: this.captionLanguage,
      platform: this.captionPlatform,
      tone: this.captionTone,
      colorTheme: this.imageColorTheme,
      mood: this.imageMood,
      focus: this.imageFocus
    };

    this.isGeneratingImage = true;
    this.generatedImageUrl = '';
    this.compositedImageUrl = '';

    this.promotionService.generateImage(request)
      .pipe(catchError(error => {
        console.error('Error generating image:', error);
        this.isGeneratingImage = false;
        return of(null);
      }))
      .subscribe((response: any) => {
        if (response?.imageUrl) {
          this.generatedImageUrl = response.imageUrl;
          this.createCompositedImage();
        }
        this.isGeneratingImage = false;
      });
  }

  private async createCompositedImage(): Promise<void> {
    if (!this.generatedImageUrl || !this.shopData) {
      this.compositedImageUrl = this.generatedImageUrl;
      return;
    }

    this.isCompositingImage = true;

    const taglines = [
      'Discover the magic of authentic Tunisian craftsmanship',
      'Handmade with love, just for you',
      'Exclusive treasures from the heart of Tunisia',
      'Support local artisans • Shop authentic',
      'Bring home a piece of Tunisian heritage'
    ];
    const tagline = taglines[Math.floor(Math.random() * taglines.length)];

    const ctaText = this.selectedProduct
      ? `${this.selectedProduct.name} • Shop Now`
      : 'Explore Collection • Shop Now';

    try {
      this.compositedImageUrl = await this.promoCardOverlayService.createCompositedImage(
        this.generatedImageUrl,
        this.shopData,
        tagline,
        ctaText
      );
    } catch (error) {
      console.error('Error compositing image:', error);
      this.compositedImageUrl = this.generatedImageUrl;
    } finally {
      this.isCompositingImage = false;
    }
  }

  regenerateImage(): void {
    this.generateImage();
  }

  downloadImage(): void {
    const imageUrl = this.compositedImageUrl || this.generatedImageUrl;
    if (imageUrl) {
      const filename = `${this.shopData?.name || 'promotion'}-${Date.now()}.png`;
      this.promoCardOverlayService.downloadImage(imageUrl, filename);
    }
  }

  switchTab(tab: 'caption' | 'image'): void {
    this.activeTab = tab;
  }

  shareCaption(): void {
    const text = encodeURIComponent(this.generatedCaption);
    const url = encodeURIComponent(window.location.origin);
    let shareUrl = '';

    switch (this.captionPlatform) {
      case 'Instagram':
        this.copyCaption();
        alert('Caption copied! Open Instagram to paste it.');
        return;
      case 'Facebook':
        shareUrl = `https://www.facebook.com/sharer/sharer.php?quote=${text}&u=${url}`;
        break;
      case 'WhatsApp':
        shareUrl = `https://wa.me/?text=${text}`;
        break;
      default:
        shareUrl = `https://www.facebook.com/sharer/sharer.php?quote=${text}&u=${url}`;
    }

    window.open(shareUrl, '_blank', 'width=600,height=400');
  }
}