import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { AccommodationStats } from '../../../../models/accommodations/statistics.model';
import {
  ApexChart,
  ApexNonAxisChartSeries,
  ApexAxisChartSeries,
  ApexXAxis,
  ApexDataLabels,
  ApexLegend,
  ApexPlotOptions,
  ApexTitleSubtitle
} from 'ng-apexcharts';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { ReservationService } from '../../../../services/reservation.service';
@Component({
  selector: 'app-accommodation-statistics',
  templateUrl: './accommodation-statistics.component.html',
  styleUrls: ['./accommodation-statistics.component.css']
})
export class AccommodationStatisticsComponent implements OnInit {

  stats!: AccommodationStats;
  isLoading = true;
  errorMessage = '';

  // Pie chart — accommodation types
  typeChartSeries: ApexNonAxisChartSeries = [];
  typeChartLabels: string[] = [];
  typeChart: ApexChart = {
    type: 'pie', height: 300,
    toolbar: { show: false }
  };
  statusChartColors: string[] = ['#28a745', '#dc3545'];
  priceChartColors: string[] = ['#0d6efd'];
  reservedChartColors: string[] = ['#6f42c1'];
  capacityChartColors: string[] = ['#fd7e14'];

  // Donut chart — reservation status
  statusChartSeries: ApexNonAxisChartSeries = [];
  statusChart: ApexChart = {
    type: 'donut', height: 300,
    toolbar: { show: false }
  };
  statusChartLabels: string[] = ['Confirmed', 'Cancelled'];

  // Bar chart — price distribution
  priceChartSeries: ApexAxisChartSeries = [];
  priceChart: ApexChart = {
    type: 'bar', height: 300,
    toolbar: { show: false }
  };
  priceChartXAxis: ApexXAxis = {
    categories: ['< 100 TND', '100-200 TND', '200-300 TND', '> 300 TND']
  };

  // Bar chart — top reserved
  reservedChartSeries: ApexAxisChartSeries = [];
  reservedChart: ApexChart = {
    type: 'bar', height: 300,
    toolbar: { show: false }
  };
  reservedChartXAxis: ApexXAxis = { categories: [] };

  // Bar chart — capacity distribution
  capacityChartSeries: ApexAxisChartSeries = [];
  capacityChart: ApexChart = {
    type: 'bar', height: 300,
    toolbar: { show: false }
  };
  capacityChartXAxis: ApexXAxis = {
    categories: ['1-2 persons', '3-5 persons', '6-10 persons', '10+ persons']
  };

  dataLabels: ApexDataLabels = { enabled: true };
  legend: ApexLegend = { show: true };
  plotOptions: ApexPlotOptions = {
    bar: { borderRadius: 6, columnWidth: '50%' }
  };

  constructor(
    private reservationService: ReservationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.reservationService.getStatistics().subscribe({
      next: (data) => {
        this.stats = data;
        this.buildCharts();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load statistics.';
        this.isLoading = false;
      }
    });
  }

  buildCharts(): void {
    // Type distribution pie chart
    this.typeChartLabels = Object.keys(this.stats.accommodationsByType);
    this.typeChartSeries = Object.values(this.stats.accommodationsByType);

    // Reservation status donut
    this.statusChartSeries = [
      this.stats.confirmedReservations,
      this.stats.cancelledReservations
    ];

    // Price distribution bar
    this.priceChartSeries = [{
      name: 'Accommodations',
      data: [
        this.stats.under100,
        this.stats.between100and200,
        this.stats.between200and300,
        this.stats.above300
      ]
    }];

    // Capacity distribution bar
    this.capacityChartSeries = [{
      name: 'Accommodations',
      data: [
        this.stats.capacity1to2,
        this.stats.capacity3to5,
        this.stats.capacity6to10,
        this.stats.capacityAbove10
      ]
    }];

    // Top reserved bar
    this.reservedChartXAxis = {
      categories: this.stats.topReservedAccommodations
        .map(a => a['title'])
    };
    this.reservedChartSeries = [{
      name: 'Reservations',
      data: this.stats.topReservedAccommodations
        .map(a => a['count'])
    }];
  }
  @ViewChild('statisticsContent') statisticsContent!: ElementRef;

isGeneratingPDF = false;

async generatePDF(): Promise<void> {
  this.isGeneratingPDF = true;

  try {
    const content = this.statisticsContent.nativeElement;

    const canvas = await html2canvas(content, {
      scale: 2,               // high resolution
      useCORS: true,
      backgroundColor: '#ffffff',
      scrollY: -window.scrollY
    });

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'mm',
      format: 'a4'
    });

    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = pdf.internal.pageSize.getHeight();
    const imgWidth = canvas.width;
    const imgHeight = canvas.height;
    const ratio = imgWidth / imgHeight;
    const imgPdfHeight = pdfWidth / ratio;

    // Add header
    pdf.setFillColor(13, 110, 253);
    pdf.rect(0, 0, pdfWidth, 20, 'F');
    pdf.setTextColor(255, 255, 255);
    pdf.setFontSize(14);
    pdf.setFont('helvetica', 'bold');
    pdf.text('TunisiaHub — Accommodation Statistics Report', pdfWidth / 2, 13, {
      align: 'center'
    });

    // Add date
    pdf.setFontSize(9);
    pdf.setFont('helvetica', 'normal');
    const date = new Date().toLocaleDateString('en-GB', {
      day: '2-digit', month: 'long', year: 'numeric'
    });
    pdf.text(`Generated on: ${date}`, pdfWidth / 2, 18, { align: 'center' });

    // Add content
    let yPosition = 25;
    let remainingHeight = imgPdfHeight;

    while (remainingHeight > 0) {
      const pageHeight = pdfHeight - yPosition - 10;
      const sourceY = (imgPdfHeight - remainingHeight) * (imgHeight / imgPdfHeight);
      const sourceHeight = Math.min(
        pageHeight * (imgHeight / imgPdfHeight),
        imgHeight - sourceY
      );

      const pageCanvas = document.createElement('canvas');
      pageCanvas.width = imgWidth;
      pageCanvas.height = sourceHeight;
      const ctx = pageCanvas.getContext('2d');
      ctx?.drawImage(
        canvas, 0, sourceY,
        imgWidth, sourceHeight,
        0, 0,
        imgWidth, sourceHeight
      );

      const pageImgData = pageCanvas.toDataURL('image/png');
      const pageImgHeight = (sourceHeight / imgHeight) * imgPdfHeight;

      pdf.addImage(pageImgData, 'PNG', 0, yPosition, pdfWidth, pageImgHeight);
      remainingHeight -= pageHeight;

      if (remainingHeight > 0) {
        pdf.addPage();
        yPosition = 10;
      }
    }

    // Add footer on last page
    pdf.setTextColor(150, 150, 150);
    pdf.setFontSize(8);
    pdf.text(
      'TunisiaHub © 2026 — Confidential Admin Report',
      pdfWidth / 2,
      pdfHeight - 5,
      { align: 'center' }
    );

    // Save
    const filename = `TunisiaHub_Accommodation_Stats_${new Date().toISOString().split('T')[0]}.pdf`;
    pdf.save(filename);

  } catch (error) {
    console.error('PDF generation error:', error);
  } finally {
    this.isGeneratingPDF = false;
  }
}
  goBack(): void {
    this.router.navigate(['/accommodations/admin']);
  }
}