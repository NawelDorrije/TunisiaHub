import { AfterViewInit, Component, ElementRef, Inject, OnDestroy, OnInit, PLATFORM_ID, ViewChild } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Shop } from '../../../../../models/souvenirs-shops/shop.model';
import { ShopService } from '../../../../../services/souvenirs-shops/shop.service';
import { ImageService } from '../../../../../services/image.service';

@Component({
  selector: 'app-shop-form',
  templateUrl: './shop-form.component.html',
  styleUrls: ['./shop-form.component.css']
})
export class ShopFormComponent implements OnInit, AfterViewInit, OnDestroy {
  shop: Shop = {
    name: '',
    description: ''
  };
  isEditMode = false;
  isLoading = false;
  errorMessage = '';
  selectedFile: File | null = null;
  photoUrl = '';
  isUploadingImage = false;
  uploadError = '';
  isReverseGeocoding = false;
  @ViewChild('mapContainer', { static: false }) mapContainer!: ElementRef<HTMLDivElement>;
  private map: any;
  private marker: any;
  private leaflet: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private shopService: ShopService,
    private imageService: ImageService,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      const id = Number(idParam);
      if (!isNaN(id)) {
        this.loadShop(id);
      } else {
        this.errorMessage = 'Invalid shop id.';
      }
    }
  }

  async ngAfterViewInit(): Promise<void> {
    if (isPlatformBrowser(this.platformId)) {
      await this.initMap();
    }
  }

  ngOnDestroy(): void {
    if (isPlatformBrowser(this.platformId) && this.map) {
      this.map.off();
      this.map.remove();
    }
  }

  private async initMap(): Promise<void> {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    const L = await import('leaflet');
    this.leaflet = L;

    const initialCenter: [number, number] = [
      this.shop.latitude ?? 36.8065,
      this.shop.longitude ?? 10.1815
    ];

    this.map = L.map(this.mapContainer.nativeElement).setView(initialCenter, 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    if (this.shop.latitude != null && this.shop.longitude != null) {
      this.addMarker(initialCenter);
    }

    this.map.on('click', (event: any) => this.onMapClick(event));
  }

  private addMarker(latlng: [number, number]): void {
    if (!this.leaflet || !this.map) {
      return;
    }

    if (this.marker) {
      this.marker.setLatLng(latlng);
    } else {
      this.marker = this.leaflet.marker(latlng).addTo(this.map);
    }
  }

  private async reverseGeocode(lat: number, lng: number): Promise<void> {
    this.isReverseGeocoding = true;
    try {
      const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lng)}&zoom=18&addressdetails=1`;
      const response = await fetch(url, {
        headers: {
          'Accept': 'application/json'
        }
      });
      if (!response.ok) {
        return;
      }
      const data = await response.json();
      const address = data.address ?? {};
      const city = address.city || address.town || address.village || address.county || address.state;
      const road = address.road || address.pedestrian || address.residential || address.neighbourhood || '';
      const houseNumber = address.house_number ? `${address.house_number} ` : '';
      const locationText = [houseNumber + road, address.suburb, address.city_district, address.county, address.state]
        .filter(Boolean)
        .join(', ');

      if (city) {
        this.shop.city = city;
      }
      if (locationText) {
        this.shop.address = locationText;
      } else if (data.display_name) {
        this.shop.address = data.display_name;
      }
    } catch (error) {
      console.warn('Reverse geocode failed', error);
    } finally {
      this.isReverseGeocoding = false;
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.uploadError = '';
    }
  }

  uploadShopImage(): void {
    if (!this.selectedFile) {
      return;
    }

    this.isUploadingImage = true;
    this.uploadError = '';

    this.imageService.uploadShopImage(this.selectedFile).subscribe({
      next: (url) => {
        this.shop.photoUrl = url;
        this.photoUrl = url;
        this.selectedFile = null;
      },
      error: (err) => {
        console.error(err);
        this.uploadError = 'Image upload failed. Please try again.';
      },
      complete: () => {
        this.isUploadingImage = false;
      }
    });
  }

  private updateMapWithShopLocation(): void {
    if (!this.map || this.shop.latitude == null || this.shop.longitude == null) {
      return;
    }

    const latlng: [number, number] = [this.shop.latitude, this.shop.longitude];
    this.map.setView(latlng, 13);
    this.addMarker(latlng);
  }

  private onMapClick(event: any): void {
    const lat = event.latlng.lat;
    const lng = event.latlng.lng;
    this.shop.latitude = Number(lat.toFixed(6));
    this.shop.longitude = Number(lng.toFixed(6));
    this.addMarker([lat, lng]);
    void this.reverseGeocode(lat, lng);
  }

  private loadShop(id: number): void {
    this.isLoading = true;
    this.shopService.getShopById(id).subscribe({
      next: (shop) => {
        this.shop = shop;
        this.photoUrl = shop.photoUrl ?? '';
        this.isLoading = false;
        this.updateMapWithShopLocation();
      },
      error: () => {
        this.errorMessage = 'Unable to load shop data.';
        this.isLoading = false;
      }
    });
  }

  save(form: NgForm): void {
    if (form.invalid) {
      this.errorMessage = 'Please fill in all required fields.';
      return;
    }

    const payload: any = {
      name: this.shop.name,
      description: this.shop.description,
      category: this.shop.category,
      city: this.shop.city,
      address: this.shop.address,
      latitude: this.shop.latitude,
      longitude: this.shop.longitude,
      photoUrl: this.shop.photoUrl,
    };

    if (this.isEditMode && this.shop.id) {
      payload.id = this.shop.id;
      this.shopService.updateShop(payload).subscribe({
        next: (updatedShop) => {
          this.router.navigate(['/shops', updatedShop.id]);
        },
        error: () => {
          this.errorMessage = 'Failed to update shop. Please try again.';
        }
      });
      return;
    }

    this.shopService.addShop(payload).subscribe({
      next: (createdShop) => {
        this.router.navigate(['/shops', createdShop.id]);
      },
      error: () => {
        this.errorMessage = 'Failed to create shop. Please try again.';
      }
    });
  }

  cancel(): void {
    if (this.shop.id) {
      this.router.navigate(['/shops', this.shop.id]);
      return;
    }
    this.router.navigate(['/shops']);
  }
}



