const fs = require('fs');
const path = require('path');

const BUILD_DIR = path.resolve(__dirname);
const OUTPUT_DIR = path.resolve(__dirname, '..', 'graphics', 'screenshots');

if (!fs.existsSync(OUTPUT_DIR)) {
  fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

// Common CSS & Head
function getHead(title) {
  return `<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<title>${title}</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght@0,9..144,300..800;1,9..144,300..800&family=Hanken+Grotesk:ital,wght@0,300..900;1,300..900&display=swap" rel="stylesheet">
<link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0..1,0" rel="stylesheet" />
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; -webkit-font-smoothing: antialiased; }
  body {
    width: 1080px;
    height: 1920px;
    overflow: hidden;
    background: #FBF9F5;
    background: radial-gradient(ellipse at 50% 12%, rgba(141,160,140,0.22) 0%, rgba(251,249,245,0) 62%), #FBF9F5;
    font-family: 'Hanken Grotesk', -apple-system, sans-serif;
    color: #191C1A;
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
  }

  /* Ambient texture */
  .grain {
    position: absolute;
    inset: 0;
    opacity: 0.035;
    pointer-events: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='200' height='200' filter='url(%23n)' opacity='1'/%3E%3C/svg%3E");
    z-index: 99;
  }

  /* Header Section */
  .header-section {
    width: 960px;
    margin-top: 54px;
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    z-index: 10;
  }

  .badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 7px 22px;
    background: rgba(81, 99, 81, 0.08);
    border: 1.5px solid rgba(81, 99, 81, 0.22);
    border-radius: 999px;
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.22em;
    color: #516351;
    text-transform: uppercase;
  }

  .badge-icon {
    font-size: 18px;
    line-height: 1;
  }

  .headline {
    font-family: 'Fraunces', Georgia, serif;
    font-size: 48px;
    font-weight: 600;
    line-height: 1.14;
    letter-spacing: -0.02em;
    color: #191C1A;
    margin-top: 18px;
    max-width: 900px;
  }

  .subtitle {
    font-size: 22px;
    font-weight: 400;
    color: #585F55;
    margin-top: 10px;
    letter-spacing: -0.01em;
  }

  /* Phone Mockup */
  .phone-chassis {
    position: absolute;
    bottom: -80px;
    width: 820px;
    height: 1540px;
    background: #181B19;
    border-radius: 64px 64px 0 0;
    padding: 14px 14px 0 14px;
    box-shadow:
      0 40px 100px -15px rgba(27, 36, 29, 0.35),
      0 12px 36px rgba(0, 0, 0, 0.15),
      inset 0 1px 2px rgba(255, 255, 255, 0.25);
    display: flex;
    flex-direction: column;
    z-index: 5;
  }

  .phone-screen {
    width: 100%;
    height: 100%;
    background: #FBF9F5;
    border-radius: 52px 52px 0 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    position: relative;
  }

  /* Status Bar */
  .status-bar {
    height: 52px;
    padding: 0 36px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 15px;
    font-weight: 600;
    color: #191C1A;
    background: transparent;
    z-index: 20;
    margin-top: 4px;
  }

  .camera-punch {
    width: 14px;
    height: 14px;
    background: #101211;
    border-radius: 50%;
    position: absolute;
    left: 50%;
    top: 20px;
    transform: translateX(-50%);
    box-shadow: inset 0 0 2px rgba(255,255,255,0.2);
  }

  .status-icons {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }

  /* App Screen Body */
  .screen-content {
    flex: 1;
    padding: 12px 28px 24px 28px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    position: relative;
  }

  /* App Top Bar */
  .app-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .app-logo {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .app-logo-text {
    font-family: 'Fraunces', serif;
    font-size: 26px;
    font-weight: 700;
    letter-spacing: 0.12em;
    color: #191C1A;
  }

  .profile-pill {
    display: flex;
    align-items: center;
    gap: 8px;
    background: #FFFFFF;
    border: 1px solid rgba(0,0,0,0.06);
    border-radius: 999px;
    padding: 4px 14px 4px 6px;
    box-shadow: 0 2px 8px rgba(81,99,81,0.06);
  }

  .profile-avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: #516351;
    color: #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 700;
  }

  .profile-name {
    font-size: 14px;
    font-weight: 600;
    color: #191C1A;
  }

  /* Bottom Navigation */
  .bottom-nav {
    height: 84px;
    background: #FFFFFF;
    border-top: 1px solid rgba(0,0,0,0.06);
    display: flex;
    justify-content: space-around;
    align-items: center;
    padding: 0 24px;
    box-shadow: 0 -4px 24px rgba(0,0,0,0.02);
  }

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: #747872;
  }

  .nav-item.active {
    color: #516351;
  }

  .nav-item .material-symbols-outlined {
    font-size: 26px;
  }

  .nav-item.active .material-symbols-outlined {
    font-variation-settings: 'FILL' 1;
  }

  .nav-label {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.04em;
  }

  /* Cards */
  .ui-card {
    background: #FFFFFF;
    border-radius: 24px;
    padding: 20px;
    border: 1px solid rgba(0,0,0,0.05);
    box-shadow: 0 4px 20px rgba(81,99,81,0.04);
  }

  .dose-item {
    background: #FFFFFF;
    border-radius: 20px;
    padding: 16px 18px;
    border: 1px solid rgba(0,0,0,0.05);
    box-shadow: 0 2px 10px rgba(81,99,81,0.03);
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;
  }

  .dose-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .check-circle {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: #516351;
    color: #FFFFFF;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: auto;
    box-shadow: 0 4px 12px rgba(81,99,81,0.25);
  }

  .take-button {
    background: #516351;
    color: #FFFFFF;
    border: none;
    border-radius: 999px;
    padding: 10px 22px;
    font-size: 13px;
    font-weight: 700;
    letter-spacing: 0.04em;
    margin-left: auto;
    box-shadow: 0 4px 16px rgba(81,99,81,0.25);
  }
</style>
</head>
<body>
<div class="grain"></div>
`;
}

// SCREEN 1: DAILY RITUAL
function generateScreen1() {
  return `${getHead('Hävn — Daily Ritual')}
  <div class="header-section">
    <div class="badge">
      <span class="badge-icon">✦</span>
      <span>Daily Ritual</span>
    </div>
    <h1 class="headline">A calm, effortless view of every dose.</h1>
    <p class="subtitle">Organized by morning, afternoon, evening and night.</p>
  </div>

  <div class="phone-chassis">
    <div class="phone-screen">
      <div class="camera-punch"></div>
      <div class="status-bar">
        <span>09:41</span>
        <div class="status-icons">
          <span>5G</span>
          <span class="material-symbols-outlined" style="font-size:18px">wifi</span>
          <span class="material-symbols-outlined" style="font-size:18px">battery_full</span>
        </div>
      </div>

      <div class="screen-content">
        <!-- App Bar -->
        <div class="app-bar">
          <div class="app-logo">
            <svg width="26" height="26" viewBox="0 0 120 120" fill="none">
              <path d="M60 16C40.1178 16 24 32.1178 24 52V76H96V52C96 32.1178 79.8822 16 60 16Z" stroke="#516351" stroke-width="8" stroke-linecap="round"/>
              <circle cx="60" cy="42" r="10" fill="#8da08c"/>
              <line x1="36" y1="76" x2="84" y2="76" stroke="#516351" stroke-width="7" stroke-linecap="round"/>
            </svg>
            <span class="app-logo-text">Hävn</span>
          </div>
          <div class="profile-pill">
            <div class="profile-avatar">H</div>
            <span class="profile-name">Henrik</span>
            <span class="material-symbols-outlined" style="font-size:16px;color:#747872">notifications_none</span>
          </div>
        </div>

        <!-- Greeting -->
        <div style="margin-bottom: 20px;">
          <h2 style="font-family:'Fraunces',serif; font-size:32px; font-weight:500; color:#191C1A; line-height:1.15">God morgen, Henrik</h2>
          <p style="font-size:15px; color:#585F55; margin-top:4px">Monday, 24 October · All doses on schedule</p>
        </div>

        <!-- Metric Hero Card -->
        <div class="ui-card" style="display:flex; align-items:center; gap:24px; margin-bottom: 22px; background:linear-gradient(135deg, #FFFFFF 0%, #F5F7F4 100%);">
          <!-- Circular Progress Ring -->
          <div style="position:relative; width:110px; height:110px; flex-shrink:0;">
            <svg width="110" height="110" viewBox="0 0 120 120">
              <circle cx="60" cy="60" r="50" fill="none" stroke="#E3E8E2" stroke-width="12" />
              <circle cx="60" cy="60" r="50" fill="none" stroke="#516351" stroke-width="12"
                      stroke-dasharray="314.159" stroke-dashoffset="78.5" stroke-linecap="round"
                      transform="rotate(-90 60 60)" />
            </svg>
            <div style="position:absolute; inset:0; display:flex; flex-direction:column; align-items:center; justify-content:center;">
              <span style="font-family:'Fraunces',serif; font-size:26px; font-weight:700; color:#191C1A; line-height:1">75%</span>
              <span style="font-size:10px; font-weight:700; color:#516351; letter-spacing:0.06em; margin-top:2px">TODAY</span>
            </div>
          </div>

          <div style="flex:1;">
            <div style="display:inline-flex; align-items:center; gap:6px; background:#E2EBDE; padding:4px 12px; border-radius:999px; margin-bottom:8px">
              <span class="material-symbols-outlined" style="font-size:14px; color:#516351; font-variation-settings:'FILL' 1">eco</span>
              <span style="font-size:12px; font-weight:700; color:#516351; letter-spacing:0.04em">14 DAY STREAK</span>
            </div>
            <h3 style="font-size:17px; font-weight:600; color:#191C1A">3 of 4 doses logged</h3>
            <p style="font-size:13px; color:#585F55; margin-top:2px">Next dose due at 21:00 (Evening)</p>
          </div>
        </div>

        <!-- Schedule Section Header -->
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; padding:0 4px;">
          <span style="font-size:12px; font-weight:700; letter-spacing:0.12em; color:#516351; text-transform:uppercase">Today's Schedule</span>
          <span style="font-size:12px; font-weight:600; color:#747872">4 Medications</span>
        </div>

        <!-- Dose 1 -->
        <div class="dose-item">
          <div class="dose-icon" style="background:#F7EFD8;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="#E2C381">
              <rect x="5" y="9" width="14" height="6" rx="3" transform="rotate(-45 12 12)" stroke="#B3934B" stroke-width="1.5"/>
            </svg>
          </div>
          <div>
            <h4 style="font-size:16px; font-weight:600; color:#191C1A">Vitamin D3 + K2</h4>
            <p style="font-size:13px; color:#585F55">2,000 IU · 1 capsule · 08:30 AM</p>
          </div>
          <div class="check-circle">
            <span class="material-symbols-outlined" style="font-size:20px; font-weight:700">check</span>
          </div>
        </div>

        <!-- Dose 2 -->
        <div class="dose-item">
          <div class="dose-icon" style="background:#FFEADB;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="#FEB28F">
              <ellipse cx="12" cy="12" rx="7" ry="5" stroke="#D27950" stroke-width="1.5"/>
            </svg>
          </div>
          <div>
            <h4 style="font-size:16px; font-weight:600; color:#191C1A">Omega-3 EPA/DHA</h4>
            <p style="font-size:13px; color:#585F55">1,000 mg · 2 softgels · 08:30 AM</p>
          </div>
          <div class="check-circle">
            <span class="material-symbols-outlined" style="font-size:20px; font-weight:700">check</span>
          </div>
        </div>

        <!-- Dose 3 -->
        <div class="dose-item">
          <div class="dose-icon" style="background:#E2EBDE;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="#8DA08C">
              <circle cx="12" cy="12" r="7" stroke="#516351" stroke-width="1.5"/>
              <line x1="8" y1="12" x2="16" y2="12" stroke="#516351" stroke-width="1.5"/>
            </svg>
          </div>
          <div>
            <h4 style="font-size:16px; font-weight:600; color:#191C1A">Magnesium Glycinate</h4>
            <p style="font-size:13px; color:#585F55">200 mg · 1 tablet · 13:30 PM</p>
          </div>
          <div class="check-circle">
            <span class="material-symbols-outlined" style="font-size:20px; font-weight:700">check</span>
          </div>
        </div>

        <!-- Dose 4 (Evening - Upcoming) -->
        <div class="dose-item" style="border:1.5px solid rgba(81,99,81,0.3); background:#FCFBF8;">
          <div class="dose-icon" style="background:#EFECE6;">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="#8A4F33">
              <rect x="5" y="9" width="14" height="6" rx="3" transform="rotate(45 12 12)" stroke="#673922" stroke-width="1.5"/>
            </svg>
          </div>
          <div>
            <div style="display:flex; align-items:center; gap:6px;">
              <h4 style="font-size:16px; font-weight:600; color:#191C1A">Ashwagandha</h4>
              <span style="background:#FFDF9C; color:#735B23; font-size:10px; font-weight:700; padding:2px 6px; border-radius:4px">DUE 21:00</span>
            </div>
            <p style="font-size:13px; color:#585F55">600 mg · 1 capsule · Rest & calm</p>
          </div>
          <button class="take-button">Take</button>
        </div>

      </div>

      <!-- Bottom Nav -->
      <div class="bottom-nav">
        <div class="nav-item active">
          <span class="material-symbols-outlined">calendar_today</span>
          <span class="nav-label">Today</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">view_in_ar</span>
          <span class="nav-label">Organizer</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">monitoring</span>
          <span class="nav-label">Progress</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">tune</span>
          <span class="nav-label">Settings</span>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`;
}

// SCREEN 2: 3D ORGANIZER
function generateScreen2() {
  return `${getHead('Hävn — 3D Pill Organizer')}
  <div class="header-section">
    <div class="badge">
      <span class="badge-icon">❖</span>
      <span>Model H.01</span>
    </div>
    <h1 class="headline">Interactive 3D pill organizer you can tap & spin.</h1>
    <p class="subtitle">Designed after tactile Scandinavian medical cases.</p>
  </div>

  <div class="phone-chassis">
    <div class="phone-screen">
      <div class="camera-punch"></div>
      <div class="status-bar">
        <span>09:41</span>
        <div class="status-icons">
          <span>5G</span>
          <span class="material-symbols-outlined" style="font-size:18px">wifi</span>
          <span class="material-symbols-outlined" style="font-size:18px">battery_full</span>
        </div>
      </div>

      <div class="screen-content">
        <!-- App Bar -->
        <div class="app-bar" style="margin-bottom:12px">
          <div class="app-logo">
            <span class="material-symbols-outlined" style="color:#516351; font-size:24px">view_in_ar</span>
            <span class="app-logo-text">Model H.01</span>
          </div>
          <div style="background:#E2EBDE; color:#516351; font-size:11px; font-weight:700; padding:6px 14px; border-radius:999px; letter-spacing:0.08em">
            3D INTERACTIVE
          </div>
        </div>

        <div style="text-align:center; margin-bottom:14px">
          <h2 style="font-family:'Fraunces',serif; font-size:24px; font-weight:500; color:#191C1A">Weekly Compartment Ritual</h2>
          <p style="font-size:13px; color:#585F55">Tap a compartment or drag to rotate 360°</p>
        </div>

        <!-- 3D Organizer Model Canvas Container -->
        <div style="position:relative; width:100%; height:380px; background:radial-gradient(ellipse at 50% 50%, rgba(255,255,255,0.9) 0%, rgba(240,238,232,0.6) 100%); border-radius:32px; border:1px solid rgba(0,0,0,0.06); box-shadow:0 12px 36px rgba(81,99,81,0.08); overflow:hidden; display:flex; align-items:center; justify-content:center;">
          <iframe src="three_organizer_embed.html" style="width:100%; height:100%; border:none; background:transparent;"></iframe>
          <div style="position:absolute; bottom:14px; display:flex; gap:8px; align-items:center; background:rgba(255,255,255,0.85); backdrop-filter:blur(8px); padding:4px 14px; border-radius:999px; border:1px solid rgba(0,0,0,0.06);">
            <span class="material-symbols-outlined" style="font-size:16px; color:#516351">360</span>
            <span style="font-size:11px; font-weight:700; color:#516351; letter-spacing:0.08em">DRAG TO ROTATE</span>
          </div>
        </div>

        <!-- Period Selector Chips -->
        <div style="display:flex; gap:8px; justify-content:space-between; margin-top:18px; margin-bottom:16px;">
          <div style="flex:1; text-align:center; padding:10px 4px; background:#516351; color:#FFFFFF; border-radius:16px; font-size:13px; font-weight:700; box-shadow:0 4px 14px rgba(81,99,81,0.25)">
            Morning
          </div>
          <div style="flex:1; text-align:center; padding:10px 4px; background:#FFFFFF; color:#585F55; border:1px solid rgba(0,0,0,0.06); border-radius:16px; font-size:13px; font-weight:600">
            Afternoon
          </div>
          <div style="flex:1; text-align:center; padding:10px 4px; background:#FFFFFF; color:#585F55; border:1px solid rgba(0,0,0,0.06); border-radius:16px; font-size:13px; font-weight:600">
            Evening
          </div>
          <div style="flex:1; text-align:center; padding:10px 4px; background:#FFFFFF; color:#585F55; border:1px solid rgba(0,0,0,0.06); border-radius:16px; font-size:13px; font-weight:600">
            Night
          </div>
        </div>

        <!-- Compartment Contents List -->
        <div class="ui-card" style="padding:16px 18px; margin-bottom:14px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
            <span style="font-size:12px; font-weight:700; letter-spacing:0.1em; color:#516351; text-transform:uppercase">Morning Chamber · 2 Items</span>
            <span style="font-size:12px; color:#747872">Due 08:30 AM</span>
          </div>

          <div style="display:flex; align-items:center; gap:12px; padding:8px 0; border-bottom:1px solid rgba(0,0,0,0.04);">
            <div style="width:10px; height:10px; border-radius:50%; background:#E2C381"></div>
            <span style="font-size:15px; font-weight:600; color:#191C1A; flex:1">Vitamin D3 (2,000 IU)</span>
            <span style="font-size:13px; color:#585F55">1 capsule</span>
          </div>
          <div style="display:flex; align-items:center; gap:12px; padding:8px 0;">
            <div style="width:10px; height:10px; border-radius:50%; background:#FEB28F"></div>
            <span style="font-size:15px; font-weight:600; color:#191C1A; flex:1">Omega-3 Ultra (1,000 mg)</span>
            <span style="font-size:13px; color:#585F55">2 softgels</span>
          </div>
        </div>

        <!-- Bento row -->
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px;">
          <div style="background:#FFFFFF; border-radius:18px; padding:12px 14px; border:1px solid rgba(0,0,0,0.05); display:flex; align-items:center; gap:10px;">
            <div style="width:36px; height:36px; border-radius:50%; background:#FFEADB; display:flex; align-items:center; justify-content:center; color:#8A4F33">
              <span class="material-symbols-outlined" style="font-size:20px">water_drop</span>
            </div>
            <div>
              <p style="font-size:10px; font-weight:700; color:#747872; text-transform:uppercase; letter-spacing:0.06em">Hydration</p>
              <p style="font-size:13px; font-weight:600; color:#191C1A">250ml water</p>
            </div>
          </div>
          <div style="background:#FFFFFF; border-radius:18px; padding:12px 14px; border:1px solid rgba(0,0,0,0.05); display:flex; align-items:center; gap:10px;">
            <div style="width:36px; height:36px; border-radius:50%; background:#E2EBDE; display:flex; align-items:center; justify-content:center; color:#516351">
              <span class="material-symbols-outlined" style="font-size:20px">check_circle</span>
            </div>
            <div>
              <p style="font-size:10px; font-weight:700; color:#747872; text-transform:uppercase; letter-spacing:0.06em">All Slots Filled</p>
              <p style="font-size:13px; font-weight:600; color:#191C1A">Refilled Oct 23</p>
            </div>
          </div>
        </div>

      </div>

      <!-- Bottom Nav -->
      <div class="bottom-nav">
        <div class="nav-item">
          <span class="material-symbols-outlined">calendar_today</span>
          <span class="nav-label">Today</span>
        </div>
        <div class="nav-item active">
          <span class="material-symbols-outlined">view_in_ar</span>
          <span class="nav-label">Organizer</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">monitoring</span>
          <span class="nav-label">Progress</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">tune</span>
          <span class="nav-label">Settings</span>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`;
}

// SCREEN 3: PROGRESS & ADHERENCE
function generateScreen3() {
  return `${getHead('Hävn — Insights & Progress')}
  <div class="header-section">
    <div class="badge">
      <span class="badge-icon">📈</span>
      <span>Adherence Insights</span>
    </div>
    <h1 class="headline">Celebrate consistency without guilt or anxiety.</h1>
    <p class="subtitle">30-day trends, monthly calendar, and habit streaks.</p>
  </div>

  <div class="phone-chassis">
    <div class="phone-screen">
      <div class="camera-punch"></div>
      <div class="status-bar">
        <span>09:41</span>
        <div class="status-icons">
          <span>5G</span>
          <span class="material-symbols-outlined" style="font-size:18px">wifi</span>
          <span class="material-symbols-outlined" style="font-size:18px">battery_full</span>
        </div>
      </div>

      <div class="screen-content">
        <!-- App Bar -->
        <div class="app-bar">
          <div class="app-logo">
            <span class="material-symbols-outlined" style="color:#516351; font-size:24px">monitoring</span>
            <span class="app-logo-text">Progress</span>
          </div>
          <div style="background:#E2EBDE; color:#516351; font-size:12px; font-weight:700; padding:6px 14px; border-radius:999px">
            LAST 30 DAYS
          </div>
        </div>

        <div style="margin-bottom:18px">
          <h2 style="font-family:'Fraunces',serif; font-size:30px; font-weight:500; color:#191C1A">Your Health Rhythm</h2>
          <p style="font-size:14px; color:#585F55; margin-top:2px">Consistent rituals build lasting well-being.</p>
        </div>

        <!-- 3-Column Bento Metric Cards -->
        <div style="display:grid; grid-template-columns:1fr 1fr 1fr; gap:12px; margin-bottom:18px;">
          <div class="ui-card" style="padding:16px 12px; text-align:center;">
            <p style="font-size:10px; font-weight:700; color:#747872; letter-spacing:0.08em; text-transform:uppercase">Adherence</p>
            <h3 style="font-family:'Fraunces',serif; font-size:28px; font-weight:700; color:#516351; margin-top:4px">96%</h3>
            <p style="font-size:11px; color:#585F55; margin-top:2px">+4% vs last mo</p>
          </div>
          <div class="ui-card" style="padding:16px 12px; text-align:center;">
            <p style="font-size:10px; font-weight:700; color:#747872; letter-spacing:0.08em; text-transform:uppercase">Streak</p>
            <h3 style="font-family:'Fraunces',serif; font-size:28px; font-weight:700; color:#8A4F33; margin-top:4px">14d</h3>
            <p style="font-size:11px; color:#585F55; margin-top:2px">Best record</p>
          </div>
          <div class="ui-card" style="padding:16px 12px; text-align:center;">
            <p style="font-size:10px; font-weight:700; color:#747872; letter-spacing:0.08em; text-transform:uppercase">Taken</p>
            <h3 style="font-family:'Fraunces',serif; font-size:28px; font-weight:700; color:#191C1A; margin-top:4px">84</h3>
            <p style="font-size:11px; color:#585F55; margin-top:2px">Total doses</p>
          </div>
        </div>

        <!-- 30-Day Trend Chart Card -->
        <div class="ui-card" style="margin-bottom:18px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div>
              <h4 style="font-size:15px; font-weight:600; color:#191C1A">Adherence Curve</h4>
              <p style="font-size:12px; color:#585F55">Daily tracking percentage</p>
            </div>
            <span style="font-size:13px; font-weight:700; color:#516351; background:#E2EBDE; padding:3px 10px; border-radius:999px">96% Avg</span>
          </div>

          <!-- SVG Spline Graph -->
          <svg width="100%" height="110" viewBox="0 0 320 110" style="overflow:visible">
            <defs>
              <linearGradient id="curveGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#516351" stop-opacity="0.32" />
                <stop offset="100%" stop-color="#516351" stop-opacity="0.0" />
              </linearGradient>
            </defs>
            <line x1="0" y1="20" x2="320" y2="20" stroke="rgba(0,0,0,0.06)" stroke-dasharray="4 4" />
            <line x1="0" y1="60" x2="320" y2="60" stroke="rgba(0,0,0,0.06)" stroke-dasharray="4 4" />
            <line x1="0" y1="100" x2="320" y2="100" stroke="rgba(0,0,0,0.06)" stroke-dasharray="4 4" />
            <!-- Area fill -->
            <path d="M 0 45 Q 40 35 80 40 T 160 25 T 240 18 T 320 22 L 320 100 L 0 100 Z" fill="url(#curveGrad)" />
            <!-- Stroke curve -->
            <path d="M 0 45 Q 40 35 80 40 T 160 25 T 240 18 T 320 22" fill="none" stroke="#516351" stroke-width="3.5" stroke-linecap="round" />
            <!-- Current point dot -->
            <circle cx="320" cy="22" r="5" fill="#516351" stroke="#FFFFFF" stroke-width="2" />
          </svg>
          <div style="display:flex; justify-content:space-between; font-size:11px; color:#747872; margin-top:8px">
            <span>Week 1</span>
            <span>Week 2</span>
            <span>Week 3</span>
            <span>Week 4 (Current)</span>
          </div>
        </div>

        <!-- Monthly Calendar Heatmap -->
        <div class="ui-card">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
            <h4 style="font-size:15px; font-weight:600; color:#191C1A">October Calendar</h4>
            <div style="display:flex; align-items:center; gap:6px; font-size:12px; color:#516351; font-weight:600">
              <div style="width:8px; height:8px; border-radius:50%; background:#516351"></div>
              <span>All Doses Taken</span>
            </div>
          </div>

          <!-- Heatmap grid -->
          <div style="display:grid; grid-template-columns:repeat(7, 1fr); gap:8px; text-align:center;">
            <span style="font-size:11px; font-weight:700; color:#747872">M</span>
            <span style="font-size:11px; font-weight:700; color:#747872">T</span>
            <span style="font-size:11px; font-weight:700; color:#747872">W</span>
            <span style="font-size:11px; font-weight:700; color:#747872">T</span>
            <span style="font-size:11px; font-weight:700; color:#747872">F</span>
            <span style="font-size:11px; font-weight:700; color:#747872">S</span>
            <span style="font-size:11px; font-weight:700; color:#747872">S</span>

            <div style="height:32px; border-radius:50%; background:#E2EBDE; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600; color:#516351">1</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">2</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">3</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">4</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">5</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">6</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">7</div>

            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">8</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">9</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">10</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">11</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">12</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">13</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">14</div>

            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">15</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">16</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">17</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">18</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">19</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">20</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">21</div>

            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">22</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600">23</div>
            <div style="height:32px; border-radius:50%; background:#516351; color:#FFF; display:flex; align-items:center; justify-content:center; font-size:12px; font-weight:600; border:2px solid #FEB28F">24</div>
            <div style="height:32px; border-radius:50%; background:#F4F0E8; color:#747872; display:flex; align-items:center; justify-content:center; font-size:12px">25</div>
            <div style="height:32px; border-radius:50%; background:#F4F0E8; color:#747872; display:flex; align-items:center; justify-content:center; font-size:12px">26</div>
            <div style="height:32px; border-radius:50%; background:#F4F0E8; color:#747872; display:flex; align-items:center; justify-content:center; font-size:12px">27</div>
            <div style="height:32px; border-radius:50%; background:#F4F0E8; color:#747872; display:flex; align-items:center; justify-content:center; font-size:12px">28</div>
          </div>
        </div>

      </div>

      <!-- Bottom Nav -->
      <div class="bottom-nav">
        <div class="nav-item">
          <span class="material-symbols-outlined">calendar_today</span>
          <span class="nav-label">Today</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">view_in_ar</span>
          <span class="nav-label">Organizer</span>
        </div>
        <div class="nav-item active">
          <span class="material-symbols-outlined">monitoring</span>
          <span class="nav-label">Progress</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">tune</span>
          <span class="nav-label">Settings</span>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`;
}

// SCREEN 4: CUSTOMIZE REGIMEN
function generateScreen4() {
  return `${getHead('Hävn — Customize Regimen')}
  <div class="header-section">
    <div class="badge">
      <span class="badge-icon">💊</span>
      <span>Custom Regimen</span>
    </div>
    <h1 class="headline">Pill shapes, colors, dosages & smart schedules.</h1>
    <p class="subtitle">Personalize every detail with Scandinavian aesthetic elegance.</p>
  </div>

  <div class="phone-chassis">
    <div class="phone-screen">
      <div class="camera-punch"></div>
      <div class="status-bar">
        <span>09:41</span>
        <div class="status-icons">
          <span>5G</span>
          <span class="material-symbols-outlined" style="font-size:18px">wifi</span>
          <span class="material-symbols-outlined" style="font-size:18px">battery_full</span>
        </div>
      </div>

      <div class="screen-content">
        <!-- App Bar -->
        <div class="app-bar" style="margin-bottom:16px;">
          <div style="display:flex; align-items:center; gap:8px;">
            <span class="material-symbols-outlined" style="color:#191C1A; font-size:24px">close</span>
            <span style="font-family:'Fraunces',serif; font-size:22px; font-weight:600; color:#191C1A">Add Medication</span>
          </div>
          <button style="background:#516351; color:#FFFFFF; border:none; border-radius:999px; padding:6px 18px; font-size:13px; font-weight:700">Save</button>
        </div>

        <!-- Input Field: Name -->
        <div style="margin-bottom:16px;">
          <label style="font-size:11px; font-weight:700; letter-spacing:0.12em; color:#516351; text-transform:uppercase; display:block; margin-bottom:6px">Medication Name</label>
          <div style="background:#FFFFFF; border:1.5px solid rgba(81,99,81,0.25); border-radius:18px; padding:14px 18px; font-size:16px; font-weight:600; color:#191C1A; box-shadow:0 2px 8px rgba(81,99,81,0.04)">
            Vitamin D3 + K2
          </div>
        </div>

        <!-- Dosage & Unit -->
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:12px; margin-bottom:16px;">
          <div>
            <label style="font-size:11px; font-weight:700; letter-spacing:0.12em; color:#516351; text-transform:uppercase; display:block; margin-bottom:6px">Dosage</label>
            <div style="background:#FFFFFF; border:1px solid rgba(0,0,0,0.06); border-radius:18px; padding:14px 18px; font-size:15px; font-weight:600; color:#191C1A">
              2,000 IU
            </div>
          </div>
          <div>
            <label style="font-size:11px; font-weight:700; letter-spacing:0.12em; color:#516351; text-transform:uppercase; display:block; margin-bottom:6px">Units per Dose</label>
            <div style="background:#FFFFFF; border:1px solid rgba(0,0,0,0.06); border-radius:18px; padding:14px 18px; font-size:15px; font-weight:600; color:#191C1A">
              1 Capsule
            </div>
          </div>
        </div>

        <!-- Pill Shape Selector -->
        <div style="margin-bottom:18px;">
          <label style="font-size:11px; font-weight:700; letter-spacing:0.12em; color:#516351; text-transform:uppercase; display:block; margin-bottom:8px">Pill Shape</label>
          <div style="display:grid; grid-template-columns:repeat(5, 1fr); gap:8px;">
            <div style="background:#E2EBDE; border:2px solid #516351; border-radius:16px; padding:12px 6px; display:flex; flex-direction:column; align-items:center; gap:6px">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="#516351">
                <rect x="4" y="9" width="16" height="6" rx="3" transform="rotate(-45 12 12)" />
              </svg>
              <span style="font-size:10px; font-weight:700; color:#516351">Capsule</span>
            </div>
            <div style="background:#FFFFFF; border:1px solid rgba(0,0,0,0.06); border-radius:16px; padding:12px 6px; display:flex; flex-direction:column; align-items:center; gap:6px">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="#747872">
                <circle cx="12" cy="12" r="7"/>
              </svg>
              <span style="font-size:10px; font-weight:600; color:#747872">Tablet</span>
            </div>
            <div style="background:#FFFFFF; border:1px solid rgba(0,0,0,0.06); border-radius:16px; padding:12px 6px; display:flex; flex-direction:column; align-items:center; gap:6px">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="#747872">
                <ellipse cx="12" cy="12" rx="8" ry="5"/>
              </svg>
              <span style="font-size:10px; font-weight:600; color:#747872">Oval</span>
            </div>
            <div style="background:#FFFFFF; border:1px solid rgba(0,0,0,0.06); border-radius:16px; padding:12px 6px; display:flex; flex-direction:column; align-items:center; gap:6px">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="#747872">
                <path d="M12 3L6 14C6 17.3 8.7 20 12 20C15.3 20 18 17.3 18 14L12 3Z"/>
              </svg>
              <span style="font-size:10px; font-weight:600; color:#747872">Drop</span>
            </div>
            <div style="background:#FFFFFF; border:1px solid rgba(0,0,0,0.06); border-radius:16px; padding:12px 6px; display:flex; flex-direction:column; align-items:center; gap:6px">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="#747872">
                <circle cx="8" cy="12" r="2.5"/><circle cx="16" cy="12" r="2.5"/><circle cx="12" cy="16" r="2.5"/>
              </svg>
              <span style="font-size:10px; font-weight:600; color:#747872">Powder</span>
            </div>
          </div>
        </div>

        <!-- Color Palette Picker -->
        <div style="margin-bottom:18px;">
          <label style="font-size:11px; font-weight:700; letter-spacing:0.12em; color:#516351; text-transform:uppercase; display:block; margin-bottom:8px">Color Accent</label>
          <div style="display:flex; justify-content:space-between; align-items:center; background:#FFFFFF; border-radius:20px; padding:12px 18px; border:1px solid rgba(0,0,0,0.06)">
            <div style="width:36px; height:36px; border-radius:50%; background:#516351; outline:3px solid #516351; outline-offset:3px; display:flex; align-items:center; justify-content:center; color:#FFF">
              <span class="material-symbols-outlined" style="font-size:18px">check</span>
            </div>
            <div style="width:36px; height:36px; border-radius:50%; background:#FEB28F;"></div>
            <div style="width:36px; height:36px; border-radius:50%; background:#E2C381;"></div>
            <div style="width:36px; height:36px; border-radius:50%; background:#8A4F33;"></div>
            <div style="width:36px; height:36px; border-radius:50%; background:#8DA08C;"></div>
            <div style="width:36px; height:36px; border-radius:50%; background:#735B23;"></div>
          </div>
        </div>

        <!-- Schedule & Timing -->
        <div class="ui-card" style="margin-bottom:16px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px">
            <div>
              <h4 style="font-size:15px; font-weight:600; color:#191C1A">Schedule Timing</h4>
              <p style="font-size:12px; color:#585F55">Daily at breakfast</p>
            </div>
            <span style="font-size:14px; font-weight:700; color:#516351; background:#E2EBDE; padding:4px 12px; border-radius:999px">08:30 AM</span>
          </div>
          <div style="display:flex; justify-content:space-between; align-items:center; padding-top:10px; border-top:1px solid rgba(0,0,0,0.05)">
            <span style="font-size:13px; font-weight:600; color:#191C1A">Refill Alert (42 remaining)</span>
            <div style="width:38px; height:22px; background:#516351; border-radius:999px; position:relative;">
              <div style="position:absolute; right:2px; top:2px; width:18px; height:18px; background:#FFFFFF; border-radius:50%"></div>
            </div>
          </div>
        </div>

        <!-- Full button -->
        <button style="width:100%; padding:16px; background:#516351; color:#FFFFFF; border:none; border-radius:999px; font-size:16px; font-weight:700; letter-spacing:0.02em; box-shadow:0 8px 24px rgba(81,99,81,0.25)">
          Add to Daily Ritual
        </button>

      </div>

      <!-- Bottom Nav -->
      <div class="bottom-nav">
        <div class="nav-item">
          <span class="material-symbols-outlined">calendar_today</span>
          <span class="nav-label">Today</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">view_in_ar</span>
          <span class="nav-label">Organizer</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">monitoring</span>
          <span class="nav-label">Progress</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">tune</span>
          <span class="nav-label">Settings</span>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`;
}

// SCREEN 5: SANCTUARY & NOTIFICATIONS
function generateScreen5() {
  return `${getHead('Hävn — Sanctuary & Reminders')}
  <div class="header-section">
    <div class="badge">
      <span class="badge-icon">🌿</span>
      <span>Sanctuary & Alerts</span>
    </div>
    <h1 class="headline">Mindful notifications with instant lockscreen actions.</h1>
    <p class="subtitle">Mark taken or snooze in 15 minutes right from notification.</p>
  </div>

  <div class="phone-chassis">
    <div class="phone-screen">
      <div class="camera-punch"></div>
      <div class="status-bar">
        <span>08:30</span>
        <div class="status-icons">
          <span>5G</span>
          <span class="material-symbols-outlined" style="font-size:18px">wifi</span>
          <span class="material-symbols-outlined" style="font-size:18px">battery_full</span>
        </div>
      </div>

      <div class="screen-content">
        <!-- App Bar -->
        <div class="app-bar">
          <div class="app-logo">
            <span class="material-symbols-outlined" style="color:#516351; font-size:24px">spa</span>
            <span class="app-logo-text">Sanctuary</span>
          </div>
          <div style="background:#E2EBDE; color:#516351; font-size:11px; font-weight:700; padding:6px 14px; border-radius:999px; letter-spacing:0.08em">
            ACTIVE
          </div>
        </div>

        <div style="margin-bottom:16px">
          <h2 style="font-family:'Fraunces',serif; font-size:28px; font-weight:500; color:#191C1A">Quiet Space for Rituals</h2>
          <p style="font-size:14px; color:#585F55; margin-top:2px">Silences distracting noise during medication times.</p>
        </div>

        <!-- Android Lock-Screen Notification Preview Hero -->
        <div style="background:rgba(255,255,255,0.92); backdrop-filter:blur(20px); border:1.5px solid rgba(81,99,81,0.2); border-radius:28px; padding:22px; box-shadow:0 16px 40px rgba(81,99,81,0.12); margin-bottom:20px;">
          <!-- Notification Top Header -->
          <div style="display:flex; align-items:center; gap:8px; margin-bottom:12px;">
            <div style="width:24px; height:24px; border-radius:6px; background:#516351; display:flex; align-items:center; justify-content:center; color:#FFF">
              <span class="material-symbols-outlined" style="font-size:14px">medication</span>
            </div>
            <span style="font-size:13px; font-weight:700; color:#191C1A">Hävn · Medication Reminder</span>
            <span style="font-size:12px; color:#747872; margin-left:auto">Just now</span>
          </div>

          <!-- Notification Content -->
          <h3 style="font-size:17px; font-weight:700; color:#191C1A; margin-bottom:4px">Morning Ritual Due (2 pills)</h3>
          <p style="font-size:14px; color:#434842; line-height:1.45; margin-bottom:16px">
            Time for Vitamin D3 & Omega-3. Please take with water and breakfast.
          </p>

          <!-- Interactive Action Buttons -->
          <div style="display:flex; gap:10px;">
            <div style="flex:1; background:#516351; color:#FFFFFF; border-radius:999px; padding:11px 0; text-align:center; font-size:13px; font-weight:700; display:flex; align-items:center; justify-content:center; gap:6px; box-shadow:0 4px 12px rgba(81,99,81,0.25)">
              <span class="material-symbols-outlined" style="font-size:18px">check</span>
              <span>Mark Taken</span>
            </div>
            <div style="flex:1; background:#FFFFFF; color:#516351; border:1.5px solid rgba(81,99,81,0.3); border-radius:999px; padding:11px 0; text-align:center; font-size:13px; font-weight:700; display:flex; align-items:center; justify-content:center; gap:6px">
              <span class="material-symbols-outlined" style="font-size:18px">snooze</span>
              <span>In 15 min</span>
            </div>
          </div>
        </div>

        <!-- Sanctuary Window Settings -->
        <div class="ui-card" style="margin-bottom:14px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px">
            <div>
              <h4 style="font-size:15px; font-weight:600; color:#191C1A">Morning Sanctuary Window</h4>
              <p style="font-size:12px; color:#585F55">08:00 AM – 09:00 AM daily</p>
            </div>
            <div style="width:38px; height:22px; background:#516351; border-radius:999px; position:relative;">
              <div style="position:absolute; right:2px; top:2px; width:18px; height:18px; background:#FFFFFF; border-radius:50%"></div>
            </div>
          </div>
          <div style="display:flex; justify-content:space-between; align-items:center; padding-top:12px; border-top:1px solid rgba(0,0,0,0.05)">
            <div>
              <h4 style="font-size:15px; font-weight:600; color:#191C1A">Gentle Acoustic Bell</h4>
              <p style="font-size:12px; color:#585F55">Nordic ritual chime instead of shrill alarms</p>
            </div>
            <span class="material-symbols-outlined" style="color:#516351; font-size:20px">volume_up</span>
          </div>
        </div>

        <!-- Calming Reminder Tips -->
        <div style="background:#E2EBDE; border-radius:18px; padding:14px 18px; display:flex; align-items:center; gap:12px;">
          <span class="material-symbols-outlined" style="color:#516351; font-size:24px">shield_with_heart</span>
          <p style="font-size:13px; color:#384638; line-height:1.4">
            Reliable reminders guaranteed with Android 14+ exact alarms and zero battery throttle.
          </p>
        </div>

      </div>

      <!-- Bottom Nav -->
      <div class="bottom-nav">
        <div class="nav-item">
          <span class="material-symbols-outlined">calendar_today</span>
          <span class="nav-label">Today</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">view_in_ar</span>
          <span class="nav-label">Organizer</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">monitoring</span>
          <span class="nav-label">Progress</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">tune</span>
          <span class="nav-label">Settings</span>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`;
}

// SCREEN 6: PRIVACY & OFFLINE VAULT
function generateScreen6() {
  return `${getHead('Hävn — Privacy by Design')}
  <div class="header-section">
    <div class="badge">
      <span class="badge-icon">🔒</span>
      <span>100% Offline</span>
    </div>
    <h1 class="headline">Private by design. Zero cloud data transfer.</h1>
    <p class="subtitle">No accounts. No trackers. Your medical data stays on your phone.</p>
  </div>

  <div class="phone-chassis">
    <div class="phone-screen">
      <div class="camera-punch"></div>
      <div class="status-bar">
        <span>09:41</span>
        <div class="status-icons">
          <span>5G</span>
          <span class="material-symbols-outlined" style="font-size:18px">wifi</span>
          <span class="material-symbols-outlined" style="font-size:18px">battery_full</span>
        </div>
      </div>

      <div class="screen-content">
        <!-- App Bar -->
        <div class="app-bar">
          <div class="app-logo">
            <span class="material-symbols-outlined" style="color:#516351; font-size:24px">lock</span>
            <span class="app-logo-text">Privacy Vault</span>
          </div>
          <div style="background:#E2EBDE; color:#516351; font-size:11px; font-weight:700; padding:6px 14px; border-radius:999px; letter-spacing:0.08em">
            ENCRYPTED
          </div>
        </div>

        <!-- Privacy Hero Card -->
        <div class="ui-card" style="text-align:center; padding:24px 20px; background:linear-gradient(180deg, #FFFFFF 0%, #F5F7F4 100%); margin-bottom:18px;">
          <div style="width:58px; height:58px; border-radius:50%; background:#E2EBDE; display:flex; align-items:center; justify-content:center; color:#516351; margin:0 auto 12px">
            <span class="material-symbols-outlined" style="font-size:32px">verified_user</span>
          </div>
          <h3 style="font-family:'Fraunces',serif; font-size:22px; font-weight:600; color:#191C1A">Local-First Architecture</h3>
          <p style="font-size:13px; color:#585F55; line-height:1.5; margin-top:6px; max-width:320px; margin-left:auto; margin-right:auto">
            Hävn requires zero internet permissions. Room database stores all medication logs strictly in your phone's sandbox.
          </p>
        </div>

        <!-- Backup Controls Card -->
        <div class="ui-card" style="margin-bottom:16px;">
          <h4 style="font-size:15px; font-weight:600; color:#191C1A; margin-bottom:12px">Data Backup & Ownership</h4>

          <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 0; border-bottom:1px solid rgba(0,0,0,0.05);">
            <div style="display:flex; align-items:center; gap:12px;">
              <span class="material-symbols-outlined" style="color:#516351; font-size:22px">file_download</span>
              <div>
                <p style="font-size:14px; font-weight:600; color:#191C1A">Export JSON Backup</p>
                <p style="font-size:12px; color:#747872">Save to device storage or drive</p>
              </div>
            </div>
            <span class="material-symbols-outlined" style="color:#747872; font-size:20px">arrow_forward_ios</span>
          </div>

          <div style="display:flex; align-items:center; justify-content:space-between; padding:10px 0;">
            <div style="display:flex; align-items:center; gap:12px;">
              <span class="material-symbols-outlined" style="color:#516351; font-size:22px">file_upload</span>
              <div>
                <p style="font-size:14px; font-weight:600; color:#191C1A">Restore from Backup</p>
                <p style="font-size:12px; color:#747872">Load previous doses and settings</p>
              </div>
            </div>
            <span class="material-symbols-outlined" style="color:#747872; font-size:20px">arrow_forward_ios</span>
          </div>
        </div>

        <!-- Multi-Profiles Card -->
        <div class="ui-card" style="margin-bottom:16px;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px">
            <h4 style="font-size:15px; font-weight:600; color:#191C1A">Multiple Profiles</h4>
            <span style="font-size:12px; font-weight:700; color:#516351">+ Add Profile</span>
          </div>
          <div style="display:flex; gap:12px;">
            <div style="display:flex; align-items:center; gap:8px; background:#E2EBDE; border:1.5px solid #516351; padding:6px 14px; border-radius:999px">
              <div style="width:20px; height:20px; border-radius:50%; background:#516351; color:#FFF; font-size:11px; font-weight:700; display:flex; align-items:center; justify-content:center">H</div>
              <span style="font-size:13px; font-weight:700; color:#516351">Henrik</span>
            </div>
            <div style="display:flex; align-items:center; gap:8px; background:#FFFFFF; border:1px solid rgba(0,0,0,0.08); padding:6px 14px; border-radius:999px">
              <div style="width:20px; height:20px; border-radius:50%; background:#FEB28F; color:#FFF; font-size:11px; font-weight:700; display:flex; align-items:center; justify-content:center">A</div>
              <span style="font-size:13px; font-weight:600; color:#747872">Astrid</span>
            </div>
            <div style="display:flex; align-items:center; gap:8px; background:#FFFFFF; border:1px solid rgba(0,0,0,0.08); padding:6px 14px; border-radius:999px">
              <div style="width:20px; height:20px; border-radius:50%; background:#E2C381; color:#FFF; font-size:11px; font-weight:700; display:flex; align-items:center; justify-content:center">M</div>
              <span style="font-size:13px; font-weight:600; color:#747872">Milo (Pet)</span>
            </div>
          </div>
        </div>

        <!-- Themes & Glance Widget -->
        <div style="background:#FFFFFF; border-radius:20px; padding:14px 18px; border:1px solid rgba(0,0,0,0.05); display:flex; justify-content:space-between; align-items:center;">
          <div>
            <h4 style="font-size:14px; font-weight:600; color:#191C1A">Scandinavian Theme</h4>
            <p style="font-size:12px; color:#585F55">Nordic Light & Midnight Dark modes</p>
          </div>
          <div style="display:flex; gap:6px">
            <div style="width:28px; height:28px; border-radius:50%; background:#FBF9F5; border:2px solid #516351; box-shadow:0 2px 6px rgba(0,0,0,0.1)"></div>
            <div style="width:28px; height:28px; border-radius:50%; background:#191C1A; border:1px solid rgba(0,0,0,0.2)"></div>
          </div>
        </div>

      </div>

      <!-- Bottom Nav -->
      <div class="bottom-nav">
        <div class="nav-item">
          <span class="material-symbols-outlined">calendar_today</span>
          <span class="nav-label">Today</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">view_in_ar</span>
          <span class="nav-label">Organizer</span>
        </div>
        <div class="nav-item">
          <span class="material-symbols-outlined">monitoring</span>
          <span class="nav-label">Progress</span>
        </div>
        <div class="nav-item active">
          <span class="material-symbols-outlined">tune</span>
          <span class="nav-label">Settings</span>
        </div>
      </div>
    </div>
  </div>
</body>
</html>`;
}

// 3D ORGANIZER EMBED FOR SCREEN 2
function generateThreeOrganizerEmbed() {
  return `<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<style>
  body { margin:0; overflow:hidden; background:transparent; }
  #canvas-container { width:100vw; height:100vh; }
</style>
<script src="three.min.js"></script>
</head>
<body>
<div id="canvas-container"></div>
<script>
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(38, window.innerWidth / window.innerHeight, 0.1, 1000);
  camera.position.set(0, 14, 22);
  camera.lookAt(0, 0, 0);

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.setPixelRatio(window.devicePixelRatio || 2);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  document.getElementById('canvas-container').appendChild(renderer.domElement);

  // Lighting
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.85);
  scene.add(ambientLight);

  const dirLight = new THREE.DirectionalLight(0xffffff, 1.2);
  dirLight.position.set(12, 24, 16);
  dirLight.castShadow = true;
  scene.add(dirLight);

  const softLight = new THREE.DirectionalLight(0xe2ebde, 0.6);
  softLight.position.set(-12, 10, -10);
  scene.add(softLight);

  // Organizer Base Box (Frosted warm acrylic)
  const group = new THREE.Group();
  scene.add(group);

  const bodyMat = new THREE.MeshPhysicalMaterial({
    color: 0xf4f1ea,
    roughness: 0.25,
    metalness: 0.05,
    transmission: 0.6,
    thickness: 1.2,
    transparent: true,
    opacity: 0.95
  });

  const baseGeo = new THREE.BoxGeometry(21, 3.2, 8.5);
  const baseMesh = new THREE.Mesh(baseGeo, bodyMat);
  baseMesh.castShadow = true;
  baseMesh.receiveShadow = true;
  group.add(baseMesh);

  // 4 Compartment wells
  const labels = ['MORNING', 'AFTERNOON', 'EVENING', 'NIGHT'];

  labels.forEach((label, i) => {
    const x = -7.5 + i * 5.0;

    // Compartment cutout inner bed
    const bedGeo = new THREE.BoxGeometry(4.2, 2.2, 6.8);
    const bedMat = new THREE.MeshStandardMaterial({
      color: i === 0 ? 0xd4e8d2 : 0xeeece6,
      roughness: 0.4
    });
    const bed = new THREE.Mesh(bedGeo, bedMat);
    bed.position.set(x, 0.6, 0);
    group.add(bed);

    // Compartment Lid
    const lidGeo = new THREE.BoxGeometry(4.2, 0.35, 7.0);
    const lidMat = new THREE.MeshPhysicalMaterial({
      color: 0xffffff,
      roughness: 0.2,
      transmission: 0.75,
      thickness: 0.8,
      transparent: true,
      opacity: 0.88
    });
    const lid = new THREE.Mesh(lidGeo, lidMat);

    if (i === 0) {
      // Morning lid tilted open
      lid.position.set(x, 3.4, -2.8);
      lid.rotation.x = -Math.PI / 3.2;
    } else {
      lid.position.set(x, 1.8, 0);
    }
    group.add(lid);

    // Add 3D Pills inside Morning compartment
    if (i === 0) {
      // Golden Amber Capsule
      const capGeo = new THREE.CylinderGeometry(0.5, 0.5, 1.6, 16);
      const capMat = new THREE.MeshStandardMaterial({ color: 0xe2c381, roughness: 0.3 });
      const cap = new THREE.Mesh(capGeo, capMat);
      cap.rotation.z = Math.PI / 3;
      cap.rotation.x = Math.PI / 6;
      cap.position.set(x - 0.8, 1.3, 0.8);
      group.add(cap);

      // Peach Softgel
      const sphGeo = new THREE.SphereGeometry(0.7, 16, 16);
      sphGeo.scale(1.4, 0.8, 0.9);
      const sphMat = new THREE.MeshPhysicalMaterial({ color: 0xfeb28f, roughness: 0.2, transmission: 0.4 });
      const sph = new THREE.Mesh(sphGeo, sphMat);
      sph.position.set(x + 0.9, 1.2, -0.6);
      sph.rotation.y = 0.5;
      group.add(sph);
    } else if (i === 1) {
      // Afternoon pills
      const tabGeo = new THREE.CylinderGeometry(0.7, 0.7, 0.4, 20);
      const tabMat = new THREE.MeshStandardMaterial({ color: 0x8da08c, roughness: 0.4 });
      const tab = new THREE.Mesh(tabGeo, tabMat);
      tab.position.set(x, 1.2, 0);
      group.add(tab);
    }
  });

  // Rotate group slightly for Scandinavian isometric product showcase angle
  group.rotation.x = 0.32;
  group.rotation.y = -0.42;
  group.position.y = -0.5;

  renderer.render(scene, camera);
</script>
</body>
</html>`;
}

// Generate all files
const screens = [
  { id: '01_daily_ritual', title: 'Daily Ritual', html: generateScreen1() },
  { id: '02_3d_organizer', title: '3D Organizer', html: generateScreen2() },
  { id: '03_progress_adherence', title: 'Adherence', html: generateScreen3() },
  { id: '04_customize_regimen', title: 'Customize', html: generateScreen4() },
  { id: '05_sanctuary_reminders', title: 'Sanctuary', html: generateScreen5() },
  { id: '06_privacy_and_backup', title: 'Privacy', html: generateScreen6() },
];

console.log('Writing embedded 3D organizer HTML...');
fs.writeFileSync(path.join(BUILD_DIR, 'three_organizer_embed.html'), generateThreeOrganizerEmbed(), 'utf8');

screens.forEach(screen => {
  const filePath = path.join(BUILD_DIR, `${screen.id}.html`);
  console.log(`Writing HTML for ${screen.title} to ${filePath}...`);
  fs.writeFileSync(filePath, screen.html, 'utf8');
});

console.log('All HTML templates written successfully!');
