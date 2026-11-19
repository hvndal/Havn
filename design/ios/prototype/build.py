#!/usr/bin/env python3
"""Builds the full Hävn prototype: base.html (Today, motion, glass) + src/*.css/js + new markup -> havn.html.
Every replacement must match exactly once, so a changed base fails loudly instead of silently."""
import glob, os, sys
D = os.path.dirname(os.path.abspath(__file__))
s = open(os.path.join(D, 'base.html'), encoding='utf-8').read()

def rep(a, b, n=1):
    global s
    c = s.count(a)
    if c != n: sys.exit(f'build: expected {n} match(es), found {c}: {a[:90]!r}')
    s = s.replace(a, b)

def cut(start, end):
    """Remove from `start` up to (not including) `end`."""
    global s
    i = s.find(start); j = s.find(end, i)
    if i < 0 or j < 0: sys.exit(f'build: cut markers not found: {start[:40]!r} … {end[:40]!r}')
    s = s[:i] + s[j:]

# ---------- state that must now change at run time
rep("const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;", "let reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;")
rep("const MEDS = [", "let MEDS = [")
rep("function initState(){ taken = {met:'08:04', vit:'08:04'}; order = ['met','vit']; now = '13:42'; }\ninitState();",
    "function initState(){ resetPeople(); now = '13:42'; }")

# ---------- the ring and count work for any number of doses, including none
rep('<div class="dot" data-i="0"></div><div class="dot" data-i="1"></div><div class="dot" data-i="2"></div><div class="dot" data-i="3"></div>', '<div class="dots" id="dots"></div>')
rep('<div class="big"><span class="roll" id="count"><span class="reel"><span>0</span><span>1</span><span>2</span><span>3</span><span>4</span></span></span> of 4 taken</div>',
    '<div class="big"><span id="bigCount" style="display:flex;align-items:center;gap:6px"><span class="roll" id="count"><span class="reel" id="reel"></span></span> of <span id="ofN">4</span> taken</span><span id="bigEmpty" hidden>Nothing yet</span></div>')
rep("const groups = ['Morning','Afternoon','Night'];", "const groups = ['Morning','Afternoon','Evening','Night'].filter(g => MEDS.some(m => m.g === g));")
rep("""    + `<div class="sec rv"><div class="shead"><span class="t">${LOWICON}Running Low</span></div><div class="card"><div class="lrow">${icon(MEDS[1])}
      <div class="mid"><div class="nm">Vitamin D3</div><div class="dt">6 softgels left · about 6 days</div></div></div></div></div>`;""", "    + extraSections();")
rep("const ring = $('#ring'), d = doneCount(), p = d/MEDS.length*100;",
    "const ring = $('#ring'), d = doneCount(), p = MEDS.length ? d/MEDS.length*100 : 0;\n"
    "  if(!MEDS.length){ ring.style.setProperty('--p', 0); ring.classList.remove('closed'); ring.querySelectorAll('.cap').forEach(c => c.style.background = 'transparent'); return; }")
rep("  const d = doneCount(), nx = nextMed();\n  if(d === MEDS.length) return 'Your ring is closed. Rest easy.';",
    "  const d = doneCount(), nx = nextMed();\n  if(!MEDS.length) return 'Add a medication to start your ring.';\n  if(d === MEDS.length) return 'Your ring is closed. Rest easy.';")
rep("""  if(!nx) return `<div class="face">${icon(MEDS[0])}<div class="mid"><div class="nm">Tomorrow, 08:00</div><div class="dt">Metformin, Vitamin D3</div></div></div>`;""",
    """  if(!MEDS.length) return `<div class="face"><div class="mid" style="padding-left:8px"><div class="nm">No doses yet</div><div class="dt">Add a medication to get reminders</div></div><button class="pbtn" data-add="1">Add</button></div>`;
  if(!nx){ const f = MEDS[0]; return `<div class="face">${icon(f)}<div class="mid"><div class="nm">Tomorrow, ${f.t}</div><div class="dt">${MEDS.filter(m => m.t === f.t).map(m => m.n).join(', ')}</div></div></div>`; }""")

# ---------- reset goes back to a known Today, whatever scene or sheet is open
rep("  initState(); history = []; clearTimeout(accTimer);",
    "  closeAllSheets(); hideOnboarding(); setScene('app', true); notifOff = false; stash = null; $('#app').scrollTop = 0;\n"
    "  initState(); history = PEOPLE.you.history; clearTimeout(accTimer);\n"
    "  $('#openSettings').innerHTML = avatarHTML(PEOPLE.you); $('#scroll .lsub').textContent = dateLine(); syncStates();")
rep("$('#clock').innerHTML = '<span>13:42</span>'; build(); $('#ring').classList.remove('closed');",
    "$('#clock').innerHTML = '<span>13:42</span>'; build(); rebuildRingParts(); drawBanner(); $('#ring').classList.remove('closed');")

# ---------- the quick History/Medications screens from the glass pass are replaced by the full ones
cut("function miniRing(states, size){", "function showTab(i, instant){")

# ---------- markup inside the phone
rep('<button class="gbtn glass" aria-label="Add medication">', '<button class="gbtn glass" id="openAdd" aria-label="Add medication">')
rep('<div class="toolbar">', '<div id="app">\n          <div class="toolbar">')
i = s.find('<div class="sheet" id="settings"'); j = s.find('</div></div>', s.find('Hold a switch: the knob turns to glass', i)) + len('</div></div>')
if i < 0 or j < len('</div></div>'): sys.exit('build: settings sheet not found')
s = s[:i] + '''<div class="sheet" id="settings" role="dialog" aria-label="Settings"><div class="inner">
            <div class="grab"></div>
            <div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px 4px">
              <span style="width:44px"></span><span style="font-size:17px;font-weight:600">Settings</span>
              <button class="gbtn" id="closeSettings" aria-label="Done" style="background:var(--accent);color:var(--on-accent)"><svg width="18" height="18" viewBox="0 0 18 18"><path d="M3.5 9.5l3.6 3.6L14.5 5" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/></svg></button></div>
            <div id="settingsBody"></div></div></div>''' + s[j:]
rep('<div class="home"></div>', open(os.path.join(D, 'src', 'markup.html'), encoding='utf-8').read() + '\n          <div class="home"></div>')

# ---------- the page around the phone: scenes above it, states below it
rep('<div class="stage" id="stage">', '''<div class="scenes" role="group" aria-label="Show">
        <button data-scene="welcome">Welcome</button><button data-scene="app">App</button><button data-scene="home">Home Screen</button><button data-scene="lock">Lock Screen</button></div>
      <div class="stage" id="stage">''')
i = s.find('<div class="controls">'); j = s.find('</p>', s.find('<p class="hint">', i)) + 4
s = s[:i] + '''<div class="wstyle"><div class="scenes" style="margin:0;max-width:260px"><button data-ws="default" class="on">Default widgets</button><button data-ws="clear">Clear widgets</button></div></div>
      <div class="controls">
        <button id="bReset">Reset</button>
        <button id="bTheme">Phone: dark</button>
        <button id="bSound">Sound: Glass</button>
        <button id="bIron" hidden>Log Iron</button><button id="bMag" hidden>Log Magnesium</button>
      </div>
      <p class="hint">Tap a circle to log a dose, or a row to open it. The person button switches people and opens Settings. + adds a medication.</p>
      <div class="states"><h3>States to check</h3><p>Every screen should still work with these on.</p><div class="tg">
        <button data-state="ax">Largest Text</button><button data-state="rt">Reduce Transparency</button><button data-state="rm">Reduce Motion</button>
        <button data-state="hc">Increase Contrast</button><button data-state="notifoff">Reminders Off</button><button data-state="empty">No Medications</button><button data-state="locked">Face ID Lock</button></div></div>''' + s[j:]
i = s.find('<h1>Hävn, in motion</h1>'); j = s.find('</p>', i) + 4
s = s[:i] + '''<h1>Hävn, in motion</h1>
    <p>It starts like a first launch, at Welcome. Use the buttons above the phone to jump to the app, the Home Screen widgets or the Lock Screen, and the panel under it to check accessibility and empty states. Every motion is named on the right with its iOS term.</p>''' + s[j:]

# ---------- styles and scripts
css = open(os.path.join(D, 'src', 'app.css'), encoding='utf-8').read()
rep('@media (prefers-reduced-motion: reduce){', css + '\n@media (prefers-reduced-motion: reduce){')
js = '\n'.join(open(f, encoding='utf-8').read() for f in sorted(glob.glob(os.path.join(D, 'src', '*.js'))))
rep('/* ---------- start ---------- */', js + '\n/* ---------- start ---------- */')
rep("""build(); $('#sm').innerHTML = `<span>${smText()}</span>`; $('#accSwap').innerHTML = accNext();
entrance();   // the same slow reveal as Reset and the catalogue's Play""",
    """initState(); $('#openSettings').innerHTML = avatarHTML(PEOPLE.you);
refreshToday(false); drawSettings(); startOnboarding(); markScene();   // first launch: Welcome""")

open(os.path.join(D, 'havn.html'), 'w', encoding='utf-8').write(s)
print(f'built havn.html ({len(s)//1024} KB)')
