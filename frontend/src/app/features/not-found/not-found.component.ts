<<<<<<< HEAD
import { AfterViewInit, Component, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
=======
import {
  AfterViewInit,
  Component,
  Inject,
  PLATFORM_ID
} from '@angular/core';

import { isPlatformBrowser } from '@angular/common';

>>>>>>> origin/feature/integrated-app-event
import lottie from 'lottie-web';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
<<<<<<< HEAD
  styleUrls: ['./not-found.component.css'], // attention au pluriel "styleUrls"
})
export class NotFoundComponent implements AfterViewInit {
  constructor(@Inject(PLATFORM_ID) private readonly platformId: object) {}

  ngAfterViewInit(): void {
=======
  styleUrls: ['./not-found.component.css']
})
export class NotFoundComponent implements AfterViewInit {

  constructor(
    @Inject(PLATFORM_ID)
    private readonly platformId: Object
  ) {}

  ngAfterViewInit(): void {

    // ✅ Avoid SSR/browser errors
>>>>>>> origin/feature/integrated-app-event
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

<<<<<<< HEAD
    const container = document.querySelector('.lottie-animation');
=======
    const container =
      document.querySelector('.lottie-animation');

    // ✅ Avoid null container error
>>>>>>> origin/feature/integrated-app-event
    if (!container) {
      return;
    }

    lottie.loadAnimation({
<<<<<<< HEAD
      container,
      renderer: 'svg',
      loop: true,
      autoplay: true,
      path: 'https://lottie.host/d987597c-7676-4424-8817-7fca6dc1a33e/BVrFXsaeui.json',
    });
  }
}
=======

      container: container as Element,

      renderer: 'svg',

      loop: true,

      autoplay: true,

      path: 'https://lottie.host/d987597c-7676-4424-8817-7fca6dc1a33e/BVrFXsaeui.json'
    });
  }
}
>>>>>>> origin/feature/integrated-app-event
