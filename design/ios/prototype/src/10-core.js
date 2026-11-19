
/* ================= people, as-needed doses, alerts, the people menu ================= */
const CHEV = '<svg class="chev" width="8" height="14" viewBox="0 0 8 14"><path d="M1.5 1.5L6.5 7l-5 5.5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
const PRNICON = '<svg width="14" height="14" viewBox="0 0 14 14"><circle cx="7" cy="7" r="6" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M7 4v6M4 7h6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>';
GICON.Evening = '<svg width="16" height="13" viewBox="0 0 16 13"><path d="M3 10a5 5 0 0 1 10 0" fill="currentColor"/><path d="M0.5 12h15M8 1v2.6M8 3.6L6.3 2M8 3.6L9.7 2" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/></svg>';
const groupOf = t => t < '11:00' ? 'Morning' : t < '17:00' ? 'Afternoon' : t < '21:00' ? 'Evening' : 'Night';

const YOU_MEDS = MEDS.map(m => ({...m, form:'Tablet', sched:'Every day', left:{met:42, vit:6, iron:18, mag:27}[m.id]}));
YOU_MEDS.find(m => m.id==='vit').form = 'Softgel'; YOU_MEDS.find(m => m.id==='mag').form = 'Capsule';
const MUM_MEDS = [
  {id:'levo', n:'Levothyroxine', d:'50 mcg', t:'07:00', c:'blue',   p:{k:'round', a:'white'},  g:'Morning',   form:'Tablet', sched:'Every day', left:64},
  {id:'amlo', n:'Amlodipine',    d:'5 mg',   t:'08:00', c:'orange', p:{k:'diamond', a:'white'}, g:'Morning',  form:'Tablet', sched:'Every day', left:21},
  {id:'calc', n:'Calcium + D3',  d:'1 tablet', t:'12:30', c:'indigo', p:{k:'caplet', a:'cream'}, g:'Afternoon', form:'Tablet', sched:'Every day', left:8},
  {id:'ator', n:'Atorvastatin',  d:'20 mg',  t:'21:00', c:'red',    p:{k:'oval', a:'white'},   g:'Night',     form:'Tablet', sched:'Every day', left:30},
];
const PRN0 = {
  you:[{id:'ibu', n:'Ibuprofen', d:'400 mg', c:'blue', p:{k:'oval', a:'white'}, max:3, gap:4, left:12, form:'Tablet', sched:'As needed'}],
  mum:[{id:'para', n:'Paracetamol', d:'500 mg', c:'gray', p:{k:'caplet', a:'white'}, max:4, gap:4, left:16, form:'Tablet', sched:'As needed'}],
};
let PEOPLE = {}, cur = 'you', PRN = [], prnLog = {}, EXTRA = [], ARCH = [];
function freshPerson(id){
  const base = {you:{name:'You', color:'indigo', meds:YOU_MEDS, taken:{met:'08:04', vit:'08:04'}, order:['met','vit']},
                mum:{name:'Mum', color:'purple', meds:MUM_MEDS, taken:{levo:'07:02', amlo:'08:10'}, order:['levo','amlo']}}[id];
  return {id, name:base.name, color:base.color, meds:base.meds.map(m => ({...m, p:{...m.p}})), prn:(PRN0[id]||[]).map(m => ({...m, p:{...m.p}})),
    taken:{...base.taken}, order:[...base.order], history:[], prnLog:{}, extra:[], hist:makeHist(base.meds, id), arch:[{n:'Amoxicillin', d:'500 mg · course ended 2 Oct'}, {n:'Prednisolone', d:'5 mg · course ended 14 Aug'}]};
}
function resetPeople(){ PEOPLE = {you:freshPerson('you'), mum:freshPerson('mum')}; applyPerson('you'); }
function saveCur(){ const P = PEOPLE[cur]; if(!P) return; Object.assign(P, {meds:MEDS, prn:PRN, taken, order, history, prnLog, extra:EXTRA, arch:ARCH}); }
function applyPerson(id){ const P = PEOPLE[id]; cur = id;
  MEDS = P.meds; PRN = P.prn; taken = P.taken; order = P.order; history = P.history; prnLog = P.prnLog; EXTRA = P.extra; ARCH = P.arch; }
const AVATAR_YOU = $('#openSettings').innerHTML;
function avatarHTML(P, size=30){
  return P.id==='you' ? `<svg width="${size}" height="${size}" viewBox="0 0 30 30"><circle cx="15" cy="15" r="13" fill="var(--l2)"/><circle cx="15" cy="12" r="4.6" fill="var(--cell)"/><path d="M6.8 23.8c1.8-3.4 4.8-5.2 8.2-5.2s6.4 1.8 8.2 5.2A12.9 12.9 0 0 1 15 28a12.9 12.9 0 0 1-8.2-4.2z" fill="var(--cell)"/></svg>`
    : `<span class="av" style="width:${size}px;height:${size}px;border-radius:${size/2}px;background:var(--${P.color})">${P.name[0]}</span>`;
}
function dateLine(){ return (cur==='you' ? '' : PEOPLE[cur].name + ' · ') + 'Wednesday, 18 November'; }

/* the Today extras: as-needed medicines and anything running low */
function prnCount(m){ return (prnLog[m.id]||[]).length; }
const addHours = (t, h) => { let [H, M] = t.split(':').map(Number); H = Math.min(23, H + h); return String(H).padStart(2,'0') + ':' + String(M).padStart(2,'0'); };
function prnSub(m){
  const n = prnCount(m), last = (prnLog[m.id]||[]).slice(-1)[0];
  if(!n) return `${m.d} · up to ${m.max} a day`;
  if(n >= m.max) return `${n} of ${m.max} today · <span style="color:var(--orange-text)">daily limit reached</span>`;
  return `${n} of ${m.max} today · last at ${last}`;
}
function extraSections(){
  if(!MEDS.length && !PRN.length) return emptyToday();
  let h = '';
  if(PRN.length) h += `<div class="sec rv"><div class="shead"><span class="t">${PRNICON}As Needed</span></div><div class="card">${PRN.map(m => `
    <div class="lrow" data-prn="${m.id}">${icon(m)}<div class="mid"><div class="nm">${m.n}</div><div class="dt swap" data-prndt="${m.id}"><span>${prnSub(m)}</span></div></div>
    <button class="pbtn ghost sm" data-prnlog="${m.id}">Log</button></div>`).join('')}</div></div>`;
  h += notesSection();
  const low = MEDS.filter(m => m.left < 10);
  if(low.length) h += `<div class="sec rv"><div class="shead"><span class="t">${LOWICON}Running Low</span></div><div class="card">${low.map(m => `
    <div class="lrow" data-detail="${m.id}">${icon(m)}<div class="mid"><div class="nm">${m.n}</div><div class="dt">${m.left} left · about ${m.left} days</div></div>${CHEV}</div>`).join('')}</div></div>`;
  return h;
}
function emptyToday(){
  return `<div class="sec rv"><div class="card" style="padding:26px 20px 20px;text-align:center">
    <div style="display:grid;place-items:center;margin-bottom:10px">${pillSVG({k:'capsule', a:'white', b:'gray'}, 44)}</div>
    <div class="nm">No Medications</div><div class="dt" style="margin:4px 10px 16px">Your doses will appear here, grouped by morning, afternoon, evening and night.</div>
    <button class="btn2 p" data-add="1">Add Medication</button>
    <button class="btn2 g" style="margin-top:8px" data-import="1">Import from Apple Health</button></div></div>`;
}
function logPrn(id, btn){
  const m = PRN.find(x => x.id===id); if(!m) return;
  const n = prnCount(m), last = (prnLog[id]||[]).slice(-1)[0];
  if(n >= m.max){ btn.classList.remove('nudge'); btn.offsetWidth; btn.classList.add('nudge'); haptic(btn, 'error haptic'); return; }
  const go = () => {
    (prnLog[id] = prnLog[id] || []).push(now); m.left = Math.max(0, m.left - 1);
    btn.classList.add('pressed'); setTimeout(() => btn.classList.remove('pressed'), 160);
    Snd.play('tap'); setTimeout(() => Snd.play('taken'), 140); haptic(btn, 'success haptic');
    swap(document.querySelector(`[data-prndt="${id}"]`), prnSub(m));
    btn.textContent = 'Logged'; setTimeout(() => { btn.textContent = 'Log'; }, 1600);
  };
  if(last && addHours(last, m.gap) > now)
    showAlert({title:`Log ${m.n} again?`, body:`Your last dose was at ${last}. The usual gap is ${m.gap} hours, so the next one is due after ${addHours(last, m.gap)}.`,
      buttons:[{label:'Cancel'}, {label:'Log Anyway', style:'def', onTap:go}]});
  else go();
}

/* ring and count for any number of doses */
function rebuildRingParts(){
  $('#dots').innerHTML = MEDS.map((m, i) => `<div class="dot" data-i="${i}"></div>`).join('');
  $('#reel').innerHTML = Array.from({length: MEDS.length + 1}, (_, i) => `<span>${i}</span>`).join('');
  $('#ofN').textContent = MEDS.length;
  $('.big').classList.toggle('none', !MEDS.length);
  $('#bigEmpty').hidden = !!MEDS.length; $('#bigCount').style.display = MEDS.length ? 'flex' : 'none';
}

/* ---------- system alert ---------- */
function showAlert({title, body, buttons}){
  const app = $('#app'), dim = document.createElement('div'), a = document.createElement('div');
  dim.className = 'adim'; a.className = 'alert glass'; a.setAttribute('role', 'alertdialog');
  a.innerHTML = `<h4>${title}</h4><p>${body}</p><div class="btns ${buttons.length > 2 ? 'col' : ''}">${buttons.map((b, i) => `<button class="${b.style||''}" data-i="${i}">${b.label}</button>`).join('')}</div>`;
  app.append(dim, a);
  if(!reduce){ a.animate([{opacity:0, transform:'translate(-50%,-50%) scale(1.12)'}, {opacity:1, transform:'translate(-50%,-50%) scale(1)'}], {duration:380, easing:'cubic-bezier(.2,.9,.25,1.1)'});
    dim.animate([{opacity:0}, {opacity:1}], {duration:250}); }
  a.addEventListener('click', e => { const b = e.target.closest('button'); if(!b) return;
    const spec = buttons[+b.dataset.i];
    const done = () => { a.remove(); dim.remove(); spec.onTap && spec.onTap(); };
    if(reduce) return done();
    dim.animate([{opacity:1}, {opacity:0}], {duration:200, fill:'forwards'});
    a.animate([{opacity:1, transform:'translate(-50%,-50%) scale(1)'}, {opacity:0, transform:'translate(-50%,-50%) scale(.94)'}], {duration:200, fill:'forwards'}).onfinish = done;
  });
}
function toast(text){
  const t = document.createElement('div'); t.className = 'toast glass'; t.textContent = text; $('#app').appendChild(t);
  t.animate([{opacity:0, transform:'translate(-50%,-14px) scale(.9)'}, {opacity:1, transform:'translate(-50%,0) scale(1)'}], {duration:420, easing:'cubic-bezier(.2,.9,.25,1.15)'});
  setTimeout(() => t.animate([{opacity:1}, {opacity:0, transform:'translate(-50%,-10px)'}], {duration:280, fill:'forwards'}).onfinish = () => t.remove(), 1800);
}

/* ---------- the people menu: it grows out of the person button (Liquid Glass) ---------- */
function openPeopleMenu(){
  if($('.pmenu')) return closePeopleMenu();
  saveCur();
  const c = document.createElement('div'); c.className = 'catch'; c.onclick = closePeopleMenu;
  const m = document.createElement('div'); m.className = 'pmenu glass'; m.setAttribute('role', 'menu');
  m.innerHTML = Object.values(PEOPLE).map(P => `<div class="it" data-person="${P.id}" role="menuitem">${avatarHTML(P)}<span class="lbl">${P.name}</span>${P.id===cur ? '<svg class="ck" width="16" height="13" viewBox="0 0 16 13"><path d="M1.5 7l4.5 4.5L14.5 2" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>' : ''}</div>`).join('')
    + `<div class="sep"></div><div class="it" data-addperson="1" role="menuitem"><span class="tile" style="background:var(--fill3);color:var(--accent)">${PRNICON}</span><span class="lbl">Add Person</span></div>
       <div class="it" data-settings="1" role="menuitem"><span class="tile" style="background:var(--gray)"><svg width="17" height="17" viewBox="0 0 24 24"><path fill="#fff" d="M12 8.5a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7zm9 3.5-.1-1.2 2-1.6-2-3.4-2.4.9a8 8 0 0 0-2-1.2L16 3h-4l-.5 2.5a8 8 0 0 0-2 1.2l-2.4-.9-2 3.4 2 1.6L7 12l.1 1.2-2 1.6 2 3.4 2.4-.9a8 8 0 0 0 2 1.2L12 21h4l.5-2.5a8 8 0 0 0 2-1.2l2.4.9 2-3.4-2-1.6z" transform="translate(-2 0) scale(.92)"/></svg></span><span class="lbl">Settings</span></div>`;
  $('#app').append(c, m);
  if(!reduce){
    m.animate([{opacity:0, transform:'scale(.18)', filter:'blur(6px)', borderRadius:'22px'}, {opacity:1, transform:'scale(1)', filter:'blur(0)', borderRadius:'26px'}],
      {duration:480, easing:'cubic-bezier(.2,.95,.25,1.04)'});
    [...m.querySelectorAll('.it')].forEach((it, i) => it.animate([{opacity:0, transform:'translateY(-6px)'}, {opacity:1, transform:'none'}], {duration:300, delay:80 + i*35, fill:'backwards', easing:'ease-out'}));
  }
  m.addEventListener('click', e => {
    const p = e.target.closest('[data-person]'), s = e.target.closest('[data-settings]'), ad = e.target.closest('[data-addperson]');
    if(p){ closePeopleMenu(() => switchPerson(p.dataset.person)); }
    else if(s){ closePeopleMenu(openSettings); }
    else if(ad){ closePeopleMenu(addPersonFlow); }
  });
}
function closePeopleMenu(after){
  const m = $('.pmenu'), c = $('.catch'); if(!m) return; c && c.remove();
  const done = () => { m.remove(); typeof after === 'function' && after(); };
  if(reduce) return done();
  m.animate([{opacity:1, transform:'scale(1)', filter:'blur(0)'}, {opacity:0, transform:'scale(.3)', filter:'blur(4px)'}], {duration:260, easing:'cubic-bezier(.4,0,.2,1)', fill:'forwards'}).onfinish = done;
}
function switchPerson(id){
  if(id === cur) return;
  saveCur(); applyPerson(id);
  $('#openSettings').innerHTML = avatarHTML(PEOPLE[id]);
  showTab(0, true); refreshToday(true);
  haptic($('#openSettings'), 'selection haptic');
}
let nextPersonN = 0;
function addPersonFlow(){
  const names = [['Alex', 'green'], ['Sam', 'orange'], ['Noor', 'pink']], [name, color] = names[nextPersonN++ % names.length];
  const id = 'p' + Date.now();
  PEOPLE[id] = {id, name, color, meds:[], prn:[], taken:{}, order:[], history:[], prnLog:{}, extra:[], arch:[], hist:{}};
  toast(`${name} added`);
  setTimeout(() => switchPerson(id), 500);
}
/* rebuild Today for the current person: the slow reveal plays again, so a different day reads as different */
function refreshToday(reveal){
  clearTimeout(accTimer);
  build(); rebuildRingParts(); $('#ring').classList.remove('closed');
  $('#scroll .lsub').textContent = dateLine();
  renderRing({from:0, silent:true}); setCount(doneCount(), true);
  $('#sm').innerHTML = `<span>${smText()}</span>`; $('#accSwap').innerHTML = accNext();
  drawBanner();
  if(reveal) entrance();
}
