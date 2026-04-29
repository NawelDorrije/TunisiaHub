import { Component, AfterViewInit } from '@angular/core';
import lottie from 'lottie-web';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.css'] // attention au pluriel "styleUrls"
})
export class NotFoundComponent implements AfterViewInit {

  ngAfterViewInit(): void {
    lottie.loadAnimation({
      container: document.querySelector('.lottie-animation') as Element,
      renderer: 'svg',
      loop: true,
      autoplay: true,
      path: 'https://lottie.host/d987597c-7676-4424-8817-7fca6dc1a33e/BVrFXsaeui.json'
    });
  }
}
