// Opens every game in the debug app on a connected device and reports anything that does not fit one screen.
// Usage: node tools/fit-audit.js [tab] [--level N]   (needs a debug build running, adb on PATH)
const { execSync } = require('child_process');
const adb = process.env.ADB || 'adb';
const args = process.argv.slice(2);
const onlyTab = args.find((a, i) => !a.startsWith('--') && !(i > 0 && args[i - 1].startsWith('--'))) || '';
const lvIdx = args.indexOf('--level');
const level = lvIdx >= 0 ? +args[lvIdx + 1] : 0;

const pid = execSync(`${adb} shell pidof nl.alina.tovertuin`).toString().trim();
execSync(`${adb} forward tcp:9333 localabstract:webview_devtools_remote_${pid}`);

const openIdx = args.indexOf('--open');
const openName = openIdx >= 0 ? args[openIdx + 1] : '';

const inPage = async (onlyTab, level, openName) => {
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
  for (const t of tabs) {
    if (onlyTab && t !== onlyTab) continue;
    document.querySelector(`.tab[data-t="${t}"]`).click(); await wait(500);
    audit(`[${t}] menu`);
    const count = document.querySelectorAll('.tile').length;
    for (let i = 0; i < count; i++) {
      document.querySelector(`.tab[data-t="${t}"]`)?.click(); await wait(300);
      const tile = document.querySelectorAll('.tile')[i]; if (!tile) continue;
      const name = tile.querySelector('.name')?.textContent || i;
      tile.click(); await wait(1600);
      audit(`[${t}] ${name}`);
      for (let k = 0; k < 3 && !document.querySelector('.tabs .tab[data-t]'); k++) { document.getElementById('back')?.click(); await wait(500); }
      document.querySelectorAll('dialog[open]').forEach(d => d.close());
    }
  }
  return `${W}x${H}\n` + (out.join('\n') || 'everything fits');
};

setTimeout(() => { console.log('TIMEOUT: the page stopped answering'); process.exit(2); }, 420000);
(async () => {
  const list = await (await fetch('http://127.0.0.1:9333/json')).json();
  const page = list.find(p => p.type === 'page');
  const ws = new WebSocket(page.webSocketDebuggerUrl);
  await new Promise(r => ws.onopen = r);
  ws.onmessage = m => {
    const d = JSON.parse(m.data);
    if (d.id === 1) {
      const r = d.result.result;
      console.log(r.value !== undefined ? r.value : JSON.stringify(d.result));
      ws.close(); process.exit(0);
    }
  };
  ws.send(JSON.stringify({ id: 1, method: 'Runtime.evaluate', params: { expression: `(${inPage})(${JSON.stringify(onlyTab)}, ${level}, ${JSON.stringify(openName)})`, awaitPromise: true, returnByValue: true, userGesture: true } }));
})();
