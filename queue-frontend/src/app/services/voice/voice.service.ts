import { Injectable, inject } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class VoiceService {
  private synth = window.speechSynthesis;
  private utterance: SpeechSynthesisUtterance | null = null;
  private chime = new Audio('/freesound_community-ding-dong-81717.mp3');

  // Configurações de voz
  private config = {
    rate: 0.9,      // Velocidade da fala (0.1 a 10)
    pitch: 1.0,     // Tom da voz (0 a 2)
    volume: 1.0,    // Volume (0 a 1)
    lang: 'pt-BR'   // Idioma
  };

  constructor() {
    this.chime.preload = 'auto';
  }

  public speak(text: string): void {

    this.stop();

    this.utterance = new SpeechSynthesisUtterance(text);

    this.utterance.rate = this.config.rate;
    this.utterance.pitch = this.config.pitch;
    this.utterance.volume = this.config.volume;
    this.utterance.lang = this.config.lang;

    const voices = this.synth.getVoices();
    const ptBrVoice = voices.find(v => v.lang.startsWith('pt'));

    if (ptBrVoice) {
      this.utterance.voice = ptBrVoice;
    }

    // Reinicia o áudio
    this.chime.currentTime = 0;

    // Quando terminar o sino, fala
    this.chime.play();

    setTimeout(() => {
        this.synth.speak(this.utterance!);
    }, 800);

    this.chime.oncanplaythrough = () => {
        console.timeEnd('audio');
    };

    this.chime.load();

    this.chime.play();
  }

  public stop(): void {
    if (this.synth.speaking) {
      this.synth.cancel();
    }
    this.utterance = null;
  }
}
