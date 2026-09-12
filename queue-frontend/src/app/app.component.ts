import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { WebSocketService } from './services/websocket/websocket-service.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

  title = 'queue-frontend';

  private websocket = inject(WebSocketService);

  ngOnInit() {
    this.websocket.connect();
  }
}
