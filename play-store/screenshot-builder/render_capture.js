const http = require('http');
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const CHROME_PATH = 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe';
const BUILD_DIR = path.resolve(__dirname);
const OUTPUT_DIR = path.resolve(__dirname, '..', 'graphics', 'screenshots');
const PORT = 8999;

const mimeTypes = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};

const server = http.createServer((req, res) => {
  const parsedPath = req.url.split('?')[0];
  const filePath = path.join(BUILD_DIR, parsedPath === '/' ? '01_daily_ritual.html' : parsedPath);
  
  if (fs.existsSync(filePath)) {
    const ext = path.extname(filePath);
    res.writeHead(200, {
      'Content-Type': mimeTypes[ext] || 'application/octet-stream',
      'Access-Control-Allow-Origin': '*'
    });
    fs.createReadStream(filePath).pipe(res);
  } else {
    res.writeHead(404);
    res.end('Not found');
  }
});

server.listen(PORT, async () => {
  console.log(`Local screenshot asset server running on http://localhost:${PORT}`);

  const screens = [
    '01_daily_ritual',
    '02_3d_organizer',
    '03_progress_adherence',
    '04_customize_regimen',
    '05_sanctuary_reminders',
    '06_privacy_and_backup'
  ];

  for (const id of screens) {
    const outPng = path.join(OUTPUT_DIR, `${id}.png`);
    const url = `http://localhost:${PORT}/${id}.html`;
    console.log(`Capturing ${id} -> ${outPng}...`);
    
    // Virtual time budget allows Google Fonts and Three.js WebGL frames to fully render
    const cmd = `"${CHROME_PATH}" --headless=new --screenshot="${outPng}" --window-size=1080,1920 --hide-scrollbars --virtual-time-budget=4000 "${url}"`;
    try {
      execSync(cmd, { stdio: 'inherit' });
      console.log(`Successfully generated ${id}.png (${fs.statSync(outPng).size} bytes)`);
    } catch (err) {
      console.error(`Error capturing ${id}:`, err);
    }
  }

  server.close(() => {
    console.log('All screenshots generated and server stopped.');
  });
});
