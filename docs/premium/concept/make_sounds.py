"""
Hävn sound palette — draft generator for the two new cues in the premium brief.

  sfx_day.wav   D5 · F#5 · A5 felt-marimba arpeggio, 90 ms apart, long soft tail.
  sfx_lift.wav  a short breath swell into a soft wooden "tok" at A5.

Output matches the existing app/src/main/res/raw/*.wav assets exactly:
22 050 Hz, mono, 16-bit PCM. Loudness is matched (RMS over the active part)
to the existing sfx_take.wav so the new cues sit at the same level.

Modal synthesis: a marimba bar's first overtones sit near 4x and 10x the
fundamental. A felt mallet = a slow (3–6 ms) raised-cosine attack and fast
decay of the upper modes, plus a gentle one-pole low-pass. Deterministic
(fixed seed), so re-running reproduces the same files.

Usage:  python3 make_sounds.py <out_dir> [reference_wav]
"""
import sys, wave
import numpy as np

SR = 22050
rng = np.random.default_rng(7)


def write(path, x):
    x = np.clip(x, -1, 1)
    with wave.open(path, "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((x * 32767).astype("<i2").tobytes())


def read(path):
    with wave.open(path, "rb") as w:
        return np.frombuffer(w.readframes(w.getnframes()), "<i2").astype(float) / 32767


def active_rms(x, floor=0.02):
    a = np.abs(x); m = a > floor * a.max()
    return float(np.sqrt(np.mean(x[m] ** 2)))


def lowpass(x, fc):
    a = np.exp(-2 * np.pi * fc / SR); y = np.empty_like(x); s = 0.0
    for i, v in enumerate(x):
        s = (1 - a) * v + a * s; y[i] = s
    return y


def felt_note(f, dur, amp=1.0, attack=0.004):
    t = np.arange(int(dur * SR)) / SR
    modes = [(1.0, 1.00, 1.15), (3.93, 0.22, 0.32), (9.2, 0.05, 0.10)]  # ratio, gain, T60-ish
    x = sum(g * np.sin(2 * np.pi * f * r * t) * np.exp(-6.9 * t / d) for r, g, d in modes)
    # Resonator tube: a little sustained fundamental
    x += 0.18 * np.sin(2 * np.pi * f * t) * np.exp(-6.9 * t / (1.6 * dur))
    n = int(attack * SR); env = np.ones_like(t); env[:n] = 0.5 - 0.5 * np.cos(np.linspace(0, np.pi, n))
    return amp * x * env


def sfx_day():
    total = 1.6; out = np.zeros(int(total * SR))
    for i, (f, a) in enumerate([(587.33, 0.9), (739.99, 0.8), (880.0, 0.85)]):
        start = int(i * 0.09 * SR); note = felt_note(f, total - i * 0.09, a)
        out[start:start + len(note)] += note[: len(out) - start]
    out = lowpass(out, 5000)
    fade = int(0.25 * SR); out[-fade:] *= np.linspace(1, 0, fade) ** 2
    return out


def sfx_lift():
    total = 0.16; n = int(total * SR); t = np.arange(n) / SR
    breath = lowpass(rng.standard_normal(n), 2400) - lowpass(rng.standard_normal(n), 300)
    swell = np.clip(t / 0.07, 0, 1) ** 2 * np.exp(-np.clip(t - 0.07, 0, None) / 0.025)
    out = 0.35 * breath * swell
    tok = felt_note(880.0, total - 0.06, 0.9, attack=0.003)
    tok *= np.exp(-np.arange(len(tok)) / SR / 0.05)
    s = int(0.06 * SR); out[s:s + len(tok)] += tok[: n - s]
    out = lowpass(out, 6000)
    out[-int(0.02 * SR):] *= np.linspace(1, 0, int(0.02 * SR))
    return out


if __name__ == "__main__":
    out_dir = sys.argv[1]
    ref = read(sys.argv[2]) if len(sys.argv) > 2 else None
    target = active_rms(ref) if ref is not None else 0.12
    for name, fn in [("sfx_day", sfx_day), ("sfx_lift", sfx_lift)]:
        x = fn(); x *= target / active_rms(x)
        peak = np.abs(x).max()
        if peak > 0.89: x *= 0.89 / peak  # keep 1 dB of headroom
        write(f"{out_dir}/{name}.wav", x)
        print(f"{name}: {len(x)/SR:.2f}s peak {20*np.log10(np.abs(x).max()):.1f} dBFS active-RMS {20*np.log10(active_rms(x)):.1f} dBFS")
    if ref is not None:
        print(f"reference sfx_take: peak {20*np.log10(np.abs(ref).max()):.1f} dBFS active-RMS {20*np.log10(active_rms(ref)):.1f} dBFS")
