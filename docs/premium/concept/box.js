// Hävn v2 organiser renderer — concept art for the premium brief.
// Mirrors the geometry of app/.../HavnOrganizerView.kt, upgraded with the
// materials the brief specifies: frosted translucent lids, sculpted pills,
// soft studio light, contact shadows.

const PAL = {
  light: {
    lid: [255, 255, 255], lidAlpha: 0.80, body0: '#F4F0E8', body1: '#E6E0D4',
    rim: '#FFFFFF', well0: '#ECEAE4', well1: '#F7F6F2', line: '#5E6E5D',
    shadow: '40,48,38', label: '#737A71', accent: '#5E6E5D', onAccent: '#FFFFFF',
    ghost: 'rgba(94,110,93,0.28)', sheen: 'rgba(255,255,255,0.9)', glow: '255,255,255',
  },
  dark: {
    lid: [58, 64, 56], lidAlpha: 0.72, body0: '#2A2F27', body1: '#1E221C',
    rim: '#363C33', well0: '#141712', well1: '#1B1F19', line: '#A8C0A4',
    shadow: '0,0,0', label: '#8B9084', accent: '#A8C0A4', onAccent: '#17230F',
    ghost: 'rgba(168,192,164,0.30)', sheen: 'rgba(255,255,255,0.18)', glow: '168,192,164',
  },
};
const MED = {
  light: { sage: '#5E6E5D', clay: '#A85D3B', amber: '#B98A2E', slate: '#7E929A', sand: '#B8A47E', rose: '#C98B8B' },
  dark: { sage: '#A8C0A4', clay: '#E8A480', amber: '#D9BA74', slate: '#A8BEC6', sand: '#CFBE9C', rose: '#E6AFAF' },
};
const LABELS = ['MORNING', 'AFTERNOON', 'EVENING', 'NIGHT'];

function mix(a, b, t) {
  const p = h => [1, 3, 5].map(i => parseInt(h.slice(i, i + 2), 16));
  const A = p(a), B = p(b);
  return 'rgb(' + A.map((v, i) => Math.round(v + (B[i] - v) * t)).join(',') + ')';
}

function rpoly(ctx, pts, r, only) {
  const n = pts.length; ctx.beginPath();
  for (let i = 0; i < n; i++) {
    const pr = pts[(i - 1 + n) % n], c = pts[i], nx = pts[(i + 1) % n];
    if (only && !only.includes(i)) { i == 0 ? ctx.moveTo(c[0], c[1]) : ctx.lineTo(c[0], c[1]); continue; }
    const tp = [pr[0] - c[0], pr[1] - c[1]], tn = [nx[0] - c[0], nx[1] - c[1]];
    const lp = Math.hypot(...tp), ln = Math.hypot(...tn), rr = Math.min(r, lp / 2, ln / 2);
    const a = [c[0] + tp[0] * rr / lp, c[1] + tp[1] * rr / lp], b = [c[0] + tn[0] * rr / ln, c[1] + tn[1] * rr / ln];
    i == 0 ? ctx.moveTo(a[0], a[1]) : ctx.lineTo(a[0], a[1]);
    ctx.quadraticCurveTo(c[0], c[1], b[0], b[1]);
  }
  ctx.closePath();
}

// ── Pills: sculpted with light from the upper left ──────────────────────────
function pill(ctx, type, cx, cy, s, tilt, tint, opts = {}) {
  const dark = opts.dark, ghost = opts.ghost, alpha = opts.alpha ?? 1;
  ctx.save(); ctx.globalAlpha *= alpha; ctx.translate(cx, cy); ctx.rotate(tilt * Math.PI / 180);
  const rr = (x, y, w, h, r) => { ctx.beginPath(); ctx.roundRect(x, y, w, h, r); };
  const edge = mix(tint, '#000000', 0.22);
  if (ghost) {
    ctx.setLineDash([s * 0.07, s * 0.07]); ctx.lineWidth = Math.max(1, s * 0.035); ctx.strokeStyle = ghost;
  }
  if (type === 'capsule') {
    const L = s, W = s * 0.42;
    rr(-L / 2, -W / 2, L, W, W / 2);
    if (ghost) { ctx.stroke(); ctx.restore(); return; }
    const g1 = ctx.createLinearGradient(0, -W / 2, 0, W / 2);
    g1.addColorStop(0, '#FFFFFF'); g1.addColorStop(0.55, dark ? '#D8DCD4' : '#F1EFEA'); g1.addColorStop(1, dark ? '#9EA59A' : '#D6D2C8');
    ctx.fillStyle = g1; ctx.fill();
    ctx.save(); ctx.beginPath(); ctx.rect(-L / 2, -W, L / 2 + W * 0.02, 2 * W); ctx.clip();
    rr(-L / 2, -W / 2, L, W, W / 2);
    const g2 = ctx.createLinearGradient(0, -W / 2, 0, W / 2);
    g2.addColorStop(0, mix(tint, '#FFFFFF', 0.35)); g2.addColorStop(0.5, tint); g2.addColorStop(1, mix(tint, '#000000', 0.3));
    ctx.fillStyle = g2; ctx.fill(); ctx.restore();
    ctx.beginPath(); ctx.moveTo(0, -W / 2); ctx.lineTo(0, W / 2); ctx.strokeStyle = 'rgba(0,0,0,0.12)'; ctx.lineWidth = s * 0.02; ctx.stroke();
    ctx.beginPath(); ctx.moveTo(-L / 2 + W * 0.55, -W * 0.2); ctx.lineTo(L / 2 - W * 0.55, -W * 0.2);
    ctx.strokeStyle = 'rgba(255,255,255,0.65)'; ctx.lineWidth = W * 0.13; ctx.lineCap = 'round'; ctx.stroke();
  } else if (type === 'tablet') {
    const r = s * 0.34;
    ctx.beginPath(); ctx.arc(0, 0, r, 0, 7);
    if (ghost) { ctx.stroke(); ctx.restore(); return; }
    const g = ctx.createRadialGradient(-r * 0.35, -r * 0.4, r * 0.1, 0, 0, r);
    g.addColorStop(0, '#FFFFFF'); g.addColorStop(0.7, mix(tint, '#FFFFFF', 0.72)); g.addColorStop(1, mix(tint, '#FFFFFF', 0.45));
    ctx.fillStyle = g; ctx.fill();
    ctx.beginPath(); ctx.arc(0, 0, r * 0.82, 0, 7); ctx.strokeStyle = 'rgba(0,0,0,0.07)'; ctx.lineWidth = s * 0.03; ctx.stroke();
    ctx.beginPath(); ctx.moveTo(-r * 0.62, 0); ctx.lineTo(r * 0.62, 0); ctx.strokeStyle = mix(tint, '#000000', 0.1); ctx.globalAlpha *= 0.45; ctx.lineWidth = s * 0.04; ctx.lineCap = 'round'; ctx.stroke();
  } else if (type === 'softgel') {
    const w = s * 0.66, h = s * 0.46;
    ctx.beginPath(); ctx.ellipse(0, 0, w / 2, h / 2, 0, 0, 7);
    if (ghost) { ctx.stroke(); ctx.restore(); return; }
    const g = ctx.createRadialGradient(-w * 0.15, -h * 0.2, h * 0.05, 0, 0, w * 0.55);
    g.addColorStop(0, mix(tint, '#FFFFFF', 0.65)); g.addColorStop(0.45, tint); g.addColorStop(1, mix(tint, '#000000', 0.35));
    ctx.globalAlpha *= 0.94; ctx.fillStyle = g; ctx.fill(); ctx.globalAlpha /= 0.94;
    ctx.beginPath(); ctx.ellipse(-w * 0.14, -h * 0.18, w * 0.16, h * 0.1, -0.3, 0, 7); ctx.fillStyle = 'rgba(255,255,255,0.75)'; ctx.fill();
  } else if (type === 'oval') {
    const w = s * 0.72, h = s * 0.36;
    rr(-w / 2, -h / 2, w, h, h / 2);
    if (ghost) { ctx.stroke(); ctx.restore(); return; }
    const g = ctx.createLinearGradient(0, -h / 2, 0, h / 2);
    g.addColorStop(0, mix(tint, '#FFFFFF', 0.55)); g.addColorStop(1, mix(tint, '#000000', 0.15));
    ctx.fillStyle = g; ctx.fill();
    ctx.beginPath(); ctx.moveTo(-w * 0.28, -h * 0.18); ctx.lineTo(w * 0.28, -h * 0.18); ctx.strokeStyle = 'rgba(255,255,255,0.5)'; ctx.lineWidth = h * 0.16; ctx.lineCap = 'round'; ctx.stroke();
  }
  ctx.restore();
}

// ── The box ─────────────────────────────────────────────────────────────────
// opts: { dark, lids:[deg x4], pills:[[{type,tag,taken,lift,alpha,dx,dy,scale}]], selected,
//         current, top (0..1), glow (0..1), labels, marks:[ 'tick'|n|null ] }
function drawBox(ctx, W, H, opts) {
  const dark = !!opts.dark, P = dark ? PAL.dark : PAL.light, M = dark ? MED.dark : MED.light;
  const left = W * (opts.inset ?? 0.05), right = W - left, width = right - left;
  const persp = width * 0.055, depth = width * 0.27, fh = width * 0.15, rise = depth * 1.08;
  const back = H * (opts.top ?? 0.40), front = back + depth, bottom = front + fh;
  const corner = width * 0.035, stroke = Math.max(1.4, width / 300);
  const bL = left + persp, bR = right - persp;
  const fX = f => left + width * f, bX = f => bL + (bR - bL) * f;
  const X = (f, u) => bX(f) + (fX(f) - bX(f)) * u, Y = u => back + depth * u;
  const lids = opts.lids || [0, 0, 0, 0];
  const pills = opts.pills || [[], [], [], []];

  // Ground: wide soft pool + tight contact shadow
  for (const [ry, rx, a] of [[0.22, 0.66, dark ? 0.6 : 0.16], [0.06, 0.52, dark ? 0.7 : 0.22]]) {
    ctx.save(); ctx.translate(W / 2, bottom + 2); ctx.scale(1, ry);
    const g = ctx.createRadialGradient(0, 0, 0, 0, 0, width * rx);
    g.addColorStop(0, `rgba(${P.shadow},${a})`); g.addColorStop(1, `rgba(${P.shadow},0)`);
    ctx.fillStyle = g; ctx.beginPath(); ctx.arc(0, 0, width * rx, 0, 7); ctx.fill(); ctx.restore();
  }
  // Day-complete glow
  if (opts.glow) {
    const g = ctx.createRadialGradient(W / 2, back + depth * 0.5, 0, W / 2, back + depth * 0.5, width * 0.75);
    g.addColorStop(0, `rgba(${P.glow},${0.55 * opts.glow})`); g.addColorStop(1, `rgba(${P.glow},0)`);
    ctx.fillStyle = g; ctx.fillRect(0, 0, W, H);
  }

  const sil = [[bL, back], [bR, back], [right, front], [right, bottom], [left, bottom], [left, front]];
  rpoly(ctx, sil, corner); ctx.fillStyle = P.rim; ctx.fill();
  // Front face: linen with a falloff and a hairline sheen just under the lip
  rpoly(ctx, [[left, front], [right, front], [right, bottom], [left, bottom]], corner, [2, 3]);
  const fg = ctx.createLinearGradient(0, front, 0, bottom);
  fg.addColorStop(0, P.body0); fg.addColorStop(1, P.body1); ctx.fillStyle = fg; ctx.fill();
  ctx.beginPath(); ctx.moveTo(left + corner, front + fh * 0.08); ctx.lineTo(right - corner, front + fh * 0.08);
  ctx.strokeStyle = dark ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.8)'; ctx.lineWidth = stroke; ctx.stroke();

  // Wells
  const wellQuad = i => {
    const f0 = (i + 0.1) / 4, f1 = (i + 0.9) / 4;
    return [[X(f0, 0.12), Y(0.12)], [X(f1, 0.12), Y(0.12)], [X(f1, 0.88), Y(0.88)], [X(f0, 0.88), Y(0.88)]];
  };
  const pillPos = (i, k, n) => {
    const cols = n <= 2 ? n : 3, rows = Math.ceil(n / cols), col = k % cols, row = Math.floor(k / cols);
    const fx = (i + 0.22 + 0.56 * (col + 0.5) / cols) / 4;
    const u = rows == 1 ? 0.56 : 0.38 + 0.38 * row / (rows - 1);
    const size = (width / 4) * 0.56 * (0.86 + 0.14 * u) * (cols == 3 ? 0.7 : cols == 2 ? 0.86 : 1);
    return [X(fx, u), Y(u), size];
  };
  const lifted = [];
  for (let i = 0; i < 4; i++) {
    const q = wellQuad(i);
    rpoly(ctx, q, corner * 0.8);
    const wg = ctx.createLinearGradient(0, Y(0.12), 0, Y(0.88));
    wg.addColorStop(0, P.well0); wg.addColorStop(1, P.well1); ctx.fillStyle = wg; ctx.fill();
    ctx.save(); ctx.clip();
    const ig = ctx.createLinearGradient(0, Y(0.12), 0, Y(0.12) + depth * 0.3);
    ig.addColorStop(0, `rgba(${P.shadow},${dark ? 0.55 : 0.14})`); ig.addColorStop(1, 'rgba(0,0,0,0)');
    ctx.fillStyle = ig; ctx.fillRect(left, Y(0.12), width, depth);
    ctx.restore();
    rpoly(ctx, q, corner * 0.8); ctx.strokeStyle = dark ? 'rgba(168,192,164,0.18)' : 'rgba(94,110,93,0.18)'; ctx.lineWidth = stroke * 0.7; ctx.stroke();

    const list = pills[i] || [];
    const closed = lids[i] < 20;
    list.forEach((p, k) => {
      const [x, y, s] = pillPos(i, k, list.length);
      const tilt = p.tilt ?? (-34 + ((k * 37 + i * 11) % 50));
      if (p.lift) { lifted.push({ p, x, y, s, tilt }); return; }
      if (closed) {
        // Seen through the frosted lid: softened, desaturated
        ctx.save(); ctx.filter = 'blur(2.2px)'; pill(ctx, p.type, x, y, s, tilt, M[p.tag], { dark, alpha: p.taken ? 0 : 0.9 }); ctx.restore();
      } else if (p.taken) {
        pill(ctx, p.type, x, y, s, tilt, M[p.tag], { dark, ghost: P.ghost, alpha: p.alpha ?? 1 });
      } else {
        ctx.fillStyle = `rgba(${P.shadow},${dark ? 0.5 : 0.16})`;
        ctx.beginPath(); ctx.ellipse(x, y + s * 0.2, s * 0.42, s * 0.13, 0, 0, 7); ctx.fill();
        pill(ctx, p.type, x, y, s, tilt, M[p.tag], { dark, alpha: p.alpha ?? 1 });
      }
    });
    if (list.length && list.every(p => p.taken) && !closed && (opts.tickIn ?? 1) > 0) {
      const cx = X((i + 0.5) / 4, 0.55), cy = Y(0.55), r = width / 4 * 0.12, t = opts.tickIn ?? 1;
      ctx.save(); ctx.globalAlpha = t; ctx.beginPath(); ctx.moveTo(cx - r, cy); ctx.lineTo(cx - r * 0.25, cy + r * 0.7); ctx.lineTo(cx + r * 1.05, cy - r * 0.65);
      ctx.strokeStyle = P.accent; ctx.lineWidth = stroke * 1.6; ctx.lineCap = 'round'; ctx.lineJoin = 'round'; ctx.stroke(); ctx.restore();
    }
  }

  // Front labels
  for (let i = 1; i < 4; i++) {
    ctx.beginPath(); ctx.moveTo(fX(i / 4), front + fh * 0.22); ctx.lineTo(fX(i / 4), bottom - fh * 0.22);
    ctx.strokeStyle = dark ? 'rgba(168,192,164,0.16)' : 'rgba(94,110,93,0.2)'; ctx.lineWidth = stroke * 0.7; ctx.stroke();
  }
  if (opts.labels !== false) {
    const fs = Math.max(7.5 * (opts.dpr || 2), Math.min(10.5 * (opts.dpr || 2), width / 38));
    LABELS.forEach((l, i) => {
      const sel = i === opts.selected;
      ctx.font = `${sel ? 600 : 500} ${fs}px Sans`; ctx.letterSpacing = (1.6 * (opts.dpr || 2)) + 'px';
      ctx.fillStyle = sel ? P.accent : P.label; ctx.textAlign = 'center'; ctx.textBaseline = 'middle';
      ctx.fillText(l, fX((i + 0.5) / 4), front + fh * 0.58);
      if (i === opts.current) { ctx.fillStyle = P.accent; ctx.beginPath(); ctx.arc(fX((i + 0.5) / 4), front + fh * 0.26, stroke * 1.5, 0, 7); ctx.fill(); }
    });
    ctx.letterSpacing = '0px';
  }

  // Lids: frosted, translucent, bevelled
  for (let i = 0; i < 4; i++) {
    const g = 0.03, f0 = (i + g) / 4, f1 = (i + 1 - g) / 4, th = lids[i] * Math.PI / 180, fw = Math.cos(th), up = Math.sin(th);
    const Pt = (f, s) => { const u = s * fw; return [X(f, u), Y(u) - s * up * rise]; };
    const hL = Pt(f0, 0), hR = Pt(f1, 0), tR = Pt(f1, 1), tL = Pt(f0, 1), t = fh * 0.1, top = fw > 0;
    if (fw > -0.2) {
      rpoly(ctx, [tL, tR, [tR[0], tR[1] + t], [tL[0], tL[1] + t]], t * 0.5);
      ctx.fillStyle = dark ? 'rgba(80,88,76,0.9)' : 'rgba(236,238,233,0.95)'; ctx.fill();
      ctx.strokeStyle = P.line; ctx.globalAlpha = 0.55; ctx.lineWidth = stroke * 0.8; ctx.stroke(); ctx.globalAlpha = 1;
    }
    rpoly(ctx, [hL, hR, tR, tL], corner * 0.9);
    const [r, gg, b] = P.lid;
    const lg = ctx.createLinearGradient(hL[0], hL[1], tR[0], tR[1]);
    lg.addColorStop(0, `rgba(${r},${gg},${b},${P.lidAlpha + (top ? 0.05 : -0.1)})`);
    lg.addColorStop(1, `rgba(${r},${gg},${b},${P.lidAlpha - (top ? 0.08 : 0.2)})`);
    if (!top) {
      // The underside: a denser frosted panel with its own soft shadow so it
      // separates from a white page.
      ctx.save(); ctx.shadowColor = `rgba(${P.shadow},${dark ? 0.6 : 0.16})`; ctx.shadowBlur = 18; ctx.shadowOffsetY = 6;
      const ug = ctx.createLinearGradient(0, tL[1], 0, hL[1]);
      ug.addColorStop(0, dark ? '#3A4037' : '#F6F7F4'); ug.addColorStop(1, dark ? '#2A2F27' : '#E7E9E3');
      ctx.fillStyle = ug; ctx.fill(); ctx.restore();
      rpoly(ctx, [hL, hR, tR, tL], corner * 0.9);
      const hg = ctx.createLinearGradient(tL[0], 0, tR[0], 0);
      hg.addColorStop(0, 'rgba(255,255,255,0)'); hg.addColorStop(0.35, dark ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.85)'); hg.addColorStop(0.6, 'rgba(255,255,255,0)');
      ctx.fillStyle = hg; ctx.fill();
    } else { ctx.fillStyle = lg; ctx.fill(); }
    // Specular streak across the frosted top
    if (top) {
      ctx.save(); rpoly(ctx, [hL, hR, tR, tL], corner * 0.9); ctx.clip();
      const sx = (hL[0] + tR[0]) / 2;
      const sg = ctx.createLinearGradient(sx - width * 0.12, hL[1], sx + width * 0.04, tR[1]);
      sg.addColorStop(0, 'rgba(255,255,255,0)'); sg.addColorStop(0.5, P.sheen); sg.addColorStop(1, 'rgba(255,255,255,0)');
      ctx.globalAlpha = dark ? 0.6 : 0.55; ctx.fillStyle = sg; ctx.fillRect(left, back - rise, width, depth + rise); ctx.restore();
    }
    rpoly(ctx, [hL, hR, tR, tL], corner * 0.9);
    ctx.strokeStyle = P.line; ctx.globalAlpha = top ? 0.85 : 0.6; ctx.lineWidth = stroke; ctx.stroke(); ctx.globalAlpha = 1;
    // Lid marks
    const mark = opts.marks ? opts.marks[i] : null;
    if (top && lids[i] < 25 && mark) {
      const c = Pt((f0 + f1) / 2, 0.55), rr2 = (width / 4) * 0.075;
      if (mark === 'tick') {
        ctx.fillStyle = P.accent; ctx.beginPath(); ctx.arc(c[0], c[1], rr2 * 1.25, 0, 7); ctx.fill();
        ctx.beginPath(); ctx.moveTo(c[0] - rr2 * 0.55, c[1]); ctx.lineTo(c[0] - rr2 * 0.12, c[1] + rr2 * 0.42); ctx.lineTo(c[0] + rr2 * 0.6, c[1] - rr2 * 0.38);
        ctx.strokeStyle = P.onAccent; ctx.lineWidth = stroke * 1.2; ctx.lineCap = 'round'; ctx.stroke();
      } else if (typeof mark === 'number') {
        const st = rr2 * 0.95, s0 = c[0] - st * (mark - 1) / 2; ctx.fillStyle = P.accent;
        for (let k = 0; k < mark; k++) { ctx.beginPath(); ctx.arc(s0 + st * k, c[1], rr2 * 0.28, 0, 7); ctx.fill(); }
      }
    }
  }
  rpoly(ctx, sil, corner); ctx.strokeStyle = P.line; ctx.lineWidth = stroke * 1.1; ctx.lineJoin = 'round'; ctx.stroke();
  ctx.beginPath(); ctx.moveTo(left, front); ctx.lineTo(right, front); ctx.stroke();

  // Lifted pills float above everything, casting a shadow back onto the box
  lifted.forEach(({ p, x, y, s, tilt }) => {
    if (p.to) {
      // Flight along a quadratic arc from the well to an absolute target.
      const t = p.t, [ex, ey] = p.to, cx = (x + ex) / 2, cy = Math.min(y, ey) - (p.arc ?? depth * 1.5);
      const bx = (1 - t) * (1 - t) * x + 2 * (1 - t) * t * cx + t * t * ex;
      const by = (1 - t) * (1 - t) * y + 2 * (1 - t) * t * cy + t * t * ey;
      if (t < 0.5) {
        ctx.fillStyle = `rgba(${P.shadow},${(dark ? 0.5 : 0.18) * (1 - t * 2)})`;
        ctx.beginPath(); ctx.ellipse(x, y + s * 0.22, s * 0.45 * (1 + t), s * 0.14 * (1 + t), 0, 0, 7); ctx.fill();
      }
      pill(ctx, p.type, bx, by, s * (p.scale ?? 1), tilt * (1 - t), M[p.tag], { dark, alpha: p.alpha ?? 1 });
      return;
    }
    if (p.touch !== undefined) {
      ctx.save(); ctx.globalAlpha = 0.45 * (1 - p.touch); ctx.strokeStyle = P.accent; ctx.lineWidth = stroke * 1.4;
      ctx.beginPath(); ctx.arc(x, y, s * (0.55 + 0.6 * p.touch), 0, 7); ctx.stroke(); ctx.restore();
    }
    const L = p.lift, sc = p.scale ?? (1 + 0.25 * L);
    const tx = x + (p.dx ?? 0) * L, ty = y + (p.dy ?? -depth * 1.4) * L;
    ctx.fillStyle = `rgba(${P.shadow},${(dark ? 0.5 : 0.18) * (1 - L * 0.6)})`;
    ctx.beginPath(); ctx.ellipse(x + (p.dx ?? 0) * L * 0.3, y + s * 0.22, s * 0.45 * (1 + L * 0.4), s * 0.14 * (1 + L * 0.4), 0, 0, 7); ctx.fill();
    pill(ctx, p.type, tx, ty, s * sc, tilt * (1 - L) + (p.endTilt ?? 0) * L, M[p.tag], { dark, alpha: p.alpha ?? 1 });
  });
  return { left, right, back, front, bottom, width, depth, X, Y };
}

function setupCanvas(id, w, h, dpr = 2) {
  const cv = document.getElementById(id); cv.width = w * dpr; cv.height = h * dpr;
  cv.style.width = w + 'px'; cv.style.height = h + 'px';
  return cv.getContext('2d');
}
