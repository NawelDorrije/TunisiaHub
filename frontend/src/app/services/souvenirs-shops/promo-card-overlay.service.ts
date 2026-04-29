import { Injectable } from '@angular/core';

export interface ShopData {
  id?: number;
  name?: string;
  rating?: number;
  city?: string;
  category?: string;
  photoUrl?: string;
  products?: any[];
}

@Injectable({
  providedIn: 'root'
})
export class PromoCardOverlayService {

  /**
   * Creates a beautiful promotional overlay on the AI-generated image
   * and returns a blob URL for preview
   */
  async createCompositedImage(
    imageUrl: string,
    shopData: ShopData,
    tagline: string = 'Discover the magic of authentic Tunisian craftsmanship',
    ctaText?: string
  ): Promise<string> {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d', { alpha: true });

      if (!ctx) {
        reject(new Error('Could not get canvas 2D context'));
        return;
      }

      const img = new Image();
      img.crossOrigin = 'anonymous';

      img.onload = () => {
        canvas.width = img.width;
        canvas.height = img.height;
        const W = canvas.width;
        const H = canvas.height;

        // 1. Draw the base AI image
        ctx.drawImage(img, 0, 0, W, H);

        // 2. Top "EXCLUSIVE OFFER" badge
        this.drawExclusiveBadge(ctx, W, H);

        // 3. Main Shop Name
        this.drawHeadline(ctx, shopData.name || 'TunisiaHub Shop', W, H);

        // 4. Tagline
        this.drawTagline(ctx, tagline, W, H);

        // 5. Bottom dark overlay + info
        this.drawBottomOverlay(ctx, shopData, ctaText || 'Explore Collection • Shop Now', W, H);

        // 6. QR Code (placeholder to avoid CORS issues)
        this.drawQRPlaceholder(ctx, W, H);

        // Convert canvas to blob URL
        canvas.toBlob((blob) => {
          if (blob) {
            resolve(URL.createObjectURL(blob));
          } else {
            reject(new Error('Failed to create image blob'));
          }
        }, 'image/png', 0.95);
      };

      img.onerror = () => reject(new Error(`Failed to load image: ${imageUrl}`));
      img.src = imageUrl;
    });
  }

  /** Download the composited image */
  downloadImage(imageUrl: string, filename: string = 'promotion.png'): void {
    const link = document.createElement('a');
    link.href = imageUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  // ==================== PRIVATE HELPERS ====================

  private drawExclusiveBadge(ctx: CanvasRenderingContext2D, W: number, H: number): void {
    const badgeHeight = H * 0.085;
    const badgeY = H * 0.035;
    const badgeWidth = W * 0.58;
    const badgeX = (W - badgeWidth) / 2;

    // Gradient background
    const grad = ctx.createLinearGradient(0, badgeY, 0, badgeY + badgeHeight);
    grad.addColorStop(0, 'rgba(232, 67, 147, 0.95)');
    grad.addColorStop(1, 'rgba(253, 203, 110, 0.95)');

    this.roundRect(ctx, badgeX, badgeY, badgeWidth, badgeHeight, badgeHeight / 2);
    ctx.fillStyle = grad;
    ctx.fill();

    // Text
    ctx.fillStyle = '#ffffff';
    ctx.font = `bold ${H * 0.033}px Arial`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.shadowColor = 'rgba(0,0,0,0.4)';
    ctx.shadowBlur = 6;
    ctx.fillText('✨ EXCLUSIVE OFFER ✨', W / 2, badgeY + badgeHeight / 2);
    ctx.shadowBlur = 0;
  }

  private drawHeadline(ctx: CanvasRenderingContext2D, shopName: string, W: number, H: number): void {
    const y = H * 0.22;
    ctx.fillStyle = '#ffffff';
    ctx.font = `bold ${H * 0.058}px Arial`;
    ctx.textAlign = 'center';
    ctx.shadowColor = 'rgba(0,0,0,0.7)';
    ctx.shadowBlur = 10;
    this.wrapText(ctx, shopName.toUpperCase(), W / 2, y, W * 0.88, H * 0.072);
    ctx.shadowBlur = 0;
  }

  private drawTagline(ctx: CanvasRenderingContext2D, tagline: string, W: number, H: number): void {
    const y = H * 0.33;
    ctx.fillStyle = 'rgba(255,255,255,0.95)';
    ctx.font = `${H * 0.033}px Arial`;
    ctx.textAlign = 'center';
    this.wrapText(ctx, tagline, W / 2, y, W * 0.82, H * 0.045);
  }

  private drawBottomOverlay(
    ctx: CanvasRenderingContext2D,
    shopData: ShopData,
    ctaText: string,
    W: number,
    H: number
  ): void {
    const overlayHeight = H * 0.34;
    const overlayY = H - overlayHeight;

    const grad = ctx.createLinearGradient(0, overlayY, 0, H);
    grad.addColorStop(0, 'rgba(0,0,0,0)');
    grad.addColorStop(0.25, 'rgba(0,0,0,0.75)');
    grad.addColorStop(1, 'rgba(0,0,0,0.94)');

    ctx.fillStyle = grad;
    ctx.fillRect(0, overlayY, W, overlayHeight);

    // Location & Category
    const infoY = overlayY + overlayHeight * 0.24;
    ctx.fillStyle = '#fdcb6e';
    ctx.font = `bold ${H * 0.029}px Arial`;
    const cityText = shopData.city ? `📍 ${shopData.city}, Tunisia` : '🇹🇳 Authentic Tunisian Craft';
    const catText = shopData.category ? ` • ${this.capitalize(shopData.category)}` : '';
    ctx.textAlign = 'center';
    ctx.fillText(cityText + catText, W / 2, infoY);

    // Rating
    if (shopData.rating && shopData.rating > 0) {
      const ratingY = infoY + H * 0.06;
      ctx.fillStyle = '#f1c40f';
      ctx.font = `${H * 0.034}px Arial`;
      const stars = '★'.repeat(Math.round(shopData.rating)) + '☆'.repeat(5 - Math.round(shopData.rating));
      ctx.fillText(`${stars} ${shopData.rating.toFixed(1)}/5`, W / 2, ratingY);
    }

    // CTA Button
    const btnWidth = W * 0.58;
    const btnHeight = H * 0.068;
    const btnX = (W - btnWidth) / 2;
    const btnY = H - H * 0.095;

    const btnGrad = ctx.createLinearGradient(btnX, btnY, btnX, btnY + btnHeight);
    btnGrad.addColorStop(0, '#e84393');
    btnGrad.addColorStop(1, '#d63031');

    this.roundRect(ctx, btnX, btnY, btnWidth, btnHeight, btnHeight / 3);
    ctx.fillStyle = btnGrad;
    ctx.fill();

    ctx.strokeStyle = 'rgba(255,255,255,0.4)';
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.fillStyle = '#ffffff';
    ctx.font = `bold ${H * 0.032}px Arial`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText(ctaText, W / 2, btnY + btnHeight / 2);
  }

  private drawQRPlaceholder(ctx: CanvasRenderingContext2D, W: number, H: number): void {
    const size = 68;
    const x = W - size - 35;
    const y = H - size - 35;

    // White background
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(x - 4, y - 4, size + 8, size + 8);

    // Fake QR pattern
    ctx.fillStyle = '#111111';
    const block = size / 10;
    for (let i = 0; i < 10; i++) {
      for (let j = 0; j < 10; j++) {
        if ((i + j) % 2 === 0 || Math.random() > 0.4) {
          ctx.fillRect(x + i * block, y + j * block, block - 1, block - 1);
        }
      }
    }
  }

  // ==================== CANVAS HELPERS ====================

  private roundRect(
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    w: number,
    h: number,
    r: number
  ): void {
    ctx.beginPath();
    ctx.moveTo(x + r, y);
    ctx.lineTo(x + w - r, y);
    ctx.quadraticCurveTo(x + w, y, x + w, y + r);
    ctx.lineTo(x + w, y + h - r);
    ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h);
    ctx.lineTo(x + r, y + h);
    ctx.quadraticCurveTo(x, y + h, x, y + h - r);
    ctx.lineTo(x, y + r);
    ctx.quadraticCurveTo(x, y, x + r, y);
    ctx.closePath();
  }

  private wrapText(
    ctx: CanvasRenderingContext2D,
    text: string,
    x: number,
    y: number,
    maxWidth: number,
    lineHeight: number
  ): void {
    const words = text.split(' ');
    let line = '';
    let currentY = y;

    for (let n = 0; n < words.length; n++) {
      const testLine = line + words[n] + ' ';
      const metrics = ctx.measureText(testLine);
      if (metrics.width > maxWidth && n > 0) {
        ctx.fillText(line.trim(), x, currentY);
        line = words[n] + ' ';
        currentY += lineHeight;
      } else {
        line = testLine;
      }
    }
    ctx.fillText(line.trim(), x, currentY);
  }

  private capitalize(s?: string): string {
    return s ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : '';
  }
}