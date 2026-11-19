
/* ================= notes, and a PDF report for your doctor ================= */
const PENCIL = '<svg class="pen" width="13" height="13" viewBox="0 0 14 14" aria-hidden="true"><path d="M9.6 1.8l2.6 2.6-7.7 7.7-3.2.6.6-3.2z" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round"/></svg>';
const SAMPLE_NOTES = {
  you:{day:{7:'Busy day at work. Missed the morning doses and only remembered at night.', 14:'Away for the weekend and left the pill box at home.'}, dose:{'16:iron':'Skipped it. Stomach upset after breakfast.', '3:iron':'Forgot, was out all afternoon.'}},
  mum:{day:{14:'Check-up at the clinic. Doctor said keep taking calcium with lunch.'}, dose:{'10:ator':'Fell asleep early and missed it.', '17:calc':'Lunch out, didn’t have it with me.'}},
};
const QUICK = ['Felt dizzy', 'Upset stomach', 'Headache', 'Slept badly', 'Feeling good', 'Forgot', 'Away from home', 'Ran out'];
const cleanNote = s => (s || '').replace(/[<>]/g, '').slice(0, 600).trim();
const escNote = s => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/\n/g, '<br>');
function notesOf(id = cur){ const P = PEOPLE[id]; if(!P.notes){ const s = SAMPLE_NOTES[id] || {day:{}, dose:{}}; P.notes = {day:{...s.day}, dose:{...s.dose}}; } return P.notes; }
function doseNote(id, d = 18){ return notesOf().dose[`${d}:${baseId(id)}`] || ''; }
function setDoseNote(id, text, d = 18){ const N = notesOf(), k = `${d}:${baseId(id)}`, t = cleanNote(text); if(t) N.dose[k] = t; else delete N.dose[k]; }
function dayNote(d){ return notesOf().day[d] || ''; }

/* Today: a quiet place to write */
function notesSection(){
  const t = dayNote(18);
  return `<div class="sec rv"><div class="shead"><span class="t">${PENCIL}Notes</span></div><div class="card"><div class="lrow" data-writenote="18">
    ${t ? `<div class="mid"><div class="dt" style="color:var(--label);font-size:16px;line-height:21px">${escNote(t)}</div></div>`
        : `<span class="tile" style="background:var(--fill3);color:var(--accent)">${PENCIL}</span><div class="mid"><div class="nm" style="font-weight:400;color:var(--accent)">Write a note about today</div><div class="dt">How you feel, side effects, anything to tell your doctor</div></div>`}${CHEV}</div></div></div>`;
}
/* the writing sheet */
let noteDay = 18;
function openNote(d){
  noteDay = d; const sh = $('#noteSheet');
  const date = new Date(2026, 10, d).toLocaleDateString('en-GB', {weekday:'long', day:'numeric', month:'long'});
  $('#noteDate').textContent = (cur === 'you' ? '' : PEOPLE[cur].name + ' · ') + (d === 18 ? 'Today, ' : '') + date;
  $('#noteText').value = dayNote(d);
  $('#noteChips').innerHTML = QUICK.map(q => `<button data-q="${q}">${q}</button>`).join('');
  sh.classList.add('open'); $('#dim').classList.add('on'); sh.style.zIndex = 72;
  if(!reduce) sh.animate([{transform:'translateY(105%)'}, {transform:'none'}], {duration:540, easing:'cubic-bezier(.2,.95,.25,1.02)'});
  setTimeout(() => $('#noteText').focus({preventScroll:true}), reduce ? 0 : 450);
}
function closeNote(){
  const sh = $('#noteSheet'); if(!sh.classList.contains('open')) return; $('#noteText').blur();
  if(!$('#daySheet').classList.contains('open')) $('#dim').classList.remove('on');
  if(reduce){ sh.classList.remove('open'); return; }
  sh.animate([{transform:'none'}, {transform:'translateY(105%)'}], {duration:400, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = () => sh.classList.remove('open');
}
$('#noteChips').addEventListener('click', e => { const b = e.target.closest('[data-q]'); if(!b) return; const ta = $('#noteText');
  ta.value = (ta.value.trim() ? ta.value.trim().replace(/[.]?$/, '. ') : '') + b.dataset.q + '.'; haptic(b, 'selection haptic');
  if(!reduce) b.animate([{transform:'scale(.9)'}, {transform:'scale(1)'}], {duration:360, easing:'cubic-bezier(.3,1.6,.5,1)'}); });
$('#noteCancel').onclick = closeNote;
$('#noteSave').onclick = () => {
  const t = cleanNote($('#noteText').value), N = notesOf(); if(t) N.day[noteDay] = t; else delete N.day[noteDay];
  closeNote(); haptic($('#noteSave'), 'success haptic'); toast(t ? 'Note saved' : 'Note removed');
  if(noteDay === 18){ build(); rebuildRingParts(); drawBanner(); }
  const box = $('#dayNoteBox'); if(box) box.innerHTML = dayNoteHTML(noteDay);
};
function dayNoteHTML(d){ const t = dayNote(d);
  return t ? `<div class="lrow" data-writenote="${d}"><div class="mid"><div class="dt" style="color:var(--label);font-size:16px;line-height:21px">${escNote(t)}</div></div>${CHEV}</div>`
           : `<div class="navrow add" data-writenote="${d}"><span class="lbl">Add a Note</span></div>`; }
function dnoteLine(id, d){ const t = doseNote(id, d); return t ? `<div class="dnote">${PENCIL}<span>${escNote(t)}</span></div>` : ''; }
/* the dose sheet's note field saves as you type */
$('#doseNote').addEventListener('input', e => {
  const id = e.target.dataset.id; setDoseNote(id, e.target.value);
  const dt = document.querySelector(`[data-dt="${id}"]`), m = MEDS.find(x => x.id === id); if(dt && m) dt.innerHTML = `<span>${subtitle(m)}</span>`;
});

/* ---------- the report ---------- */
const HEX = {indigo:'#5856D6', pink:'#FF2D55', brown:'#A2845E', purple:'#AF52DE', blue:'#007AFF', red:'#FF3B30', yellow:'#F2C200', gray:'#8E8E93', orange:'#FF9500', green:'#34C759', accent:'#007A8F'};
const PRN_HIST = {you:{3:1, 9:1, 14:1}, mum:{2:1, 10:1, 14:2}};
function recordsFor(date){                       // what was due that day, and what was taken
  const H = hist(), m = date.getMonth(), d = date.getDate();
  if(m === 10) return H[d] || null;
  const tpl = H[1]; if(!tpl) return null;        // no history before Hävn was set up
  const miss = {'9-25':[1], '9-11':[2], '8-19':[0], '8-5':[3], '7-29':[1], '7-22':[0,2]}[`${m}-${d}`] || [];
  return tpl.map((r, i) => ({...r, v:miss.includes(i) ? 0 : 1}));
}
function reportModel(days){
  const end = new Date(2026, 10, 17), list = [];
  for(let k = days - 1; k >= 0; k--){ const date = new Date(2026, 10, 17 - k); const recs = recordsFor(date); list.push({date, recs}); }
  const withData = list.filter(x => x.recs), meds = {};
  let tot = 0, tk = 0; const missed = [];
  withData.forEach(({date, recs}) => recs.forEach(r => { tot++; tk += r.v; const M = meds[r.n] = meds[r.n] || {n:r.n, d:r.d, c:r.c, t:new Set(), tot:0, tk:0};
    M.t.add(r.t); M.tot++; M.tk += r.v; if(!r.v) missed.push({date, r, note:date.getMonth() === 10 ? doseNote(r.id, date.getDate()) : ''}); }));
  const notes = [];
  withData.forEach(({date}) => { if(date.getMonth() !== 10) return; const d = date.getDate(); const dn = dayNote(d); if(dn) notes.push({date, text:dn});
    Object.entries(notesOf().dose).forEach(([k, v]) => { const [dd, id] = k.split(':'); if(+dd === d){ const r = (hist()[d] || []).find(x => x.id === id); notes.push({date, text:v, med:r ? r.n : ''}); } }); });
  const prn = []; const ph = PRN_HIST[cur] || {};
  withData.forEach(({date}) => { const n = date.getMonth() === 10 ? ph[date.getDate()] : 0; if(n && PRN[0]) prn.push({date, n:PRN[0].n, d:PRN[0].d, count:n}); });
  return {list, withData, meds:Object.values(meds), tot, tk, missed:missed.reverse(), notes:notes.reverse(), prn:prn.reverse(), start:list[0].date, end};
}
const fmt = (d, o = {weekday:'short', day:'numeric', month:'short'}) => d.toLocaleDateString('en-GB', o);
function rRing(states, cols, size){
  const r = 15, sw = 5, C = 2*Math.PI*r, n = Math.max(1, states.length), cap = (sw/2)/r*180/Math.PI, gap = n > 1 ? 2*cap + 7 : 0, len = n > 1 ? (360/n - gap)/360*C : C;
  return `<svg width="${size}" height="${size}" viewBox="0 0 40 40">${states.map((v, i) => `<circle cx="20" cy="20" r="${r}" fill="none" stroke="${v ? HEX[cols[i]] || '#007A8F' : '#E6E9EB'}" stroke-width="${sw}" stroke-linecap="round" stroke-dasharray="${len} ${C}" transform="rotate(${-90 + i*360/n + gap/2} 20 20)"/>`).join('')}</svg>`;
}
const REPORT_CSS = `
.rpt{font-family:Inter,-apple-system,'SF Pro Text',system-ui,sans-serif;color:#14191B;-webkit-font-smoothing:antialiased}
.rpt .page{width:794px;height:1123px;background:#fff;box-sizing:border-box;padding:54px 60px 70px;position:relative;overflow:hidden}
.rpt .hd{display:flex;justify-content:space-between;align-items:flex-end;border-bottom:2px solid #14191B;padding-bottom:16px}
.rpt .brand{display:flex;align-items:center;gap:8px;font-size:13px;font-weight:600;color:#566166}
.rpt h1{font-size:30px;line-height:36px;margin:8px 0 0;letter-spacing:-.4px;font-weight:700}
.rpt .meta{font-size:12.5px;line-height:19px;color:#566166;text-align:right}.rpt .meta b{color:#14191B;font-weight:600}
.rpt .tiles{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-top:22px}
.rpt .tl{border:1px solid #E1E5E7;border-radius:12px;padding:12px 14px}
.rpt .tl b{display:block;font-size:24px;line-height:30px;font-weight:700}.rpt .tl span{font-size:11.5px;color:#566166}
.rpt h2{font-size:15px;margin:28px 0 10px;font-weight:700}
.rpt h2 small{font-weight:400;color:#566166;font-size:12px;margin-left:8px}
.rpt table{width:100%;border-collapse:collapse;font-size:12.5px}
.rpt th{text-align:left;font-weight:600;color:#566166;font-size:10.5px;letter-spacing:.4px;text-transform:uppercase;padding:6px 8px;border-bottom:1px solid #D5DADD}
.rpt td{padding:8px;border-bottom:1px solid #EEF1F2;vertical-align:top;line-height:17px}
.rpt .sw{display:inline-block;width:9px;height:9px;border-radius:5px;margin-right:8px;vertical-align:0}
.rpt .bar{height:6px;border-radius:3px;background:#EEF1F2;width:84px;display:inline-block;vertical-align:middle;margin-right:8px;position:relative;overflow:hidden}
.rpt .bar i{position:absolute;left:0;top:0;bottom:0;background:#007A8F;border-radius:3px}
.rpt .cal{display:grid;grid-template-columns:repeat(10,1fr);gap:10px 4px}
.rpt .cd{display:flex;flex-direction:column;align-items:center;gap:2px;font-size:10px;color:#566166}
.rpt .heat{display:grid;grid-template-columns:repeat(15,1fr);gap:5px}.rpt .heat i{height:22px;border-radius:5px;display:block}
.rpt .legend{display:flex;gap:16px;font-size:11px;color:#566166;margin-top:10px}.rpt .legend i{display:inline-block;width:9px;height:9px;border-radius:5px;margin-right:5px;vertical-align:-1px}
.rpt .miss{color:#C93400;font-weight:600}
.rpt .note{border-left:3px solid #007A8F;padding:1px 0 1px 12px;margin:0 0 12px}
.rpt .note .d{font-size:11px;color:#566166;font-weight:600}.rpt .note p{margin:2px 0 0;font-size:13px;line-height:19px}
.rpt .disc{margin-top:24px;padding:12px 14px;border-radius:10px;background:#F4F6F7;font-size:11.5px;line-height:17px;color:#566166}
.rpt .ft{position:absolute;left:60px;right:60px;bottom:28px;display:flex;justify-content:space-between;font-size:10.5px;color:#8A9599;border-top:1px solid #EEF1F2;padding-top:10px}
.rpt .muted{color:#8A9599}
.rpt .pat{margin-top:26px;padding:14px 16px;border:1px solid #E1E5E7;border-radius:12px}.rpt .pat p{margin:4px 0 0;font-size:13px;line-height:19px}`;
function reportPages({days = 30, name = '', notes = true, prn = true} = {}){
  const M = reportModel(days), made = 'Wednesday 18 November 2026', P = 2;
  const pct = M.tot ? Math.round(M.tk / M.tot * 100) : 0;
  const nm = cleanNote(name) || (cur === 'you' ? '' : PEOPLE[cur].name);
  const foot = i => `<div class="ft"><span>Made with Hävn on this iPhone · ${made}</span><span>Page ${i} of ${P}</span></div>`;
  const logo = `<div class="brand">${rRing([1,1,1,1], ['indigo','pink','brown','purple'], 22)}Hävn</div>`;
  const empty = !M.withData.length;
  const cal = days <= 31
    ? `<div class="cal">${M.list.map(({date, recs}) => `<div class="cd">${recs ? rRing(recs.map(r => r.v), recs.map(r => r.c), 34) : rRing([0], ['gray'], 34)}<span>${fmt(date, {day:'numeric', month:'short'})}</span></div>`).join('')}</div>
       <div class="legend"><span>Each ring is one day; a coloured piece is a dose taken, a grey piece a dose missed.</span></div>`
    : `<div class="heat">${M.list.map(({date, recs}) => { const t = recs ? recs.filter(r => r.v).length / recs.length : -1;
        return `<i title="${fmt(date)}" style="background:${t < 0 ? '#F4F6F7' : t === 1 ? '#34C759' : t > 0 ? '#FF9500' : '#C93400'}"></i>`; }).join('')}</div>
       <div class="legend"><span><i style="background:#34C759"></i>All taken</span><span><i style="background:#FF9500"></i>Some missed</span><span><i style="background:#C93400"></i>None taken</span><span><i style="background:#F4F6F7;box-shadow:inset 0 0 0 1px #E1E5E7"></i>Before Hävn</span></div>`;
  const patterns = (() => {                // plain sentences a doctor can read in five seconds
    if(!M.missed.length) return '';
    const by = f => { const o = {}; M.missed.forEach(x => { const k = f(x); o[k] = (o[k] || 0) + 1; }); const mx = Math.max(...Object.values(o)); return [Object.keys(o).filter(k => o[k] === mx), mx]; };
    const join = a => a.length > 1 ? a.slice(0, -1).join(', ') + ' and ' + a.slice(-1) : a[0];
    const [wd, nw] = by(x => x.date.toLocaleDateString('en-GB', {weekday:'long'}) + 's'), [md, nm2] = by(x => x.r.n), [pt, np] = by(x => groupOf(x.r.t).toLowerCase());
    const L = [];
    if(nw > 1) L.push(`Most missed doses were on ${join(wd)} (${nw}${wd.length > 1 ? ' each' : ''} of ${M.missed.length}).`);
    if(nm2 > 1) L.push(`${join(md)} ${md.length > 1 ? 'were' : 'was'} missed most often (${nm2} time${nm2 > 1 ? 's' : ''}${md.length > 1 ? ' each' : ''}).`);
    if(np > 1) { const t = join(pt); L.push(`${t[0].toUpperCase() + t.slice(1)} doses were missed most (${np}${pt.length > 1 ? ' each' : ''}).`); }
    return L.length ? `<div class="pat"><h2 style="margin:0 0 8px">Patterns</h2>${L.map(l => `<p>${l}</p>`).join('')}</div>` : '';
  })();
  const p1 = `<div class="page"><div class="hd"><div>${logo}<h1>Medication report</h1></div>
      <div class="meta"><div>Name: <b>${nm ? escNote(nm) : '<span class="muted">not added</span>'}</b></div><div>Period: <b>${fmt(M.start, {day:'numeric', month:'short', year:'numeric'})} – ${fmt(M.end, {day:'numeric', month:'short', year:'numeric'})}</b></div><div>${days} days · made ${fmt(new Date(2026, 10, 18), {day:'numeric', month:'short', year:'numeric'})}</div></div></div>
    ${empty ? `<p style="margin-top:40px;font-size:15px">No doses were logged in this period yet.</p>` : `
    <div class="tiles"><div class="tl"><b>${pct}%</b><span>of doses taken</span></div><div class="tl"><b>${M.tk} of ${M.tot}</b><span>doses taken</span></div>
      <div class="tl"><b class="${M.tot - M.tk ? 'miss' : ''}">${M.tot - M.tk}</b><span>doses missed</span></div><div class="tl"><b>${M.prn.reduce((a, x) => a + x.count, 0)}</b><span>as-needed doses</span></div></div>
    <h2>Medications</h2><table><tr><th>Medication</th><th>Schedule</th><th>Taken</th><th>Missed</th></tr>
      ${M.meds.map(m => `<tr><td><span class="sw" style="background:${HEX[m.c]}"></span><b style="font-weight:600">${escNote(m.n)}</b> <span class="muted">${escNote(m.d)}</span></td><td>Every day, ${[...m.t].join(', ')}</td>
        <td><span class="bar"><i style="width:${Math.round(m.tk / m.tot * 100)}%"></i></span>${m.tk} of ${m.tot}</td><td class="${m.tot - m.tk ? 'miss' : ''}">${m.tot - m.tk || '–'}</td></tr>`).join('')}
      ${prn && PRN.length ? PRN.map(m => `<tr><td><span class="sw" style="background:${HEX[m.c]}"></span><b style="font-weight:600">${escNote(m.n)}</b> <span class="muted">${escNote(m.d)}</span></td><td>As needed, up to ${m.max} a day</td><td colspan="2">${M.prn.filter(x => x.n === m.n).reduce((a, x) => a + x.count, 0)} taken in this period</td></tr>`).join('') : ''}</table>
    <h2>Day by day<small>${days <= 31 ? 'one ring per day' : 'one square per day'}</small></h2>${cal}${patterns}`}
    ${foot(1)}</div>`;
  const p2 = `<div class="page"><div class="hd"><div>${logo}<h1 style="font-size:22px;line-height:28px">Missed doses${notes ? ' and notes' : ''}</h1></div><div class="meta">${nm ? escNote(nm) + '<br>' : ''}${fmt(M.start, {day:'numeric', month:'short'})} – ${fmt(M.end, {day:'numeric', month:'short', year:'numeric'})}</div></div>
    <h2>Missed doses<small>${M.missed.length ? `${M.missed.length}, most recent first` : 'none'}</small></h2>
    ${M.missed.length ? `<table><tr><th>Date</th><th>Medication</th><th>Due</th>${notes ? '<th>Note</th>' : ''}</tr>${M.missed.slice(0, 12).map(x => `<tr><td>${fmt(x.date)}</td><td><span class="sw" style="background:${HEX[x.r.c]}"></span>${escNote(x.r.n)}</td><td>${x.r.t}</td>${notes ? `<td>${x.note ? escNote(x.note) : '<span class="muted">–</span>'}</td>` : ''}</tr>`).join('')}</table>
      ${M.missed.length > 12 ? `<p class="muted" style="font-size:12px">and ${M.missed.length - 12} earlier</p>` : ''}` : '<p class="muted">Every dose in this period was taken.</p>'}
    ${notes ? `<h2>Notes<small>written in Hävn</small></h2>${M.notes.length ? M.notes.slice(0, 8).map(n => `<div class="note"><div class="d">${fmt(n.date, {weekday:'long', day:'numeric', month:'long'})}${n.med ? ' · ' + escNote(n.med) : ''}</div><p>${escNote(n.text)}</p></div>`).join('') : '<p class="muted">No notes in this period.</p>'}` : ''}
    ${prn && M.prn.length ? `<h2>As-needed doses</h2><table><tr><th>Date</th><th>Medication</th><th>Doses</th></tr>${M.prn.map(x => `<tr><td>${fmt(x.date)}</td><td>${escNote(x.n)} <span class="muted">${escNote(x.d)}</span></td><td>${x.count}</td></tr>`).join('')}</table>` : ''}
    <div class="disc">This report lists doses as they were logged in Hävn by the person using the app. It is not a medical record and Hävn doesn’t give medical advice. It was made on the iPhone and wasn’t uploaded anywhere.</div>
    ${foot(2)}</div>`;
  return [p1, p2];
}
function reportDoc(opts){ return `<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Hävn report</title>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap">
  <style>@page{size:A4;margin:0}html,body{margin:0}.rpt .page{page-break-after:always}${REPORT_CSS}</style></head><body class="rpt">${reportPages(opts).join('')}</body></html>`; }
window.__havnReport = reportDoc;   // used by the design tooling to save the sample PDF

function reportCard(){ return `<div class="card" style="margin-top:12px;padding:16px 18px"><div style="display:flex;gap:12px;align-items:center"><span class="tile" style="background:var(--accent);color:var(--on-accent)">${T_DOC}</span>
  <div class="mid"><div class="nm">Report for Your Doctor</div><div class="dt">A PDF of your doses and notes to show at an appointment.</div></div></div>
  <button class="btn2 p" data-report="1" style="margin-top:14px">Create PDF Report</button></div>`; }
const T_DOC = '<svg width="14" height="17" viewBox="0 0 14 17"><path d="M2 1h7l4 4v10a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1z" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round"/><path d="M4 9h6M4 12h4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>';
let rp = {days:30, notes:true, prn:true, name:''};
function openReportOptions(){
  const sh = appSheet(`<h3>Report for Your Doctor</h3><p class="sub">Made on this iPhone. Nothing is uploaded; you choose where it goes.</p>
    <div class="field"><input id="rpName" placeholder="Name on the report (optional)" autocomplete="off" value="${escNote(rp.name || (cur === 'you' ? '' : PEOPLE[cur].name))}"></div>
    <div class="lbl3">Period</div><div class="seg" id="rpPeriod"><button class="${rp.days === 30 ? 'on' : ''}" data-rp="30">Last 30 Days</button><button class="${rp.days === 90 ? 'on' : ''}" data-rp="90">Last 90 Days</button></div>
    <div class="card" style="margin-top:14px"><div class="srow"><span class="lbl">Include Notes</span>${swH('Include Notes', rp.notes)}</div><div class="srow"><span class="lbl">Include As-Needed Doses</span>${swH('Include As-Needed Doses', rp.prn)}</div></div>
    <button class="btn2 p" data-mk="1" style="margin-top:16px">Create PDF</button><button class="ob-link" data-close="1">Cancel</button>`);
  sh.querySelectorAll('.sw').forEach(sw => { wireSwitch(sw); sw.addEventListener('pointerup', () => setTimeout(() => { const on = sw.classList.contains('on');
    if(sw.getAttribute('aria-label') === 'Include Notes') rp.notes = on; else rp.prn = on; }, 0)); });
  sh.querySelector('#rpPeriod').onclick = e => { const b = e.target.closest('[data-rp]'); if(!b) return; rp.days = +b.dataset.rp; sh.querySelectorAll('[data-rp]').forEach(x => x.classList.toggle('on', x === b)); };
  sh.querySelector('[data-mk]').onclick = e => { rp.name = sh.querySelector('#rpName').value; const b = e.currentTarget;
    b.innerHTML = '<span class="spin" style="width:18px;height:18px;border-color:rgba(255,255,255,.35);border-top-color:#fff"></span>';
    setTimeout(() => closeAppSheet(openReportPreview), reduce ? 0 : 700); };
}
function openReportPreview(){
  const sh = $('#reportSheet'), pages = reportPages(rp), sc = 354/794;
  $('#reportBody').innerHTML = `<style>${REPORT_CSS}</style>` + pages.map((p, i) => `<div class="rpv" style="height:${Math.round(1123*sc)}px"><div class="rpt" style="transform:scale(${sc})">${p}</div></div>`).join('')
    + `<p class="foot" style="text-align:center;padding-bottom:20px">${pages.length} pages · A4</p>`;
  $('#reportBody').scrollTop = 0;
  sh.classList.add('open'); $('#dim').classList.add('on');
  if(!reduce){ sh.animate([{transform:'translateY(105%)'}, {transform:'none'}], {duration:560, easing:'cubic-bezier(.2,.95,.25,1.02)'});
    $('#reportBody').querySelectorAll('.rpv').forEach((p, i) => p.animate([{opacity:0, transform:'translateY(24px) scale(.97)'}, {opacity:1, transform:'none'}], {duration:620, delay:220 + i*120, easing:'cubic-bezier(.2,.8,.2,1)', fill:'backwards'})); }
}
function closeReport(){ const sh = $('#reportSheet'); if(!sh.classList.contains('open')) return; $('#dim').classList.remove('on');
  if(reduce){ sh.classList.remove('open'); return; }
  sh.animate([{transform:'none'}, {transform:'translateY(105%)'}], {duration:420, easing:'cubic-bezier(.4,0,.2,1)'}).onfinish = () => sh.classList.remove('open'); }
$('#reportDone').onclick = closeReport;
$('#reportShare').onclick = () => {
  const s = appSheet(`<div class="card" style="margin-top:12px"><div class="file" style="cursor:default"><div class="fic" style="color:#C93400">${T_DOC}</div><div class="mid"><div class="nm" style="font-size:16px">Hävn Report 18 Nov 2026.pdf</div><div class="dt" style="font-size:13px">2 pages · A4 · made on this iPhone</div></div></div></div>
    <div class="card" style="margin-top:12px">${[['files','Save to Files'], ['print','Print'], ['send','Send a Copy…']].map(([k, l]) => `<div class="navrow" data-act="${k}"><span class="lbl">${l}</span>${CHEV}</div>`).join('')}</div>
    <p class="foot">Sending a copy uses the app you pick. Hävn itself never uploads it.</p><button class="ob-link" data-close="1">Cancel</button>`);
  s.style.zIndex = 79; $('#appDim').style.zIndex = 78;
  s.addEventListener('click', e => { const a = e.target.closest('[data-act]'); if(!a) return;
    const msg = {files:'Saved to Files', print:'Opening Print', send:'Choose where to send it'}[a.dataset.act];
    closeAppSheet(() => { s.style.zIndex = ''; $('#appDim').style.zIndex = 77; toast(msg); }); }, {once:true});
};
