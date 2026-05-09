import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class PromoCardOverlayService {

  /**
   * Loads the generated image, composites text/rating overlay onto a canvas,
   * and triggers a PNG download. Falls back to opening the URL in a new tab
   * if cross-origin canvas restrictions apply.
   */
  async compositeAndDownload(
    imageUrl: string,
    targetName: string,
    targetId: number,
    targetType: 'shop' | 'product',
    rating?: number
  ): Promise<void> {

    try {
      const img = await this.loadImage(imageUrl);
      const canvas = document.createElement('canvas');
      const W = img.naturalWidth  || 1024;
      const H = img.naturalHeight || 1024;
      canvas.width  = W;
      canvas.height = H;

      const ctx = canvas.getContext('2d')!;

      // 1. Draw the main AI image
      ctx.drawImage(img, 0, 0, W, H);

      // 2. Gradient overlay at the bottom third
      const grad = ctx.createLinearGradient(0, H * 0.6, 0, H);
      grad.addColorStop(0, 'rgba(0,0,0,0)');
      grad.addColorStop(1, 'rgba(0,0,0,0.82)');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, W, H);

      // 3. Shop / product name
      ctx.fillStyle = '#FFFFFF';
      ctx.font = `bold ${Math.floor(W * 0.045)}px 'Segoe UI', Arial, sans-serif`;
      ctx.shadowColor = 'rgba(0,0,0,0.6)';
      ctx.shadowBlur  = 8;
      this.fillTextMultiline(ctx, targetName, 40, H - (rating ? 90 : 55), W - 160, Math.floor(W * 0.048));
      ctx.shadowBlur = 0;

      // 4. Star rating
      if (rating != null && rating > 0) {
        const stars   = Math.round(Math.min(Math.max(rating, 0), 5));
        const starStr = '★'.repeat(stars) + '☆'.repeat(5 - stars);
        ctx.fillStyle = '#FDBB2D';
        ctx.font      = `${Math.floor(W * 0.032)}px Arial`;
        ctx.fillText(starStr, 40, H - 48);
        ctx.fillStyle = 'rgba(255,255,255,0.6)';
        ctx.font      = `${Math.floor(W * 0.022)}px Arial`;
        ctx.fillText(`${rating.toFixed(1)} / 5`, 40 + Math.floor(W * 0.032) * 6.2, H - 48);
      }

      // 5. QR code (loads from Google Charts – silent fail if blocked)
      const shopUrl = `${window.location.origin}/${targetType === 'shop' ? 'shops' : 'products/shop'}/${targetId}`;
      try {
        const qrImg = await this.loadImage(
          `https://chart.googleapis.com/chart?chs=90x90&cht=qr&choe=UTF-8&chl=${encodeURIComponent(shopUrl)}`
        );
        const qrSize = Math.floor(W * 0.1);
        const pad    = 18;
        // White background behind QR
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(W - qrSize - pad - 6, H - qrSize - pad - 6, qrSize + 12, qrSize + 12);
        ctx.drawImage(qrImg, W - qrSize - pad, H - qrSize - pad, qrSize, qrSize);
      } catch { /* QR blocked by CORS — skip silently */ }

      // 6. Trigger download
      canvas.toBlob(blob => {
        if (!blob) { window.open(imageUrl, '_blank'); return; }
        const url  = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.download = `${targetName.replace(/\s+/g, '_')}_promo.png`;
        link.href = url;
        link.click();
        setTimeout(() => URL.revokeObjectURL(url), 1500);
      }, 'image/png');

    } catch {
      // CORS fallback: just open the raw image
      window.open(imageUrl, '_blank');
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────
  private loadImage(src: string): Promise<HTMLImageElement> {
    return new Promise((resolve, reject) => {
      const img   = new Image();
      img.crossOrigin = 'anonymous';
      img.onload  = () => resolve(img);
      img.onerror = () => reject(new Error(`Image load failed: ${src}`));
      img.src     = src;
    });
  }

  /** Wraps long text across multiple lines on the canvas */
  private fillTextMultiline(
    ctx: CanvasRenderingContext2D,
    text: string,
    x: number,
    y: number,
    maxWidth: number,
    lineHeight: number
  ): void {
    const words = text.split(' ');
    let line    = '';

    for (const word of words) {
      const test = line ? `${line} ${word}` : word;
      if (ctx.measureText(test).width > maxWidth && line) {
        ctx.fillText(line, x, y);
        line = word;
        y   += lineHeight;
      } else {
        line = test;
      }
    }
    if (line) ctx.fillText(line, x, y);
  }
}
