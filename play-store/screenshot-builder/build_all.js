const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const BUILDER_DIR = path.resolve(__dirname);
const FONTS_DIR = path.join(BUILDER_DIR, 'fonts');
const GRAPHICS_DIR = path.resolve(BUILDER_DIR, '..', 'graphics');
const OUT_ROOT = path.join(GRAPHICS_DIR, 'screenshots');
const CHROME_BIN = process.env.CHROME_BIN || '/usr/bin/google-chrome';

const DIRS = ['phone', 'flip', 'foldable', 'tablet-7', 'tablet-10'];
for (const d of DIRS) {
  fs.mkdirSync(path.join(OUT_ROOT, d), { recursive: true });
}

// Clean up stale v1 top-level screenshot PNGs/HTMLs (keep builder tidy)
for (const oldFile of [
  '01_daily_ritual.png',
  '02_3d_organizer.png',
  '03_progress_adherence.png',
  '04_customize_regimen.png',
  '05_sanctuary_reminders.png',
  '06_privacy_and_backup.png',
]) {
  const p = path.join(OUT_ROOT, oldFile);
  if (fs.existsSync(p)) fs.unlinkSync(p);
}

const FONT_CSS = `
@font-face { font-family: "InstrumentSerif"; src: url("file://${FONTS_DIR}/instrument_serif.ttf"); font-weight: 400; font-style: normal; }
@font-face { font-family: "InstrumentSerif"; src: url("file://${FONTS_DIR}/instrument_serif_italic.ttf"); font-weight: 400; font-style: italic; }
@font-face { font-family: "Archivo"; src: url("file://${FONTS_DIR}/archivo.ttf"); font-weight: 100 900; font-style: normal; }
@font-face { font-family: "IBMPlexMono"; src: url("file://${FONTS_DIR}/ibm_plex_mono_regular.ttf"); font-weight: 400; font-style: normal; }
@font-face { font-family: "IBMPlexMono"; src: url("file://${FONTS_DIR}/ibm_plex_mono_medium.ttf"); font-weight: 500; font-style: normal; }
`;

const BASE_CSS = `
${FONT_CSS}
:root {
  --bg: #F4EFE6;
  --card: #FBF8F1;
  --card2: #EAE4D7;
  --ink: #1B1A22;
  --ink2: #4C4A44;
  --muted: #6F6A60;
  --rule: #DCD5C7;
  --rule-strong: #C4BBA8;
  --tang: #FF6B2C;
  --tang-deep: #B93E0B;
  --lilac: #D9C8F4;
  --peri: #B6B1E8;
  --sage: #A9B8A0;
  --butter: #F1DFA2;
  --rose: #F2B1AA;

  --night-bg: #16151B;
  --night-card: #1F1E26;
  --night-card2: #121116;
  --night-ink: #C9C3B6;
  --night-muted: #9A958A;
  --night-rule: #2E2C36;
}
* { box-sizing: border-box; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
body {
  overflow: hidden;
  font-family: "Archivo", sans-serif;
  background: var(--bg);
  color: var(--ink);
  position: relative;
}
.grain {
  position: absolute; inset: 0; opacity: 0.036; pointer-events: none; z-index: 999;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='180' height='180' filter='url(%23n)' opacity='1'/%3E%3C/svg%3E");
}
.mono-tag {
  font-family: "IBMPlexMono", monospace;
  font-size: 13px;
  font-weight: 500;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--muted);
}
.serif { font-family: "InstrumentSerif", serif; font-weight: 400; }
.serif-it { font-family: "InstrumentSerif", serif; font-weight: 400; font-style: italic; }

/* Shared App Screen Internals */
.app-vp {
  width: 100%;
  height: 100%;
  background: var(--bg);
  color: var(--ink);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  font-family: "Archivo", sans-serif;
}
.app-vp.dark {
  background: var(--night-bg);
  color: var(--night-ink);
}
.statusbar {
  height: 44px;
  padding: 14px 26px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: "IBMPlexMono", monospace;
  font-size: 13px;
  font-weight: 500;
  flex-shrink: 0;
}
.status-icons { display: flex; align-items: center; gap: 7px; }
.app-body {
  flex: 1;
  padding: 12px 22px 92px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow: hidden;
}
.app-body.centered-col {
  max-width: 580px;
  width: 100%;
  margin: 0 auto;
}
.screen-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}
.screen-title {
  font-family: "InstrumentSerif", serif;
  font-size: 46px;
  line-height: 0.96;
  letter-spacing: -0.025em;
}
.profile-pill {
  font-family: "IBMPlexMono", monospace;
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid var(--rule-strong);
  background: var(--card);
  color: var(--ink2);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.dark .profile-pill {
  background: var(--night-card);
  border-color: var(--night-rule);
  color: var(--night-ink);
}
.day-tape {
  border: 1px solid var(--rule);
  background: var(--card);
  border-radius: 18px;
  padding: 10px 14px 12px;
}
.tape-ticks {
  display: flex;
  justify-content: space-between;
  font-family: "IBMPlexMono", monospace;
  font-size: 10.5px;
  color: var(--muted);
  margin-bottom: 7px;
}
.tape-track {
  position: relative;
  height: 14px;
  border-radius: 999px;
  background: var(--card2);
  display: flex;
  align-items: center;
}
.tape-fill {
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 34%;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255,107,44,0.22), rgba(255,107,44,0.45));
}
.tape-needle {
  position: absolute;
  left: 34%;
  top: -4px; bottom: -4px;
  width: 2.5px;
  border-radius: 2px;
  background: var(--tang);
}
.tape-dot {
  position: absolute;
  width: 9px; height: 9px;
  border-radius: 50%;
  border: 1.5px solid var(--ink);
}
.tape-dot.done { left: 19%; background: var(--sage); }
.tape-dot.next { left: 37%; background: var(--tang); width: 11px; height: 11px; }
.tape-dot.later { left: 66%; background: var(--lilac); }
.tape-dot.night { left: 86%; background: var(--peri); }

.aura-card {
  background:
    radial-gradient(ellipse at 86% 14%, rgba(255,154,107,0.44) 0%, rgba(217,200,244,0.36) 46%, rgba(251,248,241,0.96) 94%),
    var(--card);
  border: 1.5px solid var(--ink);
  border-radius: 26px;
  padding: 20px;
}
.dark .aura-card {
  background:
    radial-gradient(ellipse at 84% 16%, rgba(194,80,31,0.48) 0%, rgba(122,58,82,0.34) 48%, rgba(31,30,38,0.98) 92%),
    var(--night-card);
  border-color: rgba(201,195,182,0.28);
}
.card {
  background: var(--card);
  border: 1px solid var(--rule);
  border-radius: 24px;
  padding: 18px;
}
.dark .card {
  background: var(--night-card);
  border-color: var(--night-rule);
}
.hero-sub { font-size: 14px; color: var(--ink2); margin-top: 6px; }
.dark .hero-sub { color: var(--night-muted); }
.hero-big {
  font-family: "InstrumentSerif", serif;
  font-size: 56px;
  line-height: 0.96;
  letter-spacing: -0.028em;
  margin-top: 2px;
}
.dose-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(251,248,241,0.85);
  border: 1px solid var(--rule);
  border-radius: 16px;
  padding: 12px 14px;
  margin-top: 12px;
}
.dark .dose-item {
  background: rgba(18,17,22,0.58);
  border-color: var(--night-rule);
}
.pill-glyph {
  width: 28px; height: 15px; border-radius: 999px;
  border: 1.5px solid var(--ink);
  transform: rotate(-18deg);
  flex-shrink: 0;
}
.pill-glyph.tang { background: linear-gradient(90deg, #FF6B2C 50%, #FBF8F1 50%); }
.pill-glyph.yellow { background: #F2D24B; width: 22px; height: 15px; }
.pill-glyph.cream { background: #F3E6C8; }
.pill-glyph.rose { background: #E7A3B4; width: 18px; height: 18px; transform: none; }
.pill-glyph.peri { background: #7FA6D6; width: 28px; height: 13px; transform: none; }

.btn-row { display: flex; gap: 10px; margin-top: 14px; }
.btn-primary {
  flex: 1.4; height: 44px; border-radius: 999px;
  background: var(--ink); color: var(--card);
  font-size: 14px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
}
.dark .btn-primary {
  background: var(--night-ink); color: var(--night-bg);
}
.btn-sec {
  flex: 1; height: 44px; border-radius: 999px;
  border: 1px solid var(--rule-strong); background: rgba(251,248,241,0.7);
  color: var(--ink); font-size: 13.5px; font-weight: 500;
  display: flex; align-items: center; justify-content: center;
}
.dark .btn-sec {
  background: rgba(31,30,38,0.8); border-color: var(--night-rule); color: var(--night-ink);
}
.liv-note {
  display: flex; align-items: center; gap: 11px;
  padding: 11px 14px; border-radius: 18px;
  background: var(--card); border: 1px solid var(--rule);
  font-size: 13.5px; color: var(--ink2); line-height: 1.32;
}
.liv-dot {
  width: 24px; height: 24px; border-radius: 50%;
  border: 1.5px solid var(--ink); background: #FFF2E6;
  display: flex; align-items: center; justify-content: center; gap: 4px;
  flex-shrink: 0;
}
.liv-dot i { width: 3px; height: 4px; border-radius: 2px; background: var(--ink); display: block; }
.section-hdr {
  font-family: "IBMPlexMono", monospace; font-size: 11.5px; font-weight: 500;
  letter-spacing: 0.14em; text-transform: uppercase; color: var(--muted);
  display: flex; justify-content: space-between;
}
.list-row {
  display: flex; align-items: center; justify-content: space-between; gap: 10px;
  padding: 12px 14px; border-radius: 16px;
  background: var(--card); border: 1px solid var(--rule);
}
.dark .list-row { background: var(--night-card); border-color: var(--night-rule); }
.check-badge {
  width: 24px; height: 24px; border-radius: 50%;
  background: rgba(169,184,160,0.28); border: 1.5px solid #6E8264;
  color: #3E4F37; font-size: 12px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}

/* Segmented & Chips */
.seg {
  display: flex; padding: 4px; border-radius: 999px;
  background: var(--card2); border: 1px solid var(--rule);
}
.dark .seg { background: var(--night-card2); border-color: var(--night-rule); }
.seg-item {
  flex: 1; text-align: center; padding: 7px 10px; border-radius: 999px;
  font-size: 13px; font-weight: 500; color: var(--ink2);
}
.dark .seg-item { color: var(--night-muted); }
.seg-item.on { background: var(--ink); color: var(--card); font-weight: 600; }
.dark .seg-item.on { background: var(--night-ink); color: var(--night-bg); }
.chips-row { display: flex; gap: 7px; flex-wrap: nowrap; }
.chip {
  padding: 6px 13px; border-radius: 999px; border: 1px solid var(--rule-strong);
  background: var(--card); font-size: 12.5px; font-weight: 500; color: var(--ink2); white-space: nowrap;
}
.chip.on { background: var(--ink); border-color: var(--ink); color: var(--card); }
.dark .chip { background: var(--night-card); border-color: var(--night-rule); color: var(--night-muted); }
.dark .chip.on { background: var(--night-ink); color: var(--night-bg); }

/* History Calendar Stamps */
.cal-grid {
  display: grid; grid-template-columns: repeat(7, 1fr); gap: 5px; margin-top: 8px; justify-items: center;
}
.cal-dow {
  text-align: center; font-family: "IBMPlexMono", monospace; font-size: 10.5px; color: var(--muted);
}
.cal-cell {
  width: 34px; height: 34px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-family: "IBMPlexMono", monospace; font-size: 11px;
}
.cal-cell.kept {
  background: rgba(255,107,44,0.14); border: 1.5px solid #FF6B2C; color: #B93E0B; font-weight: 600;
}
.cal-cell.partial {
  border: 1.5px dashed #C98B6B; color: var(--ink2);
}
.cal-cell.future { color: #B2AAA0; }

/* Bottom Pill TabBar (RootKt) */
.tabbar-wrap {
  position: absolute; bottom: 14px; left: 18px; right: 18px;
  display: flex; justify-content: center; pointer-events: none; z-index: 20;
}
.tabbar {
  width: 100%; max-width: 430px; height: 54px; border-radius: 999px;
  background: rgba(251,248,241,0.96); border: 1px solid var(--rule-strong);
  box-shadow: 0 10px 26px rgba(27,26,34,0.10);
  display: flex; align-items: center; justify-content: space-around; padding: 0 8px;
}
.dark .tabbar {
  background: rgba(31,30,38,0.96); border-color: var(--night-rule);
  box-shadow: 0 12px 30px rgba(0,0,0,0.42);
}
.tab {
  display: flex; align-items: center; gap: 6px; padding: 7px 14px; border-radius: 999px;
  font-size: 12.5px; font-weight: 500; color: var(--muted);
}
.dark .tab { color: var(--night-muted); }
.tab.active { background: var(--ink); color: var(--card); font-weight: 600; }
.dark .tab.active { background: var(--night-ink); color: var(--night-bg); }
.tab-dot { width: 7px; height: 7px; border-radius: 50%; background: var(--tang); }
`;

function tabBar(activeKey) {
  const tabs = [
    ['today', 'Today'],
    ['history', 'History'],
    ['meds', 'Medicines'],
    ['settings', 'Settings'],
  ];
  return `
  <div class="tabbar-wrap">
    <div class="tabbar">
      ${tabs
        .map(
          ([k, label]) =>
            `<div class="tab ${k === activeKey ? 'active' : ''}">${
              k === activeKey ? '<span class="tab-dot"></span>' : ''
            }${label}</div>`
        )
        .join('')}
    </div>
  </div>`;
}

function statusBar(time = '08:14', dark = false) {
  const fg = dark ? '#C9C3B6' : '#1B1A22';
  return `
  <div class="statusbar" style="color:${fg}">
    <span>${time}</span>
    <span class="status-icons">
      <span>5G</span>
      <svg width="16" height="11" viewBox="0 0 16 11" fill="none">
        <rect x="0.7" y="0.7" width="12.6" height="9.6" rx="2.4" stroke="${fg}" stroke-width="1.3"/>
        <rect x="2.2" y="2.2" width="8.8" height="6.6" rx="1" fill="${fg}"/>
        <rect x="14" y="3.3" width="1.7" height="4.4" rx="0.8" fill="${fg}"/>
      </svg>
    </span>
  </div>`;
}

// =========================================================================
// REUSABLE 30.aab SCREEN RENDERERS (lightweight composable-accurate blocks)
// =========================================================================

function screenToday({ wide = false, time = '08:14', compact = false, shortPane = false } = {}) {
  const bodyPad = compact ? 'padding:8px 20px 16px;gap:10px;justify-content:space-between;' : shortPane ? 'padding:8px 22px 80px;gap:11px;' : '';
  return `
  <div class="app-vp">
    ${statusBar(time)}
    <div class="app-body ${wide ? 'centered-col' : ''}" style="${bodyPad}">
      <div style="display:flex;flex-direction:column;gap:${compact ? 10 : 12}px;">
        <div class="screen-head">
          <div>
            <div class="mono-tag">[ THURSDAY · 12 NOV ]</div>
            <div class="screen-title" style="margin-top:4px;font-size:${compact ? 34 : 44}px;">Good morning, Freja</div>
          </div>
          <div class="profile-pill"><span style="width:7px;height:7px;border-radius:50%;background:var(--tang);"></span>Freja</div>
        </div>

        <div class="day-tape" style="${compact ? 'padding:8px 12px 10px;' : ''}">
          <div class="tape-ticks"><span>06</span><span>12</span><span>18</span><span>23</span></div>
          <div class="tape-track">
            <div class="tape-fill"></div>
            <div class="tape-dot done"></div>
            <div class="tape-needle"></div>
            <div class="tape-dot next"></div>
            <div class="tape-dot later"></div>
            <div class="tape-dot night"></div>
          </div>
        </div>
      </div>

      <div class="aura-card" style="padding:${compact ? '14px 16px' : '18px 20px'};">
        <div class="mono-tag">[ MORNING · 08:00 ]</div>
        <div class="hero-sub">Next dose in</div>
        <div class="hero-big" style="font-size:${compact ? 40 : 54}px;">42 min</div>
        <div class="dose-item" style="margin-top:${compact ? 10 : 12}px;">
          <div class="pill-glyph tang"></div>
          <div style="flex:1;">
            <div style="font-size:15px;font-weight:600;">Vitamin D3 · 2000 IU</div>
            <div style="font-size:12.5px;color:var(--muted);margin-top:2px;">With breakfast · 1 capsule</div>
          </div>
          <span class="mono-tag" style="font-size:11px;">08:00</span>
        </div>
        ${
          compact || shortPane
            ? ''
            : `<div class="dose-item" style="margin-top:8px;">
                <div class="pill-glyph yellow"></div>
                <div style="flex:1;">
                  <div style="font-size:15px;font-weight:600;">Magnesium · 200 mg</div>
                  <div style="font-size:12.5px;color:var(--muted);margin-top:2px;">With food · 1 tablet</div>
                </div>
                <span class="mono-tag" style="font-size:11px;">08:00</span>
              </div>`
        }
        <div class="btn-row" style="margin-top:${compact ? 11 : 14}px;">
          <div class="btn-primary" style="height:${compact ? 40 : 44}px;">Took it</div>
          <div class="btn-sec" style="height:${compact ? 40 : 44}px;">In 15 min</div>
        </div>
      </div>

      ${
        compact
          ? ''
          : `<div class="liv-note">
              <div class="liv-dot"><i></i><i></i></div>
              <span>“Everything for morning sits right here.”</span>
            </div>
            ${
              shortPane
                ? ''
                : `<div class="section-hdr"><span>EARLIER TODAY</span><span>1 OF 1 KEPT</span></div>
                  <div class="list-row">
                    <div style="display:flex;align-items:center;gap:11px;">
                      <div class="pill-glyph cream"></div>
                      <div>
                        <div style="font-size:14.5px;font-weight:600;">Levothyroxine · 50 mcg</div>
                        <div style="font-size:12px;color:var(--muted);">Wake · Taken at 07:08</div>
                      </div>
                    </div>
                    <div class="check-badge">✓</div>
                  </div>`
            }`
      }
    </div>
    ${compact ? '' : tabBar('today')}
  </div>`;
}

function screenHistory({ wide = false, compact = false, shortPane = false } = {}) {
  const days = [
    [1, 'kept'], [2, 'kept'], [3, 'kept'], [4, 'kept'], [5, 'kept'], [6, 'partial'], [7, 'kept'],
    [8, 'kept'], [9, 'kept'], [10, 'kept'], [11, 'kept'], [12, 'kept'], [13, 'kept'], [14, 'kept'],
    [15, 'kept'], [16, 'kept'], [17, 'kept'], [18, 'kept'], [19, 'kept'], [20, 'partial'], [21, 'kept'],
    [22, 'kept'], [23, 'kept'], [24, 'kept'], [25, 'kept'], [26, 'kept'], [27, 'kept'], [28, 'future'],
  ];
  const bodyPad = compact ? 'padding:8px 20px 14px;gap:8px;justify-content:space-between;' : shortPane ? 'padding:8px 22px 80px;gap:10px;' : '';
  return `
  <div class="app-vp">
    ${statusBar('08:14')}
    <div class="app-body ${wide ? 'centered-col' : ''}" style="${bodyPad}">
      <div style="display:flex;flex-direction:column;gap:${compact ? 8 : 11}px;">
        <div class="screen-head">
          <div class="screen-title" style="font-size:${compact ? 32 : 42}px;">History</div>
          <div class="mono-tag">[ NOVEMBER ]</div>
        </div>
        <div class="seg" style="padding:3px;">
          <div class="seg-item" style="padding:${compact ? 5 : 7}px 10px;">7 days</div>
          <div class="seg-item on" style="padding:${compact ? 5 : 7}px 10px;">30 days</div>
          <div class="seg-item" style="padding:${compact ? 5 : 7}px 10px;">90 days</div>
        </div>

        <div class="card" style="padding:${compact ? '10px 14px' : '15px 18px'};">
          <div style="display:flex;align-items:center;justify-content:space-between;">
            <div>
              <div class="mono-tag">[ KEPT ]</div>
              <div class="serif" style="font-size:${compact ? 54 : 84}px;line-height:0.86;letter-spacing:-0.035em;margin-left:-4px;margin-top:2px;">26</div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:14px;font-weight:600;">of the last 30 days</div>
              <div style="font-size:12px;color:var(--muted);margin-top:2px;">87% calm rhythm</div>
            </div>
          </div>
          <div style="height:4px;border-radius:999px;background:var(--card2);margin-top:${compact ? 6 : 9}px;overflow:hidden;">
            <div style="width:87%;height:100%;background:var(--tang);border-radius:999px;"></div>
          </div>
        </div>
      </div>

      ${
        compact || shortPane
          ? ''
          : `<div class="liv-note">
              <div class="liv-dot"><i></i><i></i></div>
              <span>“Twenty-six days held steady. Morning has your strongest rhythm.”</span>
            </div>`
      }

      <div class="card" style="padding:${compact ? '11px 14px' : '13px 16px'};">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span class="mono-tag">[ MONTHLY LEDGER ]</span>
          <span style="font-size:11.5px;color:var(--muted);">✓ kept · dashed partial</span>
        </div>
        <div class="cal-grid" style="gap:${compact ? 4 : 5}px;">
          <div class="cal-dow">M</div><div class="cal-dow">T</div><div class="cal-dow">W</div><div class="cal-dow">T</div><div class="cal-dow">F</div><div class="cal-dow">S</div><div class="cal-dow">S</div>
          ${days
            .slice(0, compact ? 14 : shortPane ? 21 : 28)
            .map(([n, st]) => `<div class="cal-cell ${st}" style="width:${compact || shortPane ? 31 : 35}px;height:${compact || shortPane ? 31 : 35}px;">${st === 'kept' ? '✓' : n}</div>`)
            .join('')}
        </div>
        ${
          compact
            ? ''
            : `<div class="btn-row" style="margin-top:10px;">
                <div class="btn-sec" style="height:36px;font-size:12.5px;">Save month card</div>
                <div class="btn-primary" style="height:36px;font-size:12.5px;">Doctor PDF</div>
              </div>`
        }
      </div>
    </div>
    ${compact ? '' : tabBar('history')}
  </div>`;
}

function screenMedicines({ wide = false, shortPane = false } = {}) {
  const meds = [
    ['Levothyroxine', '50 mcg · Every day · Empty stomach · 07:00', '24 left', 'cream'],
    ['Vitamin D3', '2000 IU · Every day · Breakfast · 08:00', '38 left', 'tang'],
    ['Magnesium', '200 mg · Every day · With food · 08:00', '18 left', 'yellow'],
    ['Micronor', '0.35 mg · Cycle 21 on · 7 off · 21:00', '16 left', 'rose'],
    ['Ibuprofen', '400 mg · As needed · Up to 3 a day', 'As needed', 'peri'],
  ];
  const visibleMeds = shortPane ? meds.slice(0, 4) : meds;
  return `
  <div class="app-vp">
    ${statusBar('08:14')}
    <div class="app-body ${wide ? 'centered-col' : ''}" style="${shortPane ? 'padding:8px 22px 80px;gap:10px;' : ''}">
      <div class="screen-head">
        <div class="screen-title">Medicines</div>
        <div class="profile-pill">+ Add</div>
      </div>
      <div class="list-row" style="padding:10px 14px;color:var(--muted);font-size:13.5px;">
        <span>Search medicines...</span>
        <span class="mono-tag" style="font-size:11px;">[ 5 ON SHELF ]</span>
      </div>
      <div class="chips-row">
        <span class="chip on">All</span>
        <span class="chip">Daily</span>
        <span class="chip">As needed</span>
        <span class="chip">Paused</span>
      </div>
      <div style="display:flex;flex-direction:column;gap:${shortPane ? 8 : 9}px;">
        ${visibleMeds
          .map(
            ([name, sub, tag, g]) => `
            <div class="list-row" style="padding:${shortPane ? '10px 13px' : '13px 14px'};">
              <div style="display:flex;align-items:center;gap:12px;">
                <div class="pill-glyph ${g}"></div>
                <div>
                  <div style="font-size:15px;font-weight:600;">${name}</div>
                  <div style="font-size:12px;color:var(--muted);margin-top:2px;">${sub}</div>
                </div>
              </div>
              <span class="mono-tag" style="font-size:11px;white-space:nowrap;">${tag}</span>
            </div>`
          )
          .join('')}
      </div>
      ${
        shortPane
          ? ''
          : `<div class="liv-note">
              <div class="liv-dot"><i></i><i></i></div>
              <span>“Names stay exactly as typed. Hävn never suggests a dose.”</span>
            </div>`
      }
    </div>
    ${tabBar('meds')}
  </div>`;
}

function screenTravelPeople({ wide = false, shortPane = false } = {}) {
  const anchors = [
    ['Wake up', '07:00'],
    ['Breakfast', '07:45'],
    ['Lunch', '12:30'],
    ['Dinner', '18:30'],
    ['Bedtime', '22:30'],
  ];
  return `
  <div class="app-vp">
    ${statusBar('08:14')}
    <div class="app-body ${wide ? 'centered-col' : ''}" style="${shortPane ? 'padding:8px 22px 80px;gap:10px;' : ''}">
      <div class="screen-head">
        <div class="screen-title">Settings</div>
        <div class="mono-tag">[ HÄVN 2.0.1 ]</div>
      </div>

      <div class="card" style="padding:14px 16px;">
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span class="mono-tag">[ PEOPLE ]</span>
          <span style="font-size:12px;color:var(--tang-deep);font-weight:600;">2 people on this phone</span>
        </div>
        <div style="display:flex;gap:8px;margin-top:9px;">
          <div class="chip on" style="flex:1;text-align:center;">Freja (You)</div>
          <div class="chip" style="flex:1;text-align:center;">Mormor</div>
        </div>
        <div style="font-size:12px;color:var(--muted);margin-top:7px;">Reminders stay free forever. Supporter adds more people.</div>
      </div>

      <div class="card" style="padding:14px 16px;">
        <div class="mono-tag">[ YOUR DAY ]</div>
        <div style="display:flex;flex-direction:column;gap:6px;margin-top:8px;">
          ${(shortPane ? anchors.slice(0, 4) : anchors)
            .map(
              ([k, v]) =>
                `<div style="display:flex;justify-content:space-between;font-size:13.5px;padding-bottom:5px;border-bottom:1px dashed var(--rule);">
                  <span style="color:var(--ink2);">${k}</span>
                  <span style="font-family:'IBMPlexMono',monospace;font-weight:500;">${v}</span>
                </div>`
            )
            .join('')}
        </div>
      </div>

      <div class="card" style="padding:14px 16px;">
        <div class="mono-tag">[ TRAVEL ]</div>
        <div class="seg" style="margin-top:8px;">
          <div class="seg-item on">Keep home times</div>
          <div class="seg-item">Follow local time</div>
        </div>
        <div style="font-size:12px;color:var(--muted);margin-top:7px;">Home times suit medicines that need steady 24-hour gaps.</div>
      </div>
    </div>
    ${tabBar('settings')}
  </div>`;
}

function screenNightReminders({ wide = false, coverMode = false, shortPane = false } = {}) {
  return `
  <div class="app-vp dark">
    ${statusBar('21:04', true)}
    <div class="app-body ${wide ? 'centered-col' : ''}" style="${coverMode ? 'padding:10px 20px 18px;justify-content:space-between;' : shortPane ? 'padding:8px 22px 80px;gap:10px;' : ''}">
      <div>
        <div class="screen-head">
          <div>
            <div class="mono-tag" style="color:var(--night-muted);">[ EVENING · NIGHT THEME ]</div>
            <div class="screen-title" style="color:var(--night-ink);font-size:${coverMode ? 34 : 44}px;">Good evening</div>
          </div>
          <div class="profile-pill">21:00</div>
        </div>

        <div class="aura-card" style="padding:${coverMode ? '16px 18px' : '18px 20px'};margin-top:12px;">
          <div class="mono-tag" style="color:#FFB998;">[ DUE NOW ]</div>
          <div class="hero-big" style="color:var(--night-ink);font-size:${coverMode ? 44 : 52}px;">Melatonin · 3 mg</div>
          <div style="font-size:13.5px;color:var(--night-muted);margin-top:6px;">Bedtime anchor · 1 tablet before sleep</div>
          <div class="btn-row" style="margin-top:14px;">
            <div class="btn-primary">Took it</div>
            <div class="btn-sec">In 15 min</div>
          </div>
        </div>
      </div>

      ${
        coverMode
          ? `<div class="card" style="padding:13px 15px;">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <span class="mono-tag" style="color:var(--night-muted);">[ EARLIER TODAY ]</span>
                <span style="font-family:'IBMPlexMono',monospace;font-size:11px;color:#A9B8A0;">2 / 2 KEPT</span>
              </div>
              <div style="font-size:13.5px;color:var(--night-ink);margin-top:6px;">Morning &amp; lunch doses complete ✓</div>
            </div>`
          : `<div class="card" style="padding:${shortPane ? '12px 15px' : '15px 16px'};">
              <div class="mono-tag" style="color:var(--night-muted);">[ APPEARANCE ]</div>
              <div class="seg" style="margin-top:8px;">
                <div class="seg-item">System</div>
                <div class="seg-item">Light</div>
                <div class="seg-item on">Night</div>
              </div>
            </div>
            <div class="card" style="padding:${shortPane ? '12px 15px' : '15px 16px'};">
              <div class="mono-tag" style="color:var(--night-muted);">[ REMINDERS &amp; QUIET HOURS ]</div>
              <div style="font-size:13.5px;color:var(--night-ink);margin-top:7px;">Follow-up if you don't answer</div>
              <div class="chips-row" style="margin-top:7px;">
                <span class="chip">10 min</span>
                <span class="chip on">15 min</span>
                <span class="chip">30 min</span>
                <span class="chip">1 hour</span>
              </div>
              <div style="display:flex;justify-content:space-between;margin-top:10px;font-size:13px;color:var(--night-muted);">
                <span>Quiet hours</span>
                <span style="font-family:'IBMPlexMono',monospace;color:var(--night-ink);">22:30 – 07:00</span>
              </div>
            </div>`
      }
    </div>
    ${coverMode ? '' : tabBar('today')}
  </div>`;
}

function screenPrivacyBackup({ wide = false, shortPane = false } = {}) {
  return `
  <div class="app-vp">
    ${statusBar('08:14')}
    <div class="app-body ${wide ? 'centered-col' : ''}" style="${shortPane ? 'padding:8px 22px 80px;gap:10px;' : ''}">
      <div class="screen-head" style="align-items:center;">
        <div class="screen-title" style="font-size:40px;">Privacy</div>
        <div class="mono-tag" style="white-space:nowrap;flex-shrink:0;">[ OFFLINE ]</div>
      </div>

      <div class="card" style="padding:${shortPane ? '13px 15px' : '16px'};">
        <div class="mono-tag">[ NO TRACKING ]</div>
        <div class="serif" style="font-size:28px;line-height:1.08;margin-top:5px;">Your medicines are nobody’s business.</div>
        <div class="chips-row" style="margin-top:10px;">
          <span class="chip">0 accounts</span>
          <span class="chip">0 trackers</span>
          <span class="chip">0 uploads</span>
        </div>
      </div>

      <div class="card" style="padding:${shortPane ? '13px 15px' : '16px'};">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;">
          <span class="mono-tag" style="white-space:nowrap;">[ ENCRYPTED BACKUP ]</span>
          <span style="font-size:12px;font-weight:600;color:#3E4F37;background:rgba(169,184,160,0.28);padding:4px 10px;border-radius:999px;white-space:nowrap;">Backed up today</span>
        </div>
        <div style="font-size:13px;color:var(--ink2);margin-top:7px;line-height:1.38;">
          One <code style="font-family:'IBMPlexMono',monospace;">.havn</code> file with everything. Optional passphrase encryption. You keep it wherever you like.
        </div>
        <div class="btn-row" style="margin-top:10px;">
          <div class="btn-primary" style="height:38px;font-size:13px;">Save encrypted backup</div>
          <div class="btn-sec" style="height:38px;font-size:13px;">Restore</div>
        </div>
      </div>

      <div class="card" style="padding:${shortPane ? '12px 15px' : '15px 16px'};background:linear-gradient(135deg,#FBF8F1 55%,#EFE8DA 100%);">
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;">
          <span class="mono-tag" style="white-space:nowrap;">[ DOCTOR PDF · A4 ]</span>
          <span class="mono-tag" style="color:var(--tang-deep);white-space:nowrap;">OFFLINE EXPORT</span>
        </div>
        <div style="font-size:14px;font-weight:600;margin-top:6px;">Freja — November Adherence Summary</div>
        <div style="font-size:12px;color:var(--muted);margin-top:2px;">Medications, schedules, 30-day ledger &amp; notes. Generated directly on phone.</div>
      </div>
    </div>
    ${tabBar('settings')}
  </div>`;
}

// =========================================================================
// LAYOUT FRAMES PER GOOGLE PLAY DEVICE CATEGORY
// =========================================================================

function wrapPhone({ badge, headline, subtitle, appHtml, darkFrame = false }) {
  const bg = darkFrame ? '#16151B' : '#F4EFE6';
  const fg = darkFrame ? '#EAE4D7' : '#1B1A22';
  const subFg = darkFrame ? '#A6A095' : '#4C4A44';
  const badgeBg = darkFrame ? '#262430' : '#FBF8F1';
  const badgeBorder = darkFrame ? '#3A3746' : '#C4BBA8';
  const chassisBorder = darkFrame ? '#363342' : '#1B1A22';
  return `<!DOCTYPE html><html><head><meta charset="utf-8"/><style>${BASE_CSS}
  body { width: 1080px; height: 1920px; background: ${bg}; display:flex; flex-direction:column; align-items:center; }
  .aura-top { position:absolute; top:-80px; left:50%; transform:translateX(-50%); width:1200px; height:640px; pointer-events:none;
    background: radial-gradient(ellipse at 50% 30%, ${
      darkFrame ? 'rgba(194,80,31,0.22)' : 'rgba(255,185,152,0.45)'
    } 0%, ${darkFrame ? 'rgba(62,58,120,0.16)' : 'rgba(217,200,244,0.28)'} 45%, transparent 72%); }
  .head { width: 920px; margin-top: 66px; text-align:center; z-index:2; display:flex; flex-direction:column; align-items:center; }
  .p-badge { font-family:"IBMPlexMono",monospace; font-size:15px; font-weight:500; letter-spacing:0.16em; text-transform:uppercase;
    padding:8px 18px; border-radius:999px; background:${badgeBg}; border:1px solid ${badgeBorder}; color:${fg}; }
  .p-h1 { font-family:"InstrumentSerif",serif; font-size:74px; line-height:0.98; letter-spacing:-0.025em; color:${fg}; margin-top:20px; }
  .p-sub { font-size:24px; line-height:1.35; color:${subFg}; margin-top:14px; max-width:820px; }
  .phone-frame {
    margin-top: 46px; width: 790px; height: 1560px; border-radius: 56px;
    border: 14px solid ${chassisBorder}; background: ${bg};
    box-shadow: 0 36px 80px rgba(0,0,0,${darkFrame ? '0.55' : '0.16'});
    overflow: hidden; position: relative; z-index: 2; transform: scale(1.0);
  }
  .scale-inner { width: 412px; height: 828px; transform: scale(1.85); transform-origin: top left; }
  </style></head><body>
  <div class="grain"></div>
  <div class="aura-top"></div>
  <div class="head">
    <div class="p-badge">[ ${badge} ]</div>
    <div class="p-h1">${headline}</div>
    <div class="p-sub">${subtitle}</div>
  </div>
  <div class="phone-frame">
    <div class="scale-inner">${appHtml}</div>
  </div>
  </body></html>`;
}

function wrapFlipSquare({ badge, headline, appHtml, hinge = true, darkFrame = false }) {
  const bg = darkFrame ? '#16151B' : '#F4EFE6';
  const fg = darkFrame ? '#EAE4D7' : '#1B1A22';
  const chassisBorder = darkFrame ? '#3B3848' : '#1B1A22';
  return `<!DOCTYPE html><html><head><meta charset="utf-8"/><style>${BASE_CSS}
  body { width: 1080px; height: 1080px; background: ${bg}; display:flex; flex-direction:column; align-items:center; justify-content:space-between; padding: 36px 54px 42px; }
  .head { text-align:center; z-index:2; display:flex; flex-direction:column; align-items:center; }
  .p-badge { font-family:"IBMPlexMono",monospace; font-size:13px; letter-spacing:0.15em; text-transform:uppercase;
    padding:6px 15px; border-radius:999px; border:1px solid ${darkFrame ? '#3B3848' : '#C4BBA8'}; color:${fg}; background:${darkFrame ? '#1F1E26' : '#FBF8F1'}; }
  .p-h1 { font-family:"InstrumentSerif",serif; font-size:52px; line-height:1.0; letter-spacing:-0.025em; color:${fg}; margin-top:12px; }
  .flip-bezel {
    width: 820px; height: 820px; border-radius: 52px;
    border: 14px solid ${chassisBorder}; background: ${bg};
    box-shadow: 0 28px 64px rgba(0,0,0,${darkFrame ? '0.52' : '0.16'});
    overflow: hidden; position: relative; z-index: 2;
  }
  .scale-flip { width: 452px; height: 452px; transform: scale(1.752); transform-origin: top left; }
  .hinge {
    position: absolute; left: 0; right: 0; top: 47.5%; height: 12px; transform: translateY(-50%);
    background: linear-gradient(180deg, rgba(27,26,34,0) 0%, rgba(27,26,34,0.08) 50%, rgba(27,26,34,0) 100%);
    pointer-events: none; z-index: 50;
  }
  </style></head><body>
  <div class="grain"></div>
  <div class="head">
    <div class="p-badge">[ ${badge} ]</div>
    <div class="p-h1">${headline}</div>
  </div>
  <div class="flip-bezel">
    <div class="scale-flip">${appHtml}</div>
    ${hinge ? '<div class="hinge"></div>' : ''}
  </div>
  </body></html>`;
}

function wrapFoldableDuo({ badge, headline, subtitle, leftHtml, rightHtml }) {
  return `<!DOCTYPE html><html><head><meta charset="utf-8"/><style>${BASE_CSS}
  body { width: 2208px; height: 1656px; background: #F4EFE6; display:flex; flex-direction:column; align-items:center; padding: 66px 86px; }
  .aura-bg { position:absolute; inset:0; background:
    radial-gradient(circle at 22% 14%, rgba(255,185,152,0.34), transparent 52%),
    radial-gradient(circle at 82% 86%, rgba(217,200,244,0.34), transparent 52%); pointer-events:none; }
  .head { text-align:center; z-index:2; margin-bottom: 42px; }
  .p-badge { font-family:"IBMPlexMono",monospace; font-size:17px; letter-spacing:0.16em; text-transform:uppercase;
    padding:9px 22px; border-radius:999px; border:1.5px solid #C4BBA8; background:#FBF8F1; color:#1B1A22; display:inline-block; }
  .p-h1 { font-family:"InstrumentSerif",serif; font-size:84px; line-height:0.98; letter-spacing:-0.028em; margin-top:18px; }
  .p-sub { font-size:26px; color:#4C4A44; margin-top:10px; }
  .duo-bezel {
    width: 1960px; height: 1200px; border-radius: 48px;
    border: 18px solid #1B1A22; background: #1B1A22;
    box-shadow: 0 40px 90px rgba(27,26,34,0.18);
    display: grid; grid-template-columns: 1fr 14px 1fr; overflow: hidden; z-index: 2;
  }
  .pane { background: #F4EFE6; overflow: hidden; position: relative; }
  .scale-pane { width: 590px; height: 734px; transform: scale(1.585); transform-origin: top left; }
  .center-hinge {
    background: linear-gradient(90deg, #14131A 0%, #3A3844 50%, #14131A 100%);
  }
  </style></head><body>
  <div class="grain"></div>
  <div class="aura-bg"></div>
  <div class="head">
    <div class="p-badge">[ ${badge} ]</div>
    <div class="p-h1">${headline}</div>
    <div class="p-sub">${subtitle}</div>
  </div>
  <div class="duo-bezel">
    <div class="pane"><div class="scale-pane">${leftHtml}</div></div>
    <div class="center-hinge"></div>
    <div class="pane"><div class="scale-pane">${rightHtml}</div></div>
  </div>
  </body></html>`;
}

function wrapTabletLandscape({ width, height, badge, headline, subtitle, bullets, appHtml, darkFrame = false }) {
  const scaleCanvas = width === 2560 ? 1.3333 : 1.0;
  const bg = darkFrame ? '#16151B' : '#F4EFE6';
  const fg = darkFrame ? '#EAE4D7' : '#1B1A22';
  const subFg = darkFrame ? '#A6A095' : '#4C4A44';
  const chipBg = darkFrame ? '#1F1E26' : '#FBF8F1';
  const chipBorder = darkFrame ? '#363342' : '#C4BBA8';
  return `<!DOCTYPE html><html><head><meta charset="utf-8"/><style>${BASE_CSS}
  body { width: ${width}px; height: ${height}px; background: ${bg}; display:flex; align-items:center; justify-content:center; }
  .stage {
    width: 1920px; height: 1080px;
    transform: scale(${scaleCanvas}); transform-origin: center center;
    display: flex; align-items: center; justify-content: space-between; padding: 66px 74px; gap: 60px; position: relative;
  }
  .left { width: 560px; z-index: 2; }
  .p-badge { font-family:"IBMPlexMono",monospace; font-size:15px; letter-spacing:0.16em; text-transform:uppercase;
    padding:8px 18px; border-radius:999px; border:1px solid ${chipBorder}; background:${chipBg}; color:${fg}; display:inline-block; }
  .p-h1 { font-family:"InstrumentSerif",serif; font-size:78px; line-height:0.96; letter-spacing:-0.025em; color:${fg}; margin-top:22px; }
  .p-sub { font-size:23px; line-height:1.4; color:${subFg}; margin-top:16px; }
  .bullets { margin-top: 28px; display: flex; flex-direction: column; gap: 12px; }
  .bul { display:flex; align-items:center; gap:12px; font-size:19px; color:${fg}; }
  .bul-dot { width:10px; height:10px; border-radius:50%; background:var(--tang); flex-shrink:0; }
  .tablet-frame {
    width: 1140px; height: 900px; border-radius: 40px;
    border: 14px solid ${darkFrame ? '#3A3746' : '#1B1A22'};
    background: ${bg};
    box-shadow: 0 32px 78px rgba(0,0,0,${darkFrame ? '0.52' : '0.17'});
    overflow: hidden; position: relative; z-index: 2;
  }
  .scale-tab { width: 936px; height: 734px; transform: scale(1.188); transform-origin: top left; }
  </style></head><body>
  <div class="grain"></div>
  <div class="stage">
    <div class="left">
      <div class="p-badge">[ ${badge} ]</div>
      <div class="p-h1">${headline}</div>
      <div class="p-sub">${subtitle}</div>
      <div class="bullets">
        ${bullets.map((b) => `<div class="bul"><span class="bul-dot"></span><span>${b}</span></div>`).join('')}
      </div>
    </div>
    <div class="tablet-frame">
      <div class="scale-tab">${appHtml}</div>
    </div>
  </div>
  </body></html>`;
}

function wrapTabletPortrait({ width, height, badge, headline, subtitle, appHtml }) {
  const scaleFactor = width / 1080;
  return `<!DOCTYPE html><html><head><meta charset="utf-8"/><style>${BASE_CSS}
  body { width: ${width}px; height: ${height}px; background: #F4EFE6; display:flex; align-items:center; justify-content:center; }
  .canvas {
    width: 1080px; height: ${Math.round(height / scaleFactor)}px;
    transform: scale(${scaleFactor}); transform-origin: center center;
    display: flex; flex-direction: column; align-items: center; padding: 56px 64px;
  }
  .head { text-align:center; z-index:2; margin-bottom: 34px; }
  .p-badge { font-family:"IBMPlexMono",monospace; font-size:15px; letter-spacing:0.16em; text-transform:uppercase;
    padding:8px 18px; border-radius:999px; border:1px solid #C4BBA8; background:#FBF8F1; display:inline-block; }
  .p-h1 { font-family:"InstrumentSerif",serif; font-size:66px; line-height:0.98; letter-spacing:-0.025em; margin-top:16px; }
  .p-sub { font-size:21px; color:#4C4A44; margin-top:10px; }
  .tab-p-frame {
    width: 900px; height: 1320px; border-radius: 42px;
    border: 14px solid #1B1A22; background: #F4EFE6;
    box-shadow: 0 30px 72px rgba(27,26,34,0.16);
    overflow: hidden; z-index: 2;
  }
  .scale-tab-p { width: 616px; height: 912px; transform: scale(1.416); transform-origin: top left; }
  </style></head><body>
  <div class="grain"></div>
  <div class="canvas">
    <div class="head">
      <div class="p-badge">[ ${badge} ]</div>
      <div class="p-h1">${headline}</div>
      <div class="p-sub">${subtitle}</div>
    </div>
    <div class="tab-p-frame">
      <div class="scale-tab-p">${appHtml}</div>
    </div>
  </div>
  </body></html>`;
}

// =========================================================================
// BUILD TARGETS
// =========================================================================

const jobs = [
  // 1. Mobile Phones (1080x1920)
  {
    out: 'phone/01_today_calm.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'DAILY RITUALS',
      headline: 'A calm place<br>for every dose',
      subtitle: 'Only what’s due right now — grouped clearly by your day.',
      appHtml: screenToday(),
    }),
  },
  {
    out: 'phone/02_history_ledger.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'TRACK ADHERENCE',
      headline: 'Every kept day<br>stamped on paper',
      subtitle: 'Monthly ink-stamp ledger, PNG cards, and doctor PDFs.',
      appHtml: screenHistory(),
    }),
  },
  {
    out: 'phone/03_medicines_regimen.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'FLEXIBLE SCHEDULES',
      headline: 'Built for real<br>prescriptions',
      subtitle: 'Every day, 21/7 cycles, tapers, intervals, and refill counts.',
      appHtml: screenMedicines(),
    }),
  },
  {
    out: 'phone/04_travel_and_people.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'CARE & TRAVEL',
      headline: 'Care for others.<br>Travel calmly.',
      subtitle: 'Separate people profiles and home vs. local time-zone rules.',
      appHtml: screenTravelPeople(),
    }),
  },
  {
    out: 'phone/05_night_reminders.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'NIGHT & REMINDERS',
      headline: 'Gentle follow-ups<br>that respect rest',
      subtitle: 'Quiet hours, snooze intervals, Quick Tile, and Night theme.',
      appHtml: screenNightReminders(),
      darkFrame: true,
    }),
  },
  {
    out: 'phone/06_privacy_backup_pdf.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'OFFLINE BY DESIGN',
      headline: 'No accounts.<br>No tracking.',
      subtitle: 'Passphrase-encrypted backups and on-device A4 PDF exports.',
      appHtml: screenPrivacyBackup(),
    }),
  },

  // 2. Flip Phones (1080x1080 1:1 flex/cover + 1080x1920 tall flip)
  {
    out: 'flip/01_flip_today_flex.png',
    w: 1080,
    h: 1080,
    html: wrapFlipSquare({
      badge: 'FLIP · TODAY VIEW',
      headline: 'Calm at half-fold',
      appHtml: screenToday({ compact: true }),
      hinge: true,
    }),
  },
  {
    out: 'flip/02_flip_cover_night.png',
    w: 1080,
    h: 1080,
    html: wrapFlipSquare({
      badge: 'FLIP · COVER & NIGHT',
      headline: 'One tap to log a dose',
      appHtml: screenNightReminders({ coverMode: true }),
      hinge: false,
      darkFrame: true,
    }),
  },
  {
    out: 'flip/03_flip_history_ledger.png',
    w: 1080,
    h: 1080,
    html: wrapFlipSquare({
      badge: 'FLIP · HISTORY LEDGER',
      headline: 'Days kept, stitched on paper',
      appHtml: screenHistory({ compact: true }),
      hinge: true,
    }),
  },
  {
    out: 'flip/04_flip_tall_medicines.png',
    w: 1080,
    h: 1920,
    html: wrapPhone({
      badge: 'FLIP · UNFOLDED',
      headline: 'Your shelf,<br>clean and legible',
      subtitle: 'Every pill shape, strength, and supply count in one glance.',
      appHtml: screenMedicines(),
    }),
  },

  // 3. Duo / Book Foldables (2208x1656 4:3 unfolded)
  {
    out: 'foldable/01_foldable_today_and_history.png',
    w: 2208,
    h: 1656,
    html: wrapFoldableDuo({
      badge: 'FOLDABLE & DUO',
      headline: 'Today beside your monthly ledger',
      subtitle: 'Unfolded dual-pane view pairs live dose timing with adherence stamps.',
      leftHtml: screenToday({ shortPane: true }),
      rightHtml: screenHistory({ shortPane: true }),
    }),
  },
  {
    out: 'foldable/02_foldable_meds_and_travel.png',
    w: 2208,
    h: 1656,
    html: wrapFoldableDuo({
      badge: 'FOLDABLE & DUO',
      headline: 'Every medicine and anchor at once',
      subtitle: 'Review your full regimen next to wake, meal, and time-zone anchors.',
      leftHtml: screenMedicines({ shortPane: true }),
      rightHtml: screenTravelPeople({ shortPane: true }),
    }),
  },
  {
    out: 'foldable/03_foldable_night_and_privacy.png',
    w: 2208,
    h: 1656,
    html: wrapFoldableDuo({
      badge: 'FOLDABLE & DUO',
      headline: 'Day calm or Night clarity — offline always',
      subtitle: 'Zero accounts, passphrase-encrypted backups, and gentle night alerts.',
      leftHtml: screenNightReminders({ shortPane: true }),
      rightHtml: screenPrivacyBackup({ shortPane: true }),
    }),
  },
  {
    out: 'foldable/04_foldable_centered_single.png',
    w: 2208,
    h: 1656,
    html: wrapFoldableDuo({
      badge: 'FOLDABLE & DUO',
      headline: 'Care for family with clear separation',
      subtitle: 'Switch people cleanly and print monthly Doctor PDF reports.',
      leftHtml: screenTravelPeople({ shortPane: true }),
      rightHtml: screenHistory({ shortPane: true }),
    }),
  },

  // 4. 7-inch Tablets (1920x1080 16:9 landscape)
  {
    out: 'tablet-7/01_tablet7_today.png',
    w: 1920,
    h: 1080,
    html: wrapTabletLandscape({
      width: 1920,
      height: 1080,
      badge: '7-INCH TABLET',
      headline: 'Generous paper canvas on every screen',
      subtitle: 'Centered editorial column keeps lines effortless to read without edge stretch.',
      bullets: ['DayTape visual schedule', 'One-tap Took it & Snooze', 'Liv calm assistant cues'],
      appHtml: screenToday({ wide: true, shortPane: true }),
    }),
  },
  {
    out: 'tablet-7/02_tablet7_history.png',
    w: 1920,
    h: 1080,
    html: wrapTabletLandscape({
      width: 1920,
      height: 1080,
      badge: '7-INCH TABLET',
      headline: 'Adherence stamped cleanly by month',
      subtitle: 'Switch between 7, 30, and 90 days and share offline Doctor PDFs.',
      bullets: ['Kept-day counter & rhythm bar', 'Monthly stamp calendar', 'Anonymous PNG & A4 PDF export'],
      appHtml: screenHistory({ wide: true, shortPane: true }),
    }),
  },
  {
    out: 'tablet-7/03_tablet7_medicines.png',
    w: 1920,
    h: 1080,
    html: wrapTabletLandscape({
      width: 1920,
      height: 1080,
      badge: '7-INCH TABLET',
      headline: 'Complex schedules without clutter',
      subtitle: 'Daily, weekday, cyclic 21/7, tapers, and as-needed doses on one shelf.',
      bullets: ['Filter Daily / As needed / Paused', 'Optional shelf supply count', 'Custom shape & pill colour'],
      appHtml: screenMedicines({ wide: true, shortPane: true }),
    }),
  },
  {
    out: 'tablet-7/04_tablet7_privacy.png',
    w: 1920,
    h: 1080,
    html: wrapTabletLandscape({
      width: 1920,
      height: 1080,
      badge: '7-INCH TABLET',
      headline: 'Private to the core',
      subtitle: 'Zero accounts, zero analytics, and encrypted .havn files you control.',
      bullets: ['Passphrase-locked .havn backup', 'A4 Doctor PDF summary', '2 profiles free · Supporter adds more'],
      appHtml: screenPrivacyBackup({ wide: true, shortPane: true }),
    }),
  },

  // 5. 10-inch Tablets (2560x1440 QHD 16:9 landscape + 1200x1920 portrait)
  {
    out: 'tablet-10/01_tablet10_today.png',
    w: 2560,
    h: 1440,
    html: wrapTabletLandscape({
      width: 2560,
      height: 1440,
      badge: '10-INCH TABLET',
      headline: 'A calm daily desk companion',
      subtitle: 'Thoughtful max-600dp reading measure over warm SilkPaper background.',
      bullets: ['Aura hero highlights next dose', 'Morning, Midday & Bedtime anchors', 'Home vs. local travel timing'],
      appHtml: screenToday({ wide: true, shortPane: true }),
    }),
  },
  {
    out: 'tablet-10/02_tablet10_history.png',
    w: 2560,
    h: 1440,
    html: wrapTabletLandscape({
      width: 2560,
      height: 1440,
      badge: '10-INCH TABLET',
      headline: 'Track months cleanly at a glance',
      subtitle: 'Print-ready Doctor PDF reports and high-contrast kept-day stamps.',
      bullets: ['7, 30 & 90-day adherence windows', 'Shareable Month card PNG', 'Offline A4 Doctor PDF'],
      appHtml: screenHistory({ wide: true, shortPane: true }),
    }),
  },
  {
    out: 'tablet-10/03_tablet10_medicines.png',
    w: 2560,
    h: 1440,
    html: wrapTabletLandscape({
      width: 2560,
      height: 1440,
      badge: '10-INCH TABLET',
      headline: 'All medicines & refills organized',
      subtitle: 'Fast search, pill visuals, and automatic low-stock notices.',
      bullets: ['Every X hours with sleep pause', 'Exact step tapers supported', 'Food & timing guidance'],
      appHtml: screenMedicines({ wide: true, shortPane: true }),
    }),
  },
  {
    out: 'tablet-10/04_tablet10_night_reminders.png',
    w: 2560,
    h: 1440,
    html: wrapTabletLandscape({
      width: 2560,
      height: 1440,
      badge: '10-INCH TABLET · NIGHT',
      headline: 'Built for bedside & quiet evenings',
      subtitle: 'Warm charcoal Night theme with muted inks and gentle follow-ups.',
      bullets: ['System, Light or Night theme', 'Custom follow-up snooze intervals', 'Configurable quiet hours'],
      appHtml: screenNightReminders({ wide: true, shortPane: true }),
      darkFrame: true,
    }),
  },
  {
    out: 'tablet-10/05_tablet10_portrait_today.png',
    w: 1200,
    h: 1920,
    html: wrapTabletPortrait({
      width: 1200,
      height: 1920,
      badge: '10-INCH PORTRAIT',
      headline: 'Balanced breathing room<br>in portrait',
      subtitle: 'Tall 10:16 tablet view keeps everything comfortable to tap.',
      appHtml: screenToday({ wide: true }),
    }),
  },
  {
    out: 'tablet-10/06_tablet10_portrait_history.png',
    w: 1200,
    h: 1920,
    html: wrapTabletPortrait({
      width: 1200,
      height: 1920,
      badge: '10-INCH PORTRAIT',
      headline: 'Ledger & PDF export<br>for clinicians',
      subtitle: 'Clean monthly overview built directly on device.',
      appHtml: screenHistory({ wide: true }),
    }),
  },
];

const tmpDir = '/tmp/havn_shots_html';
fs.mkdirSync(tmpDir, { recursive: true });

for (const job of jobs) {
  const htmlFile = path.join(tmpDir, job.out.replace(/\//g, '_') + '.html');
  const outFile = path.join(OUT_ROOT, job.out);
  fs.writeFileSync(htmlFile, job.html, 'utf8');
  execFileSync(
    CHROME_BIN,
    [
      '--headless=new',
      '--no-sandbox',
      '--disable-dev-shm-usage',
      '--disable-gpu',
      `--window-size=${job.w},${job.h}`,
      `--screenshot=${outFile}`,
      htmlFile,
    ],
    { stdio: 'pipe' }
  );
  const sz = fs.statSync(outFile).size;
  console.log(`Rendered ${job.out} (${job.w}x${job.h}, ${sz} bytes)`);
}
