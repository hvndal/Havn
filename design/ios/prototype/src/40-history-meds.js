
/* ================= History and Medications ================= */
function miniRing(states, size, cols){
  const r = 15, sw = 5, C = 2*Math.PI*r, n = Math.max(1, states.length), cap = (sw/2)/r*180/Math.PI, gap = n > 1 ? 2*cap + 7 : 0, len = n > 1 ? (360/n - gap)/360*C : C;
  return `<svg width="${size}" height="${size}" viewBox="0 0 40 40" aria-hidden="true">` + (states.length ? states : [0]).map((st, i) =>
    `<circle cx="20" cy="20" r="${r}" fill="none" stroke="${st ? `var(--${(cols || MEDS.map(m => m.c))[i] || 'accent'})` : 'var(--fill)'}" stroke-width="${sw}" stroke-linecap="${n > 1 ? 'round' : 'butt'}"
      stroke-dasharray="${len} ${C}" transform="rotate(${-90 + i*360/n + gap/2} 20 20)"/>`).join('') + '</svg>';
}
/* a month of made-up but believable history: gaps mostly on one weekday. Each day keeps its own doses. */
function makeHist(meds, id){
  const misses = id === 'you' ? {3:[2], 7:[0,2], 14:[1,3], 16:[2]} : id === 'mum' ? {3:[2], 10:[3], 14:[0,3], 17:[2]} : null, H = {};
  if(misses) for(let d = 1; d <= 17; d++) H[d] = meds.map((m, i) => ({id:m.id, n:m.n, d:m.d, t:m.t, c:m.c, p:m.p, v:(misses[d] || []).includes(i) ? 0 : 1}));
  return H;
}
function hist(){ const P = PEOPLE[cur]; return P.hist || (P.hist = {}); }
const vals = recs => (recs || []).map(r => r.v);
const todayStates = () => MEDS.map(m => taken[m.id] ? 1 : 0);
function dayState(st){ if(!st || !st.length) return ''; const t = st.filter(Boolean).length; return t === st.length ? 'all' : t ? 'some' : 'none'; }
const DOT = {all:'var(--green)', some:'var(--orange)', none:'var(--l3)'};
let calMonth = 10;   // 0-based: November
function storyText(){
  const H = hist(), days = Object.keys(H); if(!days.length) return MEDS.length ? 'Your history starts today. Each day you log becomes a ring here.' : 'Add a medication and your history starts the same day.';
  let tot = 0, tk = 0; const missBy = {}, missDay = {};
  days.forEach(d => H[d].forEach(r => { tot++; tk += r.v; if(!r.v){ missBy[r.n] = (missBy[r.n]||0) + 1; const wd = (+d + 5) % 7; missDay[wd] = (missDay[wd]||0) + 1; } }));
  const who = cur === 'you' ? 'you’ve' : PEOPLE[cur].name + ' has';
  const topDay = Object.entries(missDay).sort((a, b) => b[1] - a[1])[0], topMed = Object.entries(missBy).sort((a, b) => b[1] - a[1])[0];
  const why = tk === tot ? 'Not one missed.' : topDay && topDay[1] >= 3 ? `Most of the gaps were on ${['Mondays','Tuesdays','Wednesdays','Thursdays','Fridays','Saturdays','Sundays'][topDay[0]]}.` : topMed ? `${topMed[0]} is the one most often missed.` : '';
  return `Since 1 November ${who} taken ${tk} of ${tot} doses. ${why}`;
}
function drawHistory(){
  const S = $('#scrHistory'), H = hist();
  const week = [16,17,18,19,20,21,22].map((d, i) => { const past = d < 18, st = past ? vals(H[d]) : d === 18 ? todayStates() : MEDS.map(() => 0);
    return `<div class="d ${d === 18 ? 'today' : d > 18 ? 'later' : ''}" data-day="${d}"><span class="l">${WD[i]}</span>${miniRing(st, 34, past && H[d] ? H[d].map(r => r.c) : null)}<span class="n">${d}</span></div>`; }).join('');
  S.innerHTML = `<div class="ltitle">History</div><div class="lsub">${cur === 'you' ? '' : PEOPLE[cur].name + ' · '}November 2026</div>
    <div class="card wk">${week}</div>
    <div class="card story" id="story">${storyText()}</div>
    <div class="card cal"><div class="hd"><button data-mon="-1" aria-label="Previous month"><svg width="10" height="16" viewBox="0 0 10 16"><path d="M8 2L2 8l6 6" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg></button><b id="calTitle"></b>
      <button data-mon="1" aria-label="Next month"><svg width="10" height="16" viewBox="0 0 10 16"><path d="M2 2l6 6-6 6" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg></button></div>
      <div class="legend"><span><i style="background:var(--green)"></i>All taken</span><span><i style="background:var(--orange)"></i>Some taken</span><span><i style="background:var(--l3)"></i>Skipped</span></div>
      <div class="calbody" id="calBody">${calGrid(calMonth)}</div></div>${reportCard()}`;
  $('#calTitle').textContent = calMonth === 10 ? 'November 2026' : 'October 2026';
}
function calGrid(mon){
  const H = hist(), first = mon === 10 ? 6 : 3, days = mon === 10 ? 30 : 31;   // Mon-first: 1 Nov is a Sunday, 1 Oct a Thursday
  let h = WD.map(w => `<div class="wd">${w}</div>`).join('') + Array.from({length:first}, () => '<div></div>').join('');
  for(let d = 1; d <= days; d++){
    let st = null;
    if(mon === 9) st = MEDS.length && Object.keys(H).length ? (d === 11 || d === 25 ? 'some' : 'all') : '';
    else st = d < 18 ? dayState(vals(H[d])) : d === 18 ? dayState(todayStates()) : '';
    const fut = mon === 10 && d > 18;
    h += `<div class="c ${fut ? 'fut' : ''} ${mon === 10 && d === 18 ? 'today' : ''}" ${!fut && mon === 10 ? `data-cal="${d}"` : ''}><b>${d}</b><i style="background:${st ? DOT[st] : 'transparent'}"></i></div>`;
  }
  return `<div class="grid7">${h}</div>`;
}
$('#scrHistory').addEventListener('click', e => {
  if(e.target.closest('[data-report]')) return openReportOptions();
  const mb = e.target.closest('[data-mon]');
  if(mb){ const to = Math.max(9, Math.min(10, calMonth + +mb.dataset.mon)); if(to === calMonth) { mb.classList.remove('nudge'); mb.offsetWidth; mb.classList.add('nudge'); return; }
    const dir = to > calMonth ? 1 : -1; calMonth = to; const body = $('#calBody'), old = body.firstElementChild;
    const nw = document.createElement('div'); nw.innerHTML = calGrid(calMonth); const g = nw.firstElementChild; g.style.position = 'absolute'; g.style.inset = '0'; body.appendChild(g);
    $('#calTitle').textContent = calMonth === 10 ? 'November 2026' : 'October 2026';
    if(reduce){ old.remove(); g.style.position = ''; return; }
    old.animate([{transform:'none', opacity:1}, {transform:`translateX(${-dir*40}%)`, opacity:0}], {duration:380, easing:'cubic-bezier(.3,.9,.3,1)', fill:'forwards'}).onfinish = () => { old.remove(); g.style.position = ''; };
    g.animate([{transform:`translateX(${dir*40}%)`, opacity:0}, {transform:'none', opacity:1}], {duration:380, easing:'cubic-bezier(.3,.9,.3,1)'});
    return; }
  const c = e.target.closest('[data-cal],[data-day]'); if(c){ const d = +(c.dataset.cal || c.dataset.day); if(d <= 18) openDay(d); }
});
/* one day: what was taken, and a way to log a dose you forgot to log */
function openDay(d){
  const H = hist(), recs = d === 18 ? MEDS.map(m => ({...m, v:taken[m.id] ? 1 : 0})) : H[d]; if(!recs) return; const st = vals(recs);
  const wd = ['Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'][(d + 5) % 7];
  const rows = recs.map((m, i) => { const ok = m.v;
    const when = d === 18 ? (taken[m.id] ? `Taken at ${taken[m.id]}` : m.t < now ? `Due at ${m.t}` : `${m.t} · later today`) : ok ? `Taken at ${m.t.slice(0, 3)}${String(+m.t.slice(3) + 4).padStart(2, '0')}` : 'Not logged';
    return `<div class="lrow" style="cursor:default">${icon(m)}<div class="mid"><div class="nm">${m.n}</div><div class="dt" ${!ok && d !== 18 ? 'style="color:var(--orange-text)"' : ''}>${m.d} · ${when}</div>${dnoteLine(m.id, d)}</div>
      ${!ok && d < 18 ? `<button class="pbtn ghost sm" data-late="${i}">Log</button>` : ok ? '<svg width="22" height="22" viewBox="0 0 22 22"><circle cx="11" cy="11" r="11" fill="var(--green)"/><path d="M6.5 11.3l3 3 6-6.6" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>' : ''}</div>`; }).join('');
  const sh = $('#daySheet');
  sh.innerHTML = `<div class="inner"><div class="grab"></div><div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px 4px">
      <span style="width:44px"></span><span style="font-size:17px;font-weight:600">${wd}, ${d} November</span>
      <button class="gbtn glass" data-dayclose="1" aria-label="Done" style="background:var(--accent);color:var(--on-accent)"><svg width="18" height="18" viewBox="0 0 18 18"><path d="M3.5 9.5l3.6 3.6L14.5 5" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg></button></div>
    <div style="display:grid;place-items:center;padding:6px 0 14px">${miniRing(st, 84, recs.map(r => r.c))}</div>
    <div style="padding:0 16px 26px"><div class="card">${rows || '<div class="lrow">Nothing scheduled.</div>'}</div>
    ${d < 18 && st.includes(0) ? '<p class="foot">Forgot to log one? Log it here with the time you actually took it.</p>' : ''}
    <div class="shead" style="padding-left:20px;margin-top:18px"><span class="t">${PENCIL}Note</span></div><div class="card" id="dayNoteBox">${dayNoteHTML(d)}</div></div></div>`;
  sh.classList.add('open'); $('#dim').classList.add('on');
  if(!reduce) sh.animate([{transform:'translateY(105%)'}, {transform:'none'}], {duration:520, easing:'cubic-bezier(.2,.95,.25,1.02)'});
  sh.onclick = e => {
    if(e.target.closest('[data-dayclose]')) return closeDay();
    const b = e.target.closest('[data-late]'); if(!b) return;
    const i = +b.dataset.late, row = b.closest('.lrow');
    b.outerHTML = `<input type="time" class="timein" value="${recs[i].t}" aria-label="Taken at"><button class="pbtn sm" data-save="${i}" style="margin-left:8px">Save</button>`;
    row.querySelector('[data-save]').onclick = ev => { ev.stopPropagation(); const v = row.querySelector('input').value; H[d][i].v = 1;
      row.querySelector('.dt').style.color = ''; row.querySelector('.dt').textContent = `${recs[i].d} · Taken at ${/^\d{2}:\d{2}$/.test(v) ? v : recs[i].t} · logged later`;
      row.querySelectorAll('input,[data-save]').forEach(x => x.remove());
      row.insertAdjacentHTML('beforeend', '<svg width="22" height="22" viewBox="0 0 22 22"><circle cx="11" cy="11" r="11" fill="var(--green)"/><path d="M6.5 11.3l3 3 6-6.6" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>');
      sh.querySelector('.grab + div + div').innerHTML = miniRing(vals(H[d]), 84, H[d].map(r => r.c));
      Snd.play('taken'); haptic(row, 'success haptic'); drawHistory(); };
  };
}
function closeDay(){
  const sh = $('#daySheet'); if(!sh.classList.contains('open')) return; $('#dim').classList.remove('on');
  if(reduce){ sh.classList.remove('open'); return; }
  sh.animate([{transform:'none'}, {transform:'translateY(105%)'}], {duration:380, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = () => sh.classList.remove('open');
}

/* ---------- Medications ---------- */
let medsEditing = false;
const baseId = id => id.replace(/_\d+$/, '');
function medList(){
  const seen = {}, out = [];
  MEDS.forEach(m => { const b = baseId(m.id); if(seen[b]){ seen[b].times.push(m.t); return; } seen[b] = {...m, id:b, times:[m.t]}; out.push(seen[b]); });
  return [...out, ...EXTRA.map(m => ({...m, times:[m.t]}))];
}
function medRow(m, prn){
  const sub = prn ? `${m.d} · As needed` : `${m.d} · ${m.sched}${m.note ? '' : ', ' + m.times.join(', ')}`;
  return `<div class="lrow" data-detail="${m.id}"><span class="minus" data-archive="${m.id}" role="button" aria-label="Archive ${m.n}"><svg width="10" height="2" viewBox="0 0 10 2"><rect width="10" height="2" rx="1" fill="#fff"/></svg></span>
    ${icon(m)}<div class="mid"><div class="nm">${m.n}</div><div class="dt">${sub}${m.note ? ' · ' + m.note : ''}</div></div><span class="supply ${m.left < 10 ? 'low' : ''}">${m.left} left</span>${CHEV}</div>`;
}
function drawMeds(){
  const S = $('#scrMeds'), L = medList(), low = L.filter(m => m.left < 10)[0];
  S.innerHTML = `<button class="pill-btn glass editbtn" id="medsEdit">${medsEditing ? 'Done' : 'Edit'}</button><div class="nav2"><div class="navpage ${medsEditing ? 'editing' : ''}" id="medsList">
    <div class="ltitle">Medications</div><div class="lsub">${cur === 'you' ? '' : PEOPLE[cur].name + ' · '}${L.length} scheduled · ${PRN.length} as needed</div>
    ${L.length || PRN.length ? '' : `<div class="card" style="padding:24px 20px;margin-top:18px;text-align:center"><div class="nm">No medications yet</div><div class="dt" style="margin:4px 0 14px">Add one and it appears here with its schedule and supply.</div><button class="btn2 p" data-add="1">Add Medication</button></div>`}
    ${L.length ? `<div class="sec"><div class="shead"><span class="t">Scheduled</span></div><div class="card">${L.map(m => medRow(m)).join('')}</div>
      ${low ? `<p class="foot">${low.n} is running low. Supply goes down by one each time you log a dose.</p>` : ''}</div>` : ''}
    ${PRN.length ? `<div class="sec"><div class="shead"><span class="t">As Needed</span></div><div class="card">${PRN.map(m => medRow(m, true)).join('')}</div></div>` : ''}
    <div class="sec"><div class="card"><div class="navrow" data-archived="1"><span class="lbl">Archived Medications</span><span class="val">${ARCH.length}</span>${CHEV}</div></div></div>
  </div></div>`;
}
function pushPage(html, after){
  const nav = $('#scrMeds .nav2'), from = nav.lastElementChild, p = document.createElement('div');
  p.className = 'navpage'; p.innerHTML = html; nav.appendChild(p); $('#medsEdit').hidden = true;
  const back = document.createElement('button'); back.className = 'pill-btn glass back editbtn'; back.id = 'navBack';
  back.innerHTML = '<svg width="10" height="17" viewBox="0 0 10 17"><path d="M8.5 1.5L2 8.5l6.5 7" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>Medications';
  back.onclick = popPage; $('#scrMeds').appendChild(back);
  if(!reduce){ p.animate([{transform:'translateX(100%)'}, {transform:'none'}], {duration:520, easing:'cubic-bezier(.2,.9,.25,1)'});
    from.animate([{transform:'none', filter:'brightness(1)'}, {transform:'translateX(-30%)', filter:'brightness(.92)'}], {duration:520, easing:'cubic-bezier(.2,.9,.25,1)', fill:'forwards'});
    back.animate([{opacity:0, transform:'translateX(30px)'}, {opacity:1, transform:'none'}], {duration:420, delay:120, fill:'backwards'}); }
  after && after(p);
}
function popPage(){
  const nav = $('#scrMeds .nav2'), top = nav.lastElementChild, under = top.previousElementSibling; if(!under) return;
  const back = $('#navBack'); back && back.remove();
  const done = () => { top.remove(); under.getAnimations().forEach(a => a.cancel()); if(!under.previousElementSibling) $('#medsEdit').hidden = false; else addBack(); };
  if(reduce) return done();
  top.animate([{transform:'none'}, {transform:'translateX(100%)'}], {duration:440, easing:'cubic-bezier(.3,.9,.3,1)', fill:'forwards'}).onfinish = done;
  under.animate([{transform:'translateX(-30%)', filter:'brightness(.92)'}, {transform:'none', filter:'brightness(1)'}], {duration:440, easing:'cubic-bezier(.3,.9,.3,1)', fill:'forwards'});
}
function addBack(){ const back = document.createElement('button'); back.className = 'pill-btn glass back editbtn'; back.id = 'navBack';
  back.innerHTML = '<svg width="10" height="17" viewBox="0 0 10 17"><path d="M8.5 1.5L2 8.5l6.5 7" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg>Medications'; back.onclick = popPage; $('#scrMeds').appendChild(back); }
function findMed(id){ return medList().find(m => m.id === id) || PRN.find(m => m.id === id); }
function detailHTML(m){
  const prn = PRN.includes(m), runs = 18 + Math.round(m.left / Math.max(1, (m.times || [1]).length));
  const runsTxt = runs <= 30 ? `${runs} Nov` : `${runs - 30} Dec`;
  const taken = prn ? `${prnCount(m)} today` : cur === 'you' || cur === 'mum' ? '15 of 17' : '0 of 0';
  return `<div class="dhead"><div style="display:grid;place-items:center">${icon(m, 76)}</div><div class="nm2">${m.n}</div>
      <div class="dt">${m.d} · ${m.form || 'Tablet'} · ${prn ? 'As needed' : `${m.sched} at ${(m.times || [m.t]).join(', ')}`}</div></div>
    <div class="card stats"><div><b>${taken}</b><span>${prn ? 'logged' : 'taken this month'}</span></div><div><b style="${m.left < 10 ? 'color:var(--orange-text)' : ''}">${m.left}</b><span>left</span></div><div><b>${prn ? '–' : runsTxt}</b><span>runs out around</span></div></div>
    <div class="card" style="margin-top:14px">
      <div class="navrow"><span class="lbl">Schedule</span><span class="val">${prn ? `Up to ${m.max} a day` : m.sched}</span>${CHEV}</div>
      ${prn ? `<div class="navrow"><span class="lbl">Gap</span><span class="val">At least ${m.gap} hours</span>${CHEV}</div>` : `<div class="navrow"><span class="lbl">Time</span><span class="val">${(m.times || [m.t]).join(', ')}</span>${CHEV}</div>
      <div class="navrow"><span class="lbl">Remind With</span><span class="val">Notification</span>${CHEV}</div>`}</div>
    <div class="card" style="margin-top:14px"><div class="navrow"><span class="lbl">Supply</span><span class="val" style="${m.left < 10 ? 'color:var(--orange-text)' : ''}">${m.left} left</span>${CHEV}</div>
      <div class="navrow"><span class="lbl">Refill Reminder</span><span class="val">At 7 left</span>${CHEV}</div></div>
    ${m.left < 10 ? '<p class="foot">You’re below your refill reminder. Time to pick up more.</p>' : ''}
    <div class="card" style="margin-top:14px"><div class="navrow add" data-lognow="${m.id}"><span class="lbl">Log a Dose Now</span></div></div>
    <div class="card" style="margin-top:14px"><div class="navrow danger" data-archive="${m.id}">Archive Medication</div></div>`;
}
function openDetail(id){
  const m = findMed(id); if(!m) return;
  if(tab !== 2){ showTab(2); setTimeout(() => openDetail(id), reduce ? 0 : 260); return; }
  pushPage(detailHTML(m));
}
function archive(id){
  const m = findMed(id); if(!m) return;
  showAlert({title:`Archive ${m.n}?`, body:'It moves to Archived Medications and stops reminding you. Its history stays.',
    buttons:[{label:'Cancel'}, {label:'Archive', style:'des', onTap:() => {
      MEDS = MEDS.filter(x => baseId(x.id) !== id); PRN = PRN.filter(x => x.id !== id); EXTRA = EXTRA.filter(x => x.id !== id);
      order = order.filter(x => baseId(x) !== id); Object.keys(taken).forEach(k => { if(baseId(k) === id) delete taken[k]; });
      ARCH.unshift({n:m.n, d:`${m.d} · archived today`}); saveCur();
      if($('#navBack')) popPage(); setTimeout(() => { drawMeds(); build(); rebuildRingParts(); renderRing({silent:true}); setCount(doneCount(), true); swap($('#sm'), smText()); swap($('#accSwap'), accNext()); }, reduce ? 0 : 460);
      toast(`${m.n} archived`); }}]});
}
$('#scrMeds').addEventListener('click', e => {
  if(e.target.closest('#medsEdit')){ medsEditing = !medsEditing; $('#medsEdit').textContent = medsEditing ? 'Done' : 'Edit'; $('#medsList').classList.toggle('editing', medsEditing); return; }
  const ar = e.target.closest('[data-archive]'); if(ar){ e.stopPropagation(); archive(ar.dataset.archive); return; }
  const ln = e.target.closest('[data-lognow]'); if(ln){ const id = ln.dataset.lognow, p = PRN.find(x => x.id === id);
    if(p){ toast(`${p.n} logged`); (prnLog[id] = prnLog[id] || []).push(now); p.left--; Snd.play('taken'); }
    else { const dose = MEDS.find(x => baseId(x.id) === id && !taken[x.id]); if(dose){ logDose(dose.id); toast(`${dose.n} logged`); } else toast('Already logged today'); }
    return; }
  if(e.target.closest('[data-archived]')){ pushPage(`<div class="ltitle" style="margin-top:6px">Archived</div><div class="lsub">Kept with their history. Nothing reminds you.</div>
    <div class="card" style="margin-top:18px">${ARCH.map(a => `<div class="lrow" style="cursor:default"><div class="mid"><div class="nm">${a.n}</div><div class="dt">${a.d}</div></div></div>`).join('') || '<div class="lrow">Nothing archived.</div>'}</div>`); return; }
  const d = e.target.closest('[data-detail]'); if(d && !medsEditing) pushPage(detailHTML(findMed(d.dataset.detail)));
});
