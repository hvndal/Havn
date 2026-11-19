
/* ================= Settings, backups, Face ID lock, Home Screen, Lock Screen, states ================= */
let faceLock = false, showNames = false;
const T_CLOCK = '<svg width="16" height="16" viewBox="0 0 16 16"><circle cx="8" cy="8" r="6.4" fill="none" stroke="#fff" stroke-width="1.8"/><path d="M8 4.6V8l2.4 1.6" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/></svg>';
const T_LOCK = '<svg width="13" height="16" viewBox="0 0 13 16"><rect x="1" y="7" width="11" height="8.5" rx="2.4" fill="#fff"/><path d="M3.5 7V5a3 3 0 0 1 6 0v2" fill="none" stroke="#fff" stroke-width="1.8"/></svg>';
const T_FACE = '<svg width="16" height="16" viewBox="0 0 16 16" fill="none" stroke="#fff" stroke-width="1.7" stroke-linecap="round"><path d="M1.5 5V3.5a2 2 0 0 1 2-2H5M11 1.5h1.5a2 2 0 0 1 2 2V5M14.5 11v1.5a2 2 0 0 1-2 2H11M5 14.5H3.5a2 2 0 0 1-2-2V11M5.5 5.8v1M10.5 5.8v1M8 6v3H7M5.6 11a3.4 3.4 0 0 0 4.8 0"/></svg>';
const T_SHARE = '<svg width="14" height="16" viewBox="0 0 14 16"><path d="M7 1v9M3.8 4.2L7 1l3.2 3.2" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/><path d="M4 7H2.5A1.5 1.5 0 0 0 1 8.5v5A1.5 1.5 0 0 0 2.5 15h9a1.5 1.5 0 0 0 1.5-1.5v-5A1.5 1.5 0 0 0 11.5 7H10" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/></svg>';
const swH = (label, on) => `<button class="sw ${on ? 'on' : ''}" aria-label="${label}" aria-pressed="${on}"><span class="k"></span></button>`;
function drawSettings(){
  saveCur();
  $('#settingsBody').innerHTML = `
  <div class="shead">People</div><div class="card">${Object.values(PEOPLE).map(p => `<div class="navrow" data-sp="${p.id}">${avatarHTML(p)}<span class="lbl">${p.name}</span><span class="val">${p.meds.length + p.prn.length} medications</span>${CHEV}</div>`).join('')}
    <div class="navrow add" data-addp="1"><span class="lbl">Add Person</span></div></div>
  <div class="shead">Reminders</div><div class="card">
    <div class="srow"><span class="tile" style="background:var(--red)">${T_BELL}</span><span class="lbl">Dose Reminders</span>${swH('Dose Reminders', !notifOff)}</div>
    <div class="navrow"><span class="tile" style="background:var(--blue)">${T_CLOCK}</span><span class="lbl">Follow-Up Reminder</span><span class="val">After 30 min</span>${CHEV}</div>
    <div class="srow"><span class="tile" style="background:var(--orange)">${T_ALARM}</span><span class="lbl">Ring Through Silent Mode</span>${swH('Ring Through Silent Mode', false)}</div>
    <div class="srow"><span class="tile" style="background:var(--gray)">${T_LOCK}</span><span class="lbl">Show Names on Lock Screen</span>${swH('Show Names on Lock Screen', showNames)}</div></div>
  <p class="foot">Ringing uses an alarm, which sounds even in Silent mode and Focus. Medication names stay hidden on the Lock Screen unless you turn them on.</p>
  <div class="shead">Sounds and Haptics</div><div class="card"><div class="srow"><span class="lbl">Sounds</span>${swH('Sounds', Snd.set !== 'off')}</div><div class="srow"><span class="lbl">Haptics</span>${swH('Haptics', true)}</div></div>
  <div class="shead">Apple Health</div><div class="card"><div class="srow"><span class="tile" style="background:#FF2D55">${T_HEART}</span><span class="lbl">Read Medications from Health</span>${swH('Read Medications from Health', true)}</div></div>
  <p class="foot">Hävn only reads your medication list. Nothing leaves your iPhone.</p>
  <div class="shead">Privacy and Data</div><div class="card">
    <div class="srow"><span class="tile" style="background:var(--green)">${T_FACE}</span><span class="lbl">Lock with Face ID</span>${swH('Lock with Face ID', faceLock)}</div>
    <div class="srow"><span class="tile" style="background:var(--blue)">${T_FOLDER}</span><span class="lbl">Include in iPhone Backups</span>${swH('Include in iPhone Backups', false)}</div>
    <div class="navrow" data-export="1"><span class="tile" style="background:var(--indigo)">${T_SHARE}</span><span class="lbl">Export Backup</span>${CHEV}</div>
    <div class="navrow" data-restore="1"><span class="tile" style="background:var(--purple)">${T_RESTORE}</span><span class="lbl">Restore from Backup</span>${CHEV}</div></div>
  <p class="foot">Hävn has no servers. A backup is a file you keep in Files, or anywhere you like. With iPhone Backups off, export one before you change phones.</p>
  <div class="card" style="margin-top:22px"><div class="navrow danger" data-erase="1">Erase All Data</div></div>
  <div class="shead">About</div><div class="card"><div class="navrow" data-policy="1"><span class="lbl">Privacy Policy</span>${CHEV}</div><div class="srow"><span class="lbl">Version</span><span class="dt">1.0</span></div></div>
  <p class="foot">Hävn is a reminder tool and doesn’t give medical advice. Always follow your doctor’s or pharmacist’s instructions.</p>`;
  $('#settingsBody').querySelectorAll('.sw').forEach(sw => { wireSwitch(sw); sw.addEventListener('pointerup', () => setTimeout(() => settingChanged(sw), 0)); });
}
function settingChanged(sw){
  const on = sw.classList.contains('on'), k = sw.getAttribute('aria-label');
  if(k === 'Dose Reminders'){ notifOff = !on; drawBanner(); syncStates(); }
  if(k === 'Sounds') setSound(on ? 'glass' : 'off');
  if(k === 'Show Names on Lock Screen') showNames = on;
  if(k === 'Lock with Face ID'){ faceLock = on; if(on) toast('Hävn locks when you leave it'); syncStates(); }
}
$('#settingsBody').addEventListener('click', e => {
  const t = e.target;
  if(t.closest('[data-sp]')){ const id = t.closest('[data-sp]').dataset.sp; closeSettings(); setTimeout(() => switchPerson(id), reduce ? 0 : 380); return; }
  if(t.closest('[data-addp]')){ closeSettings(); setTimeout(addPersonFlow, reduce ? 0 : 380); return; }
  if(t.closest('[data-export]')) return exportFlow();
  if(t.closest('[data-restore]')) return restoreFlow();
  if(t.closest('[data-policy]')) return appSheet(`<h3>Privacy Policy</h3><p class="sub">The short version</p>
    <div class="card" style="padding:4px 4px">${[['Nothing collected', 'No account, no servers, no analytics, no ads, no tracking.'], ['Stays on this iPhone', 'Your medications, people, history and notes live only in Hävn’s storage. Reports are made on the iPhone and never uploaded.'], ['Health is read only', 'Hävn reads your medication list if you allow it, and never writes to Health.'], ['You can erase it', 'Erase All Data, or delete the app, removes everything.']].map(([a, b]) => `<div class="feat" style="padding:10px 12px"><div class="mid"><div class="nm">${a}</div><div class="dt">${b}</div></div></div>`).join('')}</div>
    <p class="foot" style="text-align:center">Full policy: mander.tech/havn/privacy</p><button class="btn2 p" data-close="1" style="margin-top:12px">Done</button>`);
  if(t.closest('[data-erase]')) return showAlert({title:'Erase All Data?', body:'This removes every medication and dose from this iPhone. It can’t be undone.',
    buttons:[{label:'Cancel'}, {label:'Erase', style:'des', onTap:() => { closeSettings(); setTimeout(() => { haptic($('#phone'), 'warning haptic', 201, 437); setScene('welcome'); }, reduce ? 0 : 420); }}]});
});

/* a small sheet inside the app (backups, policy) */
function appSheet(html){
  const sh = $('#appSheet'); sh.innerHTML = `<div class="grab"></div>${html}`; sh.classList.add('open'); $('#appDim').hidden = false;
  if(!reduce){ sh.animate([{transform:'translateY(110%)'}, {transform:'none'}], {duration:520, easing:'cubic-bezier(.2,.95,.25,1.02)'}); $('#appDim').animate([{opacity:0}, {opacity:1}], {duration:300}); }
  sh.onclick = e => { if(e.target.closest('[data-close]')) closeAppSheet(); };
  return sh;
}
function closeAppSheet(after){
  const sh = $('#appSheet'); if(!sh.classList.contains('open')) return; $('#appDim').hidden = true;
  const done = () => { sh.classList.remove('open'); typeof after === 'function' && after(); };
  if(reduce) return done();
  sh.animate([{transform:'none'}, {transform:'translateY(110%)'}], {duration:380, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = done;
}
function progressRing(id){ const C = 2*Math.PI*40; return `<svg width="96" height="96" viewBox="0 0 100 100" id="${id}"><circle cx="50" cy="50" r="40" fill="none" stroke="var(--fill)" stroke-width="10"/>
  <circle class="pr" cx="50" cy="50" r="40" fill="none" stroke="var(--accent)" stroke-width="10" stroke-linecap="round" stroke-dasharray="0 ${C}" transform="rotate(-90 50 50)"/></svg>`; }
function runProgress(id, ms, done){ const C = 2*Math.PI*40, c = $(`#${id} .pr`);
  requestAnimationFrame(() => { c.style.transition = `stroke-dasharray ${reduce ? .1 : ms/1000}s cubic-bezier(.4,0,.2,1)`; c.setAttribute('stroke-dasharray', `${C} ${C}`); });
  setTimeout(done, reduce ? 150 : ms + 120); }
function exportFlow(){
  saveCur(); const n = Object.values(PEOPLE).reduce((a, p) => a + p.meds.length + p.prn.length, 0);
  const sh = appSheet(`<div style="display:grid;place-items:center;margin-top:14px">${progressRing('expRing')}</div><h3 id="expT">Preparing your backup</h3><p class="sub" id="expS">${n} medications, ${Object.keys(PEOPLE).length} people and their history</p><div id="expB"></div>`);
  runProgress('expRing', 1100, () => {
    sh.querySelector('#expT').textContent = 'Backup Ready'; Snd.play('on');
    sh.querySelector('#expB').innerHTML = `<div class="card"><div class="file" style="cursor:default"><div class="fic">${miniRing([1,1,1,1], 22, ['indigo','pink','brown','purple'])}</div><div class="mid"><div class="nm" style="font-size:16px">Hävn Backup 18 Nov 2026.havn</div><div class="dt" style="font-size:13px">23 KB · readable only by Hävn</div></div></div></div>
      <button class="btn2 p" data-save="1" style="margin-top:14px">Save to Files</button><button class="ob-link" data-close="1">Not Now</button>`;
    sh.querySelector('[data-save]').onclick = () => closeAppSheet(() => toast('Saved to Files'));
  });
}
function restoreFlow(){
  appSheet(`<h3>Choose a Backup</h3><p class="sub">On My iPhone › Hävn</p><div class="card"><div class="file" data-rfile="1"><div class="fic">${miniRing([1,1,1,1], 22, ['indigo','pink','brown','purple'])}</div><div class="mid"><div class="nm" style="font-size:16px">Hävn Backup 12 Nov 2026.havn</div><div class="dt" style="font-size:13px">21 KB · 4 medications · 214 doses</div></div></div></div><button class="ob-link" data-close="1" style="margin-top:14px">Cancel</button>`)
    .addEventListener('click', e => { if(!e.target.closest('[data-rfile]')) return;
      closeAppSheet(() => showAlert({title:'Restore This Backup?', body:'It replaces everything on this iPhone with the backup from 12 November.',
        buttons:[{label:'Cancel'}, {label:'Restore', style:'des', onTap:() => {
          const sh = appSheet(`<div style="display:grid;place-items:center;margin-top:14px">${progressRing('rsRing')}</div><h3 id="rsT">Restoring</h3><p class="sub">4 medications and 214 doses</p>`);
          runProgress('rsRing', 1300, () => { sh.querySelector('#rsT').textContent = 'All back.'; Snd.play('close');
            setTimeout(() => closeAppSheet(() => { closeSettings(); resetPeople(); $('#openSettings').innerHTML = avatarHTML(PEOPLE.you); refreshToday(true); }), 700); }); }}]})); }, {once:true});
}
function closeAllSheets(){
  closePeopleMenu(); closeAppSheet(); closeNote(); closeReport(); closeDay();
  if($('#dose').classList.contains('open')) closeDose(); if($('#settings').classList.contains('open')) closeSettings(); closeAdd();
  if($('#navBack')) { const nav = $('#scrMeds .nav2'); while(nav && nav.children.length > 1) nav.lastElementChild.remove(); $('#navBack').remove(); }
}

/* ---------- Face ID app lock ---------- */
function showAppLock(){ const l = $('#applock'); l.hidden = false; $('#lockBody').innerHTML = `<div style="display:grid;place-items:center">${APPICON(64)}</div>
    <div class="nm" style="margin-top:14px;font-size:20px">Hävn is Locked</div><div class="dt" style="margin:4px 0 22px">Unlock to see your medications.</div>
    <button class="btn2 p" style="width:230px;margin:0 auto" id="unlockBtn">Unlock with Face ID</button>`;
  $('#unlockBtn').onclick = faceUnlock;
  if(!reduce) l.animate([{opacity:0}, {opacity:1}], {duration:300}); }
function faceUnlock(){
  const b = $('#lockBody'); b.innerHTML = `<svg class="faceid" viewBox="0 0 64 64"><path d="M4 18V12a8 8 0 0 1 8-8h6M46 4h6a8 8 0 0 1 8 8v6M60 46v6a8 8 0 0 1-8 8h-6M18 60h-6a8 8 0 0 1-8-8v-6M22 24v4M42 24v4M32 24v13h-3M24 44a12 12 0 0 0 16 0"/></svg>`;
  const paths = b.querySelectorAll('path');
  if(!reduce) paths.forEach(p => { const L = p.getTotalLength(); p.style.strokeDasharray = L; p.animate([{strokeDashoffset:L}, {strokeDashoffset:0}], {duration:600, easing:'ease-out'}); });
  setTimeout(() => { b.innerHTML = `<svg class="faceid" viewBox="0 0 64 64"><circle cx="32" cy="32" r="28" fill="none" stroke="var(--green)" stroke-width="3"/><path d="M20 33l8 8 16-17" style="stroke:var(--green)"/></svg>`;
    haptic($('#applock'), 'success haptic', 201, 437);
    setTimeout(() => { const l = $('#applock'); const done = () => { l.hidden = true; const t = $('.states [data-state="locked"]'); t && t.classList.remove('on'); };
      if(reduce) return done(); l.animate([{opacity:1}, {opacity:0, filter:'blur(8px)'}], {duration:380}).onfinish = done; }, 450); }, reduce ? 200 : 900);
}

/* ---------- reminders-off banner on Today ---------- */
function drawBanner(){
  const old = $('#nbanner'); old && old.remove();
  if(!notifOff || !MEDS.length) return;
  $('#groups').insertAdjacentHTML('beforebegin', `<div class="banner" id="nbanner"><span class="tile" style="background:var(--orange)">${T_BELL}</span><div class="mid"><div class="nm">Reminders are off</div><div class="dt">You’ll still see doses here, but your iPhone won’t remind you.</div></div><button class="pbtn sm" data-turnon="1">Turn On</button></div>`);
}

/* ---------- Home Screen: widgets you can use without opening the app ---------- */
let scene = 'app', hpage = 0;
function wRing(size, clear){
  const n = MEDS.length, sw = 14, r = 50 - sw/2, C = 2*Math.PI*r, d = doneCount(), closed = n && d === n;
  if(!n) return `<svg width="${size}" height="${size}" viewBox="0 0 100 100"><circle cx="50" cy="50" r="${r}" fill="none" stroke="${clear ? 'rgba(255,255,255,.3)' : 'var(--fill)'}" stroke-width="${sw}"/></svg>`;
  const cap = (sw/2)/r*180/Math.PI, gap = closed ? 0 : 2*cap + 9, seg = 360/n - gap;
  return `<svg width="${size}" height="${size}" viewBox="0 0 100 100" style="flex:none;display:block">` + MEDS.map((m, i) => {
    const on = !!taken[m.id], col = clear ? (on ? '#fff' : 'rgba(255,255,255,.28)') : on ? `var(--${m.c})` : m.t < now ? 'color-mix(in srgb,var(--orange) 45%,var(--fill))' : 'var(--fill)';
    return `<circle data-wseg="${m.id}" cx="50" cy="50" r="${r}" fill="none" stroke="${col}" stroke-width="${sw}" stroke-linecap="${closed ? 'butt' : 'round'}" stroke-dasharray="${closed ? (360/n)/360*C + .6 : seg/360*C} ${C}" transform="rotate(${-90 + i*360/n + gap/2} 50 50)"/>`; }).join('')
    + (closed ? `<path d="M36 51l10 10 19-21" fill="none" stroke="${clear ? '#fff' : 'var(--green)'}" stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/>` : '') + '</svg>';
}
function nextDue(){ const nx = nextMed(); return nx ? (nx.t < now ? {txt:`${nx.n} was due ${nx.t}`, col:'var(--orange-text)', m:nx} : {txt:`Next: ${nx.t}`, col:'var(--accent)', m:nx}) : {txt:MEDS.length ? 'All done today' : 'No doses today', col:'var(--green)'}; }
function widgetHTML(kind){
  const clear = $('#homeScene').classList.contains('clearw'), n = MEDS.length, d = doneCount(), nd = nextDue();
  if(kind === 's') return `<div style="padding:14px;height:100%;box-sizing:border-box;display:flex;flex-direction:column;justify-content:space-between">
    <div style="display:flex;justify-content:space-between;align-items:flex-start">${wRing(66, clear)}<span class="wt">Today</span></div>
    <div><div class="w2">${d} of ${n}</div><div class="w3" style="color:${clear ? '#fff' : nd.col};font-weight:600">${nd.txt}</div></div></div>`;
  const left = MEDS.filter(m => !taken[m.id]);
  const btn = (m, first) => taken[m.id] ? `<span class="wbtn2 done">✓</span>` : first && m.t <= addHours(now, 1) ? `<button class="wbtn2" data-wlog="${m.id}">Taken</button>` : '';
  if(kind === 'm') return `<div style="padding:14px 16px;display:flex;gap:16px;height:100%;box-sizing:border-box">
    <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px">${wRing(96, clear)}<div class="w3" style="font-weight:600">${d} of ${n}</div></div>
    <div style="flex:1;display:flex;flex-direction:column;justify-content:center;gap:4px;min-width:0"><span class="wt">${left.length ? 'Next' : 'Today'}</span>
    ${left.length ? left.slice(0, 2).map((m, i) => `<div class="wrow">${icon(m, 28)}<div class="mid"><div class="nm">${m.n}</div><div class="dt" style="${m.t < now && !clear ? 'color:var(--orange-text)' : ''}">${m.t < now ? 'Due ' : ''}${m.t}</div></div>${btn(m, i === 0)}</div>`).join('')
      : `<div class="nm" style="font-size:15px">All done. Rest easy.</div>`}</div></div>`;
  const groups = ['Morning','Afternoon','Evening','Night'].filter(g => MEDS.some(m => m.g === g));
  return `<div style="padding:16px 16px 12px">
    <div style="display:flex;gap:14px;align-items:center">${wRing(70, clear)}<div style="min-width:0"><div class="w2">${d} of ${n} taken</div><div class="w3" style="color:${clear ? '#fff' : nd.col};font-weight:600">${nd.txt}</div></div></div>
    <div style="margin-top:8px">${groups.map(g => `<div class="wt" style="margin:8px 0 2px">${g}</div>${MEDS.filter(m => m.g === g).map(m => `<div class="wrow">${icon(m, 28)}<div class="mid"><div class="nm">${m.n}</div><div class="dt">${taken[m.id] ? 'Taken ' + taken[m.id] : m.t}</div></div>${btn(m, m === left[0])}</div>`).join('')}`).join('')}</div>
    ${PRN.length ? `<div class="wrow" style="margin-top:6px;border-top:.5px solid ${clear ? 'rgba(255,255,255,.3)' : 'var(--sep)'};padding-top:6px"><div class="mid"><div class="dt">As needed</div><div class="nm">${PRN.map(p => p.n).join(', ')}</div></div></div>` : ''}</div>`;
}
const FAKE = ['linear-gradient(150deg,#FFB86B,#FF7A59)', 'linear-gradient(150deg,#7DD3FC,#3B82F6)', 'linear-gradient(150deg,#A7F3D0,#10B981)', 'linear-gradient(150deg,#FDE68A,#F59E0B)',
  'linear-gradient(150deg,#C4B5FD,#7C3AED)', 'linear-gradient(150deg,#FBCFE8,#EC4899)', 'linear-gradient(150deg,#E5E7EB,#9CA3AF)', 'linear-gradient(150deg,#99F6E4,#14B8A6)'];
const fake = i => `<div class="ico"><div class="sq" style="background:${FAKE[i % FAKE.length]}"><span style="position:absolute;inset:18px;border-radius:${i % 2 ? 8 : 13}px;background:rgba(255,255,255,.55)"></span></div><span class="nm3"></span></div>`;
const havnIcon = () => `<div class="ico" data-openapp="1"><div class="sq" id="havnIco" style="background:var(--cell);display:grid;place-items:center">${miniRing([1,1,1,1], 46, ['indigo','pink','brown','purple'])}</div><span class="nm3">Hävn</span></div>`;
function drawHome(){
  const H = $('#homeScene');
  H.innerHTML = `<div class="wall"></div><div class="hpages">
    <div class="hpage" style="transform:translateX(${-hpage*100}%)"><div class="wg s" data-w="s">${widgetHTML('s')}</div>${fake(0)}${fake(1)}${fake(2)}${havnIcon()}<div class="wg m" data-w="m">${widgetHTML('m')}</div>${fake(3)}${fake(4)}${fake(5)}${fake(6)}</div>
    <div class="hpage" style="transform:translateX(${(1 - hpage)*100}%)"><div class="wg l" data-w="l">${widgetHTML('l')}</div>${fake(7)}${fake(1)}${fake(4)}${fake(2)}</div></div>
    <div class="hpdots"><i class="${hpage ? '' : 'on'}" data-hp="0"></i><i class="${hpage ? 'on' : ''}" data-hp="1"></i></div>
    <div class="dock glass">${fake(1).replace('<span class="nm3"></span>', '')}${fake(2).replace('<span class="nm3"></span>', '')}${fake(5).replace('<span class="nm3"></span>', '')}${fake(3).replace('<span class="nm3"></span>', '')}</div>`;
}
function refreshWidgets(justTaken){
  ['s','m','l'].forEach(k => { const w = $(`#homeScene [data-w="${k}"]`); if(w) w.innerHTML = widgetHTML(k); });
  if(justTaken && !reduce) $('#homeScene').querySelectorAll(`[data-wseg="${justTaken}"]`).forEach(c => { const da = c.getAttribute('stroke-dasharray').split(' ');
    c.animate([{strokeDasharray:`0 ${da[1]}`}, {strokeDasharray:`${da[0]} ${da[1]}`}], {duration:900, easing:'cubic-bezier(.22,1.12,.36,1)'}); });
}
$('#homeScene').addEventListener('click', e => {
  const b = e.target.closest('[data-wlog]');
  if(b){ e.stopPropagation(); const id = b.dataset.wlog; b.innerHTML = '<span class="spin" style="width:14px;height:14px;border-width:2px;border-color:rgba(255,255,255,.35);border-top-color:#fff"></span>';
    setTimeout(() => { quiet(() => logDose(id)); refreshWidgets(id); }, reduce ? 0 : 450); return; }
  const dot = e.target.closest('[data-hp]'); if(dot){ hpage = +dot.dataset.hp; $('#homeScene').querySelectorAll('.hpage').forEach((p, i) => p.style.transform = `translateX(${(i - hpage)*100}%)`);
    $('#homeScene').querySelectorAll('[data-hp]').forEach((d, i) => d.classList.toggle('on', i === hpage)); return; }
  const w = e.target.closest('[data-w],[data-openapp]'); if(w) openAppFrom(w);
});
/* the app closes into its icon and opens out of whatever you tapped */
function rectIn(el){ const p = $('#phone').getBoundingClientRect(), r = el.getBoundingClientRect(), s = +$('#phone').dataset.s || 1; return {x:(r.left - p.left)/s, y:(r.top - p.top)/s, w:r.width/s, h:r.height/s}; }
function zoomFrames(r){ return [{transform:`translate(${r.x}px,${r.y}px) scale(${r.w/402},${r.h/874})`, borderRadius:`${16*402/r.w}px`, opacity:.4}, {transform:'none', borderRadius:'56px', opacity:1}]; }
function openAppFrom(el){
  const r = rectIn(el), app = $('#app'); app.style.zIndex = 71;
  const done = () => { app.style.zIndex = ''; $('#homeScene').hidden = true; setSceneClass('app'); };
  if(reduce) return done();
  app.animate(zoomFrames(r), {duration:560, easing:'cubic-bezier(.2,.9,.25,1)'}).onfinish = done;
  $('#homeScene').animate([{transform:'scale(1)', filter:'blur(0)'}, {transform:'scale(1.08)', filter:'blur(6px)'}], {duration:560, easing:'cubic-bezier(.2,.9,.25,1)'});
  scene = 'app'; markScene();
}
function closeAppToHome(){
  drawHome(); const H = $('#homeScene'); H.hidden = false; const app = $('#app'); app.style.zIndex = 71; setSceneClass('home');
  const ico = $('#havnIco'); if(hpage !== 0 || !ico){ app.style.zIndex = ''; return; }
  const r = rectIn(ico);
  if(reduce){ app.style.zIndex = ''; return; }
  app.animate(zoomFrames(r).reverse(), {duration:520, easing:'cubic-bezier(.3,.9,.3,1)'}).onfinish = () => app.style.zIndex = '';
  H.querySelectorAll('.ico,.wg').forEach((el, i) => el.animate([{transform:'scale(1.12)', opacity:.0}, {transform:'scale(1)', opacity:1}], {duration:520, delay:60 + (i % 6)*18, easing:'cubic-bezier(.2,.9,.25,1.05)', fill:'backwards'}));
}

/* ---------- Lock Screen ---------- */
function drawLock(){
  const L = $('#lockScene'), nd = nextDue(), due = MEDS.filter(m => !taken[m.id] && m.t <= now), n = MEDS.length, d = doneCount();
  const lr = `<svg width="54" height="54" viewBox="0 0 100 100">${MEDS.map((m, i) => { const r = 42, C = 2*Math.PI*r, cap = 7/r*180/Math.PI, gap = 2*cap + 9, len = (360/Math.max(1, n) - gap)/360*C;
      return `<circle cx="50" cy="50" r="${r}" fill="none" stroke="${taken[m.id] ? 'rgba(255,255,255,.95)' : 'rgba(255,255,255,.3)'}" stroke-width="14" stroke-linecap="round" stroke-dasharray="${len} ${C}" transform="rotate(${-90 + i*360/Math.max(1, n) + gap/2} 50 50)"/>`; }).join('')}
      <text x="50" y="52" text-anchor="middle" dominant-baseline="central" font-family="var(--rounded)" font-weight="600" font-size="26" fill="#fff">${d}/${n}</text></svg>`;
  const names = due.map(m => m.n).join(', ');
  L.innerHTML = `<div class="wall" style="filter:brightness(.5) saturate(1.3)"></div><div class="lk-date">Wednesday 18 November</div><div class="lk-time">${now}</div>
    <div class="lk-wg"><div class="lk-c">${lr}</div><div class="lk-r"><div style="display:flex;gap:6px;align-items:center;font-weight:600;opacity:.85">${PRNICON}Hävn</div>
      <div style="font-size:15px;font-weight:600;margin-top:3px">${due.length ? 'Dose due now' : nd.m ? `Next dose ${nd.m.t}` : 'All done today'}</div><div style="opacity:.8">${due.length ? (showNames ? names : `${due.length} medication${due.length > 1 ? 's' : ''}`) : `${d} of ${n} taken`}</div></div></div>
    ${due.length ? `<div class="notif2" id="lkNotif"><div class="appi">${miniRing([1,1,1,1], 26, ['indigo','pink','brown','purple'])}</div><div style="flex:1;min-width:0">
      <div style="display:flex;justify-content:space-between;font-size:15px"><b style="font-weight:600">Hävn</b><span style="color:rgba(60,60,67,.6);font-size:13px">now</span></div>
      <div style="font-size:15px;font-weight:600;margin-top:1px">Time for your ${due[0].t} dose</div>
      <div style="font-size:15px">${showNames ? `${names} ${due.length > 1 ? 'are' : 'is'} due.` : `${due.length} medication${due.length > 1 ? 's are' : ' is'} due.`} Hold to log it.</div></div></div>` : ''}`;
  const nt = $('#lkNotif'); if(!nt) return;
  let held = null;
  nt.addEventListener('pointerdown', () => { held = setTimeout(() => { held = 'open'; expandNotif(due); }, 420); });
  nt.addEventListener('pointerup', () => { if(held === 'open') return; clearTimeout(held); held = null; unlockTo(); });
}
function expandNotif(due){
  const nt = $('#lkNotif'), L = $('#lockScene'); haptic(nt, 'press haptic');
  const dim = document.createElement('div'); dim.style.cssText = 'position:absolute;inset:0;background:rgba(0,0,0,.35);-webkit-backdrop-filter:blur(14px);backdrop-filter:blur(14px)'; L.insertBefore(dim, nt);
  const box = nt.getBoundingClientRect(), s = +$('#phone').dataset.s || 1;
  nt.style.bottom = '330px'; if(!reduce) nt.animate([{transform:'translateY(210px) scale(1)'}, {transform:'scale(1.02)'}], {duration:420, easing:'cubic-bezier(.2,.9,.25,1.1)', fill:'forwards'});
  const act = document.createElement('div'); act.className = 'nact2'; act.style.bottom = '150px';
  act.innerHTML = `<button data-n="taken">Taken<svg width="20" height="20" viewBox="0 0 22 22"><circle cx="11" cy="11" r="11" fill="var(--green)"/><path d="M6.5 11.3l3 3 6-6.6" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg></button><button data-n="later">Remind Me in 10 Minutes<span>⏱</span></button><button data-n="skip">Skipped<span>⊖</span></button>`;
  L.appendChild(act); if(!reduce) act.animate([{opacity:0, transform:'scale(.9)'}, {opacity:1, transform:'none'}], {duration:360, delay:80, easing:'cubic-bezier(.2,.9,.25,1.1)', fill:'backwards'});
  const close = (logged) => { act.remove(); dim.remove(); nt.getAnimations().forEach(a => a.cancel()); nt.style.bottom = '';
    if(logged){ if(!reduce) nt.animate([{opacity:1, transform:'none'}, {opacity:0, transform:'translateX(-110%)'}], {duration:360, easing:'cubic-bezier(.4,0,.2,1)', fill:'forwards'}).onfinish = drawLock; else drawLock(); } };
  dim.onclick = () => close(false);
  act.onclick = e => { const b = e.target.closest('[data-n]'); if(!b) return;
    if(b.dataset.n === 'taken'){ quiet(() => due.forEach(m => logDose(m.id))); haptic(b, 'success haptic'); }
    if(b.dataset.n === 'later') toast('Reminding you at ' + addHours(now, 0).slice(0, 3) + String(+now.slice(3) + 10).padStart(2, '0'));
    close(b.dataset.n === 'taken'); };
}
function unlockTo(){ const L = $('#lockScene'); if(reduce){ L.hidden = true; scene = 'app'; markScene(); setSceneClass('app'); return; }
  L.animate([{transform:'none'}, {transform:'translateY(-100%)'}], {duration:480, easing:'cubic-bezier(.3,.9,.3,1)'}).onfinish = () => { L.hidden = true; };
  $('#app').animate([{transform:'scale(.94)', filter:'blur(6px)'}, {transform:'none', filter:'blur(0)'}], {duration:520, easing:'cubic-bezier(.2,.9,.25,1)'});
  scene = 'app'; markScene(); setSceneClass('app'); }

/* ---------- switching scenes from the buttons above the phone ---------- */
function setSceneClass(s){ const p = $('#phone'); p.classList.toggle('scene-home', s === 'home'); p.classList.toggle('scene-lock', s === 'lock'); }
function markScene(){ document.querySelectorAll('.scenes [data-scene]').forEach(b => b.classList.toggle('on', b.dataset.scene === (scene === 'app' && !$('#ob').hidden ? 'welcome' : scene)));
  $('.wstyle').classList.toggle('show', scene === 'home'); }
function setScene(s, instant){
  const was = scene; scene = s;
  if(s === 'welcome'){ $('#homeScene').hidden = true; $('#lockScene').hidden = true; setSceneClass('app'); startOnboarding(); scene = 'app'; markScene(); return; }
  if(s === 'app'){ if(!$('#ob').hidden){ hideOnboarding(); refreshToday(true); }
    if(was === 'home' && !instant) openAppFrom($('#havnIco') || $('#homeScene')); else if(was === 'lock' && !instant) unlockTo(); else { $('#homeScene').hidden = true; $('#lockScene').hidden = true; setSceneClass('app'); } }
  if(s === 'home'){ $('#lockScene').hidden = true; hpage = 0; if(!$('#ob').hidden) hideOnboarding(); closeAllSheets(); if(instant){ drawHome(); $('#homeScene').hidden = false; setSceneClass('home'); } else closeAppToHome(); }
  if(s === 'lock'){ drawLock(); const L = $('#lockScene'); L.hidden = false; setSceneClass('lock'); if(!instant && !reduce) L.animate([{opacity:0, transform:'scale(1.06)'}, {opacity:1, transform:'none'}], {duration:420, easing:'cubic-bezier(.2,.9,.25,1)'}); }
  markScene();
}
document.querySelector('.scenes').addEventListener('click', e => { const b = e.target.closest('[data-scene]'); if(b) setScene(b.dataset.scene); });
document.querySelector('.wstyle').addEventListener('click', e => { const b = e.target.closest('[data-ws]'); if(!b) return;
  $('#homeScene').classList.toggle('clearw', b.dataset.ws === 'clear'); document.querySelectorAll('.wstyle [data-ws]').forEach(x => x.classList.toggle('on', x === b)); refreshWidgets(); });

/* ---------- states to check, from the panel under the phone ---------- */
let stash = null;
function syncStates(){ const S = {notifoff:notifOff, locked:!$('#applock').hidden, empty:!!stash};
  document.querySelectorAll('.states [data-state]').forEach(b => { const k = b.dataset.state; if(k in S) b.classList.toggle('on', S[k]); }); }
document.querySelector('.states').addEventListener('click', e => {
  const b = e.target.closest('[data-state]'); if(!b) return; const k = b.dataset.state, on = !b.classList.contains('on'); b.classList.toggle('on', on);
  const ph = $('#phone');
  if(['ax','rt','hc'].includes(k)) ph.classList.toggle(k, on);
  if(k === 'rm'){ ph.classList.toggle('rm', on); reduce = on || matchMedia('(prefers-reduced-motion: reduce)').matches; }
  if(k === 'notifoff'){ notifOff = on; drawBanner(); }
  if(k === 'locked'){ if(on) showAppLock(); else $('#applock').hidden = true; }
  if(k === 'empty'){ saveCur();
    if(on){ stash = {meds:MEDS, prn:PRN, extra:EXTRA, taken, order, hist:PEOPLE[cur].hist}; MEDS = []; PRN = []; EXTRA = []; taken = {}; order = []; PEOPLE[cur].hist = {}; }
    else if(stash){ ({meds:MEDS, prn:PRN, extra:EXTRA, taken, order} = stash); PEOPLE[cur].hist = stash.hist; stash = null; }
    saveCur(); if($('#ob').hidden === false) hideOnboarding(); setScene('app', true); showTab(0, true); refreshToday(true); }
  if(scene === 'home') refreshWidgets(); if(scene === 'lock') drawLock();
});
