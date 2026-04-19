import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';

interface LocationSuggestion {
  label: string;
  value: string;
  type: 'city';
}

@Component({
  selector: 'app-carpooling-search-bar',
  templateUrl: './carpooling-search-bar.component.html',
  styleUrls: ['./carpooling-search-bar.component.css'],
})
export class CarpoolingSearchBarComponent {
  @Input() searchForm!: FormGroup;
  @Input() departureSuggestions: LocationSuggestion[] = [];
  @Input() destinationSuggestions: LocationSuggestion[] = [];
  @Input() showDepartureSuggestions = false;
  @Input() showDestinationSuggestions = false;
  @Input() loadingLocations = false;
  @Input() locationError = '';
  @Input() submitLabel = 'Search';
  @Input() disableSubmit = false;

  @Output() searchSubmitted = new EventEmitter<void>();
  @Output() swapLocations = new EventEmitter<void>();
  @Output() locationInput = new EventEmitter<'departure' | 'destination'>();
  @Output() suggestionOpen = new EventEmitter<'departure' | 'destination'>();
  @Output() suggestionClose = new EventEmitter<'departure' | 'destination'>();
  @Output() suggestionSelected = new EventEmitter<{
    field: 'departure' | 'destination';
    suggestion: LocationSuggestion;
  }>();

  submit(): void {
    this.searchSubmitted.emit();
  }

  swap(): void {
    this.swapLocations.emit();
  }

  onLocationInput(field: 'departure' | 'destination'): void {
    this.locationInput.emit(field);
  }

  openSuggestions(field: 'departure' | 'destination'): void {
    this.suggestionOpen.emit(field);
  }

  closeSuggestions(field: 'departure' | 'destination'): void {
    this.suggestionClose.emit(field);
  }

  selectSuggestion(
    field: 'departure' | 'destination',
    suggestion: LocationSuggestion,
  ): void {
    this.suggestionSelected.emit({ field, suggestion });
  }

  shouldShowNoResults(field: 'departure' | 'destination'): boolean {
    const value = `${this.searchForm?.value?.[field] || ''}`.trim();
    const suggestions =
      field === 'departure'
        ? this.departureSuggestions
        : this.destinationSuggestions;

    return (
      !this.loadingLocations &&
      !this.locationError &&
      value.length > 0 &&
      suggestions.length === 0
    );
  }
}
