# Concept sources

Everything in `../assets/` is rendered from these files, so it is reproducible.

```bash
mkdir -p fonts && cp ../../../app/src/main/res/font/*.ttf fonts/
# Boards → PNG (Playwright + Chromium)
node render-board.js "$PWD" board1.html ../assets/01-today.png
# Animation frames → MP4
mkdir -p frames && node render-anim.js "$PWD" "$PWD/frames"
ffmpeg -framerate 30 -i frames/f%04d.png -vf "scale=1260:-2,format=yuv420p" -c:v libx264 -crf 20 ../assets/take-moment.mp4
# Draft sounds (matched to sfx_take loudness)
python3 make_sounds.py ../assets/sound ../../../app/src/main/res/raw/sfx_take.wav
```

The render scripts require Playwright from `/opt/node-tools/node_modules/playwright`.
Change that path to `playwright` if it is installed locally.

`box.js` is the reference implementation to port to `HavnOrganizerView.kt`.
`drawBox(ctx, W, H, opts)` covers geometry, frosted lids, wells and the
two-layer ground shadow. `pill(...)` draws the sculpted pills. The `p.to` and
`p.t` flight block draws the arc to the progress line.
