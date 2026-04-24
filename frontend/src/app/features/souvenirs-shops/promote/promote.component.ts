import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PromotionService, GeneratePromotionRequest } from '../../../services/promotion.service';
import { PromoCardOverlayService, ShopData } from '../../../services/promo-card-overlay.service';
import { Observable, Subject, of } from 'rxjs';
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
  @Input() ownerShops?: any[] = [];
  @Output() close = new EventEmitter<void>();

  activeTab: 'caption' | 'image' = 'caption';
  
  // Selection state
  selectedShopId?: number;
  selectedProductId?: number;
  selectedShop?: any;
  selectedProduct?: any;
  
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

  // Options for dropdowns
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
    // Set default selection if ownerShops are provided
    if (this.ownerShops && this.ownerShops.length > 0) {
      this.selectedShopId = this.ownerShops[0].id;
      this.selectedShop = this.ownerShops[0];
    }
  }

  closePanel(): void {
    this.isVisible = false;
    this.close.emit();
  }

  // Shop/Product selection methods
  onShopChange(): void {
    this.selectedShop = this.ownerShops?.find(shop => shop.id === this.selectedShopId);
    this.selectedProductId = undefined;
    this.selectedProduct = undefined;
  }

  onProductChange(): void {
    if (this.selectedShop && this.selectedShop.products) {
      this.selectedProduct = this.selectedShop.products.find((product: any) => product.id === this.selectedProductId);
    }
  }

  get availableProducts(): any[] {
    return this.selectedShop?.products || [];
  }

  // Caption methods
  generateCaption(): void {
    if (this.isGeneratingCaption) return;

    // Use selected shop/product if available, otherwise fall back to targetId
    const shopId = this.selectedShopId || (this.targetType === 'shop' ? this.targetId : undefined);
    const productId = this.selectedProductId || (this.targetType === 'product' ? this.targetId : undefined);

    if (!shopId && !productId) {
      alert('Please select a shop or product to promote');
      return;
    }

    const request: GeneratePromotionRequest = {
      shopId,
      productId,
      language: this.captionLanguage,
      platform: this.captionPlatform,
      tone: this.captionTone,
      colorTheme: this.captionStyle, // Use style as additional context
      mood: this.captionLength // Use length as additional context
    };

    this.isGeneratingCaption = true;
    this.generatedCaption = '';

    this.promotionService.generateCaption(request)
      .pipe(
        catchError(error => {
          console.error('Error generating caption:', error);
          this.generatedCaption = 'Failed to generate caption. Please try again.';
          this.isGeneratingCaption = false;
          return of(null);
        })
      )
      .subscribe((response: any) => {
        if (response) {
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
    }).catch(err => {
      console.error('Failed to copy text:', err);
    });
  }

  regenerateCaption(): void {
    this.generateCaption();
  }

  // Image methods
  generateImage(): void {
    if (this.isGeneratingImage) return;

    // Use selected shop/product if available, otherwise fall back to targetId
    const shopId = this.selectedShopId || (this.targetType === 'shop' ? this.targetId : undefined);
    const productId = this.selectedProductId || (this.targetType === 'product' ? this.targetId : undefined);

    if (!shopId && !productId) {
      alert('Please select a shop or product to promote');
      return;
    }

    const request: GeneratePromotionRequest = {
      shopId,
      productId,
      language: this.captionLanguage, // Include language for better results
      platform: this.captionPlatform, // Include platform for context
      tone: this.captionTone, // Include tone for consistency
      colorTheme: this.imageColorTheme,
      mood: this.imageMood,
      focus: this.imageFocus
    };

    this.isGeneratingImage = true;
    this.generatedImageUrl = '';
    this.compositedImageUrl = '';

    this.promotionService.generateImage(request)
      .pipe(
        catchError(error => {
          console.error('Error generating image:', error);
          this.isGeneratingImage = false;
          return of(null);
        })
      )
      .subscribe((response: any) => {
        if (response) {
          this.generatedImageUrl = response.imageUrl;
          this.createCompositedImage();
        }
        this.isGeneratingImage = false;
      });
  }

  private async createCompositedImage(): Promise<void> {
    if (!this.generatedImageUrl || !this.shopData) return;

    this.isCompositingImage = true;
    
    try {
      this.compositedImageUrl = await this.promoCardOverlayService.createCompositedImage(
        this.generatedImageUrl,
        this.shopData
      );
    } catch (error) {
      console.error('Error creating composited image:', error);
      this.compositedImageUrl = this.generatedImageUrl; // Fallback to original image
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

  // Tab switching
  switchTab(tab: 'caption' | 'image'): void {
    this.activeTab = tab;
  }
}
