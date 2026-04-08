import { Component, OnInit } from '@angular/core';
import { Lieu } from '../../../../models/trendy-places/lieu.model';
import { TrendyPlacesService } from '../../../../services/trendy-places.service';

@Component({
  selector: 'app-admin-lieu',
  templateUrl: './admin-lieu.component.html',
  styleUrls: ['./admin-lieu.component.css']
})
export class AdminLieuComponent implements OnInit {
  lieux: Lieu[] = [];
  loading = true;
  showForm = false;
  isEditing = false;
  confirmDeleteId: number | null = null;
  successMessage = '';
  errorMessage = '';

  // IA
  aiLoading = false;
  aiError = '';
  aiSuccess = '';
  uploadedImagePreview: string | null = null;
  uploadedImageBase64: string | null = null;
  suggestedActivites: any[] = [];
  showActivitesPreview = false;

  private readonly OPENROUTER_KEY = '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';

  form: Lieu = this.emptyForm();

  constructor(private service: TrendyPlacesService) {}

  ngOnInit(): void { this.loadLieux(); }

  emptyForm(): Lieu {
    return {
      id: 0, nom: '', description: '', type: '',
      ville: '', image: '', latitude: 0, longitude: 0, horaires: ''
    };
  }

  loadLieux(): void {
    this.loading = true;
    this.service.getAllLieux().subscribe({
      next: (data) => { this.lieux = data; this.loading = false; },
      error: () => { this.errorMessage = 'Erreur de chargement'; this.loading = false; }
    });
  }

  openAdd(): void {
    this.form = this.emptyForm();
    this.isEditing = false;
    this.showForm = true;
    this.resetAI();
  }

  openEdit(lieu: Lieu): void {
    this.form = { ...lieu };
    this.isEditing = true;
    this.showForm = true;
    this.resetAI();
  }

  closeForm(): void {
    this.showForm = false;
    this.form = this.emptyForm();
    this.resetAI();
  }

  resetAI(): void {
    this.aiLoading = false;
    this.aiError = '';
    this.aiSuccess = '';
    this.uploadedImagePreview = null;
    this.uploadedImageBase64 = null;
    this.suggestedActivites = [];
    this.showActivitesPreview = false;
  }

  onImageUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    if (!file.type.startsWith('image/')) {
      this.aiError = 'Veuillez uploader une image valide (jpg, png, webp)';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.aiError = 'Image trop grande (max 5MB)';
      return;
    }

    this.aiError = '';
    this.aiSuccess = '';

    const reader = new FileReader();
    reader.onload = (e) => {
      const result = e.target?.result as string;
      this.uploadedImagePreview = result;
      this.uploadedImageBase64 = result.split(',')[1];
      this.form.image = file.name;
    };
    reader.readAsDataURL(file);
  }

  async analyserAvecIA(): Promise<void> {
  if (!this.uploadedImageBase64) {
    this.aiError = 'Veuillez d\'abord uploader une image';
    return;
  }

  this.aiLoading = true;
  this.aiError = '';
  this.aiSuccess = '';

  try {
    const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.OPENROUTER_KEY}`,
        'HTTP-Referer': 'http://localhost:4200',
        'X-Title': 'Discover Tunisia'
      },
      body: JSON.stringify({
        model: 'nvidia/nemotron-nano-12b-v2-vl:free',
        messages: [
          {
            role: 'user',
            content: [
              {
                type: 'image_url',
                image_url: {
                  url: `data:image/jpeg;base64,${this.uploadedImageBase64}`
                }
              },
              {
                type: 'text',
                text: `Tu es un expert en tourisme tunisien. Analyse cette image d'un lieu en Tunisie et retourne UNIQUEMENT un JSON valide sans markdown, sans backticks, sans explication.

Format exact:
{
  "nom": "nom du lieu",
  "description": "description détaillée en français (2-3 phrases)",
  "type": "un seul parmi: Culturel, Naturel, Historique, Sportif, Gastronomique, Artistique",
  "ville": "ville tunisienne",
  "latitude": nombre,
  "longitude": nombre,
  "horaires": "ex: 08:00 - 18:00",
  "activites": [
    {
      "nomActivite": "nom activite",
      "description": "description courte",
      "prix": nombre,
      "duree": nombre en minutes,
      "capaciteMax": nombre,
      "disponible": true
    }
  ]
}`
              }
            ]
          }
        ]
      })
    });

    console.log('Status:', response.status);

    if (!response.ok) {
      const err = await response.json();
      console.error('API Error:', err);
      throw new Error(err.error?.message || `Erreur HTTP ${response.status}`);
    }

    const data = await response.json();
    console.log('Response:', data);

    const text = data.choices?.[0]?.message?.content;
    if (!text) throw new Error('Réponse vide de l\'IA');

    console.log('Texte reçu:', text);

    // Nettoyer le JSON — extraire entre { }
    let jsonStr = text;
    const match = text.match(/\{[\s\S]*\}/);
    if (match) jsonStr = match[0];

    let parsed: any;
    try {
      parsed = JSON.parse(jsonStr);
    } catch {
      throw new Error('Format JSON invalide reçu de l\'IA');
    }

    this.form.nom         = parsed.nom         || '';
    this.form.description = parsed.description || '';
    this.form.type        = parsed.type        || '';
    this.form.ville       = parsed.ville       || '';
    this.form.latitude    = parsed.latitude    || 0;
    this.form.longitude   = parsed.longitude   || 0;
    this.form.horaires    = parsed.horaires    || '';

    this.suggestedActivites   = parsed.activites || [];
    this.showActivitesPreview = this.suggestedActivites.length > 0;

    this.aiSuccess  = `✨ Formulaire rempli ! ${this.suggestedActivites.length} activité(s) suggérée(s).`;
    this.aiLoading  = false;

  } catch (error: any) {
    console.error('Erreur analyserAvecIA:', error);
    this.aiError   = 'Erreur : ' + (error.message || 'Inconnue');
    this.aiLoading = false;
  }
}

  removeSuggestedActivite(index: number): void {
    this.suggestedActivites.splice(index, 1);
  }

  submit(): void {
    if (this.isEditing) {
      this.service.updateLieu(this.form.id, this.form).subscribe({
        next: () => {
          this.successMessage = 'Lieu modifié avec succès !';
          this.closeForm();
          this.loadLieux();
        },
        error: () => this.errorMessage = 'Erreur lors de la modification'
      });
    } else {
      const { id, ...lieuSansId } = this.form;
      this.service.createLieu(lieuSansId as Lieu).subscribe({
        next: (lieuCree) => {
          if (this.suggestedActivites.length > 0) {
            this.creerActivitesIA(lieuCree.id);
          } else {
            this.successMessage = 'Lieu ajouté avec succès !';
            this.closeForm();
            this.loadLieux();
          }
        },
        error: () => this.errorMessage = 'Erreur lors de l\'ajout'
      });
    }
  }

  creerActivitesIA(lieuId: number): void {
    let count = 0;
    const total = this.suggestedActivites.length;

    this.suggestedActivites.forEach(activite => {
      const { id, ...activiteSansId } = activite;
      this.service.createActivite(activiteSansId, lieuId).subscribe({
        next: () => {
          count++;
          if (count === total) {
            this.successMessage = `🎉 Lieu + ${count} activité(s) créé(s) avec succès !`;
            this.closeForm();
            this.loadLieux();
          }
        },
        error: () => {
          count++;
          if (count === total) {
            this.successMessage = 'Lieu créé (certaines activités ont échoué)';
            this.closeForm();
            this.loadLieux();
          }
        }
      });
    });
  }

  confirmDelete(id: number): void { this.confirmDeleteId = id; }
  cancelDelete(): void           { this.confirmDeleteId = null; }

  deleteLieu(id: number): void {
    this.service.deleteLieu(id).subscribe({
      next: () => {
        this.successMessage = 'Lieu supprimé !';
        this.confirmDeleteId = null;
        this.loadLieux();
      },
      error: () => this.errorMessage = 'Erreur lors de la suppression'
    });
  }

  getImageUrl(image: string): string {
    if (!image) return '/assets/images/lieux/default.jpg';
    if (image.startsWith('http')) return image;
    return `/assets/images/lieux/${image}`;
  }
}