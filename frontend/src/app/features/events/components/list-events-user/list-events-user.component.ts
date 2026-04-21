import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { EventService } from '../../services/event.service';
import { Router } from '@angular/router';
import { AuthService } from '../../../../services/auth.service';
import { CalendarOptions } from '@fullcalendar/core';
import { calendarPlugins } from '../../../../shared/config/calendar.config';
import {
  WeatherDTO,
  ForecastResponse,
  ForecastItem
} from '../../../../models/weather.model';

export interface DayForecast {
  day: string;
  date: string;
  dateObj: Date;
  tempHi: number;
  tempLo: number;
  feelsLike: number;
  humidity: number;
  windSpeed: number;
  description: string;
  icon: 'sun' | 'cloud' | 'rain' | 'snow' | 'storm';
  iconCode: string;
  barPct: number;
}

@Component({
  selector: 'app-list-events-user',
  templateUrl: './list-events-user.component.html',
  styleUrls: ['./list-events-user.component.css']
})
export class ListEventsUserComponent implements OnInit {

  events: any[] = [];
  filteredEvents: any[] = [];
  searchQuery = '';

  showPopup = false;
  errorMessage = '';

 



getSentimentEmoji(comment: string): string {
  if (!comment) return '🙂';

  const lower = comment.toLowerCase();

  const positive = ['excellent', 'amazing', 'love', 'great', 'perfect', 'awesome'];
  const negative = ['bad', 'terrible', 'worst', 'hate', 'awful', 'shit', 'fuck'];

  if (positive.some(word => lower.includes(word))) return '😍';
  if (negative.some(word => lower.includes(word))) return '😡';

  return '🙂';
}


  // ── Calendar popup ──
  showCalendarPopup = false;
  selectedCalendarDate: Date | null = null;
  selectedDayEvents: any[] = [];
  selectedEventDetail: any | null = null;
  currentCalDate: Date = new Date();
  calendarCells: {
    date: Date | null;
    day: number | null;
    isCurrentMonth: boolean;
    isToday: boolean;
    events: any[];
  }[] = [];
  readonly dayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

  // ── Weather ──
  todayWeather: WeatherDTO | null = null;
  weekForecast: DayForecast[] = [];
  selectedDay: DayForecast | null = null;
  weatherLoading = false;
  weatherError = false;

  private readonly defaultLat = 36.8065;
  private readonly defaultLon = 10.1815;

  calendarOptions: CalendarOptions = {
    plugins: calendarPlugins,
    initialView: 'dayGridMonth',
    height: 'auto',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay'
    },
    eventContent: (arg) => ({
      html: `<div class="fc-ev-inner">
               <span class="fc-ev-dot"></span>
               <span class="fc-ev-title">${arg.event.title}</span>
             </div>`
    }),
    eventClick: (info) => {
      this.router.navigate(['/events/details', info.event.id]);
    }
  };

  constructor(
    private eventService: EventService,
    private router: Router,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadEvents();
      this.loadWeather();
    }
  }

  // ────────────────────────────── Events
  loadEvents(): void {
    this.eventService.getAllEvents().subscribe(data => {
      this.events = data;
      this.filteredEvents = data;
      this.buildCalendar();
    });
  }

  filterEvents(): void {
    const q = this.searchQuery.toLowerCase().trim();
    this.filteredEvents = !q
      ? this.events
      : this.events.filter(e =>
          e.title?.toLowerCase().includes(q) ||
          e.description?.toLowerCase().includes(q) ||
          e.lieu?.toLowerCase().includes(q)
        );
  }

  isCompleted(event: any): boolean {
    return event.status === 'COMPLETED';
  }

  getStatusClass(status: string): string {
    return status === 'COMPLETED' ? 's-c' : 's-a';
  }

  goToDetails(id: number): void {
    this.router.navigate(['/events/details', id]);
  }

  reserveEvent(event: any): void {
    if (this.isCompleted(event)) return;
    const userId = this.authService.getUserId();
    this.eventService.reserveEvent(userId, event.id).subscribe({
      next: () => this.router.navigate(['/events/reserve', event.id]),
      error: (err) => {
        this.errorMessage = err.error?.message || 'An error occurred. Please try again.';
        this.showPopup = true;
      }
    });
  }

  closePopup(): void {
    this.showPopup = false;
  }

  // ────────────────────────────── Calendar popup
  get currentMonthLabel(): string {
    return this.currentCalDate.toLocaleDateString('en-GB', {
      month: 'long', year: 'numeric'
    });
  }

  openCalendar(): void {
    this.showCalendarPopup = true;
    this.selectedCalendarDate = null;
    this.selectedDayEvents = [];
    this.selectedEventDetail = null;
    this.buildCalendar();
  }

  closeCalendar(): void {
    this.showCalendarPopup = false;
  }

  buildCalendar(): void {
    const year = this.currentCalDate.getFullYear();
    const month = this.currentCalDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const today = new Date();

    // Monday-first: shift
    let startDow = firstDay.getDay(); // 0=Sun
    startDow = startDow === 0 ? 6 : startDow - 1; // convert to Mon=0

    const start = new Date(firstDay);
    start.setDate(start.getDate() - startDow);

    this.calendarCells = [];
    const cur = new Date(start);

    for (let i = 0; i < 42; i++) {
      const isCurrent = cur.getMonth() === month;
      const isToday = cur.toDateString() === today.toDateString();
      const cellDate = new Date(cur);
      const cellEvents = this.events.filter(e =>
        e.startDate && new Date(e.startDate).toDateString() === cellDate.toDateString()
      );

      this.calendarCells.push({
        date: cellDate,
        day: isCurrent ? cur.getDate() : null,
        isCurrentMonth: isCurrent,
        isToday,
        events: cellEvents
      });
      cur.setDate(cur.getDate() + 1);
    }
  }

  prevMonth(): void {
    this.currentCalDate = new Date(
      this.currentCalDate.getFullYear(),
      this.currentCalDate.getMonth() - 1,
      1
    );
    this.buildCalendar();
    this.selectedCalendarDate = null;
    this.selectedDayEvents = [];
    this.selectedEventDetail = null;
  }

  nextMonth(): void {
    this.currentCalDate = new Date(
      this.currentCalDate.getFullYear(),
      this.currentCalDate.getMonth() + 1,
      1
    );
    this.buildCalendar();
    this.selectedCalendarDate = null;
    this.selectedDayEvents = [];
    this.selectedEventDetail = null;
  }

  goToToday(): void {
    this.currentCalDate = new Date();
    this.buildCalendar();
  }

  selectCalDay(cell: any): void {
    if (!cell.date) return;
    this.selectedCalendarDate = cell.date;
    this.selectedDayEvents = cell.events;
    this.selectedEventDetail = null;
  }

  selectEventDetail(event: any): void {
    this.selectedEventDetail = event;
  }

  isSameDay(a: Date, b: Date): boolean {
    return a.toDateString() === b.toDateString();
  }

  // ────────────────────────────── Weather — real forecast API
  loadWeather(): void {
    this.weatherLoading = true;
    this.weatherError = false;

    // Load today's current weather
    this.eventService.getWeather(this.defaultLat, this.defaultLon).subscribe({
      next: (w: WeatherDTO) => {
        this.todayWeather = w;
      },
      error: () => {}
    });

    // Load 7-day forecast from /weather/weekly
    this.eventService.getWeeklyForecast(this.defaultLat, this.defaultLon).subscribe({
      next: (res: ForecastResponse) => {
        this.weekForecast = this.parseForecast(res);
        this.selectedDay = this.weekForecast[0] ?? null;
        this.weatherLoading = false;
      },
      error: () => {
        this.weatherError = true;
        this.weatherLoading = false;
      }
    });
  }

  private parseForecast(res: ForecastResponse): DayForecast[] {
    const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    // Group by calendar day (keep the noon slot or first slot of each day)
    const grouped: Record<string, ForecastItem[]> = {};
    res.list.forEach(item => {
      const key = item.dt_txt.slice(0, 10); // "YYYY-MM-DD"
      if (!grouped[key]) grouped[key] = [];
      grouped[key].push(item);
    });

    return Object.entries(grouped).slice(0, 10).map(([dateKey, items], i) => {
      const d = new Date(dateKey);
      const temps = items.map(x => x.main.temp);
      const tempHi = Math.round(Math.max(...temps));
      const tempLo = Math.round(Math.min(...temps));
      // pick noon or midday slot for representative values
      const rep = items.find(x => x.dt_txt.includes('12:00')) ?? items[0];
      const iconCode = rep.weather[0]?.icon ?? '01d';
      const mainWeather = rep.weather[0]?.main?.toLowerCase() ?? '';

      let icon: DayForecast['icon'] = 'sun';
      if (mainWeather.includes('snow'))                         icon = 'snow';
      else if (mainWeather.includes('thunder'))                 icon = 'storm';
      else if (mainWeather.includes('rain') ||
               mainWeather.includes('drizzle'))                 icon = 'rain';
      else if (mainWeather.includes('cloud'))                   icon = 'cloud';

      const barPct = Math.min(100, Math.max(5,
        Math.round(((tempHi - 0) / 45) * 100)
      ));

      return {
        day: i === 0 ? 'Today' : i === 1 ? 'Tomorrow' : dayNames[d.getDay()],
        date: d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }),
        dateObj: d,
        tempHi,
        tempLo,
        feelsLike: Math.round(rep.main.feels_like),
        humidity: rep.main.humidity,
        windSpeed: Math.round(rep.wind.speed),
        description: rep.weather[0]?.description ?? '',
        icon,
        iconCode,
        barPct
      };
    });
  }

 





  selectDay(day: DayForecast): void {
    this.selectedDay = day;
  }
}  