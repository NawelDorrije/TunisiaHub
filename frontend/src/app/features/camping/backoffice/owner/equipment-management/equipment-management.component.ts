import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Equipment } from '../../../../../models/campings/equipment';
import { EquipmentService } from '../../../../../services/campings/equipment.service';
import { CampingService } from '../../../../../services/campings/camping.service';
import { Camping } from '../../../../../models/campings/camping';

@Component({
  selector: 'app-equipment-management',
  templateUrl: './equipment-management.component.html',
  styleUrls: ['./equipment-management.component.css']
})
export class EquipmentManagementComponent implements OnInit, OnDestroy {

  campingId!: number;
  camping: Camping | null = null;
  equipments: Equipment[] = [];
  loading = true;
  successMsg: string | null = null;
  errorMsg: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private equipmentService: EquipmentService,
    private campingService: CampingService,
  ) {}

  ngOnInit(): void {
    this.campingId = +this.route.snapshot.params['campingId'];

    this.campingService.getCampingById(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: c => this.camping = c });

    this.loadEquipments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadEquipments(): void {
    this.loading = true;
    this.equipmentService.getEquipmentByCamping(this.campingId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.equipments = data;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this.errorMsg = 'Failed to load equipment.';
        }
      });
  }

  // ==================== SIMPLE DELETE WITH NATIVE CONFIRM ====================
  deleteEquipment(eq: Equipment): void {
    const message = `Voulez-vous vraiment supprimer "${eq.name}" ?\nCette action est irréversible.`;

    if (!confirm(message)) {
      return; // L'utilisateur a annulé
    }

    // Confirmation acceptée → suppression
    this.equipmentService.deleteEquipment(eq.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.equipments = this.equipments.filter(e => e.id !== eq.id);
          this.flash('success', `Equipment "${eq.name}" supprimé avec succès.`);
        },
        error: () => {
          this.flash('error', 'Échec de la suppression de l\'équipement.');
        }
      });
  }

  // ==================== TOGGLE AVAILABLE ====================
  toggleAvailable(eq: Equipment): void {
    const updated = { ...eq, available: !eq.available };

    this.equipmentService.updateEquipment(eq.id!, updated)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedEq) => {
          const index = this.equipments.findIndex(e => e.id === updatedEq.id);
          if (index >= 0) {
            this.equipments[index] = updatedEq;
          }
        },
        error: () => {
          this.flash('error', 'Failed to update equipment status.');
          eq.available = !eq.available; // revert
        }
      });
  }

  private flash(type: 'success' | 'error', msg: string): void {
    if (type === 'success') {
      this.successMsg = msg;
      this.errorMsg = null;
    } else {
      this.errorMsg = msg;
      this.successMsg = null;
    }
    setTimeout(() => {
      this.successMsg = null;
      this.errorMsg = null;
    }, 4500);
  }
}
