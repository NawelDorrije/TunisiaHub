import { Component, OnInit } from '@angular/core';
import { ActiviteLieu, Lieu } from '../../../../models/trendy-places/lieu.model';
import { TrendyPlacesService } from '../../../../services/trendy-places.service';

@Component({
  selector: 'app-admin-activite',
  templateUrl: './admin-activite.component.html',
  styleUrls: ['./admin-activite.component.css']
})
export class AdminActiviteComponent implements OnInit {
  activites: ActiviteLieu[] = [];
  filteredActivites: ActiviteLieu[] = [];
  lieux: Lieu[] = [];
  loading = true;
  showForm = false;
  isEditing = false;
  confirmDeleteId: number | null = null;
  successMessage = '';
  errorMessage = '';
  selectedLieuId = 0;
  searchTerm = '';
  filterDisponible = '';
  validationErrors: string[] = [];

  form: ActiviteLieu = this.emptyForm();

  constructor(private service: TrendyPlacesService) {}

  ngOnInit(): void { this.loadData(); }

  emptyForm(): ActiviteLieu {
    return { id: 0, nomActivite: '', description: '', prix: 0, duree: 0, capaciteMax: 0, disponible: true };
  }

  loadData(): void {
    this.loading = true;
    this.service.getAllActivites().subscribe({
      next: (data) => { this.activites = data; this.filteredActivites = data; this.loading = false; },
      error: () => { this.errorMessage = 'Erreur de chargement'; this.loading = false; }
    });
    this.service.getAllLieux().subscribe({ next: (data) => this.lieux = data });
  }

  onSearch(): void { this.applyFilters(); }
  onFilterChange(): void { this.applyFilters(); }

  applyFilters(): void {
    const t = this.searchTerm.toLowerCase();
    this.filteredActivites = this.activites.filter(a => {
      const matchSearch = a.nomActivite.toLowerCase().includes(t) ||
                          (a.lieu?.nom || '').toLowerCase().includes(t) ||
                          (a.description || '').toLowerCase().includes(t);
      const matchDispo = this.filterDisponible === '' ? true :
                         this.filterDisponible === 'true' ? a.disponible === true : a.disponible === false;
      return matchSearch && matchDispo;
    });
  }

  // Stats
  get totalActivites(): number { return this.activites.length; }
  get activitesDisponibles(): number { return this.activites.filter(a => a.disponible).length; }
  get activitesIndispo(): number { return this.activites.filter(a => !a.disponible).length; }
  get prixMoyen(): number {
    const avec = this.activites.filter(a => (a.prix || 0) > 0);
    if (!avec.length) return 0;
    return Math.round(avec.reduce((s, a) => s + (a.prix || 0), 0) / avec.length);
  }

  getLieuNom(activite: any): string { return activite.lieu?.nom || '—'; }

  // Validation
  validateForm(): boolean {
    this.validationErrors = [];
    if (!this.selectedLieuId) this.validationErrors.push('Le lieu associé est obligatoire');
    if (!this.form.nomActivite?.trim()) this.validationErrors.push('Le nom de l\'activité est obligatoire');
    if ((this.form.prix || 0) < 0) this.validationErrors.push('Le prix ne peut pas être négatif');
    if ((this.form.duree || 0) <= 0) this.validationErrors.push('La durée doit être supérieure à 0');
    if ((this.form.capaciteMax || 0) <= 0) this.validationErrors.push('La capacité max doit être supérieure à 0');
    return this.validationErrors.length === 0;
  }

  openAdd(): void { this.form = this.emptyForm(); this.selectedLieuId = 0; this.isEditing = false; this.showForm = true; this.validationErrors = []; }
  openEdit(activite: any): void { this.form = { ...activite }; this.selectedLieuId = activite.lieu?.id || 0; this.isEditing = true; this.showForm = true; this.validationErrors = []; }
  closeForm(): void { this.showForm = false; this.form = this.emptyForm(); this.validationErrors = []; }

  submit(): void {
    if (!this.validateForm()) return;
    if (this.isEditing) {
      this.service.updateActivite(this.form.id!, this.form, this.selectedLieuId).subscribe({
        next: () => { this.successMessage = 'Activité modifiée !'; this.closeForm(); this.loadData(); },
        error: () => this.errorMessage = 'Erreur modification'
      });
    } else {
      const { id, ...a } = this.form;
      this.service.createActivite(a as ActiviteLieu, this.selectedLieuId).subscribe({
        next: () => { this.successMessage = 'Activité ajoutée !'; this.closeForm(); this.loadData(); },
        error: () => this.errorMessage = 'Erreur ajout'
      });
    }
  }

  confirmDelete(id: number): void { this.confirmDeleteId = id; }
  cancelDelete(): void { this.confirmDeleteId = null; }

  deleteActivite(id: number): void {
    this.service.deleteActivite(id).subscribe({
      next: () => { this.successMessage = 'Activité supprimée !'; this.confirmDeleteId = null; this.loadData(); },
      error: () => this.errorMessage = 'Erreur suppression'
    });
  }

  getTypeColor(type: string): string {
    const colors: {[k:string]: string} = { 'Culturel': '#3b82f6', 'Naturel': '#10b981', 'Historique': '#f59e0b', 'Sportif': '#ef4444', 'Gastronomique': '#8b5cf6', 'Artistique': '#ec4899' };
    return colors[type] || '#6b7280';
  }
}