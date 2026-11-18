// Controleert de moeilijkere levels in de gebundelde spelcode: node tools/check-levels.js
const fs = require('fs');
const path = require('path');
const html = fs.readFileSync(path.join(__dirname, '..', 'app', 'src', 'main', 'assets', 'web', 'index.html'), 'utf8').replace(/\r\n/g, '\n');

function grab(re, what) {
  const m = html.match(re);
  if (!m) throw new Error('Niet gevonden in index.html: ' + what);
  return m[0];
}
const src = [
  grab(/const rnd = .*?;\n/, 'rnd'),
  grab(/const NL_UNITS = .*?;\n/, 'NL_UNITS'),
  grab(/const NL_TENS = .*?;\n/, 'NL_TENS'),
  grab(/const numNl = .*?;\n/, 'numNl'),
  grab(/const SUM_LEVELS = \{[\s\S]*?\n {2}\};\n/, 'SUM_LEVELS'),
  grab(/function makeSum\(op, n\) \{[\s\S]*?\n {2}\}\n/, 'makeSum'),
  grab(/const nextH = .*?;\n/, 'nextH'),
  grab(/const timeLabel = .*?;\n/, 'timeLabel'),
].join('');
const api = new Function(src + 'return { numNl, SUM_LEVELS, makeSum, timeLabel };')();

let checks = 0;
function ok(cond, msg) { checks++; if (!cond) { console.error('FOUT: ' + msg); process.exit(1); } }

ok(api.SUM_LEVELS['+'].length === 16 && api.SUM_LEVELS['-'].length === 16, '16 plus- en minlevels');
ok(api.SUM_LEVELS['×'].length === 10 && api.SUM_LEVELS[':'].length === 10, '10 keer- en deellevels');
ok(api.SUM_LEVELS['+'][12] === 'Grote sommen tot 1000', 'oude levels blijven gelijk');

for (let i = 0; i < 400; i++) {
  for (const op of ['+', '-']) {
    for (let n = 1; n <= 18; n++) {
      const s = api.makeSum(op, n);
      const vals = [s.a, s.b, s.res, s.ans].concat(s.c == null ? [] : [s.c]);
      ok(vals.every(v => Number.isInteger(v) && v >= 0), `${op} level ${n}: hele getallen >= 0 (${JSON.stringify(s)})`);
      const real = op === '+' ? s.a + s.b + (s.c || 0) : s.a - s.b - (s.c || 0);
      ok(real === s.res, `${op} level ${n}: uitkomst klopt`);
      ok(s.ans === (s.miss ? s.b : s.res), `${op} level ${n}: antwoord klopt`);
      if (n === 14) ok((op === '+' ? s.res : s.a) <= 5000 && (op === '+' ? s.res : s.a) >= 1000, `${op} level 14 tot 5000`);
      if (n === 15) ok((op === '+' ? s.res : s.a) <= 10000 && (op === '+' ? s.res : s.a) > 1000, `${op} level 15 tot 10000`);
      if (n >= 16) ok(s.miss && (op === '+' ? s.res : s.a) <= 999, `${op} level 16 wat mist er tot 1000`);
    }
  }
  for (const op of ['×', ':']) {
    for (let n = 1; n <= 12; n++) {
      const s = api.makeSum(op, n);
      const real = op === '×' ? s.a * s.b : s.a / s.b;
      ok(real === s.res && Number.isInteger(real), `${op} level ${n}: uitkomst klopt`);
      if (n === 9) ok([11, 12].includes(s.b), `${op} level 9: tafel van 11 of 12`);
      if (n >= 10) ok(s.b >= 13 && (op === '×' ? s.res : s.a) <= 240, `${op} level 10: tot 240`);
    }
  }
}

ok(api.numNl(1000) === 'duizend', 'duizend');
ok(api.numNl(2345) === 'tweeduizend driehonderdvijfenveertig', 'tweeduizend driehonderdvijfenveertig: ' + api.numNl(2345));
ok(api.numNl(10000) === 'tienduizend', 'tienduizend');
ok(api.numNl(1001) === 'duizend één', 'duizend één: ' + api.numNl(1001));
ok(api.numNl(21) === 'eenentwintig' && api.numNl(999) === 'negenhonderdnegenennegentig', 'getallen onder 1000 blijven gelijk');

ok(api.timeLabel(3, 0) === '3 uur' && api.timeLabel(3, 30) === 'half 4', 'hele en halve uren blijven gelijk');
ok(api.timeLabel(3, 5) === 'vijf over 3', 'vijf over 3');
ok(api.timeLabel(3, 20) === 'tien voor half 4', 'tien voor half 4');
ok(api.timeLabel(3, 40) === 'tien over half 4', 'tien over half 4');
ok(api.timeLabel(12, 55) === 'vijf voor 1', 'vijf voor 1');
ok(api.timeLabel(3, 7) === 'zeven over 3' && api.timeLabel(3, 59) === 'één voor 4', 'op de minuut');
const labels = new Set();
for (let m = 0; m < 60; m++) labels.add(api.timeLabel(3, m));
ok(labels.size === 60, 'elke minuut heeft een eigen tekst');

console.log(`PASS: ${checks} levelcontroles`);
