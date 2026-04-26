import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../../services/api.service';
import { ActivatedRoute, Router } from '@angular/router';
import { TableLayout } from '../../shared/floor-plan-canvas/floor-plan-canvas.component';

@Component({
  selector: 'app-floor-plan-designer',
  templateUrl: './floor-plan-designer.component.html',
  styleUrls: ['./floor-plan-designer.component.css']
})
export class FloorPlanDesignerComponent implements OnInit {
  restaurantId!: number;
  restaurant: any;
  tables: TableLayout[] = [];
  selectedTableIds: number[] = [];
  selectedTable: TableLayout | null = null;
  loading = true;
  saving = false;

  constructor(
    private api: ApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.restaurantId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadData();
  }

  loadData(): void {
    this.api.getRestaurantById(this.restaurantId).subscribe(res => {
      this.restaurant = res;
    });

    this.api.getTablesByRestaurant(this.restaurantId).subscribe({
      next: (data) => {
        this.tables = data.map(t => ({
          ...t,
          x: t.x || 50,
          y: t.y || 50,
          width: t.width || 60,
          height: t.height || 60,
          rotation: t.rotation || 0,
          shapeType: t.shapeType || 'rectangle',
          color: t.color || '#e2e8f0',
          label: t.label || t.tableNumber.toString(),
        }));
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  addTable(shape: 'rectangle' | 'circle' | 'line'): void {
    const isLine = shape === 'line';
    const newTable: TableLayout = {
      id: Date.now(),
      tableNumber: isLine ? 0 : this.generateTableNumber(),
      capacity: isLine ? 0 : 4,
      x: 100,
      y: 100,
      width: isLine ? 150 : 60,
      height: isLine ? 4 : 60,
      rotation: 0,
      shapeType: shape,
      label: '',
      color: isLine ? '#64748b' : '#e2e8f0'
    };
    this.tables = [...this.tables, newTable];
    this.selectedTableIds = [newTable.id!];
    this.selectedTable = newTable;
  }

  generateTableNumber(): number {
    const max = this.tables.reduce((prev, curr) => Math.max(prev, curr.tableNumber), 0);
    return max + 1;
  }

  onTableSelect(ids: number[]): void {
    this.selectedTableIds = ids;
    if (ids.length > 0) {
      this.selectedTable = this.tables.find(t => t.id === ids[0]) || this.tables.find(t => ids.includes(t.tableNumber)) || null;
    } else {
      this.selectedTable = null;
    }
  }

  onTableModified(updated: TableLayout): void {
    const idx = this.tables.findIndex(t => t.id === updated.id || t.tableNumber === updated.tableNumber);
    if (idx !== -1) {
      this.tables[idx] = updated;
      this.refreshTables();
    }
  }

  refreshTables(): void {
    this.tables = [...this.tables]; // Trigger change detection for child components
  }

  deleteSelected(): void {
    if (this.selectedTableIds.length === 0 && !this.selectedTable) return;
    
    this.tables = this.tables.filter(t => 
      !this.selectedTableIds.includes(t.id!) && 
      (this.selectedTable ? t !== this.selectedTable : true)
    );
    this.selectedTableIds = [];
    this.selectedTable = null;
    this.refreshTables();
  }

  duplicateSelected(): void {
    if (this.selectedTableIds.length === 0 && !this.selectedTable) return;

    const toDuplicate = this.tables.filter(t => 
      this.selectedTableIds.includes(t.id!) || (this.selectedTable && t === this.selectedTable)
    );

    const newTables: TableLayout[] = toDuplicate.map((t, idx) => ({
      ...t,
      id: Date.now() + idx,
      tableNumber: t.shapeType === 'line' ? 0 : this.generateTableNumber(),
      x: t.x + 20,
      y: t.y + 20,
      label: t.label ? `${t.label} (Copy)` : ''
    }));

    this.tables = [...this.tables, ...newTables];
    this.refreshTables();
  }

  save(): void {
    this.saving = true;
    // For simplicity, we assume the backend handles list update
    // In a real app, you might want to call specific add/update/delete endpoints
    this.api.updateTableLayout(this.restaurantId, this.tables).subscribe({
      next: () => {
        alert('Floor plan saved successfully!');
        this.saving = false;
        this.router.navigate(['/restaurants/manage']);
      },
      error: (err) => {
        console.error(err);
        alert('Error saving floor plan.');
        this.saving = false;
      }
    });
  }
}
