/**
 * Hävn Sound System — Warm Acoustic & Muji Ceramic Audio Engine
 * Zero external asset dependencies.
 * Synthesizes organic, acoustic, physically-modeled soundscapes via the Web Audio API:
 *  - Muted ceramic container lid clicks
 *  - Warm 432Hz Tibetan singing bowl / meditative harmonic bell chimes
 *  - Soft wooden finger-pad haptic taps
 *  - Organic rosewood marimba strikes
 */

class SoundService {
  private ctx: AudioContext | null = null;
  private enabled = true;
  private noiseBuffer: AudioBuffer | null = null;

  private getContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.ctx) {
      const AudioCtxClass =
        window.AudioContext ||
        (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (AudioCtxClass) {
        this.ctx = new AudioCtxClass();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume().catch(() => {});
    }
    return this.ctx;
  }

  // Pre-generate a 0.5s pink/brown noise buffer for soft, organic acoustic transient thuds
  private getNoiseBuffer(ctx: AudioContext): AudioBuffer {
    if (this.noiseBuffer && this.noiseBuffer.sampleRate === ctx.sampleRate) {
      return this.noiseBuffer;
    }
    const length = Math.floor(ctx.sampleRate * 0.5);
    const buffer = ctx.createBuffer(1, length, ctx.sampleRate);
    const output = buffer.getChannelData(0);
    let b0 = 0, b1 = 0, b2 = 0;
    for (let i = 0; i < length; i++) {
      const white = Math.random() * 2 - 1;
      // Pink noise filter
      b0 = 0.99886 * b0 + white * 0.0555179;
      b1 = 0.99332 * b1 + white * 0.0750759;
      b2 = 0.96900 * b2 + white * 0.1538520;
      output[i] = (b0 + b1 + b2 + white * 0.5362) * 0.12;
    }
    this.noiseBuffer = buffer;
    return buffer;
  }

  setEnabled(enabled: boolean) {
    this.enabled = enabled;
  }

  /**
   * Ceramic Lid Click:
   * Recreates the acoustic sound of a matte ceramic or stone pill container lid
   * closing or latching softly. Muted, tactile, warm, hollow acoustic body.
   */
  playCeramicClick() {
    if (!this.enabled) return;
    try {
      const ctx = this.getContext();
      if (!ctx) return;
      const now = ctx.currentTime;

      // Master bus for this click
      const master = ctx.createGain();
      master.gain.setValueAtTime(0.45, now);
      master.connect(ctx.destination);

      // Layer 1: Soft tactile transient impact (pink noise bandpassed at 750Hz)
      const noise = ctx.createBufferSource();
      noise.buffer = this.getNoiseBuffer(ctx);
      const noiseFilter = ctx.createBiquadFilter();
      noiseFilter.type = 'bandpass';
      noiseFilter.frequency.setValueAtTime(750, now);
      noiseFilter.Q.setValueAtTime(2.2, now);

      const noiseGain = ctx.createGain();
      noiseGain.gain.setValueAtTime(0.0001, now);
      noiseGain.gain.linearRampToValueAtTime(0.35, now + 0.003);
      noiseGain.gain.exponentialRampToValueAtTime(0.0001, now + 0.038);

      noise.connect(noiseFilter);
      noiseFilter.connect(noiseGain);
      noiseGain.connect(master);
      noise.start(now);
      noise.stop(now + 0.045);

      // Layer 2: Hollow ceramic cavity resonance (warm sine, 340Hz decaying to 270Hz)
      const osc = ctx.createOscillator();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(340, now);
      osc.frequency.exponentialRampToValueAtTime(270, now + 0.045);

      const lowpass = ctx.createBiquadFilter();
      lowpass.type = 'lowpass';
      lowpass.frequency.setValueAtTime(620, now);

      const oscGain = ctx.createGain();
      oscGain.gain.setValueAtTime(0.0001, now);
      oscGain.gain.linearRampToValueAtTime(0.28, now + 0.002);
      oscGain.gain.exponentialRampToValueAtTime(0.0001, now + 0.065);

      osc.connect(lowpass);
      lowpass.connect(oscGain);
      oscGain.connect(master);
      osc.start(now);
      osc.stop(now + 0.07);
    } catch (err) {
      console.warn('Audio click failed:', err);
    }
  }

  /**
   * Warm Harmonic Chime (432Hz Tibetan Singing Bowl / Wellness Bell):
   * Pure, soothing, resonant mindfulness bell with 432Hz fundamental,
   * natural harmonic decay, gentle acoustic beating, and zero digital harshness.
   */
  playSoftChime() {
    if (!this.enabled) return;
    try {
      const ctx = this.getContext();
      if (!ctx) return;
      const now = ctx.currentTime;

      // Master output with gentle lowpass to ensure pure warm acoustic timbre
      const masterFilter = ctx.createBiquadFilter();
      masterFilter.type = 'lowpass';
      masterFilter.frequency.setValueAtTime(2200, now);

      const master = ctx.createGain();
      master.gain.setValueAtTime(0.4, now);
      masterFilter.connect(master);
      master.connect(ctx.destination);

      // Harmonics for a Tibetan Singing Bowl / Acoustic Meditation Bell
      const harmonics = [
        { freq: 432.0, gain: 0.55, decay: 2.2 }, // Fundamental
        { freq: 433.4, gain: 0.38, decay: 2.0 }, // Subtle 1.4Hz binaural warmth/beating
        { freq: 648.0, gain: 0.22, decay: 1.8 }, // Perfect fifth
        { freq: 864.0, gain: 0.12, decay: 1.4 }, // Octave
        { freq: 1296.0, gain: 0.04, decay: 0.9 }, // 3rd harmonic sparkle (very subtle)
      ];

      harmonics.forEach((h) => {
        const osc = ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(h.freq, now);

        const gainNode = ctx.createGain();
        gainNode.gain.setValueAtTime(0.0001, now);
        // Soft 18ms attack avoids digital click on start
        gainNode.gain.linearRampToValueAtTime(h.gain, now + 0.018);
        // Long natural acoustic exponential decay
        gainNode.gain.exponentialRampToValueAtTime(0.00005, now + h.decay);

        osc.connect(gainNode);
        gainNode.connect(masterFilter);
        osc.start(now);
        osc.stop(now + h.decay + 0.1);
      });

      // Subtle warm second chime note 110ms later for an uplifting ascending pair (576Hz - D5)
      setTimeout(() => {
        if (!this.enabled || !this.ctx) return;
        try {
          const cNow = this.ctx.currentTime;
          const osc2 = this.ctx.createOscillator();
          osc2.type = 'sine';
          osc2.frequency.setValueAtTime(576, cNow);

          const gain2 = this.ctx.createGain();
          gain2.gain.setValueAtTime(0.0001, cNow);
          gain2.gain.linearRampToValueAtTime(0.24, cNow + 0.02);
          gain2.gain.exponentialRampToValueAtTime(0.00005, cNow + 1.8);

          osc2.connect(gain2);
          gain2.connect(masterFilter);
          osc2.start(cNow);
          osc2.stop(cNow + 1.9);
        } catch {}
      }, 110);
    } catch (err) {
      console.warn('Audio chime failed:', err);
    }
  }

  /**
   * Soft Wooden / Matte Tap:
   * A gentle acoustic finger-pad tap on smooth wood or stone.
   * Completely replaces artificial pitch-drop zaps with a soft, tactile thud.
   */
  playSoftTap() {
    if (!this.enabled) return;
    try {
      const ctx = this.getContext();
      if (!ctx) return;
      const now = ctx.currentTime;

      // Soft low-passed rounded thump
      const osc = ctx.createOscillator();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(145, now);
      osc.frequency.exponentialRampToValueAtTime(95, now + 0.025);

      const filter = ctx.createBiquadFilter();
      filter.type = 'lowpass';
      filter.frequency.setValueAtTime(360, now);

      const gain = ctx.createGain();
      gain.gain.setValueAtTime(0.0001, now);
      gain.gain.linearRampToValueAtTime(0.24, now + 0.002);
      gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.032);

      osc.connect(filter);
      filter.connect(gain);
      gain.connect(ctx.destination);

      osc.start(now);
      osc.stop(now + 0.038);
    } catch (err) {
      console.warn('Audio tap failed:', err);
    }
  }

  /**
   * Acoustic Wooden Marimba Bar:
   * Rosewood marimba struck with a soft mallet. Warm, round, natural resonance.
   */
  playMarimbaNote() {
    if (!this.enabled) return;
    try {
      const ctx = this.getContext();
      if (!ctx) return;
      const now = ctx.currentTime;

      const master = ctx.createGain();
      master.gain.setValueAtTime(0.42, now);
      master.connect(ctx.destination);

      // Fundamental and double-octave overtone characteristic of rosewood bars
      const partials = [
        { freq: 392.0, gain: 0.6, decay: 0.75 }, // G4
        { freq: 1568.0, gain: 0.15, decay: 0.12 }, // Rapidly decaying acoustic strike overtone
      ];

      partials.forEach((p) => {
        const osc = ctx.createOscillator();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(p.freq, now);

        const gain = ctx.createGain();
        gain.gain.setValueAtTime(0.0001, now);
        gain.gain.linearRampToValueAtTime(p.gain, now + 0.004);
        gain.gain.exponentialRampToValueAtTime(0.0001, now + p.decay);

        osc.connect(gain);
        gain.connect(master);
        osc.start(now);
        osc.stop(now + p.decay + 0.05);
      });
    } catch (err) {
      console.warn('Audio marimba failed:', err);
    }
  }
}

export const soundManager = new SoundService();
