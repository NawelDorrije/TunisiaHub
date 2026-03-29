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
  selectedLieu: Lieu | null = null;
  confirmDeleteId: number | null = null;
  successMessage = '';
  errorMessage = '';

  form: Lieu = this.emptyForm();

  constructor(private service: TrendyPlacesService) {}

  ngOnInit(): void {
    this.loadLieux();
  }

  emptyForm(): Lieu {
    return {
      id: 0, nom: '', description: '', type: '',
      ville: '', image: '', latitude: 0,
      longitude: 0, horaires: ''
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
    this.successMessage = '';
    this.errorMessage = '';
  }

  openEdit(lieu: Lieu): void {
    this.form = { ...lieu };
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
    // ✅ Créer une copie sans le champ id
    const { id, ...lieuSansId } = this.form;
    this.service.createLieu(lieuSansId as Lieu).subscribe({
      next: () => {
        this.successMessage = 'Lieu ajouté avec succès !';
        this.closeForm();
        this.loadLieux();
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

  deleteLieu(id: number): void {
    this.service.deleteLieu(id).subscribe({
      next: () => {
        this.successMessage = 'Lieu supprimé avec succès !';
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