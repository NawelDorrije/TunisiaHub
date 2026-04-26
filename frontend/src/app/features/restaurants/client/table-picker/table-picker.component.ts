import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { ApiService } from '../../../../services/api.service';
import { TableLayout } from '../../shared/floor-plan-canvas/floor-plan-canvas.component';

@Component({
  selector: 'app-table-picker',
  templateUrl: './table-picker.component.html',
  styleUrls: ['./table-picker.component.css']
})
export class TablePickerComponent implements OnInit, OnChanges {
  @Input() restaurantId!: number;
  @Input() dateTime: string = '';
  @Input() partySize: number = 2;
  @Input() initialSelectedIds: number[] = [];

  @Output() onSelectionChange = new EventEmitter<number[]>();

  tables: TableLayout[] = [];
  selectedIds: number[] = [];
  loading = true;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.selectedIds = [...this.initialSelectedIds];
    this.loadFloorPlan();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.restaurantId && (changes['dateTime'] || changes['partySize'])) {
      this.loadFloorPlan();
    }
  }

  loadFloorPlan(): void {
    if (!this.restaurantId) return;
    this.loading = true;
    this.api.getTablesByRestaurant(this.restaurantId, undefined, this.dateTime, this.partySize).subscribe({
      next: (data) => {
        this.tables = data.map(t => ({
          ...t,
          x: t.x || 0,
          y: t.y || 0,
          width: t.width || 60,
          height: t.height || 60,
          rotation: t.rotation || 0,
          shapeType: t.shapeType || 'rectangle',
          label: t.label || t.tableNumber.toString(),
          color: t.color || '#e2e8f0',
        }));
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading floor plan', err);
        this.loading = false;
      }
    });
  }

  handleTableSelect(ids: number[]): void {
    this.selectedIds = ids;
    this.onSelectionChange.emit(this.selectedIds);
  }

  get totalSelectedCapacity(): number {
    return this.tables
      .filter(t => t.id && this.selectedIds.includes(t.id))
      .reduce((acc, t) => acc + t.capacity, 0);
  }
}
