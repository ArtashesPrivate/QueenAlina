  /* ===== Queen Alina: grids whose number of cards changes are sized to the space, so nothing scrolls ===== */
  /* the third value lets cells grow wider and taller than the aspect so the grid fills the space */
  const FIT = [['.mem-grid', 1], ['.items', .9, 1.3], ['.cp-pages', 4 / 3], ['.pads', 1], ['.adv-map', 1.15], ['.room-shop', 1, 1.3]];
  function fitGrid(g, aspect, stretch = 1) {
    const kids = [...g.children].filter(k => k.offsetParent !== null || getComputedStyle(k).display !== 'none');
    const n = kids.length; if (!n) return;
    const cs = getComputedStyle(g);
    const W = g.clientWidth - parseFloat(cs.paddingLeft) - parseFloat(cs.paddingRight);
    const H = g.clientHeight - parseFloat(cs.paddingTop) - parseFloat(cs.paddingBottom);
    if (W < 20 || H < 20) return;
    const gx = parseFloat(cs.columnGap) || 8, gy = parseFloat(cs.rowGap) || 8;
    let best = null;
    for (let c = 1; c <= n; c++) {
      const r = Math.ceil(n / c), cw = (W - gx * (c - 1)) / c, ch = (H - gy * (r - 1)) / r;
      const w = Math.min(cw, ch * aspect);
      if (w > 0 && (!best || w > best.w + .5)) best = { c, w: Math.floor(w), h: Math.floor(w / aspect), cw, ch };
    }
    if (best && stretch > 1) { const s = best.w; best.w = Math.floor(Math.min(best.cw, s * stretch)); best.h = Math.floor(Math.min(best.ch, s / aspect * stretch)); }
    if (!best) return;
    const cols = `repeat(${best.c}, ${best.w}px)`, rows = `${best.h}px`;
    if (g.style.gridTemplateColumns !== cols || g.style.gridAutoRows !== rows) {
      g.classList.add('qa-fit');
      g.style.gridTemplateColumns = cols; g.style.gridAutoRows = rows;
      g.style.setProperty('--cell', Math.min(best.w, best.h) + 'px');
    }
  }
  let fitQueued = false;
  function fitAll() {
    if (fitQueued) return; fitQueued = true;
    requestAnimationFrame(() => {
      fitQueued = false;
      for (const [sel, aspect, stretch] of FIT) document.querySelectorAll(sel).forEach(g => fitGrid(g, aspect, stretch));
      fitContents();
    });
  }
  /* emoji pictures fill the space their box gets (Android's emoji font draws a glyph 1.25em wide and 1.36em high), text shrinks until it fits its box */
  const GLYPHS = '.tile .pic, .items.qa-fit .item .ie';
  const TEXTS = '.room-it small, .adv-node span';
  function fitGlyph(el) {
    if (el.querySelector('img, svg')) return;
    const h = el.clientHeight, w = el.clientWidth;
    if (h < 2 || w < 2) return;
    const fs = Math.max(10, Math.min(110, h / 1.4, w / 1.3)) + 'px';
    if (el.style.fontSize !== fs) el.style.fontSize = fs;
  }
  function fitText(el) {
    if (!el.clientWidth) return;
    el.style.fontSize = '';
    const cs = getComputedStyle(el), clipped = cs.overflow === 'hidden';
    let fs = parseFloat(cs.fontSize);
    const min = Math.max(8, fs * .55);
    while (fs > min && (el.scrollWidth > el.clientWidth + 1 || clipped && el.scrollHeight > el.clientHeight + 1)) { fs *= .92; el.style.fontSize = fs + 'px'; }
  }
  /* a menu tile or a dress-up item: its texts shrink together until the picture keeps (almost) its share of the box */
  function fitBox(t, picSel, textSel, share) {
    t.querySelectorAll('.qa-hide').forEach(e => e.classList.remove('qa-hide'));
    const pic = t.querySelector(picSel), texts = [...t.querySelectorAll(textSel)].filter(e => e.offsetParent);
    texts.forEach(fitText);
    if (!pic) return;
    const want = .9 * share * (t.clientHeight - parseFloat(getComputedStyle(t).paddingTop) * 2);
    for (let k = 0; k < 8 && pic.clientHeight < want; k++) {
      let shrunk = false;
      for (const e of texts) { const fs = parseFloat(getComputedStyle(e).fontSize); if (fs > 9) { e.style.fontSize = fs * .9 + 'px'; shrunk = true; } }
      if (!shrunk) break;
    }
    for (const e of texts) if (e.matches('.in') && (e.scrollWidth > e.clientWidth + 1 || e.scrollHeight > e.clientHeight + 1)) e.classList.add('qa-hide');
  }
  function fitContents() {
    document.querySelectorAll('.tile').forEach(t => fitBox(t, '.pic', '.name, .age', .46));
    document.querySelectorAll('.items.qa-fit .item').forEach(t => fitBox(t, '.ie', '.in, .ip', .5));
    document.querySelectorAll(TEXTS).forEach(fitText);
    document.querySelectorAll('.cp-pages.qa-fit .cp-free small').forEach(s => {
      s.classList.remove('qa-hide');
      const b = s.parentElement;
      if (b.scrollHeight > b.clientHeight + 1 || s.scrollWidth > s.clientWidth + 1) s.classList.add('qa-hide');
    });
    document.querySelectorAll(GLYPHS).forEach(fitGlyph);
  }
  new MutationObserver(fitAll).observe(document.getElementById('app') || document.body, { childList: true, subtree: true });
  addEventListener('resize', () => { document.querySelectorAll('.qa-fit').forEach(g => { g.style.gridTemplateColumns = ''; g.style.gridAutoRows = ''; }); fitAll(); });
  if (document.fonts) document.fonts.ready.then(fitAll);
