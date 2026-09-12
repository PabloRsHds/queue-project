import { Component, computed, effect, inject } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { UserStateService } from '../../services/states/user/user-state.service';
import { TicketStateService } from '../../services/states/ticket/ticket-state.service';
import { VoiceService } from '../../services/voice/voice.service';
import { AttendentStateService } from '../../services/states/attendent/attendent-state.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-queue-display',
  imports: [CommonModule, FormsModule],
  templateUrl: './queue-display.component.html',
  styleUrl: './queue-display.component.css'
})
export class QueueDisplayComponent {

  // ==================== INJEÇÕES ====================
  public userState = inject(UserStateService);
  public ticketState = inject(TicketStateService);
  public attendentState = inject(AttendentStateService);
  private sanitizer = inject(DomSanitizer);
  private voiceService = inject(VoiceService);

  // ==================== ESTADOS ====================
  public userLogged = this.userState.userLogged;

  // Agora vem direto do WebSocket
  public ticketForPanel = this.ticketState.ticketForPanel;
  public historyTickets = this.ticketState.historyTickets;

  insertLink = false;
  urlInput = '';
  videoUrl!: SafeResourceUrl;
  private pollingSubscription?: Subscription;


  ngOnInit(): void {
    this.loadVideo(
      'https://www.youtube.com/watch?v=ofUOATVjKF0'
    );
    this.userState.getUserByToken();
    this.ticketState.getTicketsForAttendence();
    this.ticketState.getHistoryTicketsByAttendant();
  }

  constructor() {

    effect(() => {
      if(this.ticketForPanel()) {
        this.voiceService.speak(
          `${this.ticketForPanel()?.customerName}, dirija-se ao setor de ${this.ticketForPanel()?.departmentName}`
        );
      }
    });

  }

  // ==================== COMPUTED ====================
  public finishedTickets = computed(() =>
    this.historyTickets()
      .filter(ticket => ticket.status === 'FINISHED')
  );

  // ==================== VÍDEO ====================
  loadVideo(url: string) {
    const videoId = this.extractVideoId(url);
    if (!videoId) {
      console.error('URL inválida');
      return;
    }

    this.videoUrl =
      this.sanitizer.bypassSecurityTrustResourceUrl(
        `https://www.youtube.com/embed/${videoId}`
      );
  }

  confirmLink() {
    if (this.urlInput.trim()) {
      this.loadVideo(
        this.urlInput.trim()
      );

    } else {
      this.insertLink = false;
    }
  }

  private extractVideoId(url: string): string | null {

    const regex = /(?:youtube\.com\/watch\?v=|youtu\.be\/|youtube\.com\/embed\/)([^&?/]+)/;
    const match = url.match(regex);
    return match ? match[1] : null;

  }

  onInputBlur() {
    setTimeout(() => {
      this.insertLink = false;
    },150);
  }

}
