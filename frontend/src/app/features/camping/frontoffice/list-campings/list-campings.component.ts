import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime } from 'rxjs/operators';
import { Camping } from '../../../../models/campings/camping';
import { CampingService } from '../../../../services/campings/camping.service';

@Component({
  selector: 'app-list-campings',
  templateUrl: './list-campings.component.html',
  styleUrls: ['./list-campings.component.css']
})
export class ListCampingsComponent implements OnInit, OnDestroy {
  campings: Camping[] = [];
  filtered: Camping[] = [];
  loading = true;
  error = '';
  filterForm: FormGroup;
  viewMode: 'grid' | 'list' = 'grid';
  private destroy$ = new Subject<void>();

  governorates = [
    'Tunis', 'Ariana', 'Ben Arous', 'Manouba', 'Nabeul', 'Zaghouan',
    'Bizerte', 'Béja', 'Jendouba', 'Kef', 'Siliana', 'Sousse',
    'Monastir', 'Mahdia', 'Sfax', 'Kairouan', 'Kasserine', 'Sidi Bouzid',
    'Gabès', 'Medenine', 'Tataouine', 'Gafsa', 'Tozeur', 'Kebili'
  ];

  constructor(
    private campingService: CampingService,
    private fb: FormBuilder,
    private router: Router
  ) {
    this.filterForm = this.fb.group({
      keyword: [''],
      governorate: [''],
      minCapacity: ['']
    });
  }

  ngOnInit(): void {
    this.loadCampings();
    this.filterForm.valueChanges
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => this.applyFilters());
  }

  loadCampings(): void {
    this.loading = true;
    this.error = '';
    this.campingService.getAllCampings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.campings = data;
          this.filtered = [...this.campings];
          this.loading = false;
        },
        error: () => {
          this.error = 'Failed to load campings. Please try again.';
          this.loading = false;
        }
      });
  }

  applyFilters(): void {
    const { keyword, governorate, minCapacity } = this.filterForm.value;
    this.filtered = this.campings.filter(c => {
      const matchKeyword = !keyword ||
        c.name.toLowerCase().includes(keyword.toLowerCase()) ||
        (c.address || '').toLowerCase().includes(keyword.toLowerCase());
      const matchGov = !governorate || c.governorate === governorate;
      const matchCap = !minCapacity || (c.maxCapacity ?? 0) >= +minCapacity;
      return matchKeyword && matchGov  && matchCap;
    });
  }

  resetFilters(): void {
    this.filterForm.reset({ keyword: '', governorate:  '', minCapacity: '' });
    this.filtered = [...this.campings];
  }

  goToDetail(id: number): void {
    this.router.navigate(['/camping', id]);
  }

  goToReserve(event: Event, id: number): void {
    event.stopPropagation();
    this.router.navigate(['/camping', id, 'reserve']);
  }

  goToMap(): void {
    this.router.navigate(['/camping/map']);
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      ACTIVE: 'badge-active',
      PENDING: 'badge-pending',
      SUSPENDED: 'badge-suspended',
      CLOSED: 'badge-closed'
    };
    return map[status] || '';
  }

  getStars(rating: number = 0): string[] {
    const full = Math.floor(rating);
    const half = rating % 1 >= 0.5 ? 1 : 0;
    const empty = 5 - full - half;
    return [
      ...Array(full).fill('full'),
      ...Array(half).fill('half'),
      ...Array(empty).fill('empty')
    ];
  }

  get hasActiveFilters(): boolean {
    const v = this.filterForm.value;
    return !!(v.keyword || v.governorate  || v.minCapacity);
  }

  trackById(_: number, item: Camping): number {
    return item.id!;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
