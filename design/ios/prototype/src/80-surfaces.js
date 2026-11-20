/* ================= round 3 · surfaces: Simple mode, the alarm, Apple Watch, the Dynamic Island, Control Center and the Action Button, Siri, StandBy, Focus, sharing ================= */
const X = $('#xScene'), XS = ['alarm', 'watch', 'standby', 'cc', 'siri'];
const T_SPEAK = '<svg width="17" height="15" viewBox="0 0 17 15"><path d="M1 5h3l4-3.5v12L4 10H1z" fill="#fff"/><path d="M11 4.5a4 4 0 0 1 0 6M13.4 2.2a7.2 7.2 0 0 1 0 10.6" fill="none" stroke="#fff" stroke-width="1.6" stroke-linecap="round"/></svg>';
const T_SIMPLE = '<svg width="16" height="16" viewBox="0 0 16 16"><rect x="1" y="1" width="6" height="6" rx="1.8" fill="#fff"/><rect x="9" y="1" width="6" height="6" rx="1.8" fill="#fff"/><rect x="1" y="9" width="14" height="6" rx="1.8" fill="#fff"/></svg>';
const T_MOON = '<svg width="14" height="14" viewBox="0 0 14 14"><path d="M9.5 10.8A5.6 5.6 0 0 1 5 1.2a5.8 5.8 0 1 0 7.6 7.6 5.6 5.6 0 0 1-3.1 2z" fill="#fff"/></svg>';
const T_WORK = '<svg width="16" height="14" viewBox="0 0 16 14"><rect x="1" y="4" width="14" height="9" rx="2" fill="#fff"/><path d="M5.5 4V2.5a1.2 1.2 0 0 1 1.2-1.2h2.6a1.2 1.2 0 0 1 1.2 1.2V4" fill="none" stroke="#fff" stroke-width="1.5"/></svg>';
const HAVN_RING = s => miniRing([1,1,1,1], s, ['indigo','pink','brown','purple']);
const spoken = d => d.replace(/\bmg\b/, 'milligrams').replace(/\bmcg\b/, 'micrograms').replace(/\bIU\b/, 'units').replace(/\bmL\b/, 'millilitres').replace(/\bg\b/, 'grams');
function speak(text){ try { if(!window.speechSynthesis) return; speechSynthesis.cancel(); const u = new SpeechSynthesisUtterance(text); u.rate = .95; u.lang = 'en-GB'; speechSynthesis.speak(u); } catch(_){} }
const minsTo = t => tmin(t) - tmin(now);

/* ---------- scenes: the extra ones live in #xScene ---------- */
let xTimers = [], siriRaf = 0, hint0 = null, sbNight = false, xPrev = 'app';
const setScene0 = setScene;
setScene = (s, instant) => {
  if(xscene) leaveX();
  if(XS.includes(s)) return enterX(s);
  setScene0(s, instant); setTimeout(dynSync, reduce ? 0 : 520);
};
function sceneHint(t){ const h = $('.hint'); if(hint0 === null) hint0 = h.textContent; h.textContent = t || hint0; }
function enterX(s){
  if(!$('#ob').hidden){ hideOnboarding(); refreshToday(false); }
  closeAllSheets(); closePeopleMenu(); xPrev = ['home', 'lock'].includes(scene) ? scene : 'app'; xscene = s; scene = s; markScene(); saveCur();
  X.className = 'scene'; X.hidden = false; X.style.zIndex = '';
  if(s === 'alarm') drawAlarm();
  if(s === 'watch') drawWatch('face');
  if(s === 'standby') enterStandby();
  if(s === 'cc') drawCC();
  if(s === 'siri') drawSiri(0);
  if(!reduce && s !== 'standby' && s !== 'siri') X.animate([{opacity:0, transform:'scale(1.04)'}, {opacity:1, transform:'none'}], {duration:420, easing:'cubic-bezier(.2,.9,.25,1)'});
  dynSync();
}
function leaveX(){
  const was = xscene; xscene = null; xTimers.forEach(clearTimeout); xTimers = []; cancelAnimationFrame(siriRaf);
  try { window.speechSynthesis && speechSynthesis.cancel(); } catch(_){}
  X.hidden = true; X.innerHTML = ''; X.className = 'scene'; X.style.zIndex = '';
  $('#phone').classList.remove('watchmode', 'landscape'); $('#stage').classList.remove('noab');
  if(was === 'standby'){ $('#stage').style.width = ''; fit(); setPs(); }
  sceneHint(''); scene = 'app'; setSceneClass('app'); $('#homeScene').hidden = true; $('#lockScene').hidden = true;
}
document.querySelector('.scenes.more').addEventListener('click', e => { const b = e.target.closest('[data-scene]'); if(b) setScene(b.dataset.scene); });
const later = (ms, fn) => xTimers.push(setTimeout(fn, reduce ? 0 : ms));

/* ---------- Simple mode: one big button for the next dose ---------- */
const BIGCHECK = '<svg width="96" height="96" viewBox="0 0 96 96"><circle cx="48" cy="48" r="46" fill="var(--green)"/><path d="M28 49l13 13 27-29" fill="none" stroke="#fff" stroke-width="8" stroke-linecap="round" stroke-linejoin="round"/></svg>';
function setSimple(on){
  simpleOn = on; $('#phone').classList.toggle('simple', on); if(on){ if(allMode) closeEveryone(true); drawSimple(); if(!reduce) $('#simple').animate([{opacity:0}, {opacity:1}], {duration:360}); }
  syncStates();
}
function drawSimple(){
  const S = $('#simple'), n = MEDS.length, d = doneCount(), nx = nextMed(), over = nx && nx.t < now;
  S.innerHTML = `<div class="ltitle" style="font-size:40px;line-height:48px">${cur === 'you' ? 'Today' : PEOPLE[cur].name}</div><div class="lsub" style="font-size:20px;line-height:26px;margin-top:2px">Wednesday, 18 November</div>
    <div class="bigdose" id="bigdose">${nx ? `<div style="display:grid;place-items:center">${icon(nx, 96)}</div><div style="font:700 34px/40px var(--rounded);margin-top:12px">${nx.n}</div>
      <div style="font-size:22px;line-height:28px;margin-top:4px;color:${over ? 'var(--orange-text)' : 'var(--l2)'}">${nx.d} · ${over ? 'due at ' : ''}${nx.t}</div><button class="bigbtn" data-stake="${nx.id}">I Took It</button>`
      : `<div style="display:grid;place-items:center">${n ? BIGCHECK : wRing(96)}</div><div style="font:700 30px/36px var(--rounded);margin-top:14px">${n ? 'All done today' : 'Nothing today'}</div><div style="font-size:20px;color:var(--l2);margin-top:4px">${n ? 'Rest easy.' : 'Ask whoever helps you to add a medication.'}</div>`}</div>
    <div class="card" style="margin-top:18px">${MEDS.map(m => `<div class="bigrow">${icon(m, 44)}<div class="mid"><div style="font-weight:600">${m.n}</div><div style="font-size:18px;color:var(--l2)">${taken[m.id] ? 'Taken ' + taken[m.id] : m.t}</div></div>${taken[m.id] ? CHECK22 : ''}</div>`).join('')}</div>
    <div style="display:flex;justify-content:center;gap:12px;align-items:center;margin-top:18px;font-size:20px;font-weight:600">${wRing(44)}${d} of ${n} taken</div>
    <button class="ob-link" data-sexit="1" style="margin:22px auto 0;font-size:17px">Leave Simple Mode</button>`;
}
$('#simple').addEventListener('click', e => {
  const b = e.target.closest('[data-stake]');
  if(b){ const m = MEDS.find(x => x.id === b.dataset.stake); logDose(m.id, b);
    const box = $('#bigdose'); box.innerHTML = `<div style="display:grid;place-items:center">${BIGCHECK}</div><div style="font:700 30px/36px var(--rounded);margin-top:14px">${m.n} logged</div><div style="font-size:20px;color:var(--l2);margin-top:4px">at ${taken[m.id]}</div>`;
    if(!reduce) box.firstElementChild.animate([{transform:'scale(.4)', opacity:0}, {transform:'scale(1.08)', opacity:1, offset:.7}, {transform:'none'}], {duration:560, easing:'cubic-bezier(.2,.9,.25,1)'});
    setTimeout(() => { if(simpleOn) drawSimple(); }, reduce ? 300 : 1900); return; }
  if(e.target.closest('[data-sexit]')) showAlert({title:'Leave Simple Mode?', body:'Hävn goes back to the full app with tabs, history and settings.', buttons:[{label:'Stay'}, {label:'Leave', style:'def', onTap:() => setSimple(false)}]});
});
const refreshToday1 = refreshToday;
refreshToday = reveal => { refreshToday1(reveal); if(simpleOn) drawSimple(); };

/* ---------- the alarm: it says the name out loud if you want it to ---------- */
let alarmMed = null;
function drawAlarm(){
  const m = alarmMed = nextMed() || MEDS[0];
  if(!m){ X.innerHTML = `<div class="wall" style="filter:brightness(.4)"></div><div class="alrm"><div class="al-ttl" style="margin-top:300px">No alarms set</div></div>`; return; }
  const who = cur === 'you' ? 'your' : PEOPLE[cur].name + '’s', line = `Time for ${who} ${m.n}, ${spoken(m.d)}.`;
  X.innerHTML = `<div class="wall" style="filter:brightness(.42) saturate(1.2)"></div><div class="alrm">
    <div class="al-app">${HAVN_RING(18)}Hävn · Alarm</div><div class="al-time">${m.t}</div>
    <div class="al-card glass2"><div style="display:flex;gap:12px;align-items:center">${icon(m, 52)}<div class="mid"><div class="al-ttl">${m.n} ${m.d}</div><div class="al-sub">${cur === 'you' ? '' : 'For ' + PEOPLE[cur].name + ' · '}${m.g} dose</div></div></div>
      ${speakNames ? `<div class="al-say" id="alSay">${T_SPEAK}<span>“${line}”</span></div>` : ''}</div>
    <div class="al-btns"><button class="al-b snooze" data-al="snooze">Snooze</button><button class="al-b stop" data-al="taken">Taken</button></div></div>`;
  Snd.play('reminder'); const ring = () => { if(xscene === 'alarm'){ Snd.play('reminder'); later(2600, ring); } }; later(2600, ring);
  if(speakNames){ later(700, () => speak(line)); if(!reduce) $('#alSay').animate([{opacity:0, transform:'translateY(8px)'}, {opacity:1, transform:'none'}], {duration:420, delay:600, fill:'backwards'}); }
  if(!reduce) X.querySelector('.al-time').animate([{transform:'scale(1)'}, {transform:'scale(1.03)'}, {transform:'scale(1)'}], {duration:1300, iterations:Infinity, easing:'ease-in-out'});
}
X.addEventListener('click', e => {
  if(xscene !== 'alarm') return; const b = e.target.closest('[data-al]'); if(!b || !alarmMed) return;
  xTimers.forEach(clearTimeout); xTimers = []; try { window.speechSynthesis && speechSynthesis.cancel(); } catch(_){}
  const m = alarmMed;
  if(b.dataset.al === 'taken'){ if(!taken[m.id]) quiet(() => logDose(m.id)); haptic(b, 'success haptic'); Snd.play('taken');
    b.closest('.al-btns').innerHTML = `<div class="al-done">${CHECK22}${m.n} logged at ${taken[m.id]}</div>`; }
  else { haptic(b, 'light haptic'); b.closest('.al-btns').innerHTML = `<div class="al-done">Snoozed until ${tstr(tmin(now) + 10)}</div>`; }
  setTimeout(() => setScene('lock'), reduce ? 200 : 1300);
});

/* ---------- Apple Watch: the ring on your wrist ---------- */
function drawWatch(view){
  $('#phone').classList.add('watchmode'); $('#stage').classList.add('noab'); X.classList.add('clear');
  sceneHint('Tap the Hävn complication to open the app on the watch. Press the crown to go back to the face.');
  X.innerHTML = `<div class="watch"><div class="scr" id="wscr">${view === 'app' ? watchApp() : watchFace()}</div><button class="crown" data-wcrown="1" aria-label="Digital Crown"></button><div class="side"></div></div><div class="wlabel">Apple Watch · Hävn</div>`;
}
function watchFace(){
  const n = MEDS.length, d = doneCount(), nd = nextDue();
  return `<div class="wface"><div style="display:flex;justify-content:space-between;align-items:flex-start"><span style="font:600 14px var(--sys);color:#FF9F0A;margin-top:6px">WED 18</span><span style="font:600 46px/1 var(--rounded);letter-spacing:-1px">${now}</span></div>
    <div style="display:flex;gap:8px;margin-top:12px">${['#30D158', '#40C8E0', '#FF9F0A'].map((c, i) => `<span class="wcirc" style="border-color:${c}"><i style="background:${c};height:${[60, 35, 80][i]}%"></i></span>`).join('')}</div>
    <div class="wcomp" data-wopen="1">${ringOf(MEDS, taken, 46, {sw:14, col:(m, on) => on ? `var(--${m.c})` : 'rgba(255,255,255,.22)', check:'#30D158'})}<div style="min-width:0">
      <div style="font:600 12px var(--sys);color:#40C8E0;letter-spacing:.4px">HÄVN</div><div style="font:600 15px/18px var(--sys);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${nd.m ? (nd.m.t < now ? nd.m.n + ' · due' : 'Next ' + nd.m.t) : 'All done'}</div>
      <div style="font-size:13px;color:rgba(255,255,255,.65)">${d} of ${n} taken</div></div></div></div>`;
}
function watchApp(){
  const nx = nextMed(), n = MEDS.length, d = doneCount();
  return `<div class="wapp"><div style="display:flex;justify-content:space-between;font:600 14px var(--sys)"><span style="color:#40C8E0">Hävn</span><span>${now}</span></div>
    <div style="display:grid;place-items:center;margin-top:6px;position:relative" id="wring">${ringOf(MEDS, taken, 96, {sw:13, key:'w:', col:(m, on) => on ? `var(--${m.c})` : 'rgba(255,255,255,.2)', check:'#30D158'})}
      ${d < n ? `<span style="position:absolute;font:600 20px var(--rounded)">${d}/${n}</span>` : ''}</div>
    ${nx ? `<div style="margin-top:8px;font:600 16px/19px var(--sys)">${nx.n} <span style="color:rgba(255,255,255,.6);font-weight:400">${nx.d}</span></div><div style="font-size:13px;color:${nx.t < now ? '#FF9F0A' : 'rgba(255,255,255,.65)'}">${nx.t < now ? 'Due at ' : ''}${nx.t}</div>
      <button class="wbtn3" data-wtake="${nx.id}" style="margin-top:8px">Taken</button>` : `<div style="margin-top:12px;font:600 16px var(--sys);text-align:center">All done today</div>`}</div>`;
}
X.addEventListener('click', e => {
  if(xscene !== 'watch') return;
  if(e.target.closest('[data-wopen]')){ $('#wscr').innerHTML = watchApp(); haptic($('#wscr'), 'wrist tap'); if(!reduce) $('#wscr').firstElementChild.animate([{transform:'scale(.6)', opacity:0}, {transform:'none', opacity:1}], {duration:420, easing:'cubic-bezier(.2,.9,.25,1.05)'}); return; }
  if(e.target.closest('[data-wcrown]')){ $('#wscr').innerHTML = watchFace(); if(!reduce) $('#wscr').firstElementChild.animate([{transform:'scale(1.3)', opacity:0}, {transform:'none', opacity:1}], {duration:380, easing:'cubic-bezier(.2,.9,.25,1)'}); return; }
  const t = e.target.closest('[data-wtake]'); if(t){ const id = t.dataset.wtake; quiet(() => logDose(id)); Snd.play('taken'); haptic(t, 'wrist tap');
    $('#wscr').innerHTML = watchApp(); growSegs($('#wscr'), 'w:' + id);
    if(doneCount() === MEDS.length) setTimeout(() => Snd.play('close'), 700); }
});

/* ---------- the Dynamic Island: a Live Activity while a dose is due ---------- */
let dynBusy = 0;
function dueSoon(){ const nx = nextMed(); if(!nx) return null; const dm = minsTo(nx.t); return dm <= 30 ? {m:nx, dm} : null; }
function dynSync(){
  const D2 = $('#dyn'); if(dynBusy) return; const ds = dueSoon();
  if(!ds || !['home', 'cc'].includes(scene)){ D2.className = ''; D2.innerHTML = ''; return; }
  const {m, dm} = ds, late = dm < 0;
  D2.className = 'on compact';
  D2.innerHTML = `<div class="cl">${ringOf(MEDS, taken, 22, {sw:16, col:(x, on) => on ? `var(--${x.c})` : 'rgba(255,255,255,.25)', check:'#30D158'})}</div><div class="cr" style="color:${late ? '#FF9F0A' : '#40C8E0'}">${late ? `${-dm}m` : `${dm}m`}</div>
    <div class="ex"><div style="display:flex;gap:12px;align-items:center">${icon(m, 46)}<div class="mid"><div style="font:600 17px var(--sys)">${m.n} ${m.d}</div>
      <div style="font-size:14px;color:${late ? '#FF9F0A' : '#40C8E0'}">${late ? `Due at ${m.t} · ${-dm} min ago` : `Due at ${m.t} · in ${dm} min`}</div></div>${ringOf(MEDS, taken, 40, {sw:14, col:(x, on) => on ? `var(--${x.c})` : 'rgba(255,255,255,.25)'})}</div>
      <div style="display:flex;gap:8px;margin-top:14px"><button class="dbtn" data-dyn="later">Snooze</button><button class="dbtn g" data-dyn="taken" data-id="${m.id}">Taken</button></div></div>`;
}
function dynFlash(ic, text, color, ms = 2200){
  const D2 = $('#dyn'); clearTimeout(dynBusy); D2.className = 'on wide'; D2.innerHTML = `<div class="cl">${ic}</div><div class="cr" style="color:${color}">${text}</div>`;
  if(!reduce) D2.animate([{transform:'scale(.96)'}, {transform:'scale(1.03)'}, {transform:'none'}], {duration:420, easing:'cubic-bezier(.3,1.4,.5,1)'});
  dynBusy = setTimeout(() => { dynBusy = 0; dynSync(); }, ms);
}
const DCHECK = '<svg width="22" height="22" viewBox="0 0 22 22"><circle cx="11" cy="11" r="11" fill="#30D158"/><path d="M6.5 11.3l3 3 6-6.6" fill="none" stroke="#000" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>';
function afterQuietLog(id){ if(scene === 'home') refreshWidgets(id); if(scene === 'lock') drawLock(); if(xscene === 'cc') drawCC(); if(simpleOn) drawSimple(); }
$('#dyn').addEventListener('click', e => {
  const D2 = $('#dyn'), b = e.target.closest('[data-dyn]');
  if(b){ e.stopPropagation();
    if(b.dataset.dyn === 'taken'){ const m = MEDS.find(x => x.id === b.dataset.id); quiet(() => logDose(m.id)); Snd.play('taken'); haptic(D2, 'success haptic', 201, 30); afterQuietLog(m.id); dynFlash(DCHECK, `${m.n} logged`, '#30D158'); }
    else { dynFlash('<span style="font-size:17px">⏱</span>', `Snoozed to ${tstr(tmin(now) + 10)}`, '#FF9F0A'); }
    return; }
  if(D2.classList.contains('compact')){ D2.classList.remove('compact'); D2.classList.add('big'); haptic(D2, 'press haptic', 201, 30); }
  else if(D2.classList.contains('big')){ D2.classList.remove('big'); D2.classList.add('compact'); }
});
/* the same Live Activity on the Lock Screen */
const drawLock1 = drawLock;
drawLock = () => {
  drawLock1(); const ds = dueSoon(); if(!ds) return; const {m, dm} = ds, L = $('#lockScene'), n = L.querySelectorAll('.notif2').length;
  const el = document.createElement('div'); el.className = 'la'; el.style.bottom = (130 + n*100) + 'px';
  el.innerHTML = `${icon(m, 44)}<div class="mid"><div style="font:600 16px var(--sys)">${showNames ? m.n + ' ' + m.d : 'Dose due'}</div><div style="font-size:14px;color:${dm < 0 ? '#FF9F0A' : '#40C8E0'}">${dm < 0 ? `Due at ${m.t} · ${-dm} min ago` : `Due at ${m.t}`}</div></div>
    ${ringOf(MEDS, taken, 38, {sw:14, col:(x, on) => on ? `var(--${x.c})` : 'rgba(255,255,255,.25)'})}<button class="dbtn g" data-la="${m.id}" style="flex:none;width:84px;height:38px">Taken</button>`;
  L.appendChild(el);
  el.querySelector('[data-la]').onclick = ev => { ev.stopPropagation(); quiet(() => logDose(m.id)); Snd.play('taken'); haptic(el, 'success haptic');
    if(!reduce) el.animate([{opacity:1}, {opacity:0, transform:'scale(.94)'}], {duration:320, fill:'forwards'}).onfinish = () => drawLock(); else drawLock(); };
};

/* ---------- Control Center, and the Action Button ---------- */
function drawCC(){
  const nx = nextMed(), c = (bg, g) => `<span class="ccdot" style="background:${bg}">${g}</span>`;
  X.innerHTML = `<div class="wall" style="filter:blur(22px) brightness(.5) saturate(1.5);transform:scale(1.15)"></div><div class="ccgrid">
    <div class="cc big ccconn">${c('#FF9F0A', '✈︎')}${c('#30D158', '▴')}${c('#0A84FF', '◠')}${c('#0A84FF', 'ᛒ')}</div>
    <div class="cc big ccmedia"><span style="opacity:.6;font-size:13px">Not Playing</span><span style="font-size:18px;letter-spacing:2px;opacity:.9;white-space:nowrap">◀︎◀︎&nbsp;&nbsp;▶︎&nbsp;&nbsp;▶︎▶︎</span></div>
    <div class="cc wide havncc" data-cclog="1" id="ccHavn">${HAVN_RING(30)}<div style="min-width:0"><div style="font:600 14px var(--sys);white-space:nowrap">Log Next Dose</div><div style="font-size:13px;opacity:.75;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${nx ? `${nx.n} · ${nx.t}` : 'All done today'}</div></div></div>
    <div class="cc round"></div><div class="cc round"></div>
    <div class="cc tall"><i class="ccbar" style="height:62%"></i></div><div class="cc tall"><i class="ccbar" style="height:40%"></i></div><div class="cc round"></div><div class="cc round"></div>
    <div class="cc round"></div><div class="cc round"></div></div>`;
  sceneHint('Tap Log Next Dose, or press the Action Button on the side of the phone. Other controls are placeholders.');
}
X.addEventListener('click', e => { if(xscene === 'cc' && e.target.closest('[data-cclog]')) actionPress($('#ccHavn')); });
function actionPress(src){
  if(xscene === 'watch' || xscene === 'standby') return;
  if(!src) haptic($('#phone'), 'Action Button', 14, 190);
  const nx = nextMed();
  if(!nx) return dynFlash(DCHECK, 'All done today', '#30D158');
  if(minsTo(nx.t) > 60) return dynFlash(`<span style="display:block;width:22px">${icon(nx, 22)}</span>`, `Next at ${nx.t}`, '#40C8E0');   // too early: say when, log nothing
  quiet(() => logDose(nx.id)); Snd.play('taken'); haptic(src || $('#phone'), 'success haptic', src ? undefined : 201, src ? undefined : 30);
  dynFlash(DCHECK, `${nx.n} logged`, '#30D158'); afterQuietLog(nx.id);
  if(src && !reduce) src.animate([{transform:'scale(.95)', background:'rgba(48,209,88,.5)'}, {transform:'none'}], {duration:520, easing:'cubic-bezier(.3,1.4,.5,1)'});
}
$('#actionBtn').onclick = () => actionPress();
$('#bAction').onclick = () => actionPress();
function setPs(){ $('#stage').style.setProperty('--ps', +$('#phone').dataset.s || 1); }
addEventListener('resize', () => { setPs(); if(xscene === 'standby') layStandby(); });
setPs();

/* ---------- Siri: ask, and log from the answer ---------- */
const QS = [['What do I take next in Hävn?', 'next'], ['Log my morning doses in Hävn', 'morning'], ['Did I take my Iron in Hävn?', 'did']];
function drawSiri(qi){
  X.classList.add('clear'); X.style.zIndex = 86;
  X.innerHTML = `<div class="siriglow" id="sglow"></div><div class="siritry">${QS.map(([q], i) => `<button data-sq="${i}" class="${i === qi ? 'on' : ''}">${q.replace(' in Hävn', '')}</button>`).join('')}</div>
    <div class="siriq" id="siriq"></div><div class="siricard" id="sircard" hidden></div>`;
  sceneHint('App Shortcuts need the app’s name in the phrase, so each one ends “in Hävn”. Tap a question to ask it.');
  const g = $('#sglow'); if(!reduce){ const t0 = performance.now(); const spin = t => { g.style.setProperty('--sa', ((t - t0)/12 % 360) + 'deg'); siriRaf = requestAnimationFrame(spin); }; siriRaf = requestAnimationFrame(spin);
    g.animate([{opacity:0}, {opacity:1}], {duration:500}); }
  ask(qi);
}
function ask(qi){
  const [q, k] = QS[qi], qe = $('#siriq'), card = $('#sircard'); card.hidden = true; qe.textContent = '';
  X.querySelectorAll('[data-sq]').forEach((b, i) => b.classList.toggle('on', i === qi));
  [...q].forEach((ch, i) => later(30 + i*28, () => { qe.textContent += ch; }));
  later(60 + q.length*28 + 450, () => { card.innerHTML = siriAnswer(k); card.hidden = false; Snd.play('on');
    if(!reduce) card.animate([{opacity:0, transform:'translateY(30px) scale(.96)'}, {opacity:1, transform:'none'}], {duration:520, easing:'cubic-bezier(.2,.9,.25,1.05)'}); });
}
function siriAnswer(k){
  const head = `<div style="display:flex;gap:6px;align-items:center;font:600 12px var(--sys);color:var(--l2);letter-spacing:.4px">${HAVN_RING(16)}HÄVN</div>`;
  const nx = nextMed(), iron = MEDS.find(m => m.id === 'iron');
  if(k === 'next'){ if(!nx) return `${head}<div class="sirisay">You’re all done for today.</div>`;
    const over = nx.t < now;
    if(minsTo(nx.t) > 60) return `${head}<div class="sirisay">${nx.n}, ${spoken(nx.d)}, at ${nx.t}. Nothing is due before then.</div>${miniStrip([nx])}`; return `${head}<div class="sirisay">${nx.n}, ${spoken(nx.d)}. ${over ? `It was due at ${nx.t}.` : `It’s at ${nx.t}.`}</div>
      <div style="display:flex;gap:12px;align-items:center;margin-top:10px">${icon(nx, 44)}<div class="mid"><div class="nm">${nx.n} ${nx.d}</div><div class="dt" style="${over ? 'color:var(--orange-text)' : ''}">${over ? `Due at ${nx.t} · ${-minsTo(nx.t)} min ago` : `At ${nx.t}`}</div></div><button class="pbtn sm" data-slog="${nx.id}">Log It</button></div>`; }
  if(k === 'morning'){ const mm = MEDS.filter(m => m.g === 'Morning'), left = mm.filter(m => !taken[m.id]);
    if(!mm.length) return `${head}<div class="sirisay">There are no morning doses today.</div>`;
    if(!left.length) return `${head}<div class="sirisay">They’re already logged: ${mm.map(m => m.n).join(' and ')}, at ${taken[mm[0].id]}.</div>${miniStrip(mm)}`;
    return `${head}<div class="sirisay">Log ${left.map(m => m.n).join(' and ')} as taken now?</div>${miniStrip(mm)}<button class="pbtn sm" data-slogall="Morning" style="margin-top:10px">Log ${left.length === 1 ? 'It' : 'Both'}</button>`; }
  if(!iron) return `${head}<div class="sirisay">I can’t find Iron in Hävn.</div>`;
  return taken.iron ? `${head}<div class="sirisay">Yes. You took Iron at ${taken.iron}.</div>${miniStrip([iron])}`
    : `${head}<div class="sirisay">Not yet. Iron was due at ${iron.t}.</div><div style="display:flex;gap:12px;align-items:center;margin-top:10px">${icon(iron, 44)}<div class="mid"><div class="nm">Iron ${iron.d}</div><div class="dt">Due at ${iron.t}</div></div><button class="pbtn sm" data-slog="iron">Log It</button></div>`;
}
const miniStrip = ms => `<div style="display:flex;gap:10px;margin-top:10px">${ms.map(m => `<div style="display:flex;gap:8px;align-items:center">${icon(m, 30)}<span class="dt">${m.n}${taken[m.id] ? ' ✓' : ''}</span></div>`).join('')}</div>`;
X.addEventListener('click', e => {
  if(xscene !== 'siri') return;
  const q = e.target.closest('[data-sq]'); if(q){ xTimers.forEach(clearTimeout); xTimers = []; ask(+q.dataset.sq); return; }
  const one = e.target.closest('[data-slog]'), all = e.target.closest('[data-slogall]');
  if(one || all){ const ids = one ? [one.dataset.slog] : MEDS.filter(m => m.g === all.dataset.slogall && !taken[m.id]).map(m => m.id);
    quiet(() => ids.forEach(id => logDose(id))); Snd.play('taken'); haptic(one || all, 'success haptic');
    const names = ids.map(id => MEDS.find(m => m.id === id).n).join(' and ');
    $('#sircard').innerHTML = `<div style="display:flex;gap:6px;align-items:center;font:600 12px var(--sys);color:var(--l2);letter-spacing:.4px">${HAVN_RING(16)}HÄVN</div><div class="sirisay">Done. ${names} ${ids.length > 1 ? 'are' : 'is'} logged at ${now}.</div>
      <div style="display:flex;gap:10px;align-items:center;margin-top:8px">${wRing(40)}<span class="dt">${doneCount()} of ${MEDS.length} taken today</span></div>`;
    return; }
  if(!e.target.closest('#sircard') && !e.target.closest('.siritry')) setScene(xPrev, true);
});

/* ---------- StandBy: on its side, charging, across the room ---------- */
function enterStandby(){
  sbNight = false; $('#phone').classList.add('landscape'); $('#stage').classList.add('noab');
  sceneHint('StandBy shows Hävn’s widget big, across the room. Tap the screen for night mode, which iPhone switches to in a dark room.');
  drawStandby(); layStandby();
  if(!reduce) $('#phone').animate([{opacity:0}, {opacity:1}], {duration:500});
}
function layStandby(){
  const st = $('#stage'), ph = $('#phone'); st.style.width = '640px';
  const s2 = Math.min(1, st.clientWidth / 874); ph.style.transform = `translate(${874*s2}px,0) rotate(90deg) scale(${s2})`; st.style.height = (402*s2) + 'px';
}
function drawStandby(){
  const n = MEDS.length, d = doneCount(), nd = nextDue(), night = sbNight, red = '#ff453a';
  const col = (m, on) => night ? (on ? red : 'rgba(255,69,58,.28)') : on ? `var(--${m.c})` : m.t < now ? 'rgba(255,159,10,.55)' : 'rgba(255,255,255,.2)';
  X.innerHTML = `<div class="sb ${night ? 'night' : ''}" style="transform:translate(0px,874px) rotate(-90deg)" data-sbtap="1">
    <div class="sbw"><div style="position:relative;display:grid;place-items:center">${ringOf(MEDS, taken, 270, {sw:13, col, check:night ? red : '#30D158', center:''})}
      <div style="position:absolute;text-align:center"><div style="font:700 64px/1 var(--rounded)">${d}/${n}</div><div style="font:600 18px var(--sys);opacity:.7;margin-top:6px">taken today</div></div></div></div>
    <div class="sbw" style="flex:1"><div class="sbtime">${now}</div><div class="sbnext" style="color:${night ? red : nd.m && nd.m.t < now ? '#FF9F0A' : '#40C8E0'}">${nd.m ? (nd.m.t < now ? `${showNames ? nd.m.n : 'Dose'} due since ${nd.m.t}` : `Next dose at ${nd.m.t}`) : 'All done today'}</div>
      <div style="font:600 20px var(--sys);opacity:.55;margin-top:10px">Wednesday 18 November</div></div></div>`;
}
X.addEventListener('click', e => { if(xscene === 'standby' && e.target.closest('[data-sbtap]')){ sbNight = !sbNight; drawStandby();
  if(!reduce) X.firstElementChild.animate([{opacity:.3}, {opacity:1}], {duration:600}); } });

/* ---------- Focus filters: who Hävn shows during a Focus ---------- */
const FOCI = [['Sleep', 'var(--indigo)', T_MOON, 'Everyone, quietly'], ['Work', 'var(--teal, #30B0C7)', T_WORK, 'You'], ['Caring', 'var(--purple)', T_HEARTS, 'Mum']];
function openFocus(){
  const sh = appSheet(`<h3>Focus Filters</h3><p class="sub">Choose who Hävn shows while a Focus is on. Reminders for everyone still arrive.</p>
    <div class="card">${FOCI.map(([n, c, ic, who]) => `<div class="navrow"><span class="tile" style="background:${c}">${ic}</span><span class="lbl">${n}</span><span class="val">${who}</span>${CHEV}</div>`).join('')}</div>
    <p class="foot">Set these up in iPhone Settings › Focus › choose a Focus › Add Filter › Hävn.</p>
    <button class="btn2 p" data-tryfocus="1" style="margin-top:12px">Try Caring Now</button><button class="ob-link" data-close="1">Done</button>`);
  sh.addEventListener('click', e => { if(!e.target.closest('[data-tryfocus]')) return;
    closeAppSheet(() => { closeSettings(); setTimeout(() => setFocus('caring'), reduce ? 0 : 400); }); }, {once:true});
}
function setFocus(f){
  focusMode = f; const old = $('.focuschip'); old && old.remove();
  if(!f){ toast('Caring Focus off'); return; }
  const c = document.createElement('button'); c.className = 'focuschip'; c.innerHTML = `${T_HEARTS.replace('width="16" height="14"', 'width="11" height="10"')} Caring`; $('#phone').appendChild(c);
  c.onclick = () => showAlert({title:'Caring Focus', body:'Hävn is showing only Mum while this Focus is on.', buttons:[{label:'Keep On'}, {label:'Turn Off', style:'def', onTap:() => setFocus(null)}]});
  if(!reduce) c.animate([{transform:'scale(.4)', opacity:0}, {transform:'none', opacity:1}], {duration:420, easing:'cubic-bezier(.3,1.5,.5,1)'});
  if(PEOPLE.mum && cur !== 'mum') switchPerson('mum');
  toast('Caring Focus on: showing Mum');
}

/* ---------- share a schedule straight to another iPhone ---------- */
function openShare(){
  saveCur(); let who = cur, hist2 = false, notes2 = false;
  const sh = appSheet('');
  const draw = () => { const P = PEOPLE[who], nMeds = P.meds.length ? new Set(P.meds.map(m => baseId(m.id))).size : 0, nPrn = P.prn.length;
    sh.innerHTML = `<div class="grab"></div><h3>Share a Schedule</h3><p class="sub">Send a copy to another iPhone, for example to whoever helps ${who === 'you' ? 'you' : P.name}.</p>
      ${Object.keys(PEOPLE).length > 1 ? `<div class="seg">${Object.values(PEOPLE).map(p => `<button class="${p.id === who ? 'on' : ''}" data-shw="${p.id}">${p.name}</button>`).join('')}</div>` : ''}
      <div class="card" style="margin-top:12px"><div class="file" style="cursor:default"><div class="fic">${HAVN_RING(22)}</div><div class="mid"><div class="nm" style="font-size:16px">${who === 'you' ? 'My' : P.name + '’s'} Schedule.havn</div>
        <div class="dt" style="font-size:13px">${nMeds} medication${nMeds === 1 ? '' : 's'}${nPrn ? `, ${nPrn} as needed` : ''} · times and reminders${hist2 ? ' · 17 days of history' : ''}${notes2 ? ' · notes' : ''}</div></div></div></div>
      <div class="card" style="margin-top:12px"><div class="srow"><span class="lbl">Include History</span>${swH('Include History', hist2)}</div><div class="srow"><span class="lbl">Include Notes</span>${swH('Include Notes', notes2)}</div></div>
      <p class="foot">The file goes straight from this iPhone to theirs. No server and no account. They get a copy; later changes don’t sync.</p>
      <div style="display:flex;gap:8px;margin-top:12px"><button class="btn2 g" data-shf="1">Save to Files</button><button class="btn2 p" data-sha="1">AirDrop</button></div><button class="ob-link" data-close="1">Cancel</button>`;
    sh.querySelectorAll('.sw').forEach(sw => { wireSwitch(sw); sw.addEventListener('pointerup', () => setTimeout(() => { const on = sw.classList.contains('on');
      if(sw.getAttribute('aria-label') === 'Include History') hist2 = on; else notes2 = on; draw(); }, 0)); }); };
  draw();
  sh.onclick = e => {
    if(e.target.closest('[data-close]')) return closeAppSheet();
    const w = e.target.closest('[data-shw]'); if(w){ who = w.dataset.shw; draw(); return; }
    if(e.target.closest('[data-sha]')) closeAppSheet(() => toast('Sent with AirDrop'));
    if(e.target.closest('[data-shf]')) closeAppSheet(() => toast('Saved to Files'));
  };
}

/* ---------- Settings: the new rows ---------- */
const drawSettings0 = drawSettings;
drawSettings = () => {
  drawSettings0(); const B = $('#settingsBody'); B.querySelectorAll('.sw').forEach(s => { s.dataset.w = 1; });
  const R = routineOf();
  const addp = B.querySelector('[data-addp]'); addp && addp.insertAdjacentHTML('beforebegin', `<div class="navrow" data-share="1"><span class="tile" style="background:var(--blue)">${T_SHARE}</span><span class="lbl">Share a Schedule</span>${CHEV}</div>`);
  const rem = B.querySelector('.sw[aria-label="Dose Reminders"]'); rem && rem.closest('.srow').insertAdjacentHTML('afterend', `<div class="navrow" data-routine="1"><span class="tile" style="background:var(--yellow)">${T_SUN}</span><span class="lbl">Daily Routine</span><span class="val">Breakfast ${R.wd.breakfast}</span>${CHEV}</div>`);
  const snd = [...B.querySelectorAll('.shead')].find(h => h.textContent === 'Sounds and Haptics');
  snd && snd.insertAdjacentHTML('beforebegin', `<div class="shead">Everyday</div><div class="card">
    <div class="srow"><span class="tile" style="background:var(--green)">${T_SIMPLE}</span><span class="lbl">Simple Mode</span>${swH('Simple Mode', simpleOn)}</div>
    <div class="srow"><span class="tile" style="background:var(--orange)">${T_SPEAK}</span><span class="lbl">Read Names Aloud</span>${swH('Read Names Aloud', speakNames)}</div>
    <div class="navrow" data-focus="1"><span class="tile" style="background:var(--indigo)">${T_MOON}</span><span class="lbl">Focus Filters</span><span class="val">${focusMode ? 'Caring on' : '3'}</span>${CHEV}</div></div>
    <p class="foot">Simple Mode shows one big button for the next dose. Read Names Aloud says the medication’s name when an alarm rings, so you know which one without looking.</p>`);
  const hl = B.querySelector('.sw[aria-label="Read Medications from Health"]');
  if(hl){ hl.closest('.card').insertAdjacentHTML('beforeend', `<div class="srow"><span class="tile" style="background:#FF2D55">${T_HEART}</span><span class="lbl">Read Blood Pressure, Glucose and Weight</span>${swH('Read Readings from Health', readHealth)}</div>`);
    const f = hl.closest('.card').nextElementSibling; if(f && f.classList.contains('foot')) f.textContent = 'Hävn only reads from Health and never writes to it. Nothing leaves your iPhone.'; }
  B.querySelectorAll('.sw:not([data-w])').forEach(sw => { sw.dataset.w = 1; wireSwitch(sw); sw.addEventListener('pointerup', () => setTimeout(() => settingChanged(sw), 0)); });
};
const settingChanged0 = settingChanged;
settingChanged = sw => { settingChanged0(sw); const on = sw.classList.contains('on'), k = sw.getAttribute('aria-label');
  if(k === 'Simple Mode'){ closeSettings(); setTimeout(() => setSimple(on), reduce ? 0 : 420); }
  if(k === 'Read Names Aloud'){ speakNames = on; if(on){ const m = MEDS[0]; m && speak(`Time for your ${m.n}, ${spoken(m.d)}.`); } }
  if(k === 'Read Readings from Health') readHealth = on;
};
$('#settingsBody').addEventListener('click', e => {
  if(e.target.closest('[data-routine]')) return openRoutine();
  if(e.target.closest('[data-focus]')) return openFocus();
  if(e.target.closest('[data-share]')) return openShare();
});

/* ---------- states panel and Reset ---------- */
const syncStates0 = syncStates;
syncStates = () => { syncStates0(); const b = $('.states [data-state="simple"]'); b && b.classList.toggle('on', simpleOn); };
document.querySelector('.states').addEventListener('click', e => { const b = e.target.closest('[data-state="simple"]'); if(!b) return;
  if(xscene) setScene('app', true); setSimple(!simpleOn); });
$('#bReset').onclick = () => {
  if(xscene) leaveX(); if(simpleOn) setSimple(false); closeEveryone(true); focusMode = null; const fc = $('.focuschip'); fc && fc.remove();
  suggDone = false; clearTimeout(dynBusy); dynBusy = 0; $('#dyn').className = ''; $('#dyn').innerHTML = '';
  reset(); dynSync();
};
