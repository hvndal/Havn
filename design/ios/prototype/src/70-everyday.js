/* ================= round 3 · everyday: scan a label, a photo of the pill, your routine, refills, a gentle suggestion, the pillbox ================= */
let suggDone = false, simpleOn = false, speakNames = false, readHealth = true, focusMode = null, allMode = false, xscene = null;
const tmin = t => +t.slice(0, 2)*60 + +t.slice(3, 5);
const tstr = m => String(Math.floor(((m % 1440) + 1440) % 1440 / 60)).padStart(2, '0') + ':' + String(((m % 60) + 60) % 60).padStart(2, '0');
const T_CAM = '<svg width="18" height="15" viewBox="0 0 18 15"><path d="M6.2 1.5h5.6l1.4 2H16a1 1 0 0 1 1 1V13a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V4.5a1 1 0 0 1 1-1h2.8z" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/><circle cx="9" cy="8.5" r="3" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>';
const T_PHOTO = '<svg width="16" height="14" viewBox="0 0 16 14"><rect x="1" y="1" width="14" height="12" rx="2.5" fill="none" stroke="#fff" stroke-width="1.7"/><path d="M2 11l4-4 3 3 2-2 3 3" fill="none" stroke="#fff" stroke-width="1.6" stroke-linejoin="round"/><circle cx="11" cy="4.6" r="1.3" fill="#fff"/></svg>';
const T_SPARK = '<svg width="16" height="16" viewBox="0 0 16 16"><path d="M8 1l1.6 4.6L14 7.2l-4.4 1.6L8 13.4 6.4 8.8 2 7.2l4.4-1.6z" fill="currentColor"/></svg>';
const T_SUN = '<svg width="16" height="16" viewBox="0 0 16 16"><circle cx="8" cy="8" r="3.2" fill="#fff"/><path d="M8 1v1.8M8 13.2V15M1 8h1.8M13.2 8H15M3 3l1.3 1.3M11.7 11.7L13 13M3 13l1.3-1.3M11.7 4.3L13 3" stroke="#fff" stroke-width="1.6" stroke-linecap="round"/></svg>';
const T_PHONE = '<svg width="14" height="14" viewBox="0 0 14 14"><path d="M3.2 1h2l1 3-1.4 1a7.5 7.5 0 0 0 4.2 4.2l1-1.4 3 1v2A1.8 1.8 0 0 1 11.2 13 10.2 10.2 0 0 1 1 2.8 1.8 1.8 0 0 1 3.2 1z" fill="#fff"/></svg>';
const T_BOX = '<svg width="18" height="14" viewBox="0 0 18 14"><rect x="1" y="1" width="16" height="12" rx="2.5" fill="none" stroke="#fff" stroke-width="1.6"/><path d="M5.3 1v12M9 1v12M12.7 1v12M1 7h16" stroke="#fff" stroke-width="1.3"/></svg>';
const CHECK22 = '<svg width="22" height="22" viewBox="0 0 22 22"><circle cx="11" cy="11" r="11" fill="var(--green)"/><path d="M6.5 11.3l3 3 6-6.6" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
function photoIcon(m, box){ const b = box || 36; return `<div class="ic ph" style="width:${b}px;height:${b}px;border-radius:${b/2}px;background:#ddd url(${m.photo}) center/cover"></div>`; }
const fmtDay = iso => new Date(iso + 'T12:00').toLocaleDateString('en-GB', {weekday:'short', day:'numeric', month:'short'});
/* one quiet refresh of Today, without replaying the slow reveal */
function refreshQuiet(){
  if(stash) return; build(); rebuildRingParts(); renderRing({silent:true}); setCount(doneCount(), true);
  $('#sm').innerHTML = `<span>${smText()}</span>`; $('#accSwap').innerHTML = accNext(); drawBanner();
}

/* ---------- your routine: times tied to breakfast, lunch, dinner and bedtime ---------- */
const ANCH = {wake:'When you wake up', breakfast:'With breakfast', lunch:'With lunch', dinner:'With dinner', bed:'At bedtime'};
const ANCH_SHORT = {wake:'Wake Up', breakfast:'Breakfast', lunch:'Lunch', dinner:'Dinner', bed:'Bedtime'};
const ROUTINE0 = {
  you:{wd:{wake:'07:00', breakfast:'08:00', lunch:'13:00', dinner:'19:00', bed:'22:00'}, we:{wake:'09:00', breakfast:'09:30', lunch:'13:30', dinner:'19:30', bed:'23:00'}, weekend:true},
  mum:{wd:{wake:'07:00', breakfast:'08:00', lunch:'12:30', dinner:'18:30', bed:'21:00'}, we:{wake:'07:00', breakfast:'08:00', lunch:'12:30', dinner:'18:30', bed:'21:00'}, weekend:false},
};
const ANCHORS = {met:'breakfast', vit:'breakfast', iron:'lunch', mag:'bed', levo:'wake', amlo:'breakfast', calc:'lunch', ator:'bed'};
[...YOU_MEDS, ...MUM_MEDS].forEach(m => { m.anchor = ANCHORS[m.id] || null; });
const PHARM = {n:'Lund Pharmacy', addr:'14 Mill Road', tel:'020 7946 0123'};
const freshPerson0 = freshPerson;
freshPerson = id => { const P = freshPerson0(id), r = ROUTINE0[id] || ROUTINE0.you;
  P.routine = JSON.parse(JSON.stringify(r)); P.pharm = id === 'you' || id === 'mum' ? PHARM : null; return P; };
function routineOf(id = cur){ const P = PEOPLE[id]; return P.routine || (P.routine = JSON.parse(JSON.stringify(ROUTINE0.you))); }
function anchorLine(m){ const R = routineOf(); return `${ANCH[m.anchor]} · ${R.wd[m.anchor]}${R.weekend && R.we[m.anchor] !== R.wd[m.anchor] ? `, ${R.we[m.anchor]} at weekends` : ''}`; }

let rtWho = 'you', rtWe = false;
function openRoutine(){ rtWho = cur; rtWe = false; appSheet(''); drawRoutine(); }
function drawRoutine(){
  const sh = $('#appSheet'), P = PEOPLE[rtWho], R = routineOf(rtWho), set = rtWe ? R.we : R.wd;
  const linked = k => P.meds.filter(m => m.anchor === k).map(m => m.n);
  const n = P.meds.filter(m => m.anchor).length;
  sh.innerHTML = `<div class="grab"></div><h3>Daily Routine</h3><p class="sub">Medications linked to a meal or bedtime move when you change it.</p>
    ${Object.keys(PEOPLE).length > 1 ? `<div class="seg" style="margin-bottom:10px">${Object.values(PEOPLE).map(p => `<button class="${p.id === rtWho ? 'on' : ''}" data-rtwho="${p.id}">${p.name}</button>`).join('')}</div>` : ''}
    <div class="seg"><button class="${rtWe ? '' : 'on'}" data-rtwe="0">Weekdays</button><button class="${rtWe ? 'on' : ''}" data-rtwe="1" ${R.weekend ? '' : 'disabled style="opacity:.4"'}>Weekends</button></div>
    <div class="card" style="margin-top:12px">${Object.keys(ANCH).map(k => `<div class="srow"><div class="mid"><div style="font-size:17px">${ANCH_SHORT[k]}</div>${linked(k).length ? `<div class="dt" style="font-size:13px;line-height:16px">${linked(k).join(', ')}</div>` : ''}</div>
      <input type="time" class="timein" data-rt="${k}" value="${set[k]}" aria-label="${ANCH_SHORT[k]} time"></div>`).join('')}</div>
    <div class="card" style="margin-top:12px"><div class="srow"><span class="lbl">Different Times at Weekends</span>${swH('Different Times at Weekends', R.weekend)}</div></div>
    <p class="foot">${n} medication${n === 1 ? ' is' : 's are'} linked to ${rtWho === 'you' ? 'your' : P.name + '’s'} routine. Today is a weekday.</p>
    <button class="btn2 p" data-close="1" style="margin-top:12px">Done</button>`;
  const sw = sh.querySelector('.sw'); wireSwitch(sw);
  sw.addEventListener('pointerup', () => setTimeout(() => { R.weekend = sw.classList.contains('on'); if(!R.weekend) rtWe = false; drawRoutine(); }, 0));
}
$('#appSheet').addEventListener('click', e => {
  const w = e.target.closest('[data-rtwho]'); if(w){ rtWho = w.dataset.rtwho; rtWe = false; drawRoutine(); return; }
  const we = e.target.closest('[data-rtwe]'); if(we && !we.disabled){ rtWe = we.dataset.rtwe === '1'; drawRoutine(); }
});
$('#appSheet').addEventListener('change', e => {
  const k = e.target.dataset && e.target.dataset.rt; if(!k || !/^\d{2}:\d{2}$/.test(e.target.value)) return;
  const P = PEOPLE[rtWho], R = routineOf(rtWho), v = e.target.value;
  if(rtWe){ R.we[k] = v; toast(`Weekend ${ANCH_SHORT[k].toLowerCase()} at ${v}`); drawRoutine(); return; }
  R.wd[k] = v; if(!R.weekend) R.we[k] = v;
  const moved = P.meds.filter(m => m.anchor === k);
  moved.forEach(m => { m.t = v; m.g = groupOf(v); });
  P.meds.sort((a, b) => a.t < b.t ? -1 : a.t > b.t ? 1 : 0);
  if(rtWho === cur) refreshQuiet();
  toast(moved.length ? `${moved.map(m => m.n).join(' and ')} now at ${v}` : `${ANCH_SHORT[k]} at ${v}`);
  drawRoutine();
});

/* ---------- Add Medication: scan the label, link to the routine, a photo of the pill, the end date ---------- */
const paneHTML0 = paneHTML;
paneHTML = i => {
  let h = paneHTML0(i);
  if(i === 0) h = h.replace('<div class="field"><input id="fName"', `<button class="scanbtn" data-scan="1">${T_CAM}Scan the Pharmacy Label</button><div class="or">or type it</div><div class="field"><input id="fName"`);
  if(i === 3){
    const R = routineOf(), mode = D.anchMode === 'routine';
    h = h.replace('<div class="lbl3">Times</div>', `<div class="lbl3">Times</div><div class="seg" style="margin-bottom:10px"><button class="${mode ? '' : 'on'}" data-anchmode="time">At a Set Time</button><button class="${mode ? 'on' : ''}" data-anchmode="routine">With Your Routine</button></div>`);
    if(mode) h = h.replace(/<div class="card" id="timesCard">[\s\S]*?<div class="navrow add" data-addtime="1">Add a Time<\/div><\/div>/,
      `<div class="chips" style="margin-bottom:10px">${Object.keys(ANCH).map(k => `<button class="${D.anchor === k ? 'on' : ''}" data-anch="${k}">${ANCH_SHORT[k]}</button>`).join('')}</div>
       <div class="card" id="timesCard"><div class="srow"><div class="mid"><div style="font-size:17px">${ANCH[D.anchor]}</div><div class="dt" style="font-size:13px">${R.wd[D.anchor]} on weekdays${R.weekend ? `, ${R.we[D.anchor]} at weekends` : ''}</div></div>${stepper('qty', D.qty)}</div></div>
       <p class="foot">It moves with your routine. Change the times in Settings › Daily Routine.</p>`);
  }
  if(i === 4) h = h.replace('<div class="lbl3">Shape</div>', `<div class="card" style="margin-top:4px">
      <div class="navrow" data-photo="1"><span class="tile" style="background:var(--gray)">${T_CAM.replace(/currentColor/g, '#fff')}</span><span class="lbl">${D.photo ? 'Retake Photo' : 'Take a Photo of the Pill'}</span>${CHEV}</div>
      <label class="navrow"><span class="tile" style="background:var(--blue)">${T_PHOTO}</span><span class="lbl">Choose from Photos</span><input type="file" accept="image/*" id="photoIn" hidden>${CHEV}</label>
      ${D.photo ? '<div class="navrow danger" data-photox="1">Remove Photo</div>' : ''}</div>
    <p class="foot">A photo of your own pill is the surest way to tell it apart. It stays in Hävn on this iPhone.</p><div class="lbl3">Shape</div>`);
  if(i === 5){
    if(D.fromLabel) h = h.replace('<div class="card"><div class="lrow" style="cursor:default">', `<div class="banner" style="margin:0 0 14px;background:color-mix(in srgb,var(--accent) 12%,var(--cell))"><span class="tile" style="background:var(--accent);color:var(--on-accent)">${T_CAM}</span><div class="mid"><div class="nm">Filled in from the label</div><div class="dt">Check it matches before you add it.</div></div></div><div class="card"><div class="lrow" style="cursor:default">`);
    if(D.anchMode === 'routine' && D.anchor) h = h.replace(`<span class="val">${D.times.join(', ')}</span>`, `<span class="val">${ANCH[D.anchor]}</span>`);
    if(D.ends) h = h.replace('<div class="navrow" data-go="4">', `<div class="navrow" data-go="3"><span class="lbl">Ends</span><span class="val">${fmtDay(D.endDate || '2026-12-18')}</span>${CHEV}</div><div class="navrow" data-go="4">`);
  }
  return h;
};
$('#addSteps').addEventListener('click', e => {
  const t = e.target;
  if(t.closest('[data-scan]')) return openScan();
  const am = t.closest('[data-anchmode]'); if(am){ D.anchMode = am.dataset.anchmode;
    if(D.anchMode === 'routine'){ D.anchor = D.anchor || 'breakfast'; D.times = [routineOf().wd[D.anchor]]; } else D.anchor = null;
    rerenderPane(); haptic(am, 'selection haptic'); return; }
  const ak = t.closest('[data-anch]'); if(ak){ D.anchor = ak.dataset.anch; D.times = [routineOf().wd[D.anchor]]; rerenderPane(); haptic(ak, 'selection haptic'); return; }
  if(t.closest('[data-photo]')) return openCamera();
  if(t.closest('[data-photox]')){ D.photo = null; rerenderPane(); }
});
$('#addSteps').addEventListener('change', e => {
  if(e.target.id === 'photoIn' && e.target.files[0]) readPhoto(e.target.files[0]);
  if(e.target.dataset.enddate && e.target.value) D.endDate = e.target.value;
});

/* the label: found by text recognition on the iPhone, never uploaded */
const LABEL = {n:'Amoxicillin', strength:'500', unit:'mg', form:'Capsule', shape:'capsule', a:'yellow', b:'red', times:['08:00','14:00','20:00'], supply:21, endDate:'2026-11-25'};
function openScan(){
  const sh = $('#scanSheet');
  sh.innerHTML = `<div class="scantop"><button class="pill-btn glass" data-scanx="1" style="color:#fff">Cancel</button><span>Scan Label</span><span style="width:84px"></span></div>
    <div class="vf" id="vf"><div class="label" id="lbl">
      <span class="ln" data-k="ph"><b>LUND PHARMACY</b> · 14 Mill Road</span><span class="ln" data-k="tel">Tel 020 7946 0123 · Rx 0418823</span>
      <span class="ln" data-k="n"><b>AMOXICILLIN 500 MG CAPSULES</b></span><span class="ln" data-k="dir">Take ONE capsule THREE times a day until all taken.</span>
      <span class="ln" data-k="qty">Qty: 21 capsules · Dr A. Okafor</span><span class="ln pt">M. LINDQVIST · 18/11/26</span></div>
      <i class="brk" data-c="tl"></i><i class="brk" data-c="tr"></i><i class="brk" data-c="bl"></i><i class="brk" data-c="br"></i><div class="sweep"></div></div>
    <div class="scanhint" id="scanHint">Hold the label inside the frame</div>
    <div class="found2" id="found2" hidden><div class="nm" style="font-size:17px;margin-bottom:6px">Found on the label</div>
      <div class="r"><span>Name</span>Amoxicillin</div><div class="r"><span>Strength</span>500 mg capsule</div><div class="r"><span>Directions</span>1 capsule, 3 times a day</div>
      <div class="r"><span>Quantity</span>21 · lasts until ${fmtDay(LABEL.endDate)}</div><div class="r"><span>Pharmacy</span>Lund Pharmacy</div>
      <button class="btn2 p" data-usescan="1" style="margin-top:12px">Use These Details</button><button class="ob-link" data-rescan="1" style="color:#fff;opacity:.8">Scan Again</button>
      <p class="foot" style="color:rgba(255,255,255,.6);text-align:center;padding:6px 0 0">Read on this iPhone. The picture isn’t kept.</p></div>`;
  sh.classList.add('open');
  if(!reduce) sh.animate([{opacity:0, transform:'scale(1.04)'}, {opacity:1, transform:'none'}], {duration:380, easing:'cubic-bezier(.2,.9,.25,1)'});
  runScan();
}
let scanTimers = [];
function runScan(){
  scanTimers.forEach(clearTimeout); scanTimers = [];
  const vf = $('#vf'), lbl = $('#lbl'), W = vf.clientWidth, H = vf.clientHeight, at = (ms, fn) => scanTimers.push(setTimeout(fn, reduce ? 0 : ms));
  const place = (x1, y1, x2, y2) => vf.querySelectorAll('.brk').forEach(b => { const c = b.dataset.c;
    b.style.left = (c[1] === 'l' ? x1 : x2 - 44) + 'px'; b.style.top = (c[0] === 't' ? y1 : y2 - 44) + 'px'; });
  vf.querySelectorAll('.ln').forEach(l => l.classList.remove('hit')); $('#found2').hidden = true; vf.classList.remove('locked');
  place(18, 18, W - 18, H - 18); $('#scanHint').textContent = 'Hold the label inside the frame';
  at(650, () => { place(lbl.offsetLeft - 12, lbl.offsetTop - 16, lbl.offsetLeft + lbl.offsetWidth + 12, lbl.offsetTop + lbl.offsetHeight + 18); vf.classList.add('locked'); $('#scanHint').textContent = 'Reading the label…'; });
  ['n', 'dir', 'qty', 'ph', 'tel'].forEach((k, i) => at(1250 + i*200, () => { const l = vf.querySelector(`[data-k="${k}"]`); l.classList.add('hit'); haptic(l, 'selection haptic'); }));
  at(2400, () => { const f = $('#found2'); f.hidden = false; Snd.play('on'); haptic($('#vf'), 'success haptic'); $('#scanHint').textContent = '';
    if(!reduce) f.animate([{transform:'translateY(110%)'}, {transform:'none'}], {duration:520, easing:'cubic-bezier(.2,.95,.25,1.02)'}); });
}
function closeScan(after){
  const sh = $('#scanSheet'); scanTimers.forEach(clearTimeout); if(!sh.classList.contains('open')){ typeof after === 'function' && after(); return; }
  const done = () => { sh.classList.remove('open'); sh.innerHTML = ''; typeof after === 'function' && after(); };
  if(reduce) return done(); sh.animate([{opacity:1}, {opacity:0, transform:'scale(1.03)'}], {duration:280, easing:'ease-in'}).onfinish = done;
}
$('#scanSheet').addEventListener('click', e => {
  const t = e.target;
  if(t.closest('[data-scanx]')) return closeScan();
  if(t.closest('[data-rescan]')) return runScan();
  if(t.closest('[data-usescan]')){ Object.assign(D, {n:LABEL.n, form:LABEL.form, shape:LABEL.shape, a:LABEL.a, b:LABEL.b, strength:LABEL.strength, unit:LABEL.unit,
      sched:'daily', times:[...LABEL.times], qty:1, supply:LABEL.supply, ends:true, endDate:LABEL.endDate, fromLabel:true, pharm:PHARM, anchMode:'time', anchor:null});
    haptic(t, 'success haptic'); closeScan(() => addPane(5, 1)); return; }
  if(t.closest('[data-shutter]')) return takePhoto();
  if(t.closest('[data-usephoto]')){ D.photo = camShot; closeScan(() => { rerenderPane(); const pv = $('#pv2 .ic'); if(pv && !reduce) pv.animate([{transform:'scale(.7)'}, {transform:'scale(1)'}], {duration:520, easing:'cubic-bezier(.3,1.6,.5,1)'}); }); return; }
  if(t.closest('[data-retake]')) return openCamera();
});

/* ---------- a photo of your own pill ---------- */
let camShot = null;
function cssColor(v){ return getComputedStyle($('#phone')).getPropertyValue(v).trim() || '#ffffff'; }
function pillPhoto(draft, size){        // a made-up photo for the prototype: your pill on a kitchen table, in soft daylight
  return new Promise(res => {
    const c = document.createElement('canvas'); c.width = c.height = size; const g = c.getContext('2d');
    const bg = g.createRadialGradient(size*.38, size*.3, size*.05, size*.5, size*.5, size*.8); bg.addColorStop(0, '#E9DFCF'); bg.addColorStop(1, '#B9A68A');
    g.fillStyle = bg; g.fillRect(0, 0, size, size);
    for(let i = 0; i < 9; i++){ g.strokeStyle = `rgba(120,95,60,${.05 + (i % 3)*.02})`; g.lineWidth = 2 + (i % 2); g.beginPath(); g.moveTo(-10, size*(i/8) + 8); g.bezierCurveTo(size*.3, size*(i/8) - 6, size*.7, size*(i/8) + 14, size + 10, size*(i/8)); g.stroke(); }
    g.fillStyle = 'rgba(40,30,20,.28)'; g.beginPath(); g.ellipse(size*.53, size*.6, size*.3, size*.11, -.5, 0, Math.PI*2); g.filter = 'blur(6px)'; g.fill(); g.filter = 'none';
    const svg = pillSVG({k:draft.shape, a:draft.a, b:draft.b}, 200).replace(/var\((--pc-[a-z]+)\)/g, (_, v) => cssColor(v)).replace('<svg ', '<svg xmlns="http://www.w3.org/2000/svg" ');
    const img = new Image(); img.onload = () => { g.drawImage(img, size*.1, size*.1, size*.8, size*.8); res(c.toDataURL('image/jpeg', .82)); };
    img.onerror = () => res(c.toDataURL('image/jpeg', .82)); img.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
  });
}
function openCamera(){
  const sh = $('#scanSheet');
  sh.innerHTML = `<div class="scantop"><button class="pill-btn glass" data-scanx="1" style="color:#fff">Cancel</button><span>Photo</span><span style="width:84px"></span></div>
    <div class="vf" id="vf" style="background:#000"><img id="camImg" alt="" style="width:100%;height:100%;object-fit:cover;display:block;filter:saturate(1.05)"><div class="camring"></div></div>
    <div class="scanhint">Put one pill in the circle, on a plain surface</div>
    <div class="shutterbar" id="shutterbar"><button class="shutter" data-shutter="1" aria-label="Take Photo"></button></div>`;
  sh.classList.add('open');
  if(!reduce) sh.animate([{opacity:0}, {opacity:1}], {duration:300});
  pillPhoto(D, 520).then(u => { const im = $('#camImg'); if(im) im.src = u; });
}
function takePhoto(){
  const flash = document.createElement('div'); flash.className = 'flash'; $('#vf').appendChild(flash);
  if(!reduce) flash.animate([{opacity:.9}, {opacity:0}], {duration:420}).onfinish = () => flash.remove(); else flash.remove();
  Snd.play('tap'); haptic($('#shutterbar'), 'shutter');
  pillPhoto(D, 160).then(u => { camShot = u;
    $('#shutterbar').innerHTML = `<button class="btn2 g" data-retake="1" style="flex:1;background:rgba(255,255,255,.16);color:#fff">Retake</button><button class="btn2 p" data-usephoto="1" style="flex:1">Use Photo</button>`; });
}
function readPhoto(file){
  const r = new FileReader();
  r.onload = () => { const img = new Image(); img.onload = () => {
      const c = document.createElement('canvas'); c.width = c.height = 160; const g = c.getContext('2d'), s = Math.min(img.width, img.height);
      g.drawImage(img, (img.width - s)/2, (img.height - s)/2, s, s, 0, 0, 160, 160); D.photo = c.toDataURL('image/jpeg', .82); rerenderPane(); };
    img.src = r.result; };
  r.readAsDataURL(file);
}

/* ---------- Medications: routine times, refills and the pharmacy on a medication's page ---------- */
const detailHTML0 = detailHTML;
detailHTML = m => {
  let h = detailHTML0(m); const prn = PRN.includes(m), times = (m.times || [m.t]).join(', ');
  if(!prn && m.anchor){
    const R = routineOf();
    h = h.replace(`${m.sched} at ${times}`, `${m.sched}, ${ANCH[m.anchor].toLowerCase()}`);
    h = h.replace(`<div class="navrow"><span class="lbl">Time</span><span class="val">${times}</span>${CHEV}</div>`,
      `<div class="navrow" data-routine="1"><span class="lbl">Time</span><span class="val">${ANCH[m.anchor]} · ${R.wd[m.anchor]}</span>${CHEV}</div>`
      + (R.weekend ? `<div class="navrow" data-routine="1"><span class="lbl">Weekends</span><span class="val">${R.we[m.anchor]}</span>${CHEV}</div>` : ''));
  }
  const ph = m.pharm || PEOPLE[cur].pharm;
  h = h.replace(`<span class="lbl">Refill Reminder</span><span class="val">At 7 left</span>${CHEV}</div>`,
    `<span class="lbl">Refill Reminder</span><span class="val">At 7 left</span>${CHEV}</div>
     <div class="navrow add" data-refill="${m.id}"><span class="lbl">I Picked Up a Refill</span></div>
     <div class="navrow" data-pharm="${m.id}"><span class="lbl">Pharmacy</span><span class="val">${ph ? ph.n : 'Add'}</span>${CHEV}</div>`);
  if(m.ends) h = h.replace(`<div class="card" style="margin-top:14px"><div class="navrow"><span class="lbl">Supply</span>`,
    `<div class="card" style="margin-top:14px"><div class="navrow"><span class="lbl">Ends</span><span class="val">${fmtDay(m.ends)}</span>${CHEV}</div></div><div class="card" style="margin-top:14px"><div class="navrow"><span class="lbl">Supply</span>`);
  return h;
};
let rfN = 28;
function openRefill(id){
  const m = findMed(id); if(!m) return; rfN = m.form === 'Capsule' ? 30 : 28;
  const sh = appSheet(''), per = Math.max(1, (m.times || [1]).length);
  const draw = () => { const left = m.left + rfN, runs = new Date(2026, 10, 18 + Math.floor(left / per));
    sh.innerHTML = `<div class="grab"></div><div style="display:grid;place-items:center;margin-top:8px">${icon(m, 56)}</div><h3>Refill ${m.n}</h3><p class="sub">How many did you pick up?</p>
      <div class="chips" style="justify-content:center">${[28, 30, 56, 90].map(n => `<button class="${n === rfN ? 'on' : ''}" data-rfn="${n}">${n}</button>`).join('')}</div>
      <div style="display:flex;justify-content:center;margin-top:12px"><div class="stepper"><button data-rfd="-1" aria-label="Fewer">−</button><span>${rfN}</span><button data-rfd="1" aria-label="More">+</button></div></div>
      <p class="foot" style="text-align:center">You’ll have ${left}, enough until about ${runs.toLocaleDateString('en-GB', {day:'numeric', month:'long'})}.</p>
      <button class="btn2 p" data-rfok="1" style="margin-top:12px">Add to Supply</button><button class="ob-link" data-close="1">Cancel</button>`; };
  draw();
  sh.onclick = e => {
    if(e.target.closest('[data-close]')) return closeAppSheet();
    const c = e.target.closest('[data-rfn]'); if(c){ rfN = +c.dataset.rfn; draw(); return; }
    const d = e.target.closest('[data-rfd]'); if(d){ rfN = Math.max(1, Math.min(500, rfN + +d.dataset.rfd)); draw(); return; }
    if(e.target.closest('[data-rfok]')){
      [...MEDS, ...PRN, ...EXTRA].filter(x => x.id === id || baseId(x.id) === id).forEach(x => { x.left += rfN; });
      const now2 = findMed(id).left; Snd.play('on');
      closeAppSheet(() => { const top = $('#scrMeds .nav2').lastElementChild; if(top){ top.innerHTML = detailHTML(findMed(id));
          const b = top.querySelector('.stats div:nth-child(2) b'); if(b && !reduce) b.animate([{transform:'scale(1.35)', color:'var(--green)'}, {transform:'scale(1)'}], {duration:620, easing:'cubic-bezier(.3,1.6,.5,1)'}); }
        refreshQuiet(); toast(`${m.n}: ${now2} left`); });
    }
  };
}
function openPharmacy(id){
  const m = findMed(id), ph = (m && m.pharm) || PEOPLE[cur].pharm;
  if(!ph) return appSheet(`<h3>Add a Pharmacy</h3><p class="sub">Keep its name and number with ${PEOPLE[cur].name === 'You' ? 'your' : PEOPLE[cur].name + '’s'} medications, so a refill is one call away.</p>
    <div class="field"><input placeholder="Pharmacy name" autocomplete="off"></div><div class="field"><input placeholder="Phone number" inputmode="tel" autocomplete="off"></div>
    <button class="btn2 p" data-close="1" style="margin-top:14px">Save</button>`);
  const sh = appSheet(`<div style="display:grid;place-items:center;margin-top:6px"><span class="tile" style="width:52px;height:52px;border-radius:14px;background:var(--green)">${T_PHONE.replace('width="14" height="14"', 'width="24" height="24"')}</span></div>
    <h3>${ph.n}</h3><p class="sub">${ph.addr}</p>
    <div class="card"><div class="navrow" data-call="1"><span class="lbl" style="user-select:text">${ph.tel}</span><span class="val" style="color:var(--accent)">Call</span></div><div class="navrow" data-copytel="1"><span class="lbl">Copy Number</span></div></div>
    <p class="foot">Hävn keeps this on your iPhone. It never contacts the pharmacy itself; a call goes through your Phone app.</p><button class="btn2 g" data-close="1" style="margin-top:12px">Done</button>`);
  sh.addEventListener('click', e => { if(e.target.closest('[data-call]')) closeAppSheet(() => toast(`Calling ${ph.tel}`));
    if(e.target.closest('[data-copytel]')){ try { navigator.clipboard.writeText(ph.tel).catch(() => {}); } catch(_){} closeAppSheet(() => toast('Number copied')); } }, {once:true});
}
$('#scrMeds').addEventListener('click', e => {
  const r = e.target.closest('[data-refill]'); if(r) return openRefill(r.dataset.refill);
  const p = e.target.closest('[data-pharm]'); if(p) return openPharmacy(p.dataset.pharm);
  if(e.target.closest('[data-routine]')) return openRoutine();
  if(e.target.closest('[data-box]')) return openBox();
});

/* ---------- a gentle suggestion, worked out on the iPhone ---------- */
const build0 = build;
build = () => { build0(); const h = suggHTML(); if(h) $('#groups').insertAdjacentHTML('afterbegin', h); };
function suggHTML(){
  if(cur !== 'you' || suggDone || stash) return '';
  const m = MEDS.find(x => x.id === 'iron'); if(!m || m.t !== '13:00' || taken.iron) return '';
  return `<div class="sec" id="suggSec"><div class="sugg"><div style="display:flex;gap:12px;align-items:flex-start"><span class="tile" style="background:var(--accent);color:var(--on-accent)">${T_SPARK}</span>
    <div class="mid"><div class="nm" style="font-size:16px;line-height:21px">Move Iron to 13:45?</div><div class="dt">You usually log it about 40 minutes after its reminder. A later time might suit your day.</div></div></div>
    <div class="row2"><button class="pbtn sm" data-sugg="yes">Move to 13:45</button><button class="pbtn ghost sm" data-sugg="no">Not Now</button></div>
    <div class="dt" style="font-size:12px;line-height:16px;margin-top:10px">Worked out on this iPhone from the last two weeks.</div></div></div>`;
}
const logDose0 = logDose;
logDose = (id, src) => { logDose0(id, src); if(id === 'iron' && $('#suggSec')) closeSugg(); };
function closeSugg(after){
  const s = $('#suggSec'); suggDone = true; if(!s){ after && after(); return; }
  const done = () => { s.remove(); after && after(); };
  if(reduce) return done();
  s.animate([{opacity:1, height:s.offsetHeight + 'px', marginTop:'22px'}, {opacity:0, height:'0px', marginTop:'0px'}], {duration:380, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = done;
}
$('#app').addEventListener('click', e => {
  const s = e.target.closest('[data-sugg]'); if(!s) return;
  if(s.dataset.sugg === 'yes'){ const m = MEDS.find(x => x.id === 'iron'); haptic(s, 'success haptic'); Snd.play('on');
    closeSugg(() => { m.t = '13:45'; m.anchor = null; refreshQuiet(); const dt = document.querySelector('[data-dt="iron"]'); dt && dt.classList.add('flashTxt'); toast('Iron now reminds you at 13:45'); }); }
  else closeSugg();
});

/* ---------- fill your pillbox: a 7-day organiser, one medication at a time ---------- */
const boxDays = () => Array.from({length:7}, (_, i) => { const d = new Date(2026, 10, 19 + i); return {l:d.toLocaleDateString('en-GB', {weekday:'short'}).slice(0, 3), n:d.getDate()}; });
let boxStep = 0, boxList = [];
const drawMeds0 = drawMeds;
drawMeds = () => {
  drawMeds0();
  const L = medList().filter(m => !m.note); if(!L.length) return;
  const P = PEOPLE[cur], filled = P.boxFilled;
  $('#medsList .lsub').insertAdjacentHTML('afterend', `<div class="card boxcard" data-box="1"><div class="minibox">${Array.from({length:14}, (_, i) => `<i style="${filled ? `background:var(--${L[i % L.length].c})` : ''}"></i>`).join('')}</div>
    <div class="mid"><div class="nm">${filled ? 'Pillbox Filled' : 'Fill Your Pillbox'}</div><div class="dt">${filled ? `For ${filled}. Fill it again next Wednesday.` : 'Sort next week into a 7-day organiser, one medication at a time.'}</div></div>${CHEV}</div>`);
};
function openBox(){
  const L = medList().filter(m => !m.note); if(!L.length) return;
  boxList = L; boxStep = 0;
  const slots = ['Morning','Afternoon','Evening','Night'].filter(g => L.some(m => (m.times || [m.t]).some(t => groupOf(t) === g))), days = boxDays();
  const sh = $('#boxSheet');
  sh.innerHTML = `<div class="inner"><div class="ahead"><button class="pill-btn glass" data-boxx="1">Cancel</button><span class="ttl">Fill Your Pillbox</span><span style="width:76px"></span></div>
    <div class="dt" style="text-align:center;margin-top:4px">Thursday 19 – Wednesday 25 November</div>
    <div class="box" id="box" style="grid-template-columns:58px repeat(7,1fr)"><span></span>${days.map(d => `<span class="bh">${d.l}<br>${d.n}</span>`).join('')}
      ${slots.map(g => `<span class="bl">${g}</span>${days.map((_, i) => `<div class="cmp" data-slot="${g}" data-dn="${i}"></div>`).join('')}`).join('')}</div>
    <div class="bstep" id="bstep"></div><div id="bwarn"></div>
    <div style="padding:14px 16px 20px;margin-top:auto"><button class="btn2 p" id="boxNext">Next Medication</button></div></div>`;
  sh.classList.add('open'); $('#dim').classList.add('on');
  if(!reduce) sh.animate([{transform:'translateY(105%)'}, {transform:'none'}], {duration:540, easing:'cubic-bezier(.2,.95,.25,1.02)'});
  setTimeout(() => boxGo(0), reduce ? 0 : 420);
}
function boxGo(k){
  boxStep = k; const m = boxList[k], per = (m.times || [m.t]).map(groupOf), word = {Capsule:'capsule', Softgel:'softgel', Liquid:'dose', Drops:'dose'}[m.form] || 'tablet';
  $('#box').querySelectorAll('.cmp').forEach(c => c.classList.toggle('target', per.includes(c.dataset.slot)));
  $('#bstep').innerHTML = `${icon(m, 44)}<div class="mid"><div class="nm">${m.n} <span class="dt">${m.d}</span></div><div class="dt">1 ${word} in each ${per.map(g => g.toLowerCase()).join(' and ')} box · ${7*per.length} in all</div></div><span class="dt" style="white-space:nowrap">${k + 1} of ${boxList.length}</span>`;
  const short = 7*per.length - m.left;
  $('#bwarn').innerHTML = short > 0 ? `<p class="foot" style="color:var(--orange-text);padding:8px 22px 0">Only ${m.left} left, so ${short === 1 ? 'one box stays' : short + ' boxes stay'} empty. Time for a refill.</p>` : '';
  $('#boxNext').textContent = k === boxList.length - 1 ? 'Done' : 'Next Medication';
  if(!reduce) $('#bstep').animate([{opacity:0, transform:'translateX(30px)'}, {opacity:1, transform:'none'}], {duration:360, easing:'cubic-bezier(.2,.9,.25,1)'});
  let n = 0;
  $('#box').querySelectorAll('.cmp.target').forEach(c => { const i = +c.dataset.dn; if(n >= m.left) return; n++;
    const p = document.createElement('span'); p.className = 'bp'; p.innerHTML = m.photo ? `<i style="background:url(${m.photo}) center/cover"></i>` : pillSVG(m.p, 17); c.appendChild(p);
    if(!reduce) p.animate([{transform:'translateY(-46px) rotate(-40deg)', opacity:0}, {transform:'translateY(3px) rotate(8deg)', opacity:1, offset:.75}, {transform:'none', opacity:1}],
      {duration:520, delay:140 + i*70, easing:'cubic-bezier(.3,.8,.4,1)', fill:'backwards'}); });
  setTimeout(() => Snd.play('tap'), reduce ? 0 : 300);
}
function closeBox(after){
  const sh = $('#boxSheet'); if(!sh.classList.contains('open')) return; $('#dim').classList.remove('on');
  const done = () => { sh.classList.remove('open'); typeof after === 'function' && after(); };
  if(reduce) return done(); sh.animate([{transform:'none'}, {transform:'translateY(105%)'}], {duration:400, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = done;
}
$('#boxSheet').addEventListener('click', e => {
  if(e.target.closest('[data-boxx]')) return closeBox();
  if(e.target.closest('#boxNext')){
    if(boxStep < boxList.length - 1) return boxGo(boxStep + 1);
    const total = $('#box').querySelectorAll('.bp').length; PEOPLE[cur].boxFilled = '19–25 November';
    haptic($('#boxNext'), 'success haptic'); Snd.play('close');
    closeBox(() => { drawMeds(); toast(`${total} doses sorted for next week`); });
  }
});
const closeAllSheets0 = closeAllSheets;
closeAllSheets = () => { closeScan(); closeBox(); closeAllSheets0(); };
/* the dim behind sheets closes the pillbox first; this runs before the dim's own handler */
$('#dim').addEventListener('click', e => { if($('#boxSheet').classList.contains('open')){ e.stopImmediatePropagation(); closeBox(); } }, true);
