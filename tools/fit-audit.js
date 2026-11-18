// Opens every game in the debug app on a connected device and reports anything that does not fit one screen.
// Usage: node tools/fit-audit.js [tab] [--level N] [--menus] [--size 360x640,915x412]   (needs a debug build running, adb on PATH)
// --menus only checks the menus (every groep on Leren); --size emulates other screens, one run per size.
const { execSync } = require('child_process');
const adb = process.env.ADB || 'adb';
const args = process.argv.slice(2);
const valued = ['--level', '--open', '--size'];
const onlyTab = args.find((a, i) => !a.startsWith('--') && !(i > 0 && valued.includes(args[i - 1]))) || '';
const lvIdx = args.indexOf('--level');
const level = lvIdx >= 0 ? +args[lvIdx + 1] : 0;
const menus = args.includes('--menus');
const sizeIdx = args.indexOf('--size');
const sizes = sizeIdx >= 0 ? args[sizeIdx + 1].split(',').map(s => s.split('x').map(Number)) : [null];

const pid = execSync(`${adb} shell pidof nl.alina.tovertuin`).toString().trim();
execSync(`${adb} forward tcp:9333 localabstract:webview_devtools_remote_${pid}`);

const openIdx = args.indexOf('--open');
const openName = openIdx >= 0 ? args[openIdx + 1] : '';

const inPage = async (onlyTab, level, openName, menus) => {
  const wait = ms => new Promise(r => setTimeout(r, ms));
  const W = innerWidth, H = innerHeight, out = [];
  const visible = el => { const s = getComputedStyle(el); return s.display !== 'none' && s.visibility !== 'hidden' && +s.opacity > 0; };
  const fixed = el => { for (let e = el; e && e !== document.body; e = e.parentElement) { const p = getComputedStyle(e).position; if (p === 'fixed') return true; } return false; };
  function audit(where) {
    const probs = [];
    const se = document.scrollingElement;
    if (se.scrollWidth > W + 1) probs.push(`page scrolls sideways ${se.scrollWidth}>${W}`);
    if (se.scrollHeight > H + 1) probs.push(`page scrolls down ${se.scrollHeight}>${H}`);
    let n = 0;
    for (const el of document.querySelectorAll('#app *')) {
      if (n > 6) break;
      if (!visible(el) || el.closest('svg') && el.tagName !== 'svg') continue;
      const r = el.getBoundingClientRect();
      if (r.width < 4 || r.height < 4) continue;
      const cls = (el.className && el.className.baseVal !== undefined ? el.className.baseVal : el.className) || el.tagName;
      if ((r.right > W + 2 || r.bottom > H + 2 || r.left < -2 || r.top < -2) && !fixed(el)) {
        const anc = el.parentElement && el.parentElement.closest('*');
        let clipped = false;
        for (let e = el.parentElement; e && e.id !== 'app'; e = e.parentElement) { const s = getComputedStyle(e); if (s.overflow !== 'visible') { const pr = e.getBoundingClientRect(); if (pr.right <= W + 2 && pr.bottom <= H + 2) clipped = true; break; } }
        if (!clipped) { probs.push(`off screen: ${String(cls).slice(0, 40)} ${Math.round(r.left)},${Math.round(r.top)} ${Math.round(r.right)}x${Math.round(r.bottom)}`); n++; }
      }
      const s = getComputedStyle(el);
      if ((s.overflowY === 'auto' || s.overflowY === 'scroll') && el.scrollHeight > el.clientHeight + 2) { probs.push(`inner scroll: ${String(cls).slice(0, 40)} ${el.scrollHeight}>${el.clientHeight}`); n++; }
      if (el.tagName === 'IMG' || el.tagName === 'svg') {
        const box = el.parentElement.getBoundingClientRect();
        if ((r.width > box.width + 3 || r.height > box.height + 3) && getComputedStyle(el.parentElement).overflow === 'visible' && el.parentElement.classList.contains('pic')) { probs.push(`picture bigger than its box: ${String(cls).slice(0, 30)} in ${el.parentElement.className}`); n++; }
      }
    }
    const BOXES = '.tile, .tile .pic, .item, .item .ie, .item .in, .item .ip, .ans, .room-it, .cp-pg, .adv-node, .groep, .mcard, .tab, .hello';
    for (const box of document.querySelectorAll(BOXES)) {
      if (n > 10) break;
      if (!visible(box) || !box.closest('#app')) continue;
      const b = box.getBoundingClientRect();
      if (b.width < 4 || b.height < 4 || b.bottom < 0 || b.top > H) continue;
      const walker = document.createTreeWalker(box, NodeFilter.SHOW_TEXT);
      for (let t = walker.nextNode(); t; t = walker.nextNode()) {
        if (!t.textContent.trim() || t.parentElement.closest('svg') || !visible(t.parentElement)) continue;
        const rg = document.createRange(); rg.selectNodeContents(t);
        const r = rg.getBoundingClientRect();
        if (r.width < 1) continue;
        // text that a box clips must stay inside it; text that may overflow visibly gets .3em, since a font's line box is taller than its letters
        let clips = false;
        for (let e = t.parentElement; e; e = e.parentElement) { if (getComputedStyle(e).overflow !== 'visible') { clips = true; break; } if (e === box) break; }
        const slack = clips ? 2 : .3 * parseFloat(getComputedStyle(t.parentElement).fontSize);
        if (r.left < b.left - slack || r.right > b.right + slack || r.top < b.top - slack || r.bottom > b.bottom + slack) {
          const cls = String(box.className.baseVal ?? box.className).split(' ')[0];
          probs.push(`cut off in ${cls}: "${t.textContent.trim().slice(0, 24)}" ${Math.round(r.width)}x${Math.round(r.height)} in ${Math.round(b.width)}x${Math.round(b.height)}`); n++;
          break;
        }
      }
    }
    if (probs.length) out.push(where + ': ' + probs.join(' | '));
  }
  for (let k = 0; k < 4 && !document.querySelector('.tabs .tab[data-t]'); k++) { document.getElementById('back')?.click(); await wait(600); }
  const tabs = [...document.querySelectorAll('.tabs .tab[data-t]')].map(t => t.dataset.t);
  if (level && openName && !Storage.prototype.__qaLv) {
    const get = Storage.prototype.getItem; Storage.prototype.__qaLv = true;
    Storage.prototype.getItem = function (k) { return /^speeltuin-lv-(mem-|cnt-|jig|money|clock|sum-)/.test(String(k)) ? String(level) : get.call(this, k); };
  }
  if (openName) {
    const gm = /^(\d):(.*)$/.exec(openName);
    if (gm) {
      document.querySelector('.tab[data-t="leer"]').click(); await wait(400);
      document.querySelector(`.groep[data-g="${gm[1]}"]`)?.click(); await wait(600);
      openName = gm[2];
    }
    for (const t of tabs) {
      document.querySelector(`.tab[data-t="${t}"]`).click(); await wait(400);
      const tile = [...document.querySelectorAll('.tile')].find(x => x.querySelector('.name')?.textContent === openName);
      if (tile) { tile.click(); await wait(1500); audit(`[${t}] ${openName}`); return out.join('\n') || 'opened, fits'; }
    }
    return 'no tile ' + openName;
  }
  if (level && !Storage.prototype.__qaLv) {
    const get = Storage.prototype.getItem; Storage.prototype.__qaLv = true;
    const grows = /^speeltuin-lv-(mem-|cnt-|jig|money|clock|sum-)/;
    Storage.prototype.getItem = function (k) { return grows.test(String(k)) ? String(level) : get.call(this, k); };
  }
  const startGroep = localStorage.getItem('speeltuin-groep');
  for (const t of tabs) {
    if (onlyTab && t !== onlyTab) continue;
    let groepen = [''];
    if (t === 'leer') { document.querySelector('.tab[data-t="leer"]').click(); await wait(500); groepen = [...document.querySelectorAll('.groep')].map(g => g.dataset.g); }
    for (const g of groepen) {
      document.querySelector(`.tab[data-t="${t}"]`).click(); await wait(500);
      if (g) { document.querySelector(`.groep[data-g="${g}"]`).click(); await wait(500); }
      audit(`[${t}${g ? ' groep ' + g : ''}] menu`);
      if (menus || (g && !['3', '5', '8'].includes(g))) continue;
      const count = document.querySelectorAll('.tile').length;
      for (let i = 0; i < count; i++) {
        document.querySelector(`.tab[data-t="${t}"]`)?.click(); await wait(300);
        const tile = document.querySelectorAll('.tile')[i]; if (!tile) continue;
        const name = tile.querySelector('.name')?.textContent || i;
        tile.click(); await wait(1600);
        audit(`[${t}${g ? ' groep ' + g : ''}] ${name}`);
        for (let k = 0; k < 3 && !document.querySelector('.tabs .tab[data-t]'); k++) { document.getElementById('back')?.click(); await wait(500); }
        document.querySelectorAll('dialog[open]').forEach(d => d.close());
      }
    }
  }
  if (startGroep) localStorage.setItem('speeltuin-groep', startGroep);
  return `${W}x${H}\n` + (out.join('\n') || 'everything fits');
};

setTimeout(() => { console.log('TIMEOUT: the page stopped answering'); process.exit(2); }, 600000 * sizes.length);
(async () => {
  const list = await (await fetch('http://127.0.0.1:9333/json')).json();
  const page = list.find(p => p.type === 'page');
  const ws = new WebSocket(page.webSocketDebuggerUrl);
  await new Promise(r => ws.onopen = r);
  let id = 0; const pending = {};
  ws.onmessage = m => { const d = JSON.parse(m.data); if (pending[d.id]) { pending[d.id](d); delete pending[d.id]; } };
  const send = (method, params) => new Promise(r => { pending[++id] = r; ws.send(JSON.stringify({ id, method, params })); });
  let failed = false;
  for (const size of sizes) {
    if (size) { await send('Emulation.setDeviceMetricsOverride', { width: size[0], height: size[1], deviceScaleFactor: 0, mobile: true }); await new Promise(r => setTimeout(r, 1200)); }
    const d = await send('Runtime.evaluate', { expression: `(${inPage})(${JSON.stringify(onlyTab)}, ${level}, ${JSON.stringify(openName)}, ${menus})`, awaitPromise: true, returnByValue: true, userGesture: true });
    const r = d.result.result;
    const text = r.value !== undefined ? String(r.value) : JSON.stringify(d.result);
    if (!/everything fits|opened, fits/.test(text)) failed = true;
    console.log(text);
  }
  if (sizes[0]) await send('Emulation.clearDeviceMetricsOverride', {});
  ws.close(); process.exit(failed ? 1 : 0);
})();
