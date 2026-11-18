  /* ===== Queen Alina: grids whose number of cards changes are sized to the space, so nothing scrolls ===== */
  const FIT = [['.mem-grid', 1], ['.items', .9], ['.cp-pages', 4 / 3], ['.pads', 1], ['.adv-map', 1.15], ['.room-shop', 1]];
  function fitGrid(g, aspect) {
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
      if (w > 0 && (!best || w > best.w + .5)) best = { c, w: Math.floor(w), h: Math.floor(w / aspect) };
    }
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
      for (const [sel, aspect] of FIT) document.querySelectorAll(sel).forEach(g => fitGrid(g, aspect));
    });
  }
  new MutationObserver(fitAll).observe(document.getElementById('app') || document.body, { childList: true, subtree: true });
  addEventListener('resize', () => { document.querySelectorAll('.qa-fit').forEach(g => { g.style.gridTemplateColumns = ''; g.style.gridAutoRows = ''; }); fitAll(); });
