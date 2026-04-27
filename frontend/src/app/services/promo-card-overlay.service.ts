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

  constructor() { }

  async createCompositedImage(
    imageUrl: string,
    shopData: ShopData,
    tagline?: string,
    ctaText?: string
  ): Promise<string> {
    return new Promise((resolve, reject) => {
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');

      if (!ctx) {
        reject(new Error('Could not get canvas context'));
        return;
      }

      const img = new Image();
      img.crossOrigin = 'anonymous';

      img.onload = () => {
        canvas.width = img.width;
        canvas.height = img.height;
        const W = canvas.width;
        const H = canvas.height;

        // Draw the main AI-generated image
        ctx.drawImage(img, 0, 0);

        // ── TOP PROMO BADGE ──
        const badgeHeight = H * 0.08;
        const badgeY = H * 0.04;
        const badgeGrad = ctx.createLinearGradient(0, badgeY, 0, badgeY + badgeHeight);
        badgeGrad.addColorStop(0, 'rgba(232, 67, 147, 0.95)');
        badgeGrad.addColorStop(1, 'rgba(253, 203, 110, 0.95)');
        const badgeWidth = W * 0.55;
        const badgeX = (W - badgeWidth) / 2;
        this.roundRect(ctx, badgeX, badgeY, badgeWidth, badgeHeight, badgeHeight / 2);
        ctx.fillStyle = badgeGrad;
        ctx.fill();
        ctx.fillStyle = '#ffffff';
        ctx.font = `bold ${H * 0.032}px Arial`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.shadowColor = 'rgba(0,0,0,0.3)';
        ctx.shadowBlur = 4;
        ctx.fillText('✨ EXCLUSIVE OFFER ✨', W / 2, badgeY + badgeHeight / 2);
        ctx.shadowBlur = 0;

        // ── MAIN HEADLINE (Shop Name) ──
        const headlineY = H * 0.22;
        const shopName = shopData.name || 'TunisiaHub Shop';
        ctx.shadowColor = 'rgba(0,0,0,0.6)';
        ctx.shadowBlur = 8;
        ctx.fillStyle = '#ffffff';
        ctx.font = `bold ${H * 0.055}px Arial`;
        ctx.textAlign = 'center';
        this.wrapText(ctx, shopName.toUpperCase(), W / 2, headlineY, W * 0.9, H * 0.07);
        ctx.shadowBlur = 0;

        // ── TAGLINE ──
        if (tagline) {
          const taglineY = headlineY + H * 0.11;
          ctx.fillStyle = 'rgba(255,255,255,0.95)';
          ctx.font = `${H * 0.032}px Arial`;
          this.wrapText(ctx, tagline, W / 2, taglineY, W * 0.85, H * 0.042);
        }

        // ── BOTTOM CTA OVERLAY ──
        const ctaHeight = H * 0.32;
        const ctaY = H - ctaHeight;
        const ctaGrad = ctx.createLinearGradient(0, ctaY, 0, H);
        ctaGrad.addColorStop(0, 'rgba(0,0,0,0)');
        ctaGrad.addColorStop(0.3, 'rgba(0,0,0,0.75)');
        ctaGrad.addColorStop(1, 'rgba(0,0,0,0.94)');
        ctx.fillStyle = ctaGrad;
        ctx.fillRect(0, ctaY, W, ctaHeight);

        // Location & Category
        const infoY = ctaY + ctaHeight * 0.22;
        ctx.fillStyle = '#fdcb6e';
        ctx.font = `bold ${H * 0.028}px Arial`;
        const cityText = shopData.city ? `📍 ${shopData.city}, Tunisia` : '🇹🇳 Authentic Tunisian';
        const catText = shopData.category ? ` • ${this.capitalize(shopData.category)}` : '';
        ctx.fillText(cityText + catText, W / 2, infoY);

        // Rating
        if (shopData.rating) {
          const ratingY = infoY + H * 0.055;
          ctx.fillStyle = '#f1c40f';
          ctx.font = `${H * 0.032}px Arial`;
          const stars = '★'.repeat(Math.round(shopData.rating)) + '☆'.repeat(5 - Math.round(shopData.rating));
          ctx.fillText(`${stars} ${shopData.rating.toFixed(1)}/5`, W / 2, ratingY);
        }

        // ── CTA BUTTON ──
        const btnWidth = W * 0.55;
        const btnHeight = H * 0.065;
        const btnX = (W - btnWidth) / 2;
        const btnY = H - H * 0.08;
        const btnGrad = ctx.createLinearGradient(btnX, btnY, btnX, btnY + btnHeight);
        btnGrad.addColorStop(0, '#e84393');
        btnGrad.addColorStop(1, '#d63031');
        this.roundRect(ctx, btnX, btnY - btnHeight, btnWidth, btnHeight, btnHeight / 3);
        ctx.fillStyle = btnGrad;
        ctx.fill();
        ctx.strokeStyle = 'rgba(255,255,255,0.3)';
        ctx.lineWidth = 2;
        ctx.stroke();
        ctx.fillStyle = '#ffffff';
        ctx.font = `bold ${H * 0.03}px Arial`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        const cta = ctaText || 'Shop Now • Limited Time';
        ctx.fillText(cta, W / 2, btnY - btnHeight / 2);

        // QR Code
        this.addQRCodePlaceholder(ctx, W - 70, H - 70);

        // Convert to blob
        canvas.toBlob((blob) => {
          if (blob) {
            resolve(URL.createObjectURL(blob));
          } else {
            reject(new Error('Could not create blob'));
          }
        }, 'image/png');
      };

      img.onerror = () => reject(new Error('Failed to load image'));
      img.src = imageUrl;
    });
  }

  private addQRCodePlaceholder(ctx: CanvasRenderingContext2D, x: number, y: number): void {
    const size = 40;
    
    // White background for QR code
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(x - size/2, y - size/2, size, size);
    
    // Simple QR code pattern placeholder
    ctx.fillStyle = '#000000';
    const blockSize = 4;
    for (let i = 0; i < 10; i++) {
      for (let j = 0; j < 10; j++) {
        if (Math.random() > 0.5) {
          ctx.fillRect(
            x - size/2 + i * blockSize,
            y - size/2 + j * blockSize,
            blockSize - 1,
            blockSize - 1
          );
        }
      }
    }
  }

  downloadImage(imageUrl: string, filename: string = 'promotion-image.png'): void {
    const link = document.createElement('a');
    link.href = imageUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  // Helper: draw rounded rectangle
  private roundRect(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number): void {
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

  // Helper: word wrap text
  private wrapText(ctx: CanvasRenderingContext2D, text: string, x: number, y: number, maxWidth: number, lineHeight: number): void {
    const words = text.split(' ');
    let line = '';
    let currentY = y;
    for (let n = 0; n < words.length; n++) {
      const testLine = line + words[n] + ' ';
      const metrics = ctx.measureText(testLine);
      if (metrics.width > maxWidth && n > 0) {
        ctx.fillText(line, x, currentY);
        line = words[n] + ' ';
        currentY += lineHeight;
      } else {
        line = testLine;
      }
    }
    ctx.fillText(line, x, currentY);
  }

  private capitalize(s?: string): string {
    return s ? s.charAt(0).toUpperCase() + s.slice(1) : '';
  }
}
