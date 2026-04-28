import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { Event } from '../../../../models/events/event.model';
import { isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../auth/services/auth.service';
//import { environment } from '@env/environment'; 
@Component({
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.css']
})
export class EventDetailsComponent implements OnInit {

  event!: Event;
  returnUrl: string | null = null;
  shareFeedback = '';
  isAdminUser = false;

  map: any;
  marker: any;
  L: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (isPlatformBrowser(this.platformId)) {
      this.returnUrl = window.history.state?.returnUrl ?? null;
      this.isAdminUser = this.authService.isAdmin();
    }

    this.eventService.getEventById(id).subscribe({
      next: (data) => {
        this.event = data;

        // charger map après data
        setTimeout(() => {
          if (isPlatformBrowser(this.platformId)) {
            this.loadMap();
          }
        }, 0);
      }
    });
  }

  goBack(): void {
    if (this.returnUrl) {
      this.router.navigateByUrl(this.returnUrl);
      return;
    }

    this.router.navigateByUrl('/events');
  }

  shareOnFacebook(): void {
    if (!this.isAdminUser) {
      return;
    }

    if (!this.event?.id || !isPlatformBrowser(this.platformId)) {
      return;
    }
    if (!this.authService.isLoggedIn()) {
      this.openFacebookSharer();
      this.shareFeedback = 'Connecte-toi pour publier sur la page Facebook. Partage standard ouvert.';
      return;
    }

    this.shareFeedback = 'Publication en cours vers Facebook...';
    this.eventService.publishToFacebook(this.event.id).subscribe({
      next: () => {
        this.shareFeedback = 'Evenement publie avec succes sur la page Facebook.';
      },
      error: (err: HttpErrorResponse) => {
        const apiMessage = this.extractErrorMessage(err);
        this.shareFeedback = `Echec publication Facebook: ${apiMessage}`;
      }
    });
  }

  private extractErrorMessage(err: HttpErrorResponse): string {
    if (err?.status === 403) {
      return 'Acces refuse (403). Reconnecte-toi puis reessaie.';
    }

    if (typeof err?.error === 'string' && err.error.trim().length > 0) {
      return err.error;
    }

    if (err?.error?.message && typeof err.error.message === 'string') {
      return err.error.message;
    }

    if (err?.message && err.message.trim().length > 0) {
      return err.message;
    }

    return 'Erreur inconnue';
  }

  private openFacebookSharer(): void {
    const detailsUrl = `${window.location.origin}/events/details/${this.event.id}`;
    const facebookShareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(detailsUrl)}`;
    window.open(facebookShareUrl, '_blank', 'noopener,noreferrer,width=640,height=520');
  }

  shareOnWhatsApp(): void {
  if (!this.isAdminUser) return;
  if (!this.event?.id || !isPlatformBrowser(this.platformId)) return;

  this.shareFeedback = '⏳ Génération du poster en cours...';

  this.eventService.getShareDebug(this.event.id).subscribe({
    next: (shareData: any) => {
      const posterUrl = shareData.ogImage;
      const description = shareData.ogDescription;

      const separator = '━━━━━━━━━━━━━━━━━━━━';

      const message = [
        `🎪 *TunisiaHub — Événement*`,
        separator,
        ``,
        `✨ *${this.event.title}*`,
        ``,
        `📅 *Début :*  ${this.event.startDate}`,
        `🏁 *Fin :*      ${this.event.endDate}`,
        `📍 *Lieu :*   ${this.event.lieu}`,
        `💰 *Prix :*   ${this.event.price} TND / personne`,
        `👥 *Places :* ${this.event.capacity} disponibles`,
        `🏷️ *Type :*   ${this.event.type}`,
        ``,
        separator,
        ``,
        `📝 *À propos de l'événement :*`,
        `_${description}_`,
        ``,
        separator,
        ``,
        `🖼️ *Poster généré par IA :*`,
        `👇 Clique pour voir l'affiche officielle`,
        `${posterUrl}`,
        ``,
        separator,
        ``,
        `🎟️ *Réserve ta place maintenant sur TunisiaHub !*`,
        `🔗 http://localhost:4200/events/details/${this.event.id}`,
        ``,
        `_Partagé via TunisiaHub 🇹🇳_`
      ].join('\n');

      const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(message)}`;
      const popup = window.open(whatsappUrl, '_blank', 'noopener,noreferrer');

      this.shareFeedback = popup
        ? '✅ WhatsApp ouvert !'
        : '❌ Popup bloquée. Autorise les popups.';
    },
    error: () => {
      this.shareFeedback = '❌ Erreur lors de la génération du poster.';
    }
  });
}

  // =========================
  // MAP
  // =========================
  async loadMap() {
    this.L = await import('leaflet');

    delete this.L.Icon.Default.prototype._getIconUrl;

    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
      iconUrl: 'assets/leaflet/marker-icon.png',
      shadowUrl: 'assets/leaflet/marker-shadow.png',
    });

    this.map = this.L.map('map').setView(
      [this.event.latitude, this.event.longitude],
      13
    );

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
      .addTo(this.map);

    this.marker = this.L.marker([
      this.event.latitude,
      this.event.longitude
    ]).addTo(this.map);
  }
}
