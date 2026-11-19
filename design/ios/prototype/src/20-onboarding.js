
/* ================= onboarding: five short screens, one idea each ================= */
let obStep = 0, obReminder = 'notif', obStart = 'import', notifOff = false, obBusy = false;
const OB_COLS = ['indigo', 'pink', 'brown', 'purple'];
function obRingSVG(){
  const r = 40, sw = 12, C = 2*Math.PI*r, cap = (sw/2)/r*180/Math.PI, gap = 2*cap + 10, len = (90 - gap)/360*C;
  return `<svg class="ob-ring" id="obRing" viewBox="0 0 100 100" aria-hidden="true">
    <circle class="trk" cx="50" cy="50" r="${r}" fill="none" stroke="var(--fill)" stroke-width="${sw}" opacity="0"/>
    ${OB_COLS.map((c, i) => `<circle class="seg" data-i="${i}" cx="50" cy="50" r="${r}" fill="none" stroke="var(--${c})" stroke-width="${sw}" stroke-linecap="round"
      stroke-dasharray="${len} ${C}" transform="rotate(${-90 + i*90 + gap/2} 50 50)"/>`).join('')}
    <circle class="prog" cx="50" cy="50" r="${r}" fill="none" stroke="var(--accent)" stroke-width="${sw}" stroke-linecap="round" stroke-dasharray="0 ${C}" transform="rotate(-90 50 50)" opacity="0"/>
    <text x="50" y="47" text-anchor="middle" font-family="var(--rounded)" font-weight="600" font-size="17" fill="var(--label)" class="ctr" opacity="0">2 of 4</text>
    <text x="50" y="61" text-anchor="middle" font-family="var(--sys)" font-size="8.5" fill="var(--l2)" class="ctr2" opacity="0">taken today</text></svg>`;
}
const LOCK = '<svg width="40" height="44" viewBox="0 0 40 44"><rect x="5" y="19" width="30" height="22" rx="7" fill="var(--accent)"/><path class="draw" d="M11.5 19v-6.5a8.5 8.5 0 0 1 17 0V19" fill="none" stroke="var(--accent)" stroke-width="4.5" stroke-linecap="round" stroke-dasharray="44" stroke-dashoffset="44"/><circle cx="20" cy="29" r="3" fill="var(--bg)"/></svg>';
const BELL = '<svg width="40" height="42" viewBox="0 0 40 42" class="bell"><path d="M20 4c-7 0-11.5 5.2-11.5 12v8.2L5 30.5h30l-3.5-6.3V16C31.5 9.2 27 4 20 4z" fill="var(--red)"/><path d="M15 34a5 5 0 0 0 10 0" fill="var(--red)"/></svg>';
const HEART = '<svg width="30" height="27" viewBox="0 0 30 27"><path d="M15 26S1.5 17.6 1.5 8.4A6.9 6.9 0 0 1 15 5.2a6.9 6.9 0 0 1 13.5 3.2C28.5 17.6 15 26 15 26z" fill="#FF2D55"/></svg>';
const FEAT = (tile, col, nm, dt) => `<div class="feat rv2"><span class="tile" style="background:var(--${col})">${tile}</span><div class="mid"><div class="nm">${nm}</div><div class="dt">${dt}</div></div></div>`;
const OPT = (key, on, tile, col, nm, dt) => `<div class="opt rv2 ${on ? 'on' : ''}" data-opt="${key}"><span class="tile" style="background:var(--${col})">${tile}</span><div class="mid"><div class="nm">${nm}</div><div class="dt">${dt}</div></div><span class="radio"></span></div>`;
const T_PERSON = '<svg width="16" height="16" viewBox="0 0 16 16"><circle cx="8" cy="5" r="3.2" fill="#fff"/><path d="M2 15c.6-3.4 3-5 6-5s5.4 1.6 6 5z" fill="#fff"/></svg>';
const T_EYE = '<svg width="18" height="14" viewBox="0 0 18 14"><path d="M1 7s3-5.5 8-5.5S17 7 17 7s-3 5.5-8 5.5S1 7 1 7z" fill="none" stroke="#fff" stroke-width="1.6"/><circle cx="9" cy="7" r="2.4" fill="#fff"/><path d="M2.5 1l13 12" stroke="#fff" stroke-width="1.6" stroke-linecap="round"/></svg>';
const T_FOLDER = '<svg width="18" height="14" viewBox="0 0 18 14"><path d="M1 3a2 2 0 0 1 2-2h4l2 2h6a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2z" fill="#fff"/></svg>';
const T_BELL = '<svg width="15" height="16" viewBox="0 0 40 42"><path d="M20 4c-7 0-11.5 5.2-11.5 12v8.2L5 30.5h30l-3.5-6.3V16C31.5 9.2 27 4 20 4zM15 34a5 5 0 0 0 10 0" fill="#fff"/></svg>';
const T_ALARM = '<svg width="16" height="16" viewBox="0 0 16 16"><circle cx="8" cy="9" r="5.6" fill="none" stroke="#fff" stroke-width="1.8"/><path d="M8 6v3.2l2 1.4M2 3l2.4-1.8M14 3l-2.4-1.8" stroke="#fff" stroke-width="1.6" stroke-linecap="round"/></svg>';
const T_PLUS = '<svg width="14" height="14" viewBox="0 0 14 14"><path d="M7 1v12M1 7h12" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/></svg>';
const T_HEART = '<svg width="16" height="14" viewBox="0 0 30 27"><path d="M15 26S1.5 17.6 1.5 8.4A6.9 6.9 0 0 1 15 5.2a6.9 6.9 0 0 1 13.5 3.2C28.5 17.6 15 26 15 26z" fill="#fff"/></svg>';
const T_RESTORE = '<svg width="16" height="16" viewBox="0 0 16 16"><path d="M3 8a5 5 0 1 0 1.5-3.6" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round"/><path d="M2.2 1.8v3.4h3.4" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>';

const OB = [
  {h:'Hävn', p:'A calm place for your medication.', next:'Get Started', small:'No account. No ads. Stays on your iPhone.'},
  {h:'Your day, as a ring.', p:'Each dose is one piece. Take it and it fills in with your medication’s colour. By tonight, the ring closes.', next:'Continue'},
  {h:'Stays on your iPhone.', p:'Your medication list is nobody’s business but yours.', next:'Continue',
   list:() => FEAT(T_PERSON, 'gray', 'No account', 'Open the app and start. Nothing to sign up for.') + FEAT(T_EYE, 'blue', 'No ads, no tracking', 'Hävn has no servers and never sends your data anywhere.') + FEAT(T_FOLDER, 'indigo', 'Backups you control', 'Export a file whenever you like and restore it on a new iPhone.')},
  {h:'How should we remind you?', p:'You can change this for each medication later.', next:() => notifOff ? 'Continue' : obReminder==='alarm' ? 'Allow Alarms' : 'Allow Notifications', alt:() => notifOff ? '' : 'Not Now',
   list:() => notifOff ? `<div class="banner rv2" style="margin-top:0"><span class="tile" style="background:var(--orange)">${T_BELL}</span><div class="mid"><div class="nm">Reminders are off</div><div class="dt">You can still log every dose. Turn reminders on in Settings any time.</div></div></div>`
     : OPT('notif', obReminder==='notif', T_BELL, 'red', 'Notification', 'A gentle reminder at each dose time.') + OPT('alarm', obReminder==='alarm', T_ALARM, 'orange', 'Alarm', 'Rings even on Silent. For doses that can’t wait.')},
  {h:'Add your first medication.', p:'It becomes the first piece of your ring.', next:'Continue',
   list:() => OPT('import', obStart==='import', T_HEART, 'pink', 'Import from Apple Health', 'Bring in the list you already keep in Health.') + OPT('manual', obStart==='manual', T_PLUS, 'accent', 'Add Manually', 'Takes about a minute.') + OPT('restore', obStart==='restore', T_RESTORE, 'indigo', 'Restore from a Backup', 'Moving from another iPhone? Open your Hävn file.')},
];

function obHero(step){
  const hero = $('#obHero'), ring = $('#obRing'), segs = ring ? [...ring.querySelectorAll('.seg')] : [];
  const tiles = hero.querySelectorAll('.ob-tile');
  const showRing = step <= 1 || step === 4 || step >= 5;
  tiles.forEach(t => { if(reduce){ t.remove(); return; }
    t.animate([{opacity:1, transform:'scale(1)'}, {opacity:0, transform:'scale(.7)', filter:'blur(6px)'}], {duration:260, fill:'forwards'}).onfinish = () => t.remove(); });
  if(showRing){
    ring.getAnimations().forEach(a => a.cancel()); ring.style.opacity = 1;
    if(ring.dataset.hidden){ delete ring.dataset.hidden; if(!reduce) ring.animate([{opacity:0, transform:'scale(.7)'}, {opacity:1, transform:'scale(1)'}], {duration:520, easing:'cubic-bezier(.2,.9,.25,1.1)'}); }
  } else if(!ring.dataset.hidden){
    ring.dataset.hidden = 1;
    if(!reduce) ring.animate([{opacity:1, transform:'scale(1)'}, {opacity:0, transform:'scale(.6)', filter:'blur(6px)'}], {duration:320, fill:'forwards'}).onfinish = () => ring.style.opacity = 0;
    else ring.style.opacity = 0;
  }
  const trk = ring.querySelector('.trk'), ctr = ring.querySelectorAll('.ctr,.ctr2'), prog = ring.querySelector('.prog');
  if(step === 1){        // the same ring, now a day: two pieces taken, two to come
    segs.forEach((s, i) => s.style.stroke = i < 2 ? `var(--${OB_COLS[i]})` : 'var(--fill)');
    ctr.forEach(c => c.setAttribute('opacity', 1)); trk.setAttribute('opacity', 0);
  } else if(step === 4){ // empty: a dashed ring waiting for its first piece
    segs.forEach(s => s.style.opacity = 0); ctr.forEach(c => c.setAttribute('opacity', 0));
    trk.setAttribute('opacity', 1); trk.setAttribute('stroke-dasharray', '3 5.4');
  } else if(step === 0){
    segs.forEach((s, i) => { s.style.opacity = 1; s.style.stroke = `var(--${OB_COLS[i]})`; }); ctr.forEach(c => c.setAttribute('opacity', 0)); trk.setAttribute('opacity', 0);
  }
  prog.setAttribute('opacity', step === 6 ? 1 : 0);
  const tile = (bg, svg, cls='') => { const t = document.createElement('div'); t.className = 'ob-tile ' + cls; t.style.background = bg; t.innerHTML = svg; hero.appendChild(t);
    if(!reduce) t.animate([{opacity:0, transform:'scale(.6)', filter:'blur(8px)'}, {opacity:1, transform:'scale(1)', filter:'blur(0)'}], {duration:560, delay:120, easing:'cubic-bezier(.2,.9,.25,1.15)', fill:'backwards'});
    return t; };
  if(step === 2){ const t = tile('color-mix(in srgb,var(--accent) 16%,var(--cell))', LOCK);
    const d = t.querySelector('.draw'); setTimeout(() => { d.style.transition = 'stroke-dashoffset .7s ease-out'; d.style.strokeDashoffset = 0; }, reduce ? 0 : 380); }
  if(step === 3){ const t = tile('color-mix(in srgb,var(--red) 16%,var(--cell))', BELL);
    if(!reduce) t.querySelector('.bell').animate([0, 14, -11, 7, -3, 0].map(d => ({transform:`rotate(${d}deg)`, transformOrigin:'50% 10%'})),
      {duration:900, delay:520, easing:'ease-out'}); }
  if(step === 5) tile('color-mix(in srgb,#FF2D55 14%,var(--cell))', HEART);
}
function obAssemble(){        // Welcome: the four pieces fly in and settle into a ring
  const ring = $('#obRing'); if(reduce) return;
  ring.querySelectorAll('.seg').forEach((s, i) => {
    const a = [-1,-1, 1,-1, 1,1, -1,1].slice(i*2, i*2+2);
    const B = -90 + i*90 + 13.6;   // the angle its transform attribute already holds
    s.animate([{transform:`translate(${a[0]*34}px,${a[1]*34}px) rotate(${B - 40}deg)`, transformOrigin:'50px 50px', opacity:0},
               {transform:`rotate(${B}deg)`, transformOrigin:'50px 50px', opacity:1}],
      {duration:900, delay:150 + i*110, easing:'cubic-bezier(.2,.95,.25,1.08)', fill:'backwards'});
  });
  ring.animate([{transform:'rotate(-30deg) scale(.86)'}, {transform:'rotate(0) scale(1)'}], {duration:1500, delay:100, easing:'cubic-bezier(.2,.9,.2,1)'});
}
function obRender(step, first){
  obStep = step; const S = OB[Math.min(step, 4)];
  const text = $('#obText'), list = $('#obList');
  const fill = () => {
    if(step <= 4){
      text.innerHTML = `<h2 class="rv2">${S.h}</h2><p class="rv2" style="animation-delay:.08s">${S.p}</p>`;
      list.innerHTML = S.list ? S.list() : step === 1 ? `<div class="rv2" style="display:flex;justify-content:center;gap:16px;font-size:13px;color:var(--l2)"><span><i style="display:inline-block;width:8px;height:8px;border-radius:4px;background:var(--indigo);margin-right:6px"></i>Taken</span><span><i style="display:inline-block;width:8px;height:8px;border-radius:4px;background:var(--accent);margin-right:6px"></i>Coming up</span><span><i style="display:inline-block;width:8px;height:8px;border-radius:4px;background:var(--fill);margin-right:6px"></i>Later</span></div>` : '';
    }
    [...text.children, ...list.children].forEach((el, i) => el.style.animationDelay = (first ? 700 : 60) + i*90 + 'ms');
    if(step === 0) $('#obHero').style.top = '170px'; else $('#obHero').style.top = '';
    text.style.top = step === 0 ? '370px' : '';
    list.style.top = (text.offsetTop + text.offsetHeight + 22) + 'px';
    $('#obNext').textContent = typeof S.next === 'function' ? S.next() : S.next;
    const alt = S.alt ? (typeof S.alt === 'function' ? S.alt() : S.alt) : '';
    $('#obAlt').textContent = alt; $('#obAlt').style.display = alt ? '' : 'none';
    $('#obSmall').textContent = S.small || ''; $('#obSmall').style.display = S.small ? '' : 'none';
    $('#obDots').querySelectorAll('i').forEach((d, i) => d.classList.toggle('on', i === Math.min(step, 4)));
    $('#obDots').style.visibility = step >= 5 ? 'hidden' : '';
  };
  obHero(step);
  const old = [...text.children, ...list.children];
  if(first || reduce || !old.length){ fill(); return; }
  old.forEach(el => el.classList.add('obout'));
  setTimeout(fill, 220);
}
function startOnboarding(){
  closeAllSheets();
  const ob = $('#ob'); ob.hidden = false; obBusy = false; notifOff = false; obReminder = 'notif'; obStart = 'import';
  $('#obHero').innerHTML = obRingSVG(); $('#obText').innerHTML = ''; $('#obList').innerHTML = '';
  ob.style.background = ''; ob.querySelectorAll('.ob-text,.ob-list,.ob-bottom').forEach(e => e.style.opacity = '');
  obRender(0, true); obAssemble();
  if(!reduce) [...ob.querySelectorAll('.ob-bottom > *')].forEach((el, i) => el.animate([{opacity:0, transform:'translateY(16px)', filter:'blur(6px)'}, {opacity:1, transform:'none', filter:'blur(0)'}], {duration:700, delay:1000 + i*80, fill:'backwards', easing:'cubic-bezier(.2,.8,.2,1)'}));
}
function hideOnboarding(){ const ob = $('#ob'); ob.hidden = true; obBusy = false; ob.querySelectorAll('.msheet').forEach(s => s.classList.remove('open'));
  $('#obDim').hidden = true; ob.getAnimations({subtree:true}).forEach(a => a.cancel()); }

/* small system sheets inside onboarding */
function openMSheet(html){
  const sh = $('#obSheet'); sh.innerHTML = `<div class="grab"></div>${html}`; sh.classList.add('open');
  $('#obDim').hidden = false;
  if(!reduce){ sh.animate([{transform:'translateY(110%)'}, {transform:'none'}], {duration:520, easing:'cubic-bezier(.2,.95,.25,1.02)'}); $('#obDim').animate([{opacity:0}, {opacity:1}], {duration:300}); }
  return sh;
}
function closeMSheet(after){
  const sh = $('#obSheet'); $('#obDim').hidden = true;
  const done = () => { sh.classList.remove('open'); after && after(); };
  if(reduce) return done();
  sh.animate([{transform:'none'}, {transform:'translateY(110%)'}], {duration:380, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = done;
}
const APPICON = (s=56) => `<div class="hk" style="width:${s}px;height:${s}px;background:var(--cell);box-shadow:0 2px 8px rgba(0,0,0,.12)">${miniRing([1,1,1,1], Math.round(s*.72), ['indigo','pink','brown','purple'])}</div>`;
function healthSheet(){
  const sh = openMSheet(`<div class="pair">${APPICON()}<svg width="18" height="10" viewBox="0 0 18 10"><path d="M1 5h16M13 1l4 4-4 4" fill="none" stroke="var(--l3)" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg><div class="hk" style="background:#fff;box-shadow:0 2px 8px rgba(0,0,0,.12)">${HEART}</div></div>
    <h3>Health Access</h3><p class="sub">Hävn would like to read your medication list. It never writes to Health.</p>
    <div class="card"><div class="srow"><span class="lbl">Medications</span><button class="sw on" aria-label="Medications" aria-pressed="true"><span class="k"></span></button></div></div>
    <p class="foot" style="padding:8px 8px 14px">You can change this any time in the Health app.</p>
    <button class="btn2 p" data-h="allow">Allow</button><button class="ob-link" data-h="deny">Don’t Allow</button>`);
  sh.querySelectorAll('.sw').forEach(wireSwitch);
  sh.onclick = e => { const b = e.target.closest('[data-h]'); if(!b) return;
    if(b.dataset.h === 'allow') closeMSheet(() => obFound()); else closeMSheet(); };
}
function filesSheet(){
  const sh = openMSheet(`<h3>Choose a Backup</h3><p class="sub">On My iPhone › Hävn</p>
    <div class="card">${[['Hävn Backup 12 Nov 2026', '21 KB · 4 medications · 214 doses'], ['Hävn Backup 1 Oct 2026', '18 KB · 4 medications · 158 doses']].map(([n, d], i) => `
      <div class="file" data-file="${i}"><div class="fic">${miniRing([1,1,1,1], 22, ['indigo','pink','brown','purple'])}</div><div class="mid"><div class="nm" style="font-size:16px">${n}.havn</div><div class="dt" style="font-size:13px">${d}</div></div></div>`).join('')}</div>
    <button class="ob-link" data-cancel="1" style="margin-top:14px">Cancel</button>`);
  sh.onclick = e => { if(e.target.closest('[data-cancel]')) return closeMSheet();
    if(e.target.closest('[data-file]')) closeMSheet(() => obRestore()); };
}
/* the one real wait in the app: reading Health. A small spinner, never a skeleton. */
function obFound(){
  obRender(5);
  const found = YOU_MEDS.map(m => ({...m, on:true}));
  setTimeout(() => {
    $('#obText').innerHTML = `<h2>Looking in Health…</h2><p style="display:flex;justify-content:center;margin-top:14px"><span class="spin"></span></p>`;
    $('#obList').innerHTML = ''; $('#obNext').disabled = true;
  }, 240);
  setTimeout(() => {
    const draw = () => { const n = found.filter(f => f.on).length;
      $('#obNext').textContent = n ? `Add ${n} Medication${n > 1 ? 's' : ''}` : 'Add Medications'; $('#obNext').disabled = !n; };
    $('#obText').innerHTML = `<h2 class="rv2">Found 4 in Health</h2><p class="rv2" style="animation-delay:.08s">Choose the ones to add. You can set reminder times next.</p>`;
    $('#obList').style.top = ($('#obText').offsetTop + $('#obText').offsetHeight + 22) + 'px';
    $('#obList').innerHTML = `<div class="card found">${found.map((m, i) => `<div class="lrow chk on rv2" data-found="${i}" style="animation-delay:${160 + i*80}ms">${icon(m)}<div class="mid"><div class="nm">${m.n}</div><div class="dt">${m.d} · ${m.form}</div></div><span class="radio"></span></div>`).join('')}</div>`;
    $('#obList').onclick = e => { const r = e.target.closest('[data-found]'); if(!r) return; const f = found[+r.dataset.found]; f.on = !f.on; r.classList.toggle('on', f.on); draw(); };
    draw(); obFound.list = found;
  }, reduce ? 300 : 1300);
}
function obRestore(){
  obStep = 6; const ring = $('#obRing'), prog = ring.querySelector('.prog'), C = 2*Math.PI*40;
  obHero(6); ring.querySelectorAll('.seg').forEach(s => s.style.opacity = 0); ring.querySelector('.trk').setAttribute('stroke-dasharray', 'none');
  prog.setAttribute('stroke-dasharray', `0 ${C}`);
  $('#obText').innerHTML = `<h2 class="rv2">Restoring your backup</h2><p class="rv2">4 medications and 214 doses</p>`; $('#obList').innerHTML = '';
  $('#obNext').disabled = true; $('#obDots').style.visibility = 'hidden'; $('#obAlt').style.display = 'none';
  requestAnimationFrame(() => { prog.style.transition = `stroke-dasharray ${reduce ? .1 : 1.6}s cubic-bezier(.4,0,.2,1)`; prog.setAttribute('stroke-dasharray', `${C} ${C}`); });
  setTimeout(() => {
    swapText($('#obText'), `<h2 class="rv2">All back.</h2><p class="rv2">Everything from 12 November is on this iPhone.</p>`);
    $('#obNext').disabled = false; $('#obNext').textContent = 'Continue'; Snd.play('close'); haptic($('#obRing'), 'success haptic');
  }, reduce ? 200 : 1800);
}
function swapText(el, html){ if(reduce){ el.innerHTML = html; return; } [...el.children].forEach(c => c.classList.add('obout')); setTimeout(() => el.innerHTML = html, 220); }

function obNext(){
  if(obBusy) return;
  if(obStep < 3) return obRender(obStep + 1);
  if(obStep === 3){
    if(notifOff) return obRender(4);
    const alarm = obReminder === 'alarm';
    showAlert({title: alarm ? '“Hävn” Would Like to Schedule Alarms' : '“Hävn” Would Like to Send You Notifications',
      body: alarm ? 'Lets Hävn ring an alarm for the medications you choose, even when your iPhone is on Silent.' : 'Notifications may include alerts, sounds and icon badges. These can be configured in Settings.',
      buttons:[{label:'Don’t Allow', onTap:() => { notifOff = true; obRender(3); }}, {label:'Allow', style:'def', onTap:() => { notifOff = false; obRender(4); }}]});
    return;
  }
  if(obStep === 4){ if(obStart === 'import') healthSheet(); else if(obStart === 'restore') filesSheet(); else obFinish('empty'); return; }
  if(obStep === 5) return obFinish('import');
  if(obStep === 6) return obFinish('restore');
}
/* leaving onboarding: Today builds underneath, and the hero ring flies into Today's ring */
function obFinish(mode){
  obBusy = true;
  resetPeople(); now = '13:42'; $('#clock').innerHTML = '<span>13:42</span>';
  if(mode === 'empty'){ MEDS = []; PRN = []; PEOPLE.you.hist = {}; saveCur(); }
  if(mode === 'import'){ MEDS = (obFound.list || YOU_MEDS).filter(f => f.on !== false).map(m => { const c = {...m}; delete c.on; return c; }); taken = {}; order = []; PEOPLE.you.hist = {}; saveCur(); }
  $('#openSettings').innerHTML = avatarHTML(PEOPLE[cur]);
  showTab(0, true); $('#scroll').scrollTop = 0;
  refreshToday(false);
  const ob = $('#ob'), heroRing = $('#obRing'), target = $('#ring');
  const pr = $('#phone').getBoundingClientRect(), s = +$('#phone').dataset.s || 1;
  const a = heroRing.getBoundingClientRect(), b = target.getBoundingClientRect();
  const dx = (b.left + b.width/2 - a.left - a.width/2)/s, dy = (b.top + b.height/2 - a.top - a.height/2)/s, sc = b.width/a.width;
  if(reduce){ hideOnboarding(); entrance(); afterOnboarding(mode); return; }
  target.style.opacity = 0;
  ob.querySelectorAll('.ob-text,.ob-list,.ob-bottom,.ob-tile').forEach(el => el.animate([{opacity:1}, {opacity:0, filter:'blur(6px)'}], {duration:300, fill:'forwards'}));   // only the ring travels
  ob.animate([{background:getComputedStyle(ob).backgroundColor}, {background:'transparent'}], {duration:500, delay:250, fill:'forwards'});
  heroRing.animate([{transform:'none'}, {transform:`translate(${dx}px, ${dy}px) scale(${sc})`}], {duration:820, delay:120, easing:'cubic-bezier(.25,.9,.25,1.02)', fill:'forwards'})
    .onfinish = () => {
      target.style.opacity = ''; target.animate([{opacity:0}, {opacity:1}], {duration:180});
      heroRing.animate([{opacity:1}, {opacity:0}], {duration:180, fill:'forwards'}).onfinish = () => { hideOnboarding(); ob.getAnimations().forEach(x => x.cancel()); afterOnboarding(mode); };
    };
  setTimeout(() => { [...$('#scroll').querySelectorAll('.ltitle,.lsub,.sec'), $('.acc')].forEach((el, i) => {
    el.animate([{opacity:0, transform:'translateY(22px)', filter:'blur(8px)'}, {opacity:1, transform:'none', filter:'blur(0)'}], {duration:850, delay:i*100, easing:'cubic-bezier(.2,.8,.2,1)', fill:'backwards'}); });
    $('#sum').animate([{opacity:0}, {opacity:1}], {duration:500, fill:'backwards'}); }, 260);
}
function afterOnboarding(mode){
  setScene('app', true);
  if(mode === 'empty') setTimeout(() => openAdd(), 350);
}
$('#obNext').onclick = obNext;
$('#obAlt').onclick = () => { if(obStep === 3){ notifOff = true; obRender(3); } };
$('#obList').addEventListener('click', e => {
  const o = e.target.closest('[data-opt]'); if(!o) return;
  if(obStep === 3){ obReminder = o.dataset.opt; } else if(obStep === 4){ obStart = o.dataset.opt; } else return;
  $('#obList').querySelectorAll('.opt').forEach(x => x.classList.toggle('on', x === o));
  const S = OB[obStep]; $('#obNext').textContent = typeof S.next === 'function' ? S.next() : S.next;
  haptic(o, 'selection haptic');
});
$('#obDim').onclick = () => closeMSheet();
