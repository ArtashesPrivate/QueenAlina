// Generates many questions for every school lesson (web-src/leren.js) and checks them: the right answer is a choice,
// choices differ, nothing is NaN/undefined, and decimals use a comma like at Dutch schools.
const fs = require('fs');
const path = require('path');
const root = path.join(__dirname, '..');
const html = fs.readFileSync(path.join(root, 'app/src/main/assets/web/index.html'), 'utf8').replace(/\r\n/g, '\n');
const grab = re => { const m = html.match(re); if (!m) throw new Error('missing ' + re); return m[0]; };
const lib = [/const rnd = .*?;\n/, /const shuffle = .*?;\n/, /const NL_UNITS = .*?;\n/, /const NL_TENS = .*?;\n/, /const numNl = .*?;\n/].map(grab).join('');
const leren = fs.readFileSync(path.join(root, 'web-src/leren.js'), 'utf8');
const stub = 'const localStorage = { getItem: () => null }; const LV = { get: () => 1, set() {} }; const store = () => {};\n';
const LESSONS = new Function(stub + lib + '\n' + leren + '\nreturn { LESSONS, FLOOR, groepFloor, LEER_BY_GROEP };')();

let checks = 0, fails = 0;
const fail = (msg) => { if (fails++ < 25) console.log('FAIL: ' + msg); };
for (const [id, L] of Object.entries(LESSONS.LESSONS)) {
  if (!Array.isArray(L.levels) || !L.levels.length) fail(`${id}: no levels`);
  for (let lv = 1; lv <= L.levels.length; lv++) {
    for (let k = 0; k < 400; k++) {
      let q;
      try { q = L.make(lv); } catch (e) { fail(`${id} L${lv} throws ${e.message}`); break; }
      const where = `${id} L${lv} "${String(q.q).replace(/<[^>]+>/g, '')}"`;
      checks++;
      if (!q.opts || q.opts.length < 2) fail(`${where}: fewer than 2 choices`);
      else if (!q.opts.includes(q.ans)) fail(`${where}: answer ${q.ans} not in ${q.opts}`);
      else if (new Set(q.opts).size !== q.opts.length) fail(`${where}: same choice twice ${q.opts}`);
      const all = [q.q, q.say, q.ans, ...(q.opts || [])].join(' | ');
      if (/NaN|undefined|Infinity|\[object/.test(all)) fail(`${where}: broken text ${all}`);
      if ((q.opts || []).some(o => /\d\.\d/.test(String(o)))) fail(`${where}: decimal point instead of comma ${q.opts}`);
    }
  }
}
for (const [g, names] of Object.entries(LESSONS.LEER_BY_GROEP)) {
  checks++;
  for (const n of names) if (!LESSONS.LESSONS[n] && !/sommen|Letters|letter|Klok|Geld/.test(n)) fail(`groep ${g}: unknown tile ${n}`);
}
if (fails) { console.log(`${fails} of ${checks} lesson checks failed`); process.exit(1); }
console.log(`PASS: ${checks} lesvragen gecontroleerd`);
