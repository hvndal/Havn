/* ================= round 3 · health: everyone at a glance, "did it help?", readings, how you feel, the monthly recap ================= */
const CK = '<svg class="ck" width="16" height="13" viewBox="0 0 16 13"><path d="M1.5 7l4.5 4.5L14.5 2" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>';
/* a ring for any list of doses: the same geometry as the Home Screen widget */
function ringOf(meds, tk, size, o = {}){
  const n = meds.length, sw = o.sw || 12, r = 50 - sw/2, C = 2*Math.PI*r, d = meds.filter(m => tk[m.id]).length, closed = n && d === n;
  const col = o.col || ((m, on) => on ? `var(--${m.c})` : m.t < now ? 'color-mix(in srgb,var(--orange) 45%,var(--fill))' : 'var(--fill)');
  if(!n) return `<svg width="${size}" height="${size}" viewBox="0 0 100 100"><circle cx="50" cy="50" r="${r}" fill="none" stroke="${o.track || 'var(--fill)'}" stroke-width="${sw}"/></svg>`;
  const cap = (sw/2)/r*180/Math.PI, gap = closed ? 0 : 2*cap + 9, seg = 360/n - gap;
  return `<svg width="${size}" height="${size}" viewBox="0 0 100 100" style="flex:none;display:block">` + meds.map((m, i) =>
    `<circle data-rseg="${o.key || ''}${m.id}" cx="50" cy="50" r="${r}" fill="none" stroke="${col(m, !!tk[m.id])}" stroke-width="${sw}" stroke-linecap="${closed ? 'butt' : 'round'}" stroke-dasharray="${closed ? (360/n)/360*C + .6 : seg/360*C} ${C}" transform="rotate(${-90 + i*360/n + gap/2} 50 50)"/>`).join('')
    + (o.center !== undefined ? o.center : closed ? `<path d="M36 51l10 10 19-21" fill="none" stroke="${o.check || 'var(--green)'}" stroke-width="7" stroke-linecap="round" stroke-linejoin="round"/>` : '') + '</svg>';
}
function growSegs(root, key){ if(reduce) return; root.querySelectorAll(`[data-rseg="${key}"]`).forEach(c => { const da = c.getAttribute('stroke-dasharray').split(' ');
  c.animate([{strokeDasharray:`0 ${da[1]}`}, {strokeDasharray:`${da[0]} ${da[1]}`}], {duration:900, easing:'cubic-bezier(.22,1.12,.36,1)'}); }); }

/* ---------- Everyone: every person's ring on one screen ---------- */
const stackAvatar = () => `<span class="stackav">${Object.values(PEOPLE).slice(0, 2).map(P => avatarHTML(P, 24)).join('')}</span>`;
const openPeopleMenu0 = openPeopleMenu;
openPeopleMenu = () => {
  const had = !!$('.pmenu'); openPeopleMenu0(); if(had) return;
  const m = $('.pmenu'); if(!m) return;
  if(focusMode === 'caring'){ m.querySelectorAll('[data-person]').forEach(it => { if(it.dataset.person !== 'mum') it.remove(); });
    m.insertAdjacentHTML('afterbegin', `<div class="it" style="cursor:default"><span class="tile" style="background:var(--purple)">${T_HEARTS}</span><span class="lbl" style="font-size:14px;line-height:18px;color:var(--l2)">Caring Focus is on. Others are hidden.</span></div><div class="sep"></div>`); return; }
  if(Object.keys(PEOPLE).length < 2) return;
  if(allMode) m.querySelectorAll('[data-person] .ck').forEach(c => c.remove());
  m.insertAdjacentHTML('afterbegin', `<div class="it" data-everyone="1" role="menuitem">${stackAvatar()}<span class="lbl">Everyone</span>${allMode ? CK : ''}</div><div class="sep"></div>`);
  if(!reduce) m.firstElementChild.animate([{opacity:0, transform:'translateY(-6px)'}, {opacity:1, transform:'none'}], {duration:300, delay:60, fill:'backwards', easing:'ease-out'});
  m.addEventListener('click', e => { if(e.target.closest('[data-everyone]')) closePeopleMenu(openEveryone); });
};
function openEveryone(){
  saveCur(); allMode = true; if(tab !== 0) showTab(0, true);
  const A = $('#scrAll'); drawEveryone(); A.hidden = false; $('#scroll').hidden = true; A.scrollTop = 0;
  $('#openSettings').innerHTML = stackAvatar(); $('#acc').style.visibility = 'hidden';
  haptic($('#openSettings'), 'selection haptic');
  if(!reduce) A.querySelectorAll('.ltitle,.lsub,.pcard,.foot').forEach((el, i) => el.animate([{opacity:0, transform:'translateY(16px)'}, {opacity:1, transform:'none'}], {duration:520, delay:i*70, easing:'cubic-bezier(.2,.8,.2,1)', fill:'backwards'}));
}
function closeEveryone(quietly){ if(!allMode) return; allMode = false; $('#scrAll').hidden = true; $('#scroll').hidden = tab !== 0; $('#acc').style.visibility = '';
  $('#openSettings').innerHTML = avatarHTML(PEOPLE[cur]); if(!quietly) refreshQuiet(); }
function drawEveryone(){
  saveCur();
  $('#scrAll').innerHTML = `<div class="ltitle">Everyone</div><div class="lsub">Wednesday, 18 November</div>
    <div class="card" style="margin-top:18px">${Object.values(PEOPLE).map(personCard).join('')}</div>
    <p class="foot">Tap a person to see their day. Reminders say whose dose it is.</p>`;
}
function personCard(P){
  const n = P.meds.length, d = P.meds.filter(m => P.taken[m.id]).length, due = P.meds.filter(m => !P.taken[m.id] && m.t <= now), next = P.meds.find(m => !P.taken[m.id]);
  const line = !n ? 'No medications yet' : d === n ? 'All done today' : due.length ? `${due.map(m => m.n).join(', ')} · due ${due[0].t}` : `Next: ${next.n} at ${next.t}`;
  return `<div class="pcard" data-pp="${P.id}">${ringOf(P.meds, P.taken, 58, {key:P.id + ':'})}<div class="mid"><div style="display:flex;align-items:center;gap:8px">${avatarHTML(P, 22)}<span class="nm">${P.name}</span></div>
    <div class="dt">${n ? `${d} of ${n} taken · ` : ''}<span style="${due.length ? 'color:var(--orange-text)' : ''}">${line}</span></div></div>
    ${due.length ? `<button class="pbtn sm" data-ptake="${P.id}">Taken</button>` : CHEV}</div>`;
}
const showTab0 = showTab;
showTab = (i, instant) => { if(allMode && i !== 0) closeEveryone(true); showTab0(i, instant); if(allMode) $('#scroll').hidden = true; };
const switchPerson0 = switchPerson;
switchPerson = id => { const was = allMode; if(allMode) closeEveryone(true); if(id === cur && was){ refreshToday(true); return; } switchPerson0(id); };
$('#scrAll').addEventListener('click', e => {
  const b = e.target.closest('[data-ptake]');
  if(b){ const P = PEOPLE[b.dataset.ptake], due = P.meds.filter(m => !P.taken[m.id] && m.t <= now);
    due.forEach(m => { P.taken[m.id] = now; P.order.push(m.id); P.history.push(m.id); });
    Snd.play('tap'); setTimeout(() => Snd.play('taken'), 140); haptic(b, 'success haptic');
    const card = b.closest('.pcard'); card.outerHTML = personCard(P);
    const nc = $(`#scrAll [data-pp="${P.id}"]`); due.forEach(m => growSegs(nc, P.id + ':' + m.id));
    if(P.meds.length && P.meds.every(m => P.taken[m.id])) setTimeout(() => { Snd.play('close'); haptic(nc.querySelector('svg'), 'success haptic'); }, 700);
    return; }
  const c = e.target.closest('[data-pp]'); if(c) switchPerson(c.dataset.pp);
});
const T_HEARTS = '<svg width="16" height="14" viewBox="0 0 30 27"><path d="M15 26S1.5 17.6 1.5 8.4A6.9 6.9 0 0 1 15 5.2a6.9 6.9 0 0 1 13.5 3.2C28.5 17.6 15 26 15 26z" fill="#fff"/></svg>';

/* reminders say whose dose it is, and other people's doses get their own notification */
const drawLock0 = drawLock;
drawLock = () => {
  drawLock0(); saveCur(); const L = $('#lockScene'), nt = $('#lkNotif');
  if(nt && cur !== 'you'){ const b = [...nt.querySelectorAll('div')].find(x => x.textContent.startsWith('Time for your')); if(b) b.textContent = b.textContent.replace('Time for your', `Time for ${PEOPLE[cur].name}’s`); }
  const others = Object.values(PEOPLE).filter(P => P.id !== cur).map(P => ({P, due:P.meds.filter(m => !P.taken[m.id] && m.t <= now)})).filter(x => x.due.length);
  let k = nt ? 1 : 0;
  others.forEach(({P, due}) => { const el = document.createElement('div'); el.className = 'notif2 other'; el.style.bottom = (120 + k*100) + 'px'; k++;
    el.innerHTML = `<div class="appi">${miniRing([1,1,1,1], 26, ['indigo','pink','brown','purple'])}</div><div style="flex:1;min-width:0">
      <div style="display:flex;justify-content:space-between;font-size:15px"><b style="font-weight:600">Hävn</b><span style="color:rgba(60,60,67,.6);font-size:13px">${due[0].t}</span></div>
      <div style="font-size:15px;font-weight:600;margin-top:1px">Time for ${P.name}’s ${due[0].t} dose</div>
      <div style="font-size:15px">${showNames ? due.map(m => m.n).join(', ') + (due.length > 1 ? ' are' : ' is') + ' due.' : `${due.length} medication${due.length > 1 ? 's are' : ' is'} due.`} Tap to open ${P.name}’s day.</div></div>`;
    el.onclick = () => { unlockTo(); setTimeout(() => switchPerson(P.id), reduce ? 0 : 300); };
    L.appendChild(el); });
  const P = PEOPLE[cur], ci = (P.checks || []).filter(c => c.d === 18 && !c.ans).slice(-1)[0];
  if(ci){ const el = document.createElement('div'); el.className = 'notif2 other'; el.style.bottom = (120 + k*100) + 'px'; k++;
    el.innerHTML = `<div class="appi">${miniRing([1,1,1,1], 26, ['indigo','pink','brown','purple'])}</div><div style="flex:1;min-width:0">
      <div style="display:flex;justify-content:space-between;font-size:15px"><b style="font-weight:600">Hävn</b><span style="color:rgba(60,60,67,.6);font-size:13px">${addHours(ci.at, 1)}</span></div>
      <div style="font-size:15px;font-weight:600;margin-top:1px">Did the ${showNames ? ci.n : 'as-needed dose'} help?</div><div style="font-size:15px">${cur === 'you' ? 'You' : P.name} took it at ${ci.at}. Hold to answer.</div></div>`;
    el.onclick = () => lockAnswer(el, ci); L.appendChild(el); }
};
function lockAnswer(el, ci){
  const L = $('#lockScene'); if(L.querySelector('.nact2')) return; haptic(el, 'press haptic');
  const act = document.createElement('div'); act.className = 'nact2'; act.style.bottom = (parseFloat(el.style.bottom) - 160) + 'px';
  act.innerHTML = ['Better', 'About the Same', 'Worse'].map(a => `<button data-lans="${a}">${a}</button>`).join('');
  if(parseFloat(act.style.bottom) < 20){ act.style.bottom = 'auto'; act.style.top = (874 - parseFloat(el.style.bottom) + 8) + 'px'; }
  L.appendChild(act); if(!reduce) act.animate([{opacity:0, transform:'scale(.9)'}, {opacity:1, transform:'none'}], {duration:320, easing:'cubic-bezier(.2,.9,.25,1.1)'});
  act.onclick = e => { const b = e.target.closest('[data-lans]'); if(!b) return; ci.ans = b.dataset.lans; haptic(b, 'success haptic'); act.remove();
    if(!reduce) el.animate([{opacity:1}, {opacity:0, transform:'translateX(-110%)'}], {duration:340, easing:'cubic-bezier(.4,0,.2,1)', fill:'forwards'}).onfinish = () => drawLock(); else drawLock(); };
}

/* ---------- "did it help?" after an as-needed dose ---------- */
const CHECKS0 = {you:[{d:3, at:'19:20', n:'Ibuprofen', ans:'Better'}, {d:9, at:'12:40', n:'Ibuprofen', ans:'About the Same'}, {d:14, at:'21:15', n:'Ibuprofen', ans:'Better'}],
  mum:[{d:2, at:'09:10', n:'Paracetamol', ans:'Better'}, {d:10, at:'16:30', n:'Paracetamol', ans:'About the Same'}, {d:14, at:'08:20', n:'Paracetamol', ans:'Better'}, {d:14, at:'14:05', n:'Paracetamol', ans:'Better'}]};
const ANS = ['Better', 'About the Same', 'Worse'];
function afterPrn(m){
  const P = PEOPLE[cur]; (P.checks = P.checks || []).push({d:18, at:now, id:m.id, n:m.n, ans:null});
  const row = document.querySelector(`#scroll [data-prn="${m.id}"]`); if(!row) return;
  const old = row.nextElementSibling; if(old && old.classList.contains('ci')) old.remove();
  row.insertAdjacentHTML('afterend', ciHTML(m)); const el = row.nextElementSibling;
  if(!reduce) el.animate([{opacity:0, height:'0px'}, {opacity:1, height:el.offsetHeight + 'px'}], {duration:420, delay:300, easing:'cubic-bezier(.2,.9,.25,1)', fill:'backwards'});
}
function ciHTML(m){ const c = (PEOPLE[cur].checks || []).filter(x => x.d === 18 && x.n === m.n && !x.ans).slice(-1)[0]; if(!c) return '';
  return `<div class="ci" data-ci="${m.n}"><div class="dt">Did it help? Answer now, or when Hävn asks at ${addHours(c.at, 1)}.</div><div class="chips ans">${ANS.map(a => `<button data-ans="${a}">${a}</button>`).join('')}</div></div>`; }
const extraSections0 = extraSections;
extraSections = () => { let h = extraSections0(); PRN.forEach(m => { const c = ciHTML(m); if(c) h = h.replace(`data-prnlog="${m.id}">Log</button></div>`, `data-prnlog="${m.id}">Log</button></div>${c}`); }); return h; };
$('#app').addEventListener('click', e => {
  const b = e.target.closest('[data-ans]'); if(!b) return;
  const box = b.closest('.ci'), c = (PEOPLE[cur].checks || []).filter(x => x.d === 18 && x.n === box.dataset.ci && !x.ans).slice(-1)[0]; if(!c) return;
  c.ans = b.dataset.ans; b.classList.add('on'); haptic(b, 'selection haptic'); Snd.play('on');
  setTimeout(() => { box.innerHTML = `<div class="dt" style="color:var(--green)">${c.ans}. Saved for your report.</div>`;
    setTimeout(() => { if(reduce) return box.remove(); box.animate([{opacity:1, height:box.offsetHeight + 'px'}, {opacity:0, height:'0px', paddingBottom:'0px'}], {duration:380, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = () => box.remove(); }, 1400); }, 220);
});

/* ---------- readings next to the medications ---------- */
const KIND = {bp:{name:'Blood Pressure', unit:'mmHg'}, glu:{name:'Blood Sugar', unit:'mmol/L'}, wt:{name:'Weight', unit:'kg'}};
const READ0 = {
  mum:[[1,146,90],[3,144,91],[5,143,88],[7,141,87],[9,138,86],[11,136,85],[13,134,84],[15,131,82],[17,129,81]].map(([d, a, b]) => ({d, t:'08:30', k:'bp', v:[a, b], src:'Health'})),
  you:[[2,7.6],[4,7.4],[6,7.5],[9,7.1],[11,7.0],[13,6.9],[16,6.8]].map(([d, v]) => ({d, t:'07:40', k:'glu', v, src:'typed'})),
};
YOU_MEDS.find(m => m.id === 'met').track = 'glu'; MUM_MEDS.find(m => m.id === 'amlo').track = 'bp';
const fmtVal = (k, v) => k === 'bp' ? `${v[0]}/${v[1]}` : k === 'glu' ? v.toFixed(1) : v.toFixed(1);
const readsOf = (id = cur) => PEOPLE[id].read || (PEOPLE[id].read = []);
function spark(k, rs, w = 320, h = 84){
  if(!rs.length) return '';
  const vals = rs.flatMap(r => k === 'bp' ? r.v : [r.v]), lo = Math.min(...vals), hi = Math.max(...vals), pad = (hi - lo) * .25 || 1;
  const x = d => 12 + (d - 1) / 17 * (w - 24), y = v => h - 10 - (v - (lo - pad)) / ((hi + pad) - (lo - pad)) * (h - 20);
  const line = (f, col) => `<polyline points="${rs.map(r => `${x(r.d).toFixed(1)},${y(f(r)).toFixed(1)}`).join(' ')}" fill="none" stroke="${col}" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>`
    + rs.map(r => `<circle cx="${x(r.d).toFixed(1)}" cy="${y(f(r)).toFixed(1)}" r="3" fill="var(--cell)" stroke="${col}" stroke-width="2"/>`).join('');
  return `<svg class="spark" viewBox="0 0 ${w} ${h}" preserveAspectRatio="none" role="img" aria-label="${KIND[k].name} since 1 November">
    <line x1="${x(18)}" y1="4" x2="${x(18)}" y2="${h - 4}" stroke="var(--sep)" stroke-dasharray="3 3"/>
    ${k === 'bp' ? line(r => r.v[0], 'var(--pink)') + line(r => r.v[1], 'var(--accent)') : line(r => r.v, k === 'glu' ? 'var(--orange)' : 'var(--accent)')}</svg>`;
}
function readingsCard(){
  const R = readsOf(); if(!R.length) return `<div class="card" style="margin-top:12px;padding:16px 18px"><div style="display:flex;gap:12px;align-items:center"><span class="tile" style="background:#FF2D55">${T_HEART}</span>
    <div class="mid"><div class="nm">Readings</div><div class="dt">Keep blood pressure, blood sugar or weight next to your doses.</div></div></div><button class="btn2 g" data-addread="bp" style="margin-top:14px">Add a Reading</button></div>`;
  return [...new Set(R.map(r => r.k))].map(k => { const rs = R.filter(r => r.k === k), last = rs[rs.length - 1];
    return `<div class="card" style="margin-top:12px;padding:14px 16px 12px"><div style="display:flex;justify-content:space-between;align-items:center"><div class="nm">${KIND[k].name}</div><button class="pbtn ghost sm" data-addread="${k}">Add</button></div>
      <div style="display:flex;align-items:baseline;gap:6px;margin-top:2px"><b style="font:600 28px var(--rounded)">${fmtVal(k, last.v)}</b><span class="dt">${KIND[k].unit} · ${last.d === 18 ? 'today, ' + last.t : last.d + ' Nov'}</span></div>
      ${spark(k, rs)}<div class="dt" style="font-size:13px;line-height:17px">${rs.length} readings since 1 November${k === 'bp' ? ' · <span style="color:var(--pink)">●</span> upper <span style="color:var(--accent)">●</span> lower' : ''}. From ${rs.some(r => r.src === 'Health') ? 'Apple Health' : 'what you typed'}.</div></div>`; }).join('');
}
let rdKind = 'bp';
function openReading(k){
  rdKind = KIND[k] ? k : 'bp'; const sh = appSheet('');
  const draw = () => { sh.innerHTML = `<div class="grab"></div><h3>Add a Reading</h3><p class="sub">${cur === 'you' ? '' : PEOPLE[cur].name + ' · '}Today, ${now}</p>
    <div class="seg">${Object.keys(KIND).map(x => `<button class="${x === rdKind ? 'on' : ''}" data-rk="${x}">${KIND[x].name.replace('Blood ', '')}</button>`).join('')}</div>
    <div class="bignum rdin" style="margin-top:14px">${rdKind === 'bp' ? '<input id="rdA" inputmode="numeric" placeholder="120" aria-label="Upper number"><span>/</span><input id="rdB" inputmode="numeric" placeholder="80" aria-label="Lower number">'
      : `<input id="rdA" inputmode="decimal" placeholder="${rdKind === 'glu' ? '5.5' : '70.0'}" aria-label="${KIND[rdKind].name}">`}</div>
    <div class="dt" style="text-align:center">${KIND[rdKind].unit}</div><div class="warn" id="rdWarn" style="text-align:center"></div>
    <button class="btn2 p" data-rdsave="1" style="margin-top:14px">Save</button><button class="ob-link" data-close="1">Cancel</button>
    <p class="foot" style="text-align:center">Readings stay on this iPhone and go in your report. Hävn doesn’t interpret them; your doctor does.</p>`;
    setTimeout(() => { const a = sh.querySelector('#rdA'); a && a.focus({preventScroll:true}); }, reduce ? 0 : 400); };
  draw();
  sh.onclick = e => {
    if(e.target.closest('[data-close]')) return closeAppSheet();
    const kb = e.target.closest('[data-rk]'); if(kb){ rdKind = kb.dataset.rk; draw(); return; }
    if(!e.target.closest('[data-rdsave]')) return;
    const a = parseFloat((sh.querySelector('#rdA').value || '').replace(',', '.')), b = rdKind === 'bp' ? parseFloat(sh.querySelector('#rdB').value) : 0;
    const ok = rdKind === 'bp' ? a >= 60 && a <= 260 && b >= 30 && b <= 160 && a > b : rdKind === 'glu' ? a >= 1 && a <= 35 : a >= 20 && a <= 300;
    if(!ok){ const w = sh.querySelector('#rdWarn'); w.textContent = rdKind === 'bp' ? 'Check both numbers, like 120 / 80.' : 'Check the number.'; haptic(w, 'error haptic'); return; }
    readsOf().push({d:18, t:now, k:rdKind, v:rdKind === 'bp' ? [Math.round(a), Math.round(b)] : Math.round(a*10)/10, src:'typed'});
    Snd.play('on'); closeAppSheet(() => { drawHistory(); toast('Reading saved'); });
  };
}
$('#scrHistory').addEventListener('click', e => {
  const r = e.target.closest('[data-addread]'); if(r) return openReading(r.dataset.addread);
  if(e.target.closest('[data-recapshare]')) return shareRecap();
});
/* the medication's own page shows the reading it's for */
const detailHTML1 = detailHTML;
detailHTML = m => {
  let h = detailHTML1(m); const rs = m.track ? readsOf().filter(r => r.k === m.track) : [];
  if(!rs.length) return h;
  const card = `<div class="card" style="margin-top:14px;padding:14px 16px 10px"><div style="display:flex;justify-content:space-between;align-items:baseline"><span class="nm" style="font-size:16px">${KIND[m.track].name}</span><span class="dt">${fmtVal(m.track, rs[rs.length - 1].v)} ${KIND[m.track].unit}</span></div>
    ${spark(m.track, rs)}<div class="dt" style="font-size:13px;line-height:17px">Readings since 1 November, next to the doses. Hävn doesn’t interpret them.</div></div>`;
  const i = h.indexOf('<div class="card" style="margin-top:14px">'); return i < 0 ? h + card : h.slice(0, i) + card + h.slice(i);
};

/* ---------- how you feel: mood, energy and pain, 1 to 5, in a day's note ---------- */
const SCALES = [['mood', 'Mood'], ['energy', 'Energy'], ['pain', 'Pain']];
SAMPLE_NOTES.you.scales = {3:{mood:3, energy:2}, 7:{mood:2, energy:2}, 9:{mood:3, energy:3}, 14:{mood:3, energy:3}, 16:{mood:2, energy:3, pain:3}, 17:{mood:4, energy:4}};
SAMPLE_NOTES.mum.scales = {2:{pain:3, mood:3}, 5:{pain:2}, 9:{pain:3, mood:3}, 12:{pain:2, mood:4}, 14:{pain:2, mood:4}, 17:{pain:1, mood:4}};
let scDraft = {};
const scalesOf = d => (notesOf().scales || {})[d] || {};
function scalesHTML(){ return SCALES.map(([k, l]) => `<div class="scl"><span>${l}</span><div class="dots5" role="radiogroup" aria-label="${l}">${[1,2,3,4,5].map(v =>
  `<button class="${scDraft[k] === v ? 'on' : ''}" data-sc="${k}:${v}" role="radio" aria-checked="${scDraft[k] === v}" aria-label="${l} ${v} of 5">${v}</button>`).join('')}</div></div>`).join('')
  + '<div class="dt" style="font-size:12px;line-height:16px;padding:2px 0 8px">1 is low and 5 is high. For pain, 5 is the worst.</div>'; }
const openNote0 = openNote;
openNote = d => { openNote0(d); scDraft = {...scalesOf(d)}; $('#noteScales').innerHTML = scalesHTML(); };
$('#noteScales').addEventListener('click', e => { const b = e.target.closest('[data-sc]'); if(!b) return; const [k, v] = b.dataset.sc.split(':');
  scDraft[k] = scDraft[k] === +v ? undefined : +v; if(scDraft[k] === undefined) delete scDraft[k];
  $('#noteScales').innerHTML = scalesHTML(); haptic(b, 'selection haptic'); });
function saveScales(d){ const N = notesOf(); N.scales = N.scales || {}; if(Object.keys(scDraft).length) N.scales[d] = {...scDraft}; else delete N.scales[d]; return Object.keys(scDraft).length > 0; }
const scaleLine = s => SCALES.filter(([k]) => s[k]).map(([k, l]) => `${l} ${s[k]}/5`).join(' · ');
const dayNoteHTML0 = dayNoteHTML;
dayNoteHTML = d => { const s = scaleLine(scalesOf(d)); return dayNoteHTML0(d) + (s ? `<div class="dt" style="padding:0 16px 12px">${s}</div>` : ''); };
const notesSection0 = notesSection;
notesSection = () => { const s = scaleLine(scalesOf(18)), h = notesSection0(); return s ? h.replace(/<\/div><\/div>$/, `<div class="dt" style="padding:0 16px 12px 66px">${s}</div></div></div>`) : h; };

/* ---------- the month in rings: a recap you can keep ---------- */
function octDays(){ return Array.from({length:31}, (_, i) => recordsFor(new Date(2026, 9, i + 1))); }
function recapStats(days){
  let tot = 0, tk = 0, run = 0, best = 0, worst = null;
  days.forEach((recs, i) => { const t = recs.filter(r => r.v).length; tot += recs.length; tk += t;
    if(t === recs.length){ run++; best = Math.max(best, run); } else { run = 0; if(!worst || t/recs.length < worst.f) worst = {d:i + 1, f:t/recs.length}; } });
  return {pct:tot ? Math.round(tk/tot*100) : 0, best, worst};
}
function recapCard(){
  if(!Object.keys(hist()).length || stash) return ''; const days = octDays(); if(!days[0]) return '';
  const S = recapStats(days), wd = S.worst && new Date(2026, 9, S.worst.d).toLocaleDateString('en-GB', {weekday:'long'});
  return `<div class="card" style="margin-top:12px;padding:16px 16px 14px" id="recap"><div class="dt" style="font-size:13px;font-weight:600;letter-spacing:.3px">${cur === 'you' ? 'YOUR' : PEOPLE[cur].name.toUpperCase() + '’S'} OCTOBER</div>
    <div style="display:flex;align-items:baseline;gap:8px;margin-top:2px"><b style="font:700 34px var(--rounded)">${S.pct}%</b><span class="dt">of doses taken</span></div>
    <div class="mosaic">${days.map(recs => miniRing(recs.map(r => r.v), 26, recs.map(r => r.c))).join('')}</div>
    <div class="dt" style="margin-top:10px">Longest run: <b style="color:var(--label);font-weight:600">${S.best} days</b> with every dose.${S.worst ? ` Hardest day: ${wd} ${S.worst.d} October.` : ''}</div>
    <button class="btn2 g" data-recapshare="1" style="margin-top:12px">Share as Image</button></div>`;
}
function shareRecap(){
  const days = octDays(), S = recapStats(days);
  const sh = appSheet(`<h3>October in Rings</h3><p class="sub">The image shows rings only, never medication names.</p>
    <div class="recapimg"><div style="display:flex;justify-content:space-between;align-items:center"><span style="display:flex;gap:6px;align-items:center;font-weight:600">${miniRing([1,1,1,1], 20, ['indigo','pink','brown','purple'])}Hävn</span><span style="opacity:.7;font-size:13px">October 2026</span></div>
      <div style="font:700 44px/1.1 var(--rounded);margin-top:14px">${S.pct}%</div><div style="opacity:.75;font-size:14px">of doses taken · longest run ${S.best} days</div>
      <div class="mosaic" style="margin-top:14px">${days.map(recs => miniRing(recs.map(r => r.v), 24, recs.map(r => r.c))).join('')}</div></div>
    <div style="display:flex;gap:8px;margin-top:14px"><button class="btn2 g" data-rsave="1">Save Image</button><button class="btn2 p" data-rsend="1">Share…</button></div><button class="ob-link" data-close="1">Cancel</button>`);
  if(!reduce) sh.querySelector('.recapimg').animate([{opacity:0, transform:'scale(.92) rotate(-2deg)'}, {opacity:1, transform:'none'}], {duration:560, delay:160, easing:'cubic-bezier(.2,.9,.25,1.1)', fill:'backwards'});
  sh.addEventListener('click', e => { if(e.target.closest('[data-rsave]')) closeAppSheet(() => toast('Saved to Photos')); if(e.target.closest('[data-rsend]')) closeAppSheet(() => toast('Choose where to send it')); }, {once:true});
}
const drawHistory0 = drawHistory;
drawHistory = () => {
  drawHistory0(); const rc = $('#scrHistory [data-report]'), card = rc && rc.closest('.card'), h = (stash ? '' : readingsCard()) + recapCard();
  if(card) card.insertAdjacentHTML('beforebegin', h); else $('#scrHistory').insertAdjacentHTML('beforeend', h);
};

/* ---------- the report: a third page for readings and how you felt ---------- */
const reportPages0 = reportPages;
reportPages = (opts = {}) => {
  const pages = reportPages0(opts); if(opts.extras === false) return pages;
  const p3 = extrasPage(opts); if(!p3) return pages;
  return [pages[0].replace('Page 1 of 2', 'Page 1 of 3'), pages[1].replace('Page 2 of 2', 'Page 2 of 3'), p3];
};
function extrasPage({days = 30, name = ''} = {}){
  const start = new Date(2026, 10, 17 - days + 1), inP = d => new Date(2026, 10, d) >= start && d <= 17;
  const R = readsOf().filter(r => inP(r.d)), sc = Object.entries(notesOf().scales || {}).filter(([d]) => inP(+d));
  if(!R.length && !sc.length) return '';
  const nm = cleanNote(name) || (cur === 'you' ? '' : PEOPLE[cur].name), logo = `<div class="brand">${rRing([1,1,1,1], ['indigo','pink','brown','purple'], 22)}Hävn</div>`;
  const HX = {bp:['#FF2D55', '#007A8F'], glu:['#FF9500'], wt:['#007A8F']};
  const chart = (k, rs) => { const W = 674, H = 170, vals = rs.flatMap(r => k === 'bp' ? r.v : [r.v]), lo = Math.min(...vals), hi = Math.max(...vals), pad = (hi - lo)*.2 || 1;
    const x = d => 40 + (d - 1)/16*(W - 60), y = v => H - 22 - (v - (lo - pad))/((hi + pad) - (lo - pad))*(H - 40);
    const ticks = [lo - pad, (lo + hi)/2, hi + pad].map(v => `<text x="30" y="${y(v) + 4}" text-anchor="end" font-size="10" fill="#8A9599">${k === 'bp' ? Math.round(v) : v.toFixed(1)}</text><line x1="36" x2="${W}" y1="${y(v)}" y2="${y(v)}" stroke="#EEF1F2"/>`).join('');
    const line = (f, c) => `<polyline points="${rs.map(r => `${x(r.d)},${y(f(r))}`).join(' ')}" fill="none" stroke="${c}" stroke-width="2"/>` + rs.map(r => `<circle cx="${x(r.d)}" cy="${y(f(r))}" r="3.2" fill="#fff" stroke="${c}" stroke-width="2"/>`).join('');
    const days2 = [1, 5, 9, 13, 17].map(d => `<text x="${x(d)}" y="${H - 4}" text-anchor="middle" font-size="10" fill="#8A9599">${d} Nov</text>`).join('');
    return `<svg width="${W}" height="${H}" viewBox="0 0 ${W} ${H}">${ticks}${days2}${k === 'bp' ? line(r => r.v[0], HX.bp[0]) + line(r => r.v[1], HX.bp[1]) : line(r => r.v, HX[k][0])}</svg>`; };
  const kinds = [...new Set(R.map(r => r.k))];
  const readings = kinds.map(k => { const rs = R.filter(r => r.k === k);
    return `<h2>${KIND[k].name}<small>${KIND[k].unit} · ${rs.length} readings${rs.some(r => r.src === 'Health') ? ' · from Apple Health' : ''}</small></h2>${chart(k, rs)}
      ${k === 'bp' ? '<div class="legend"><span><i style="background:#FF2D55"></i>Upper (systolic)</span><span><i style="background:#007A8F"></i>Lower (diastolic)</span></div>' : ''}
      <table style="margin-top:10px"><tr>${rs.slice(-6).map(r => `<th>${r.d} Nov</th>`).join('')}</tr><tr>${rs.slice(-6).map(r => `<td><b style="font-weight:600">${fmtVal(k, r.v)}</b></td>`).join('')}</tr></table>`; }).join('');
  const dset = Array.from({length:Math.min(days, 30)}, (_, i) => 17 - Math.min(days, 30) + 1 + i).filter(d => d >= 1);
  const S = notesOf().scales || {}, col = (k, v) => k === 'pain' ? ['#E8F5EC', '#C9E9D2', '#FFE2B8', '#FFB86B', '#F2683C'][v - 1] : ['#F2683C', '#FFB86B', '#FFE2B8', '#C9E9D2', '#7DCB93'][v - 1];
  const used = SCALES.filter(([k]) => sc.some(([, s]) => s[k]));
  const feel = used.length ? `<h2>How ${cur === 'you' ? 'you' : nm ? escNote(nm) : 'they'} felt<small>rated 1 to 5 in Hävn’s daily note</small></h2>
    <div class="mx" style="grid-template-columns:70px repeat(${dset.length},1fr)"><span></span>${dset.map(d => `<span class="mh">${d}</span>`).join('')}
      ${used.map(([k, l]) => `<span class="ml">${l}</span>${dset.map(d => { const v = (S[d] || {})[k]; return `<span class="mc" style="background:${v ? col(k, v) : '#F4F6F7'}">${v || ''}</span>`; }).join('')}`).join('')}</div>
    <div class="legend"><span>Numbers are the person’s own rating. Mood and energy: 5 is best. Pain: 5 is worst. Blank: not rated.</span></div>` : '';
  return `<div class="page"><style>.rpt .mx{display:grid;gap:3px;align-items:center;font-size:10px}.rpt .mh{text-align:center;color:#8A9599}.rpt .ml{font-size:11.5px;font-weight:600}
      .rpt .mc{height:20px;border-radius:5px;display:grid;place-items:center;font-size:10px;font-weight:600;color:#14191B}</style>
    <div class="hd"><div>${logo}<h1 style="font-size:22px;line-height:28px">Readings and how ${cur === 'you' ? 'you' : 'they'} felt</h1></div><div class="meta">${nm ? escNote(nm) + '<br>' : ''}${fmt(start, {day:'numeric', month:'short'})} – 17 Nov 2026</div></div>
    ${readings || '<p class="muted" style="margin-top:20px">No readings in this period.</p>'}${feel}
    <div class="disc">Readings are as typed in Hävn or read from Apple Health. Hävn shows them next to the doses and doesn’t interpret them.</div>
    <div class="ft"><span>Made with Hävn on this iPhone · Wednesday 18 November 2026</span><span>Page 3 of 3</span></div></div>`;
}

/* ---------- every person starts with their sample readings and check-ins ---------- */
const freshPerson1 = freshPerson;
freshPerson = id => { const P = freshPerson1(id); P.read = (READ0[id] || []).map(r => ({...r, v:Array.isArray(r.v) ? [...r.v] : r.v})); P.checks = (CHECKS0[id] || []).map(c => ({...c})); return P; };
