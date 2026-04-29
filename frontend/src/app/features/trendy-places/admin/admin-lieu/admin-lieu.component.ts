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
  filteredLieux: Lieu[] = [];
  searchTerm = '';
  loading = true;
  showForm = false;
  isEditing = false;
  confirmDeleteId: number | null = null;
  successMessage = '';
  errorMessage = '';
  validationErrors: string[] = [];

  // IA
  aiLoading = false;
  aiError = '';
  aiSuccess = '';
  uploadedImagePreview: string | null = null;
  uploadedImageBase64: string | null = null;
  suggestedActivites: any[] = [];
  showActivitesPreview = false;
  uploadedFile: File | null = null;

  private readonly OPENROUTER_KEY = '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';

  form: Lieu = this.emptyForm();

  constructor(private service: TrendyPlacesService) {}

  ngOnInit(): void { this.loadLieux(); }

  emptyForm(): Lieu {
    return { id: 0, nom: '', description: '', type: '', ville: '', image: '', latitude: 0, longitude: 0, horaires: '' };
  }

  loadLieux(): void {
    this.loading = true;
    this.service.getAllLieux().subscribe({
      next: (data) => {
        this.lieux = data;
        this.filteredLieux = data;
        this.loading = false;
      },
      error: () => { this.errorMessage = 'Erreur de chargement'; this.loading = false; }
    });
  }

  onSearch(): void {
    const t = this.searchTerm.toLowerCase();
    this.filteredLieux = this.lieux.filter(l =>
      l.nom.toLowerCase().includes(t) ||
      l.ville.toLowerCase().includes(t) ||
      l.type.toLowerCase().includes(t)
    );
  }

  // Stats
  get totalLieux(): number { return this.lieux.length; }
  get totalTypes(): number { return new Set(this.lieux.map(l => l.type)).size; }
  get totalVilles(): number { return new Set(this.lieux.map(l => l.ville)).size; }
  get typeStats(): {type: string, count: number}[] {
    const map = new Map<string, number>();
    this.lieux.forEach(l => map.set(l.type, (map.get(l.type) || 0) + 1));
    return Array.from(map.entries()).map(([type, count]) => ({type, count}));
  }

  // Validation
  validateForm(): boolean {
    this.validationErrors = [];
    if (!this.form.nom?.trim()) this.validationErrors.push('Le nom est obligatoire');
    if (!this.form.type) this.validationErrors.push('Le type est obligatoire');
    if (!this.form.ville?.trim()) this.validationErrors.push('La ville est obligatoire');
    if (!this.form.horaires?.trim()) this.validationErrors.push('Les horaires sont obligatoires');
    if (!this.form.description?.trim()) this.validationErrors.push('La description est obligatoire');
    if (this.form.latitude === 0 && this.form.longitude === 0) this.validationErrors.push('Les coordonnées GPS sont obligatoires');
    return this.validationErrors.length === 0;
  }

  openAdd(): void { this.form = this.emptyForm(); this.isEditing = false; this.showForm = true; this.validationErrors = []; this.resetAI(); }
  openEdit(lieu: Lieu): void { this.form = { ...lieu }; this.isEditing = true; this.showForm = true; this.validationErrors = []; this.resetAI(); }
  closeForm(): void { this.showForm = false; this.form = this.emptyForm(); this.validationErrors = []; this.resetAI(); }

  resetAI(): void {
    this.aiLoading = false; this.aiError = ''; this.aiSuccess = '';
    this.uploadedImagePreview = null; this.uploadedImageBase64 = null;
    this.uploadedFile = null; this.suggestedActivites = []; this.showActivitesPreview = false;
  }

  onImageUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    if (!file.type.startsWith('image/')) { this.aiError = 'Image invalide'; return; }
    if (file.size > 10 * 1024 * 1024) { this.aiError = 'Image trop grande (max 10MB)'; return; }
    this.aiError = ''; this.uploadedFile = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      const result = e.target?.result as string;
      this.uploadedImagePreview = result;
      this.uploadedImageBase64 = result.split(',')[1];
    };
    reader.readAsDataURL(file);
  }

  async analyserAvecIA(): Promise<void> {
    if (!this.uploadedImageBase64 || !this.uploadedFile) { this.aiError = 'Veuillez uploader une image'; return; }
    this.aiLoading = true; this.aiError = ''; this.aiSuccess = '';
    try {
      const uploadResult = await new Promise<any>((resolve, reject) => {
        this.service.uploadImageLieu(this.uploadedFile!).subscribe({ next: resolve, error: reject });
      });
      this.form.image = uploadResult.imageUrl;
      const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${this.OPENROUTER_KEY}`, 'HTTP-Referer': 'http://localhost:4200', 'X-Title': 'Discover Tunisia' },
        body: JSON.stringify({ model: 'nvidia/nemotron-nano-12b-v2-vl:free', messages: [{ role: 'user', content: [{ type: 'image_url', image_url: { url: `data:image/jpeg;base64,${this.uploadedImageBase64}` } }, { type: 'text', text: `Tu es un expert en tourisme tunisien. Retourne UNIQUEMENT un JSON valide sans markdown.\n{\n  "nom": "...",\n  "description": "...",\n  "type": "Culturel|Naturel|Historique|Sportif|Gastronomique|Artistique",\n  "ville": "...",\n  "latitude": 0,\n  "longitude": 0,\n  "horaires": "08:00 - 18:00",\n  "activites": [{ "nomActivite": "...", "description": "...", "prix": 0, "duree": 0, "capaciteMax": 0, "disponible": true }]\n}` }] }] })
      });
      if (!response.ok) throw new Error(`Erreur HTTP ${response.status}`);
      const data = await response.json();
      const text = data.choices?.[0]?.message?.content;
      if (!text) throw new Error('Réponse vide');
      const match = text.match(/\{[\s\S]*\}/);
      const parsed = JSON.parse(match ? match[0] : text);
      this.form.nom = parsed.nom || ''; this.form.description = parsed.description || '';
      this.form.type = parsed.type || ''; this.form.ville = parsed.ville || '';
      this.form.latitude = parsed.latitude || 0; this.form.longitude = parsed.longitude || 0;
      this.form.horaires = parsed.horaires || '';
      this.suggestedActivites = parsed.activites || []; this.showActivitesPreview = this.suggestedActivites.length > 0;
      this.aiSuccess = `✨ Formulaire rempli ! ${this.suggestedActivites.length} activité(s) suggérée(s).`;
    } catch (error: any) { this.aiError = 'Erreur : ' + (error.message || 'Inconnue'); }
    this.aiLoading = false;
  }

  removeSuggestedActivite(index: number): void { this.suggestedActivites.splice(index, 1); }

  submit(): void {
    if (!this.validateForm()) return;
    if (this.isEditing) {
      this.service.updateLieu(this.form.id, this.form).subscribe({
        next: () => { this.successMessage = 'Lieu modifié !'; this.closeForm(); this.loadLieux(); },
        error: () => this.errorMessage = 'Erreur modification'
      });
    } else {
      const { id, ...lieuSansId } = this.form;
      this.service.createLieu(lieuSansId as Lieu).subscribe({
        next: (lieuCree) => {
          if (this.suggestedActivites.length > 0) this.creerActivitesIA(lieuCree.id);
          else { this.successMessage = 'Lieu ajouté !'; this.closeForm(); this.loadLieux(); }
        },
        error: () => this.errorMessage = 'Erreur ajout'
      });
    }
  }

  creerActivitesIA(lieuId: number): void {
    let count = 0; const total = this.suggestedActivites.length;
    this.suggestedActivites.forEach(activite => {
      const { id, ...a } = activite;
      this.service.createActivite(a, lieuId).subscribe({
        next: () => { count++; if (count === total) { this.successMessage = `🎉 Lieu + ${count} activité(s) créé(s) !`; this.closeForm(); this.loadLieux(); } },
        error: () => { count++; if (count === total) { this.successMessage = 'Lieu créé (certaines activités ont échoué)'; this.closeForm(); this.loadLieux(); } }
      });
    });
  }

  confirmDelete(id: number): void { this.confirmDeleteId = id; }
  cancelDelete(): void { this.confirmDeleteId = null; }

  deleteLieu(id: number): void {
    this.service.deleteLieu(id).subscribe({
      next: () => { this.successMessage = 'Lieu supprimé !'; this.confirmDeleteId = null; this.loadLieux(); },
      error: () => this.errorMessage = 'Erreur suppression'
    });
  }

  getImageUrl(image: string): string {
    if (!image) return '/assets/images/lieux/default.jpg';
    if (image.startsWith('http')) return image;
    return `/assets/images/lieux/${image}`;
  }

  getTypeColor(type: string): string {
    const colors: {[k:string]: string} = { 'Culturel': '#3b82f6', 'Naturel': '#10b981', 'Historique': '#f59e0b', 'Sportif': '#ef4444', 'Gastronomique': '#8b5cf6', 'Artistique': '#ec4899' };
    return colors[type] || '#6b7280';
  }
}