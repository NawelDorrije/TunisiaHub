// overview/overview.component.ts (new placeholder – add this file)
import { Component } from '@angular/core';

@Component({
  selector: 'app-overview',
  template: `
    <div class="space-y-8">
      <div>
        <h1 class="section-header">Good morning, Nawel 👋</h1>
        <p class="text-gray-600 mt-1 text-lg">Here's what's happening with your campings today</p>
      </div>

      <!-- KPI Cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white rounded-3xl p-6 shadow-sm border border-[var(--border)]">
          <div class="flex justify-between items-start">
            <div>
              <p class="text-sm text-gray-500 font-medium">Total Revenue</p>
              <p class="text-4xl font-semibold text-gray-900 mt-2">TND 48,920</p>
              <p class="text-emerald-500 text-sm flex items-center gap-1 mt-3">
                <span>↑ 14%</span>
                <span class="text-gray-400">from last month</span>
              </p>
            </div>
            <div class="text-5xl">💰</div>
          </div>
        </div>

        <div class="bg-white rounded-3xl p-6 shadow-sm border border-[var(--border)]">
          <div class="flex justify-between items-start">
            <div>
              <p class="text-sm text-gray-500 font-medium">Active Reservations</p>
              <p class="text-4xl font-semibold text-gray-900 mt-2">142</p>
              <p class="text-amber-500 text-sm flex items-center gap-1 mt-3">
                <span>12 pending confirmation</span>
              </p>
            </div>
            <div class="text-5xl">📅</div>
          </div>
        </div>

        <div class="bg-white rounded-3xl p-6 shadow-sm border border-[var(--border)]">
          <div class="flex justify-between items-start">
            <div>
              <p class="text-sm text-gray-500 font-medium">Campings Managed</p>
              <p class="text-4xl font-semibold text-gray-900 mt-2">7</p>
              <p class="text-[var(--accent)] text-sm mt-3">2 new this quarter</p>
            </div>
            <div class="text-5xl">🏕️</div>
          </div>
        </div>

        <div class="bg-white rounded-3xl p-6 shadow-sm border border-[var(--border)]">
          <div class="flex justify-between items-start">
            <div>
              <p class="text-sm text-gray-500 font-medium">Available Spots</p>
              <p class="text-4xl font-semibold text-gray-900 mt-2">84</p>
              <p class="text-rose-500 text-sm mt-3">3 campings at full capacity</p>
            </div>
            <div class="text-5xl">📍</div>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="bg-white rounded-3xl p-8 shadow-sm border border-[var(--border)]">
        <h2 class="text-xl font-semibold mb-6 flex items-center justify-between">
          Recent Reservations
          <button class="text-sm font-medium text-[var(--accent)] hover:underline">View all →</button>
        </h2>
        <div class="space-y-4">
          <div *ngFor="let i of [1,2,3]" class="flex items-center justify-between py-4 border-b last:border-none">
            <div class="flex items-center gap-x-4">
              <div class="w-10 h-10 bg-amber-100 text-amber-600 rounded-2xl flex items-center justify-center text-xl">⛺</div>
              <div>
                <p class="font-medium">La Palmeraie Camping • Spot #B12</p>
                <p class="text-sm text-gray-500">Family of 4 • Apr 12 – Apr 19</p>
              </div>
            </div>
            <div class="text-right">
              <p class="font-semibold text-emerald-600">TND 680</p>
              <span class="inline-block px-3 py-1 text-xs font-medium bg-emerald-100 text-emerald-700 rounded-3xl">Confirmed</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .section-header { font-size: 2rem; font-weight: 700; background: linear-gradient(90deg, #1D9E75, #0F6E56); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
  `]
})
export class OverviewComponent {}
