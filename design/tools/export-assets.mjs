#!/usr/bin/env node
// Renders every Hävn design asset from design/havn-cinematheque.html, using the
// same WebGL model as the design file, and writes them to design/assets/ (and the
// AGSL sources to design/shaders/).
//
//   node design/tools/export-assets.mjs            # everything
//   node design/tools/export-assets.mjs pills      # only paths containing "pills"
//
// Needs Playwright with Chromium (npm i -g playwright && npx playwright install chromium).
// Uses SwiftShader so it runs on machines without a GPU; set HAVN_GPU=1 to use the GPU.

import { mkdir, writeFile } from 'node:fs/promises';
import { execSync } from 'node:child_process';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const designDir = path.resolve(here, '..');
const assetsDir = path.join(designDir, 'assets');
const page = pathToFileURL(path.join(designDir, 'havn-cinematheque.html')).href + '#export';
const only = process.argv[2] || '';

async function loadPlaywright() {
  try { return await import('playwright'); } catch { /* fall through to the global install */ }
  const root = execSync('npm root -g').toString().trim();
  return import(pathToFileURL(path.join(root, 'playwright', 'index.mjs')).href);
}

const { chromium } = await loadPlaywright();
const args = process.env.HAVN_GPU ? [] : ['--use-angle=swiftshader', '--enable-unsafe-swiftshader', '--ignore-gpu-blocklist'];
const browser = await chromium.launch({ args });
const tab = await browser.newPage({ viewport: { width: 1280, height: 900 } });
tab.on('console', m => { if (m.type() === 'error') console.error('page:', m.text()); });
await tab.goto(page);
await tab.evaluate(() => document.fonts.ready);
if (!(await tab.evaluate(() => window.havnExport && window.havnExport.ready))) {
  console.error('WebGL 2 is not available in this browser.');
  process.exit(1);
}

const list = (await tab.evaluate(() => window.havnExport.list())).filter(p => p.includes(only));
for (const rel of list) {
  const t0 = Date.now();
  const out = await tab.evaluate(p => window.havnExport.one(p), rel);
  const file = path.resolve(assetsDir, rel);
  await mkdir(path.dirname(file), { recursive: true });
  if (out.png) await writeFile(file, Buffer.from(out.png.split(',')[1], 'base64'));
  else await writeFile(file, out.text);
  console.log(`${path.relative(designDir, file)}  ${((Date.now() - t0) / 1000).toFixed(1)} s`);
}
await browser.close();
