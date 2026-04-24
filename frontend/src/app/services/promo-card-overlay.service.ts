import { Injectable } from '@angular/core';

export interface ShopData {
  name: string;
  rating?: number;
}

@Injectable({
  providedIn: 'root'
})
export class PromoCardOverlayService {

  constructor() { }

  async createCompositedImage(imageUrl: string, shopData: ShopData): Promise<string> {
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
        // Set canvas size to match image
        canvas.width = img.width;
        canvas.height = img.height;
        
        // Draw the main image
        ctx.drawImage(img, 0, 0);
        
        // Add semi-transparent overlay at bottom for text
        const overlayHeight = canvas.height * 0.15;
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.fillRect(0, canvas.height - overlayHeight, canvas.width, overlayHeight);
        
        // Add shop name
        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 24px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(shopData.name, canvas.width / 2, canvas.height - overlayHeight / 2 + 8);
        
        // Add rating if available
        if (shopData.rating) {
          ctx.font = '18px Arial';
          const ratingText = `⭐ ${shopData.rating.toFixed(1)}`;
          ctx.fillText(ratingText, canvas.width / 2, canvas.height - overlayHeight / 2 + 35);
        }
        
        // Add QR code placeholder in corner
        this.addQRCodePlaceholder(ctx, canvas.width - 60, canvas.height - 60);
        
        // Convert to blob and return URL
        canvas.toBlob((blob) => {
          if (blob) {
            const url = URL.createObjectURL(blob);
            resolve(url);
          } else {
            reject(new Error('Could not create blob from canvas'));
          }
        }, 'image/png');
      };
      
      img.onerror = () => {
        reject(new Error('Failed to load image'));
      };
      
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
}
