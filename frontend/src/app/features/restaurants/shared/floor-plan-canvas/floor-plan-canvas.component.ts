import { Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild, OnDestroy } from '@angular/core';
import Konva from 'konva';

export interface TableLayout {
  id?: number;
  tableNumber: number;
  capacity: number;
  x: number;
  y: number;
  width: number;
  height: number;
  rotation: number;
  shapeType: 'rectangle' | 'circle' | 'line';
  label: string;
  color: string;
  isAvailable?: boolean;
}

@Component({
  selector: 'app-floor-plan-canvas',
  template: `
    <div #container class="canvas-container" [style.width.px]="width" [style.height.px]="height" tabindex="0" (keydown)="onKeyDown($event)"></div>
  `,
  styles: [`
    .canvas-container {
      background: white;
      border: 1px solid #e2e8f0;
      border-radius: 20px;
      overflow: hidden;
      box-shadow: var(--shadow-premium);
      outline: none;
      transition: border-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .canvas-container:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 4px rgba(212, 165, 116, 0.1);
    }
  `]
})
export class FloorPlanCanvasComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('container', { static: true }) container!: ElementRef;

  @Input() mode: 'edit' | 'select' = 'select';
  @Input() tables: TableLayout[] = [];
  @Input() partySize: number = 2;
  @Input() width: number = 800;
  @Input() height: number = 600;
  @Input() selectedTableIds: number[] = [];

  @Output() onTableSelect = new EventEmitter<number[]>();
  @Output() onTableModified = new EventEmitter<TableLayout>();
  @Output() onCanvasClick = new EventEmitter<void>();
  @Output() onDeleteRequest = new EventEmitter<void>();

  private readonly GRID_SIZE = 20;

  stage!: Konva.Stage;
  layer!: Konva.Layer;
  gridLayer!: Konva.Layer;
  transformer!: Konva.Transformer;
  selectionRect!: Konva.Rect;
  
  private tableNodes: Map<number, Konva.Group> = new Map();
  private x1 = 0;
  private y1 = 0;
  private x2 = 0;
  private y2 = 0;

  ngOnInit(): void {
    this.initKonva();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.stage && (changes['tables'] || changes['selectedTableIds'] || changes['partySize'])) {
      this.drawTables();
    }
  }

  ngOnDestroy(): void {
    if (this.stage) {
      this.stage.destroy();
    }
  }

  private initKonva(): void {
    this.stage = new Konva.Stage({
      container: this.container.nativeElement,
      width: this.width,
      height: this.height,
    });

    this.gridLayer = new Konva.Layer();
    this.stage.add(this.gridLayer);
    if (this.mode === 'edit') {
      this.drawGrid();
    }

    this.layer = new Konva.Layer();
    this.stage.add(this.layer);

    if (this.mode === 'edit') {
      this.transformer = new Konva.Transformer({
        rotateEnabled: true,
        padding: 5,
        anchorSize: 10,
        anchorFill: '#fff',
        anchorStroke: '#3b82f6',
        anchorCornerRadius: 2,
        borderStroke: '#3b82f6',
        borderDash: [4, 4],
        enabledAnchors: ['top-left', 'top-right', 'bottom-left', 'bottom-right', 'top-center', 'bottom-center', 'left-center', 'right-center'],
        rotationSnaps: [0, 45, 90, 135, 180, 225, 270, 315],
        rotateAnchorOffset: 30,
        boundBoxFunc: (oldBox, newBox) => {
          const minSize = this.GRID_SIZE / 2;
          if (Math.abs(newBox.width) < minSize || Math.abs(newBox.height) < minSize) {
            return oldBox;
          }
          return newBox;
        },
      });
      this.layer.add(this.transformer);

      this.selectionRect = new Konva.Rect({
        fill: 'rgba(59, 130, 246, 0.1)',
        stroke: '#3b82f6',
        strokeWidth: 1,
        visible: false,
      });
      this.layer.add(this.selectionRect);
    }

    this.stage.on('mousedown touchstart', (e) => {
      if (this.mode !== 'edit') return;
      if (e.target !== this.stage) return;

      e.evt.preventDefault();
      this.x1 = this.stage.getPointerPosition()!.x;
      this.y1 = this.stage.getPointerPosition()!.y;
      this.x2 = this.stage.getPointerPosition()!.x;
      this.y2 = this.stage.getPointerPosition()!.y;

      this.selectionRect.visible(true);
      this.selectionRect.width(0);
      this.selectionRect.height(0);
    });

    this.stage.on('mousemove touchmove', (e) => {
      if (!this.selectionRect || !this.selectionRect.visible()) return;

      e.evt.preventDefault();
      this.x2 = this.stage.getPointerPosition()!.x;
      this.y2 = this.stage.getPointerPosition()!.y;

      this.selectionRect.setAttrs({
        x: Math.min(this.x1, this.x2),
        y: Math.min(this.y1, this.y2),
        width: Math.abs(this.x2 - this.x1),
        height: Math.abs(this.y2 - this.y1),
      });
    });

    this.stage.on('mouseup touchend', (e) => {
      if (!this.selectionRect || !this.selectionRect.visible()) return;

      e.evt.preventDefault();
      setTimeout(() => {
        this.selectionRect.visible(false);
      });

      const shapes = this.stage.find('.group');
      const box = this.selectionRect.getClientRect();
      const selected = shapes.filter((shape) =>
        Konva.Util.haveIntersection(box, shape.getClientRect())
      );
      this.transformer.nodes(selected);
      this.onTableSelect.emit(selected.map(s => Number(s.id())));
    });

    this.stage.on('click tap', (e) => {
      if (this.mode === 'edit') {
        if (e.target === this.stage) {
          this.transformer.nodes([]);
          this.onTableSelect.emit([]);
          this.onCanvasClick.emit();
        }
      } else {
        if (e.target === this.stage) {
          this.onCanvasClick.emit();
        }
      }
      this.layer.draw();
    });

    this.drawTables();
  }

  private drawGrid(): void {
    this.gridLayer.destroyChildren();
    const padding = 20;
    for (let i = 0; i < this.width / this.GRID_SIZE; i++) {
      for (let j = 0; j < this.height / this.GRID_SIZE; j++) {
        this.gridLayer.add(new Konva.Circle({
          x: i * this.GRID_SIZE,
          y: j * this.GRID_SIZE,
          radius: 1,
          fill: '#e2e8f0',
          listening: false
        }));
      }
    }
    this.gridLayer.batchDraw();
  }

  private drawTables(): void {
    // Clear existing nodes and transformer to prevent doubling
    this.layer.destroyChildren();
    if (this.mode === 'edit') {
      this.layer.add(this.transformer);
      this.transformer.nodes([]);
    }
    this.tableNodes.clear();

    this.tables.forEach(table => {
      const group = new Konva.Group({
        id: table.id?.toString(),
        x: table.x,
        y: table.y,
        width: table.width,
        height: table.height,
        rotation: table.rotation,
        draggable: this.mode === 'edit',
        name: 'group',
        dragBoundFunc: (pos) => {
          if (this.mode !== 'edit') return pos;
          return {
            x: Math.round(pos.x / this.GRID_SIZE) * this.GRID_SIZE,
            y: Math.round(pos.y / this.GRID_SIZE) * this.GRID_SIZE,
          };
        }
      });

      const isSelected = this.selectedTableIds.includes(table.id!);
      const statusColor = this.getTableColor(table, isSelected);

      let shape: Konva.Shape;
      if (table.shapeType === 'circle') {
        shape = new Konva.Ellipse({
          radiusX: table.width / 2,
          radiusY: table.height / 2,
          x: table.width / 2,
          y: table.height / 2,
          fill: statusColor,
          stroke: isSelected ? '#3b82f6' : '#cbd5e1',
          strokeWidth: isSelected ? 2 : 1,
          shadowBlur: isSelected ? 15 : 4,
          shadowColor: 'rgba(0,0,0,0.2)',
          shadowOpacity: 0.1,
          name: 'mainShape'
        });
      } else if (table.shapeType === 'line') {
        shape = new Konva.Line({
          points: [0, 0, table.width, 0],
          stroke: table.color || '#94a3b8',
          strokeWidth: table.height || 4,
          hitStrokeWidth: 20,
          lineCap: 'round',
          lineJoin: 'round',
          name: 'mainShape'
        });
      } else {
        shape = new Konva.Rect({
          width: table.width,
          height: table.height,
          fill: statusColor,
          stroke: isSelected ? '#3b82f6' : '#cbd5e1',
          strokeWidth: isSelected ? 2 : 1,
          cornerRadius: 6,
          shadowBlur: isSelected ? 15 : 4,
          shadowColor: 'rgba(0,0,0,0.2)',
          shadowOpacity: 0.1,
          name: 'mainShape'
        });
      }

      group.add(shape);

      if (table.shapeType !== 'line') {
        const text = new Konva.Text({
          text: table.label ? table.label : table.tableNumber.toString(),
          fontSize: 13,
          fontFamily: "'Inter', sans-serif",
          fontWeight: '600',
          fill: '#334155',
          width: table.width,
          padding: 5,
          align: 'center',
          verticalAlign: 'middle',
          height: table.height,
          listening: false
        });
        group.add(text);
      }
      
      group.on('click tap', (e) => {
        e.cancelBubble = true;
        const isShift = e.evt && e.evt.shiftKey;
        this.handleTableClick(table, group, !!isShift);
      });

      if (this.mode === 'edit') {
        group.on('dragmove', () => {
          // No need for manual snapping here as dragBoundFunc handles it
          this.layer.batchDraw();
        });

        group.on('dragend', () => {
          this.updateTableData(table, group);
        });

        group.on('transform', () => {
          // Precise sizing feedback
          const nodes = this.transformer.nodes();
          nodes.forEach(node => {
            const scaleX = node.scaleX();
            const scaleY = node.scaleY();
            
            // Snap width/height to grid effectively
            // we round the calculated size
            const newWidth = Math.round((node.width() * scaleX) / this.GRID_SIZE) * this.GRID_SIZE;
            const newHeight = Math.round((node.height() * scaleY) / this.GRID_SIZE) * this.GRID_SIZE;
            
            // No need to set it here yet, transform handles visual.
            // But we can snap the scale to achieve snap effect
          });
        });

        group.on('transformend', () => {
          this.updateTableData(table, group);
        });

        group.on('mouseenter', () => {
          this.stage.container().style.cursor = 'move';
        });
        group.on('mouseleave', () => {
          this.stage.container().style.cursor = 'default';
        });
      } else {
        group.on('mouseenter', () => {
          if (table.isAvailable !== false && table.capacity >= this.partySize) {
            this.stage.container().style.cursor = 'pointer';
          }
        });
        group.on('mouseleave', () => {
          this.stage.container().style.cursor = 'default';
        });
      }

      this.layer.add(group);
      if (table.id) this.tableNodes.set(table.id, group);
    });

    if (this.mode === 'edit' && this.transformer) {
      this.transformer.moveToTop();
    }
    
    this.layer.batchDraw();
  }

  private handleTableClick(table: TableLayout, group: Konva.Group, isShift: boolean = false): void {
    if (this.mode === 'edit') {
      const nodes = this.transformer.nodes().slice();
      if (isShift) {
        if (nodes.includes(group)) {
          const index = nodes.indexOf(group);
          nodes.splice(index, 1);
        } else {
          nodes.push(group);
        }
      } else {
        nodes.splice(0, nodes.length, group);
      }
      this.transformer.nodes(nodes);
      this.onTableSelect.emit(nodes.map(n => Number(n.id())));
    } else {
      if (table.isAvailable === false || table.capacity < this.partySize) {
        return; // Non-selectable
      }

      let newSelection: number[];
      if (this.selectedTableIds.includes(table.id!)) {
        newSelection = this.selectedTableIds.filter(id => id !== table.id);
      } else {
        newSelection = [...this.selectedTableIds, table.id!];
      }
      this.onTableSelect.emit(newSelection);
    }
    this.layer.draw();
  }

  onKeyDown(e: KeyboardEvent): void {
    if (this.mode !== 'edit' || this.transformer.nodes().length === 0) return;

    const nodes = this.transformer.nodes();
    const isShift = e.shiftKey;
    const delta = isShift ? 10 : 1;

    switch (e.key) {
      case 'ArrowLeft':
        nodes.forEach(n => n.x(n.x() - delta));
        e.preventDefault();
        break;
      case 'ArrowRight':
        nodes.forEach(n => n.x(n.x() + delta));
        e.preventDefault();
        break;
      case 'ArrowUp':
        nodes.forEach(n => n.y(n.y() - delta));
        e.preventDefault();
        break;
      case 'ArrowDown':
        nodes.forEach(n => n.y(n.y() + delta));
        e.preventDefault();
        break;
      case 'Delete':
      case 'Backspace':
        this.onDeleteRequest.emit();
        e.preventDefault();
        break;
    }
    this.layer.batchDraw();
    
    if (['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(e.key)) {
       // Update database after move
       nodes.forEach(node => {
         const id = Number(node.id());
         const table = this.tables.find(t => t.id === id);
         if (table) this.updateTableData(table, node as Konva.Group);
       });
    }
  }

  private updateTableData(table: TableLayout, group: Konva.Group): void {
    // Snap final data to grid
    const updatedTable = {
      ...table,
      x: Math.round(group.x() / this.GRID_SIZE) * this.GRID_SIZE,
      y: Math.round(group.y() / this.GRID_SIZE) * this.GRID_SIZE,
      rotation: Math.round(group.rotation() / 15) * 15, // Snap rotation to 15 deg steps
      width: Math.max(this.GRID_SIZE, Math.round((group.width() * group.scaleX()) / this.GRID_SIZE) * this.GRID_SIZE),
      height: Math.max(table.shapeType === 'line' ? 2 : this.GRID_SIZE, Math.round((group.height() * group.scaleY()) / this.GRID_SIZE) * this.GRID_SIZE),
    };
    
    // Reset scale to 1 after applying to width/height
    group.scaleX(1);
    group.scaleY(1);
    
    this.onTableModified.emit(updatedTable);
    this.drawTables(); // Redraw with snapped values
  }

  private getTableColor(table: TableLayout, isSelected: boolean): string {
    if (this.mode === 'edit') {
       return isSelected ? '#d4a574' : (table.color || '#f1f5f9');
    }
    
    if (isSelected) return '#d4a574'; // Brand Gold
    if (table.isAvailable === false) return '#fecaca'; // Soft error red
    if (table.capacity < this.partySize) return '#f1f5f9'; // Muted gray
    return '#d1fae5'; // Soft success green
  }
}
