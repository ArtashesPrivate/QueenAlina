  /* ===== Queen Alina: the main character, the child's name, start without code ===== */
  /* Alina is drawn in parts (head, eyes, mouth, arms, curls, dress) that move separately; alina.css animates them.
     Keep the proportions: head top ~5%, eyes ~43%, chin ~64%, feet ~97%, right hand at 78%/82%, so clothes and held items still fit. */
  function alinaSVG(cls, still, pose) {
    const T = k => pose && pose[k] ? ` transform="${pose[k]}"` : '';
    const SK = '#ffd8c2', SKD = '#f2b79c', HAIR = '#6b3e26', HAIRD = '#4f2b19', HAIRL = '#8a5434', LIL = '#e9d8f6', LILD = '#c9a6e6', PUR = '#a35ec4', PINK = '#f3a6d8', INK = '#3a2416';
    const size = still ? ' width="200" height="200"' : '';
    return `<svg class="alina${cls ? ' ' + cls : ''}" viewBox="0 0 200 200"${size} xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
<ellipse cx="100" cy="194" rx="40" ry="5" fill="#000" opacity=".12"/>
<g class="al-all"${T('all')}>
 <g class="al-legs"${T('legs')}>
  <rect x="81" y="164" width="9" height="24" rx="4" fill="${SK}"/><rect x="110" y="164" width="9" height="24" rx="4" fill="${SK}"/>
  <path d="M74 190 Q74 182 86 183 Q95 184 94 191 Q84 195 74 190 Z" fill="${PUR}" stroke="${INK}" stroke-width="1.5"/>
  <path d="M126 190 Q126 182 114 183 Q105 184 106 191 Q116 195 126 190 Z" fill="${PUR}" stroke="${INK}" stroke-width="1.5"/>
  <circle cx="88" cy="185" r="2" fill="#fff"/><circle cx="112" cy="185" r="2" fill="#fff"/>
 </g>
 <g class="al-dress"${T('dress')}>
  <path d="M80 130 Q100 124 120 130 L124 150 Q140 164 146 176 Q100 188 54 176 Q60 164 76 150 Z" fill="${LIL}" stroke="${INK}" stroke-width="1.6" stroke-linejoin="round"/>
  <path d="M76 150 Q100 156 124 150" fill="none" stroke="${LILD}" stroke-width="2"/>
  <path d="M56 174 q6 8 12 1 q6 8 12 1 q6 8 12 1 q6 8 12 1 q6 8 12 1 q6 8 12 1 q6 8 12 1 q5 6 10 -1" fill="#fff" stroke="${PINK}" stroke-width="1.6"/>
  <path d="M94 133 L106 141 M106 133 L94 141 M94 140 L106 148 M106 140 L94 148" stroke="${PUR}" stroke-width="1.4"/>
  <path d="M100 150 L86 143 L86 157 Z M100 150 L114 143 L114 157 Z" fill="${PINK}" stroke="${INK}" stroke-width="1.2" stroke-linejoin="round"/>
  <circle cx="100" cy="150" r="3.4" fill="${PUR}" stroke="${INK}" stroke-width="1.2"/>
  <g fill="#fff" opacity=".7"><circle cx="76" cy="166" r="1.6"/><circle cx="92" cy="170" r="1.6"/><circle cx="110" cy="169" r="1.6"/><circle cx="126" cy="165" r="1.6"/></g>
 </g>
 <g class="al-armL"${T('armL')}>
  <path d="M76 138 Q68 152 64 164" fill="none" stroke="${SK}" stroke-width="8" stroke-linecap="round"/>
  <rect x="61" y="155" width="10" height="5" rx="2" fill="${PUR}" transform="rotate(20 66 157)"/>
  <circle cx="63" cy="166" r="5" fill="${SK}"/>
 </g>
 <g class="al-armR"${T('armR')}>
  <path d="M124 138 Q140 150 152 160" fill="none" stroke="${SK}" stroke-width="8" stroke-linecap="round"/>
  <rect x="139" y="148" width="10" height="5" rx="2" fill="${PUR}" transform="rotate(-40 144 150)"/>
  <circle cx="154" cy="162" r="5" fill="${SK}"/>
 </g>
 <circle cx="77" cy="136" r="9" fill="${LIL}" stroke="${INK}" stroke-width="1.5"/>
 <circle cx="123" cy="136" r="9" fill="${LIL}" stroke="${INK}" stroke-width="1.5"/>
 <g class="al-head"${T('head')}>
  <g class="al-curlL"${T('curlL')}><circle cx="50" cy="96" r="15" fill="${HAIR}"/><circle cx="46" cy="114" r="12" fill="${HAIR}"/><circle cx="52" cy="128" r="9" fill="${HAIRD}"/><path d="M42 92 q6 6 2 14" stroke="${HAIRL}" stroke-width="2.5" fill="none" stroke-linecap="round"/></g>
  <g class="al-curlR"${T('curlR')}><circle cx="150" cy="96" r="15" fill="${HAIR}"/><circle cx="154" cy="114" r="12" fill="${HAIR}"/><circle cx="148" cy="128" r="9" fill="${HAIRD}"/><path d="M158 92 q-6 6 -2 14" stroke="${HAIRL}" stroke-width="2.5" fill="none" stroke-linecap="round"/></g>
  <ellipse cx="100" cy="70" rx="58" ry="58" fill="${HAIR}"/>
  <rect x="94" y="120" width="12" height="10" fill="${SKD}"/>
  <ellipse cx="100" cy="84" rx="46" ry="44" fill="${SK}" stroke="${INK}" stroke-width="1.2"/>
  <path d="M52 76 Q48 26 100 22 Q152 26 148 76 Q140 56 124 58 Q114 44 98 54 Q82 46 72 62 Q60 60 52 76 Z" fill="${HAIR}"/>
  <path d="M70 40 Q86 30 104 32" stroke="${HAIRL}" stroke-width="3" fill="none" stroke-linecap="round"/>
  <path d="M52 64 Q100 10 148 64" fill="none" stroke="${PUR}" stroke-width="6" stroke-linecap="round"/>
  <g class="al-flower"${T('flower')}><circle cx="140" cy="30" r="7" fill="${PINK}"/><circle cx="151" cy="36" r="7" fill="${PINK}"/><circle cx="148" cy="48" r="7" fill="${PINK}"/><circle cx="135" cy="47" r="7" fill="${PINK}"/><circle cx="131" cy="36" r="7" fill="${PINK}"/><circle cx="141" cy="40" r="5" fill="#ffd23c"/></g>
  <g class="al-brows" fill="none" stroke="${HAIRD}" stroke-width="2.6" stroke-linecap="round"><path d="M70 68 Q79 63 88 67"/><path d="M112 67 Q121 63 130 68"/></g>
  <g class="al-eyes"${T('eyes')}>
   <g class="al-look"${T('look')}>
    <ellipse cx="80" cy="86" rx="10.5" ry="12.5" fill="#fff"/><circle cx="81" cy="87" r="8.5" fill="#3b82c4"/><circle cx="81" cy="87" r="5.5" fill="#1f4f86"/><circle cx="81" cy="87" r="3.6" fill="#13213a"/><circle cx="77.5" cy="83" r="3" fill="#fff"/><circle cx="84" cy="91" r="1.4" fill="#fff"/>
    <ellipse cx="120" cy="86" rx="10.5" ry="12.5" fill="#fff"/><circle cx="121" cy="87" r="8.5" fill="#3b82c4"/><circle cx="121" cy="87" r="5.5" fill="#1f4f86"/><circle cx="121" cy="87" r="3.6" fill="#13213a"/><circle cx="117.5" cy="83" r="3" fill="#fff"/><circle cx="124" cy="91" r="1.4" fill="#fff"/>
   </g>
   <path d="M68 80 Q80 70 92 79 M68 80 l-5 -3 M131 80 l5 -3 M108 79 Q120 70 132 80" fill="none" stroke="${INK}" stroke-width="2.6" stroke-linecap="round"/>
  </g>
  <ellipse cx="68" cy="104" rx="8" ry="5" fill="#ff8fa8" opacity=".45"/><ellipse cx="132" cy="104" rx="8" ry="5" fill="#ff8fa8" opacity=".45"/>
  <path d="M97 99 Q100 101 103 99" fill="none" stroke="${SKD}" stroke-width="2" stroke-linecap="round"/>
  <g class="al-mouth"${T('mouth')}><path d="M90 108 Q100 119 110 108 Q100 112 90 108 Z" fill="#e86f86" stroke="#b5465a" stroke-width="1.8" stroke-linejoin="round"/></g>
 </g>
</g>
</svg>`;
  }

  /* Canvas games draw Alina with drawImage, which only takes a still picture. This canvas looks like an
     Image (complete, naturalWidth, onload) and keeps flipping through pose frames, so she moves there too.
     Moves stay as small as in alina.css: hats, glasses and held items are drawn on top at fixed spots.
     Turning an SVG into pixels is slow on cheap phones, so frames are made only once a game asks for her,
     one at a time in the background. */
  function alinaSprite() {
    const N = 16, TALK = 4, CYCLE = 2600, el = document.createElement('canvas'), g = el.getContext('2d'), done = [];
    el.width = el.height = 200;
    const R = (a, x, y) => `rotate(${a.toFixed(2)} ${x} ${y})`;
    const S = (sy, y) => `translate(0 ${y}) scale(1 ${sy}) translate(0 ${-y})`;
    const pose = i => {
      const ph = i < N ? i : (i - N) * (N / TALK), p = ph / N * Math.PI * 2, s = Math.sin(p);
      return alinaSVG('', true, {
        head: R(3 * s, 100, 128), armL: R(7 * s, 76, 138), armR: R(-7 * s, 124, 138),
        curlL: R(5 * s, 56, 84), curlR: R(-5 * s, 144, 84), flower: R(12 * s, 141, 40),
        dress: `translate(100 130) skewX(${(2 * s).toFixed(2)}) translate(-100 -130)`,
        look: `translate(${(2 * Math.sin(p / 2)).toFixed(2)} 0)`,
        eyes: i === 11 ? S(.08, 86) : '',
        mouth: i >= N ? S(1.35, 110) : '',
      });
    };
    let started = false, ready = false, lastUse = 0, shown = -1;
    const make = i => {
      if (i >= N + TALK) return;
      const im = new Image();
      im.onload = () => {
        const c = document.createElement('canvas'); c.width = c.height = 200; c.getContext('2d').drawImage(im, 0, 0, 200, 200); done[i] = c;
        if (!ready) { ready = true; show(); if (el.onload) el.onload(); setInterval(show, 40); }
        setTimeout(() => make(i + 1), 60);
      };
      im.src = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(pose(i));
    };
    const use = () => { lastUse = performance.now(); if (!started) { started = true; make(0); } return ready; };
    function show() {
      if (document.hidden || performance.now() - lastUse > 1500) return;
      const ph = Math.floor(performance.now() / (CYCLE / N)) % N, talk = document.body.classList.contains('qa-talk') && Math.floor(performance.now() / 120) % 2;
      let i = talk && done[N + Math.floor(ph / (N / TALK))] ? N + Math.floor(ph / (N / TALK)) : ph;
      if (!done[i]) i = 0;
      if (i === shown) return;
      shown = i; g.clearRect(0, 0, 200, 200); g.drawImage(done[i], 0, 0);
    }
    Object.defineProperty(el, 'complete', { get: use });
    Object.defineProperty(el, 'naturalWidth', { get: () => use() ? 200 : 0 });
    Object.defineProperty(el, 'naturalHeight', { get: () => use() ? 200 : 0 });
    return el;
  }

  /* Wherever the game says LOU, it now says the name of the child who plays (default Alina). */
  function kidName() { try { return (typeof curName === 'function' && curName()) || 'Alina'; } catch (e) { return 'Alina'; } }
  function kidText(s) { const n = kidName(); return String(s).replace(/(Hoi [^!.]+! )Ik ben LOU\. /, '$1').replace(/\bLOU\b/g, n).replace(/\bLou\b/g, n); }
  (function nameEverywhere() {
    const re = /\bLOU\b|\bLou\b/;
    const fixNode = n => { if (n.nodeType === 3) { if (re.test(n.nodeValue)) n.nodeValue = kidText(n.nodeValue); return; }
      if (n.nodeType !== 1 || n.namespaceURI === 'http://www.w3.org/2000/svg') return;
      ['aria-label', 'alt', 'title'].forEach(a => { const v = n.getAttribute(a); if (v && re.test(v)) n.setAttribute(a, kidText(v)); });
      const w = document.createTreeWalker(n, NodeFilter.SHOW_TEXT | NodeFilter.SHOW_ELEMENT);
      for (let c = w.nextNode(); c; c = w.nextNode()) {
        if (c.nodeType === 3) { if (re.test(c.nodeValue)) c.nodeValue = kidText(c.nodeValue); }
        else ['aria-label', 'alt', 'title'].forEach(a => { const v = c.getAttribute && c.getAttribute(a); if (v && re.test(v)) c.setAttribute(a, kidText(v)); });
      } };
    const start = () => {
      fixNode(document.body);
      new MutationObserver(list => list.forEach(m => { if (m.type === 'characterData') fixNode(m.target); else m.addedNodes.forEach(fixNode); }))
        .observe(document.body, { childList: true, subtree: true, characterData: true });
    };
    if (document.body) start(); else addEventListener('DOMContentLoaded', start);
  })();

  /* Text drawn on a canvas gets the child's name as well, and centred text never runs past the canvas edges */
  (function canvasText() {
    const P = CanvasRenderingContext2D.prototype, re = /\bLOU\b|\bLou\b/;
    const fit = (ctx, max) => { if (max !== undefined || ctx.textAlign !== 'center') return max; const a = Math.hypot(ctx.getTransform().a, ctx.getTransform().b) || 1; return ctx.canvas.width / a * .94; };
    ['fillText', 'strokeText'].forEach(fn => { const orig = P[fn]; P[fn] = function (t, x, y, max) { if (re.test(t)) t = kidText(t); max = fit(this, max); return max === undefined ? orig.call(this, t, x, y) : orig.call(this, t, x, y, max); }; });
    const measure = P.measureText; P.measureText = function (t) { return measure.call(this, re.test(t) ? kidText(t) : t); };
  })();

  /* Alina's mouth moves while she talks */
  let talkT = null;
  function qaTalk(text) {
    document.body.classList.add('qa-talk'); clearTimeout(talkT);
    talkT = setTimeout(() => document.body.classList.remove('qa-talk'), Math.max(900, Math.min(7000, String(text).length * 62)));
  }
