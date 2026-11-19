
/* ================= Add Medication: six short steps in one sheet ================= */
const clean = s => (s || '').replace(/[<>&"'`]/g, '').replace(/\s+/g, ' ').trim().slice(0, 40);   // names never carry markup
const FORMS = [['Tablet','round'], ['Capsule','capsule'], ['Softgel','softgel'], ['Liquid','liquid'], ['Drops','drops'], ['Injection','pen'], ['Inhaler','inhaler'], ['Other','square']];
const UNITS = ['mg', 'mcg', 'g', 'mL', 'IU', '%'];
const SCHED = [['daily','Every Day'], ['days','On Specific Days'], ['every','Every Few Days'], ['cycle','In a Cycle'], ['prn','As Needed']];
const WD = ['M','T','W','T','F','S','S'], WDN = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];
const SHAPES8 = ['round','oval','caplet','capsule','softgel','square','diamond','triangle'];
let D = null, aStep = 0;
function unitWord(n){ const w = {Tablet:'tablet', Capsule:'capsule', Softgel:'softgel', Liquid:'mL', Drops:'drop', Injection:'injection', Inhaler:'puff', Other:'dose'}[D.form];
  return w === 'mL' ? 'mL' : w + (n === 1 ? '' : 's'); }
function schedText(){
  return D.sched === 'daily' ? 'Every day' : D.sched === 'days' ? D.days.map(i => WDN[i]).join(', ') || 'No days chosen'
    : D.sched === 'every' ? `Every ${D.every} days` : D.sched === 'cycle' ? `${D.on} days on, ${D.off} off` : `As needed, up to ${D.max} a day`;
}
function freeColour(){ const used = MEDS.map(m => m.c); return BGS.find(c => !used.includes(c)) || 'blue'; }
function openAdd(){
  closePeopleMenu(); if($('#settings').classList.contains('open')) closeSettings();
  D = {n:'', nick:'', form:'Tablet', strength:'', unit:'mg', sched:'daily', days:[0,2,4], every:2, on:21, off:7, max:3, gap:4,
       times:['20:00'], qty:1, remind:'Notification', ends:false, shape:'round', a:'white', b:'white', c:freeColour(), supply:30};
  aStep = 0;
  const sh = $('#addSheet'); $('#addSteps').innerHTML = ''; addPane(0, 0);
  sh.classList.add('open'); $('#dim').classList.add('on');
  if(!reduce) sh.animate([{transform:'translateY(105%)'}, {transform:'none'}], {duration:540, easing:'cubic-bezier(.2,.95,.25,1.02)'});
}
function closeAdd(after){
  const sh = $('#addSheet'); if(!sh.classList.contains('open')) return; $('#dim').classList.remove('on');
  document.activeElement && document.activeElement.blur && document.activeElement.blur();
  const done = () => { sh.classList.remove('open'); typeof after === 'function' && after(); };
  if(reduce) return done();
  sh.animate([{transform:'none'}, {transform:'translateY(105%)'}], {duration:400, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = done;
}
const stepper = (k, v) => `<div class="stepper"><button data-st="${k}:-1" aria-label="Less">−</button><span>${v}</span><button data-st="${k}:1" aria-label="More">+</button></div>`;
const more = (k, open, html) => `<div class="more ${open ? 'open' : ''}" data-more="${k}"><div>${html}</div></div>`;
function paneHTML(i){
  const kick = `<div class="kick">Step ${i + 1} of 6</div>`, nm = clean(D.n) || 'it';
  if(i === 0) return `${kick}<h2>Name</h2><p class="q">What is it called on the label?</p>
    <div class="field"><input id="fName" placeholder="Medication name" autocomplete="off" value="${clean(D.n)}"></div>
    <div class="field"><input id="fNick" placeholder="Nickname (Optional)" autocomplete="off" value="${clean(D.nick)}"></div>
    <div class="warn" id="fWarn"></div><p class="foot">A nickname is handy for something like “blood pressure pill”. It shows instead of the name.</p>`;
  if(i === 1) return `${kick}<h2>Form</h2><p class="q">What kind of medication is ${nm}?</p>
    <div class="card list">${FORMS.map(([f, k]) => `<div class="lrow ${D.form === f ? 'on' : ''}" data-form="${f}"><span style="width:30px;display:grid;place-items:center;margin-right:12px">${pillSVG({k, a:'white', b:'gray'}, 26)}</span>${f}<svg class="tick2" width="16" height="13" viewBox="0 0 16 13"><path d="M1.5 7l4.5 4.5L14.5 2" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg></div>`).join('')}</div>`;
  if(i === 2) return `${kick}<h2>Strength</h2><p class="q">How much is in one ${unitWord(1)}?</p>
    <div class="bignum"><input id="fStr" inputmode="decimal" placeholder="0" value="${D.strength}" aria-label="Strength"><span>${D.unit}</span></div>
    <div class="seg" id="unitSeg">${UNITS.map(u => `<button class="${u === D.unit ? 'on' : ''}" data-unit="${u}">${u}</button>`).join('')}</div>
    <p class="foot" style="text-align:center">Check the strength on your pharmacy label.</p>`;
  if(i === 3) return `${kick}<h2>Schedule</h2><p class="q">How often do you take ${nm}?</p>
    <div class="card list">${SCHED.map(([k, l]) => `<div class="lrow ${D.sched === k ? 'on' : ''}" data-sched="${k}">${l}<svg class="tick2" width="16" height="13" viewBox="0 0 16 13"><path d="M1.5 7l4.5 4.5L14.5 2" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg></div>`).join('')}</div>
    ${more('days', D.sched === 'days', `<div class="card" style="margin-top:10px"><div class="daychips">${WD.map((w, j) => `<button class="${D.days.includes(j) ? 'on' : ''}" data-day="${j}" aria-label="${WDN[j]}">${w}</button>`).join('')}</div></div>`)}
    ${more('every', D.sched === 'every', `<div class="card" style="margin-top:10px"><div class="srow"><span class="lbl">Every</span>${stepper('every', D.every)}<span class="dt">days</span></div></div><p class="foot">Starts today, Wednesday.</p>`)}
    ${more('cycle', D.sched === 'cycle', `<div class="card" style="margin-top:10px"><div class="srow"><span class="lbl">Days on</span>${stepper('on', D.on)}</div><div class="srow"><span class="lbl">Days off</span>${stepper('off', D.off)}</div></div><p class="foot">Today is day 1 of ${D.on + D.off}.</p>`)}
    ${more('prn', D.sched === 'prn', `<div class="card" style="margin-top:10px"><div class="srow"><span class="lbl">Up to</span>${stepper('max', D.max)}<span class="dt">a day</span></div><div class="srow"><span class="lbl">At least</span>${stepper('gap', D.gap)}<span class="dt">hours apart</span></div></div><p class="foot">Hävn warns you if you log one sooner. As-needed doses aren’t part of the ring.</p>`)}
    ${more('times', D.sched !== 'prn', `<div class="lbl3">Times</div><div class="card" id="timesCard">${D.times.map((t, j) => `<div class="srow"><input type="time" class="timein" data-time="${j}" value="${t}" aria-label="Time ${j + 1}"><span class="lbl"></span><span class="dt" style="white-space:nowrap">${D.qty} ${unitWord(D.qty)}</span>${stepper('qty', D.qty)}</div>`).join('')}
      <div class="navrow add" data-addtime="1">Add a Time</div></div>
      <div class="lbl3">Remind With</div><div class="seg" id="remSeg">${['Notification','Alarm'].map(r => `<button class="${D.remind === r ? 'on' : ''}" data-rem="${r}">${r}</button>`).join('')}</div>
      <p class="foot">An alarm rings even when your iPhone is on Silent or in a Focus.</p>`)}
    <div class="card" style="margin-top:14px"><div class="srow"><span class="lbl">Ends</span><div class="seg" style="width:180px">${[['0','Never'],['1','On a Date']].map(([v, l]) => `<button class="${(+v === 1) === D.ends ? 'on' : ''}" data-ends="${v}">${l}</button>`).join('')}</div></div>
      ${more('ends', D.ends, `<div class="srow"><span class="lbl">Last day</span><input type="date" class="timein" value="2026-12-18" aria-label="Last day"></div>`)}</div>`;
  if(i === 4) return `${kick}<h2>Shape and Colour</h2><p class="q">This helps you tell ${nm} apart from your other medications.</p>
    <div class="preview" id="pv2">${lookPreview()}</div>
    <div class="lbl3">Shape</div><div class="shapes2">${SHAPES8.map(k => `<button class="${D.shape === k ? 'on' : ''}" data-shape="${k}">${pillSVG({k, a:D.a, b:D.b}, 30)}${k[0].toUpperCase() + k.slice(1)}</button>`).join('')}</div>
    <div class="lbl3">Colour</div><div class="sws2">${PCOLS.map(c => `<button class="${D.a === c ? 'on' : ''}" data-ca="${c}" style="background:var(--pc-${c})" aria-label="${c}"></button>`).join('')}</div>
    ${D.shape === 'capsule' ? `<div class="lbl3">Second Colour</div><div class="sws2">${PCOLS.map(c => `<button class="${D.b === c ? 'on' : ''}" data-cb="${c}" style="background:var(--pc-${c})" aria-label="${c}"></button>`).join('')}</div>` : ''}
    <div class="lbl3">Colour in Your Ring</div><div class="sws2">${BGS.map(c => `<button class="${D.c === c ? 'on' : ''}" data-cc="${c}" style="background:var(--${c})" aria-label="${c}"></button>`).join('')}</div>`;
  const m = draftMed();
  return `${kick}<h2>Review</h2><p class="q">Check the details before you add ${nm}.</p>
    <div class="card"><div class="lrow" style="cursor:default">${icon(m)}<div class="mid"><div class="nm">${clean(D.n)}</div><div class="dt">${m.d} · ${D.form}</div></div></div></div>
    <div class="card" style="margin-top:14px">
      <div class="navrow" data-go="3"><span class="lbl">Schedule</span><span class="val">${schedText()}</span>${CHEV}</div>
      ${D.sched !== 'prn' ? `<div class="navrow" data-go="3"><span class="lbl">Times</span><span class="val">${D.times.join(', ')}</span>${CHEV}</div>
      <div class="navrow" data-go="3"><span class="lbl">Remind With</span><span class="val">${D.remind}</span>${CHEV}</div>` : ''}
      <div class="navrow" data-go="4"><span class="lbl">Look</span><span class="val">${D.shape[0].toUpperCase() + D.shape.slice(1)}, ${D.a}</span>${CHEV}</div>
      <div class="srow"><span class="lbl">Supply</span>${stepper('supply', D.supply)}<span class="dt">${unitWord(D.supply)}</span></div></div>
    <p class="foot">You can change any of this later in Medications.</p>`;
}
function draftMed(){ return {n:clean(D.n), d:`${D.strength || '–'} ${D.unit}`, c:D.c, p:{k:D.shape, a:D.a, b:D.b}}; }
function lookPreview(){ return `<div style="display:grid;place-items:center;gap:6px">${icon(draftMed(), 72)}<div class="nm">${clean(D.n) || 'New Medication'}</div></div>`; }
function addPane(i, dir){
  const steps = $('#addSteps'), old = steps.querySelector('.pane:not(.leaving)');
  const p = document.createElement('div'); p.className = 'pane'; p.innerHTML = paneHTML(i); steps.appendChild(p);
  aStep = i;
  $('#addCancel').innerHTML = i === 0 ? 'Cancel' : '<span style="display:flex;align-items:center;gap:4px"><svg width="9" height="15" viewBox="0 0 9 15"><path d="M7.5 1.5L2 7.5l5.5 6" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>Back</span>';
  $('#addNext').textContent = i === 5 ? 'Add Medication' : 'Next';
  validate();
  if(old){ old.classList.add('leaving');
    if(reduce){ old.remove(); }
    else { old.animate([{transform:'none', opacity:1}, {transform:`translateX(${dir > 0 ? -30 : 100}%)`, opacity:dir > 0 ? .3 : 1}], {duration:460, easing:'cubic-bezier(.3,.9,.3,1)', fill:'forwards'}).onfinish = () => old.remove();
      p.animate([{transform:`translateX(${dir > 0 ? 100 : -30}%)`, opacity:dir > 0 ? 1 : .3}, {transform:'none', opacity:1}], {duration:460, easing:'cubic-bezier(.3,.9,.3,1)'}); }
  }
  const nameIn = p.querySelector('#fName'); if(nameIn) nameIn.addEventListener('input', () => { D.n = nameIn.value; validate(); });
  const nickIn = p.querySelector('#fNick'); if(nickIn) nickIn.addEventListener('input', () => { D.nick = nickIn.value; });
  const strIn = p.querySelector('#fStr'); if(strIn) strIn.addEventListener('input', () => { D.strength = strIn.value.replace(/[^0-9.,]/g, '').slice(0, 6); strIn.value = D.strength; validate(); });
  p.querySelectorAll('[data-time]').forEach(t => t.addEventListener('change', () => { if(/^\d{2}:\d{2}$/.test(t.value)) D.times[+t.dataset.time] = t.value; }));
}
function validate(){
  let ok = true;
  if(aStep === 0){ const n = clean(D.n); ok = !!n;
    const dup = [...MEDS, ...PRN, ...EXTRA].find(m => m.n.toLowerCase() === n.toLowerCase());
    const w = $('#fWarn'); if(w) w.textContent = dup ? `You already have ${dup.n}. You can still add it, for example at another time.` : ''; }
  if(aStep === 2) ok = +String(D.strength).replace(',', '.') > 0;
  if(aStep === 3 && D.sched === 'days') ok = D.days.length > 0;
  $('#addNext').disabled = !ok;
}
function rerenderPane(){ const p = $('#addSteps .pane:not(.leaving)'); const top = p.scrollTop; p.innerHTML = paneHTML(aStep); p.scrollTop = top; validate();
  p.querySelectorAll('[data-time]').forEach(t => t.addEventListener('change', () => { if(/^\d{2}:\d{2}$/.test(t.value)) D.times[+t.dataset.time] = t.value; })); }
function setMore(k, open){ const el = $(`#addSteps [data-more="${k}"]`); el && el.classList.toggle('open', open); }
$('#addSteps').addEventListener('click', e => {
  const t = e.target, row = k => t.closest(`[data-${k}]`);
  if(row('form')){ D.form = row('form').dataset.form; D.shape = FORMS.find(f => f[0] === D.form)[1]; $('#addSteps').querySelectorAll('[data-form]').forEach(r => r.classList.toggle('on', r === row('form'))); haptic(row('form'), 'selection haptic'); return; }
  if(row('unit')){ D.unit = row('unit').dataset.unit; $('#addSteps').querySelectorAll('[data-unit]').forEach(b => b.classList.toggle('on', b === row('unit'))); $('#addSteps .bignum span').textContent = D.unit; return; }
  if(row('sched')){ D.sched = row('sched').dataset.sched; $('#addSteps').querySelectorAll('[data-sched]').forEach(r => r.classList.toggle('on', r === row('sched')));
    ['days','every','cycle','prn'].forEach(k => setMore(k, D.sched === k)); setMore('times', D.sched !== 'prn'); haptic(row('sched'), 'selection haptic'); validate(); return; }
  if(row('day')){ const j = +row('day').dataset.day; D.days = D.days.includes(j) ? D.days.filter(x => x !== j) : [...D.days, j].sort(); row('day').classList.toggle('on'); validate(); return; }
  if(row('st')){ const [k, d] = row('st').dataset.st.split(':'); const lim = {every:[2,30], on:[1,90], off:[1,90], max:[1,12], gap:[1,24], qty:[1,10], supply:[0,500]}[k];
    D[k] = Math.max(lim[0], Math.min(lim[1], D[k] + (+d) * (k === 'supply' ? 10 : 1))); row('st').parentElement.querySelector('span').textContent = D[k];
    if(k === 'qty' || k === 'supply') rerenderPane(); return; }
  if(row('addtime')){ const last = D.times[D.times.length - 1]; D.times.push(last < '12:00' ? '20:00' : '08:00'); rerenderPane(); return; }
  if(row('rem')){ D.remind = row('rem').dataset.rem; $('#addSteps').querySelectorAll('[data-rem]').forEach(b => b.classList.toggle('on', b === row('rem'))); return; }
  if(row('ends')){ D.ends = row('ends').dataset.ends === '1'; $('#addSteps').querySelectorAll('[data-ends]').forEach(b => b.classList.toggle('on', b === row('ends'))); setMore('ends', D.ends); return; }
  if(row('shape') || row('ca') || row('cb') || row('cc')){
    if(row('shape')) D.shape = row('shape').dataset.shape; if(row('ca')) D.a = row('ca').dataset.ca; if(row('cb')) D.b = row('cb').dataset.cb; if(row('cc')) D.c = row('cc').dataset.cc;
    rerenderPane(); const pv = $('#pv2 .ic'); if(pv && !reduce) pv.animate([{transform:'scale(.85)'}, {transform:'scale(1)'}], {duration:420, easing:'cubic-bezier(.3,1.6,.5,1)'}); return; }
  if(row('go')){ addPane(+row('go').dataset.go, -1); return; }
});
$('#addNext').onclick = () => { if($('#addNext').disabled) return; if(aStep < 5) addPane(aStep + 1, 1); else commitAdd(); };
$('#addCancel').onclick = () => { if(aStep === 0) closeAdd(); else addPane(aStep - 1, -1); };
function commitAdd(){
  const id = 'm' + Date.now().toString(36), n = clean(D.n), d = `${String(D.strength).replace(',', '.')} ${D.unit}`, p = {k:D.shape, a:D.a, b:D.b};
  const base = {n, d, c:D.c, p, form:D.form, left:D.supply};
  let added = [];
  if(D.sched === 'prn'){ PRN.push({...base, id, max:D.max, gap:D.gap, sched:schedText()}); added = [id]; }
  else {
    const today = D.sched !== 'days' || D.days.includes(2);       // today is a Wednesday
    if(today){ D.times.forEach((t, k) => { const did = id + '_' + k; MEDS.push({...base, id:did, t, g:groupOf(t), sched:schedText(), times:D.times}); added.push(did); });
      MEDS.sort((a, b) => a.t < b.t ? -1 : 1); }
    else EXTRA.push({...base, id, t:D.times[0], sched:schedText(), note:'Not scheduled today'});
  }
  saveCur();
  closeAdd(() => {
    build(); rebuildRingParts(); renderRing({silent:true}); setCount(doneCount(), true); swap($('#sm'), smText()); swap($('#accSwap'), accNext()); drawBanner();
    showTab(0);
    const el = added.map(a => document.querySelector(`.row[data-id="${a}"],.lrow[data-prn="${a}"]`)).filter(Boolean)[0];
    if(el){ const sc = $('#scroll'), s = +$('#phone').dataset.s || 1, er = el.getBoundingClientRect(), sr = sc.getBoundingClientRect();   // scroll only the list, never the phone
      sc.scrollTo({top: sc.scrollTop + (er.top - sr.top)/s - sc.clientHeight/2 + er.height/(2*s), behavior:reduce ? 'auto' : 'smooth'});
      setTimeout(() => { el.classList.add('flash'); haptic(el, 'success haptic'); }, 450); }
    Snd.play('on'); toast(`${n} added`);
  });
}
