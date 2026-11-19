
/* ================= wiring ================= */
let muted = false;
{ const play0 = Snd.play; Snd.play = (...a) => { if(!muted) play0(...a); }; }   // widgets and the Lock Screen act silently
function quiet(fn){ muted = true; fn(); setTimeout(() => { muted = false; }, 400); }   // long enough to cover the delayed 'taken' chime
$('#dim').onclick = () => {
  if($('#addSheet').classList.contains('open')) return closeAdd();
  if($('#daySheet').classList.contains('open')) return closeDay();
  if($('#dose').classList.contains('open')) return closeDose();
  closeSettings();
};
$('#openSettings').onclick = openPeopleMenu;
$('#openAdd').onclick = () => openAdd();
const openSettings0 = openSettings;
openSettings = () => { drawSettings(); openSettings0(); };
$('#app').addEventListener('click', e => {
  const t = e.target;
  const pl = t.closest('[data-prnlog]'); if(pl){ e.stopPropagation(); logPrn(pl.dataset.prnlog, pl); return; }
  if(t.closest('[data-add]')){ openAdd(); return; }
  if(t.closest('[data-import]')){ setScene('welcome'); setTimeout(() => { obRender(4, true); obStart = 'import'; healthSheet(); }, 60); return; }
  if(t.closest('[data-turnon]')){ showAlert({title:'Turn On Reminders', body:'Notifications for Hävn are off in iPhone Settings. Open Settings › Hävn › Notifications and turn on Allow Notifications.',
      buttons:[{label:'Not Now'}, {label:'Open Settings', style:'def', onTap:() => { notifOff = false; drawBanner(); syncStates(); toast('Reminders on'); }}]}); return; }
  const dt = t.closest('#scroll [data-detail], #scroll [data-prn]'); if(dt && !t.closest('button')){ openDetail(dt.dataset.detail || dt.dataset.prn); return; }
});
$('#appDim').onclick = () => closeAppSheet();
/* the catalogue's Play buttons act on Today, so they leave onboarding and other scenes first */
$('#list').addEventListener('click', e => {
  if(!e.target.closest('.play')) return;
  if(!$('#ob').hidden){ hideOnboarding(); setScene('app', true); }
  if(scene !== 'app') setScene('app', true);
  if(cur !== 'you') switchPerson('you');
}, true);
CAT.splice(CAT.findIndex(c => c[0] === 'Glass melting together'), 0,
  ['The ring flies home','Matched geometry (shared element)','.matchedGeometryEffect(id:in:)', 'The last onboarding screen: its ring flies into the Day Ring on Today while everything else fades.', () => setScene('welcome')],
  ['A menu that grows from its button','Liquid Glass menu morph','Menu { } on a .glass button, iOS 26', 'The person button opens into the People menu, and folds back into it.', () => openPeopleMenu()],
  ['Screens that slide in','Navigation push','NavigationStack + .navigationDestination', 'Tap a medication in Medications: its detail slides in from the right and the list eases back.', () => { showTab(2); setTimeout(() => openDetail('iron'), 300); }]);
$('#list').innerHTML = CAT.map(([n,aka,api,where,fn,demo],i) => `<article class="m">
  <div class="top"><h3>${n}</h3></div><div class="aka">${aka}</div>
  <p><code>${api}</code></p><div class="where">${where}</div>
  ${demo==='goo' ? `<div class="demo"><svg class="goo" viewBox="0 0 220 84" role="img" aria-label="Two glass drops merging">
      <defs><filter id="gooF"><feGaussianBlur in="SourceGraphic" stdDeviation="7" result="b"/><feColorMatrix in="b" mode="matrix" values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 22 -9"/></filter></defs>
      <g filter="url(#gooF)" fill="var(--page-accent)" fill-opacity=".85"><circle id="gA" cx="70" cy="42" r="26"/><circle id="gB" cx="150" cy="42" r="26"/></g></svg></div>` : ''}
  ${demo==='switch' ? `<div class="demo"><button class="sw on" id="demoSw" aria-label="Example switch" aria-pressed="true" style="--fill:var(--page-line)"><span class="k"></span></button><span class="dt" style="font-size:13px">Hold it</span></div>` : ''}
  ${fn || demo==='goo' ? `<button class="play" data-i="${i}">${demo==='goo' ? 'Merge' : 'Play'}</button>` : ''}
</article>`).join('');
if($('#demoSw')) wireSwitch($('#demoSw'));
