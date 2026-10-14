const { chromium } = require('/opt/node-tools/node_modules/playwright');
(async () => {
  const [,, dir, outDir, sampleOnly] = process.argv;
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 900, height: 940 }, deviceScaleFactor: 2 });
  await p.goto('file://' + dir + '/anim.html');
  await p.waitForSelector('body[data-ready]');
  const el = await p.$('#wrap');
  const fps = 30, total = 4200;
  const times = sampleOnly ? [0, 380, 800, 1200, 2000, 2800, 2950, 3600] : [...Array(Math.ceil(total / 1000 * fps) + 1).keys()].map(i => i * 1000 / fps);
  let i = 0;
  for (const t of times) {
    await p.evaluate(t => render(t), t);
    await el.screenshot({ path: `${outDir}/f${String(i++).padStart(4, '0')}.png` });
  }
  await b.close();
})();
