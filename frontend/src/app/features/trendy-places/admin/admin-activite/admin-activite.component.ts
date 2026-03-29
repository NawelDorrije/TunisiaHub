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
  lieux: Lieu[] = [];
  loading = true;
  showForm = false;
  isEditing = false;
  confirmDeleteId: number | null = null;
  successMessage = '';
  errorMessage = '';
  selectedLieuId: number = 0;

  form: ActiviteLieu = this.emptyForm();

  constructor(private service: TrendyPlacesService) {}

  ngOnInit(): void {
    this.loadData();
  }

  emptyForm(): ActiviteLieu {
    return {
      id: 0, nomActivite: '', description: '',
      prix: 0, duree: 0, capaciteMax: 0, disponible: true
    };
  }

  loadData(): void {
    this.loading = true;
    this.service.getAllActivites().subscribe({
      next: (data) => { this.activites = data; this.loading = false; },
      error: () => { this.errorMessage = 'Erreur de chargement'; this.loading = false; }
    });
    this.service.getAllLieux().subscribe({
      next: (data) => this.lieux = data
    });
  }

  getLieuNom(activite: any): string {
    return activite.lieu?.nom || '-';
  }

  openAdd(): void {
    this.form = this.emptyForm();
    this.selectedLieuId = 0;
    this.isEditing = false;
    this.showForm = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  openEdit(activite: any): void {
    this.form = { ...activite };
    this.selectedLieuId = activite.lieu?.id || 0;
    this.isEditing = true;
    this.showForm = true;
    this.successMessage = '';
    this.errorMessage = '';
  }

  closeForm(): void {
    this.showForm = false;
    this.form = this.emptyForm();
  }

  submit(): void {
  if (!this.selectedLieuId) {
    this.errorMessage = 'Veuillez sélectionner un lieu';
    return;
  }
  if (this.isEditing) {
    this.service.updateActivite(this.form.id, this.form, this.selectedLieuId).subscribe({
      next: () => {
        this.successMessage = 'Activité modifiée avec succès !';
        this.closeForm();
        this.loadData();
      },
      error: () => this.errorMessage = 'Erreur lors de la modification'
    });
  } else {
    // ✅ Créer une copie sans le champ id
    const { id, ...activiteSansId } = this.form;
    this.service.createActivite(activiteSansId as ActiviteLieu, this.selectedLieuId).subscribe({
      next: () => {
        this.successMessage = 'Activité ajoutée avec succès !';
        this.closeForm();
        this.loadData();
      },
      error: () => this.errorMessage = 'Erreur lors de l\'ajout'
    });
  }
}

  confirmDelete(id: number): void {
    this.confirmDeleteId = id;
  }

  cancelDelete(): void {
    this.confirmDeleteId = null;
  }

  deleteActivite(id: number): void {
    this.service.deleteActivite(id).subscribe({
      next: () => {
        this.successMessage = 'Activité supprimée avec succès !';
        this.confirmDeleteId = null;
        this.loadData();
      },
      error: () => this.errorMessage = 'Erreur lors de la suppression'
    });
  }
}