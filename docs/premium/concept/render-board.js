const { chromium } = require('/opt/node-tools/node_modules/playwright');
(async () => {
  const [,, dir, page, out] = process.argv;
  const b = await chromium.launch();
  const p = await b.newPage({ viewport: { width: 1400, height: 900 }, deviceScaleFactor: 2 });
  await p.goto('file://' + dir + '/' + page);
  await p.waitForSelector('body[data-ready]');
  await p.screenshot({ path: out, fullPage: true });
  await b.close();
})();
