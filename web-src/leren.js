  /* ===== Queen Alina: groep 3 t/m 9 and the subjects children learn at school ===== */
  const GROEPEN = [3, 4, 5, 6, 7, 8, 9];
  function groep() { let g = 3; try { g = +localStorage.getItem('speeltuin-groep') || 3; } catch (e) {} return GROEPEN.includes(g) ? g : 3; }
  const R = (a, b) => a + rnd(b - a + 1), pk = a => a[rnd(a.length)];
  const uniq = a => [...new Set(a)];
  /* fill up to n options around the right answer, without duplicates */
  function around(ans, n, gen) { const o = [ans]; for (let t = 0; o.length < n && t < 300; t++) { const v = gen(); if (v != null && !o.includes(v)) o.push(v); } return o; }
  const kom = v => String(Math.round(v * 1e6) / 1e6).replace('.', ',');
  const eur = v => '€ ' + (Number.isInteger(v) ? String(v) : v.toFixed(2).replace('.', ','));
  const sayKom = s => { const [i, f] = String(s).replace('−', 'min ').split(','); const w = x => x.trim().startsWith('min') ? 'min ' + numNl(+x.replace('min', '').trim()) : numNl(+x); return f ? `${w(i)} komma ${f.startsWith('0') ? 'nul ' + numNl(+f) : numNl(+f)}` : w(i); };
  const ORD = { 3: 'derde', 4: 'vierde', 5: 'vijfde', 6: 'zesde', 7: 'zevende', 8: 'achtste', 9: 'negende', 10: 'tiende', 12: 'twaalfde' };
  const fr = (n, d) => d === 1 ? String(n) : `<span class="frac"><b>${n}</b><i>${d}</i></span>`;
  const frSay = (n, d) => d === 1 ? numNl(n) : d === 2 ? (n === 1 ? 'een half' : numNl(n) + ' halve') : d === 4 && n === 1 ? 'een kwart' : `${n === 1 ? 'een' : numNl(n)} ${ORD[d] || numNl(d) + 'de'}`;
  const gcd = (a, b) => b ? gcd(b, a % b) : a;
  function pie(n, d) {
    let s = '';
    for (let i = 0; i < d; i++) {
      const a0 = i / d * Math.PI * 2 - Math.PI / 2, a1 = (i + 1) / d * Math.PI * 2 - Math.PI / 2;
      s += `<path d="M50 50 L${(50 + 44 * Math.cos(a0)).toFixed(2)} ${(50 + 44 * Math.sin(a0)).toFixed(2)} A44 44 0 0 1 ${(50 + 44 * Math.cos(a1)).toFixed(2)} ${(50 + 44 * Math.sin(a1)).toFixed(2)} Z" fill="${i < n ? '#ff7ccf' : '#fff'}" stroke="#1d2a4d" stroke-width="2"/>`;
    }
    return `<svg class="les-pie" viewBox="0 0 100 100">${s}</svg>`;
  }
  const eq = s => `<div class="les-eq">${s}</div>`;
  const big = s => `<div class="les-big">${s}</div>`;
  const num = v => String(v).replace('-', '−');

  /* ---- Lezen (groep 3–4) ---- */
  const LEES = [
    [['vis', '🐟'], ['kat', '🐱'], ['bus', '🚌'], ['pen', '🖊️'], ['bed', '🛏️'], ['zon', '☀️'], ['mes', '🔪'], ['bal', '⚽'], ['kip', '🐔'], ['pet', '🧢'], ['tas', '👜'], ['sok', '🧦'], ['vos', '🦊'], ['pop', '🪆']],
    [['maan', '🌙'], ['boom', '🌳'], ['roos', '🌹'], ['vuur', '🔥'], ['been', '🦵'], ['haan', '🐓'], ['boot', '⛵'], ['kaas', '🧀'], ['raam', '🪟'], ['muur', '🧱'], ['peer', '🍐'], ['zeep', '🧼'], ['oor', '👂'], ['zaag', '🪚']],
    [['koe', '🐄'], ['boek', '📖'], ['neus', '👃'], ['huis', '🏠'], ['ijs', '🍦'], ['ei', '🥚'], ['muis', '🐭'], ['duif', '🕊️'], ['hoed', '🎩'], ['bijl', '🪓'], ['touw', '🪢'], ['uil', '🦉'], ['geit', '🐐'], ['pauw', '🦚'], ['ui', '🧅'], ['tien', '🔟']],
    [['trein', '🚂'], ['fiets', '🚲'], ['slang', '🐍'], ['krab', '🦀'], ['spin', '🕷️'], ['stoel', '🪑'], ['bloem', '🌸'], ['vlag', '🚩'], ['brood', '🍞'], ['plant', '🪴'], ['slak', '🐌'], ['schip', '🚢'], ['kwal', '🪼'], ['sneeuw', '❄️']],
    [['banaan', '🍌'], ['konijn', '🐰'], ['olifant', '🐘'], ['tomaat', '🍅'], ['giraf', '🦒'], ['robot', '🤖'], ['raket', '🚀'], ['ballon', '🎈'], ['citroen', '🍋'], ['pinguïn', '🐧'], ['zebra', '🦓'], ['tijger', '🐅'], ['appel', '🍎'], ['ananas', '🍍'], ['wortel', '🥕'], ['paraplu', '☂️']],
  ];
  const VOW = ['aa', 'ee', 'oo', 'uu', 'ie', 'oe', 'eu', 'ui', 'ei', 'ij', 'ou', 'au', 'a', 'e', 'i', 'o', 'u'];
  function swapVowel(w) {
    for (let i = 0; i < w.length; i++) for (const v of VOW) if (w.startsWith(v, i)) {
      const alt = VOW.filter(x => x !== v && x.length === v.length);
      return w.slice(0, i) + pk(alt) + w.slice(i + v.length);
    }
    return w + 'e';
  }

  /* ---- Spelling (groep 4–6) ---- */
  const SP = {
    eiij: { ei: ['trein', 'geit', 'klein', 'zeil', 'plein', 'reis', 'brein', 'wei', 'eind', 'meisje'], ij: ['ijs', 'pijl', 'rijst', 'tijger', 'prijs', 'lijn', 'dijk', 'vijf', 'krijt', 'bijl', 'kijken', 'schrijven'] },
    auou: { au: ['saus', 'pauw', 'auto', 'blauw', 'paus', 'klauw', 'rauw', 'flauw'], ou: ['goud', 'koud', 'oud', 'touw', 'vrouw', 'hout', 'zout', 'fout', 'kous', 'jou'] },
    dt: { d: ['hond', 'kind', 'brood', 'hand', 'bed', 'bord', 'paard', 'hoed', 'rood', 'tijd', 'goed', 'land', 'wind', 'avond'], t: ['kat', 'boot', 'pet', 'fiets', 'voet', 'groot', 'plant', 'wit', 'licht', 'geit', 'muts', 'zout'] },
    gch: { g: ['berg', 'dag', 'weg', 'oog', 'vlag', 'brug', 'rug', 'zaag', 'vlieg', 'zorg'], ch: ['lach', 'pech', 'toch', 'nacht', 'licht', 'lucht', 'acht', 'zacht', 'vracht', 'kuch'] },
  };
  const VERKL = [['boom', 'boompje'], ['huis', 'huisje'], ['bal', 'balletje'], ['raam', 'raampje'], ['stoel', 'stoeltje'], ['koe', 'koetje'], ['kip', 'kippetje'], ['ster', 'sterretje'], ['kam', 'kammetje'], ['auto', 'autootje'], ['bloem', 'bloempje'], ['deur', 'deurtje'], ['boek', 'boekje'], ['vis', 'visje'], ['zon', 'zonnetje'], ['tafel', 'tafeltje']];
  const IGLIJK = [['gezellig', 'ig'], ['prettig', 'ig'], ['lastig', 'ig'], ['handig', 'ig'], ['zonnig', 'ig'], ['gelukkig', 'ig'], ['grappig', 'ig'], ['nodig', 'ig'], ['vrolijk', 'lijk'], ['eerlijk', 'lijk'], ['moeilijk', 'lijk'], ['duidelijk', 'lijk'], ['vriendelijk', 'lijk'], ['heerlijk', 'lijk'], ['vreselijk', 'lijk']];
  function gapQ(set, hint) {
    const k = pk(Object.keys(set)), w = pk(set[k]), parts = Object.keys(set);
    let i = -1;
    if (k === 'd' || k === 't' || k === 'g') i = w.length - k.length;
    else { i = w.indexOf(k); if (k === 'ch' && w.endsWith('cht')) i = w.length - 3; }
    const shown = w.slice(0, i) + '<u>__</u>' + w.slice(i + k.length);
    return { q: `Hoe schrijf je <b>${w}</b>?`, say: `Hoe schrijf je ${w}?`, show: big(shown), ans: k, opts: parts, after: w, hint };
  }

  /* ---- Werkwoorden (groep 6–9) ---- */
  const WW = [['vinden', 'vind'], ['worden', 'word'], ['houden', 'houd'], ['rijden', 'rijd'], ['antwoorden', 'antwoord'], ['zitten', 'zit'], ['praten', 'praat'], ['weten', 'weet'], ['lopen', 'loop'], ['werken', 'werk'], ['spelen', 'speel'], ['fietsen', 'fiets'], ['zwemmen', 'zwem'], ['bakken', 'bak']];
  const wwT = s => s.endsWith('t') ? s : s + 't';
  const wwWrong = s => s.endsWith('d') ? s.slice(0, -1) + 't' : s.endsWith('t') ? s + 't' : s + 'd';
  const VT = [['werken', 'werkte', 'werkde'], ['fietsen', 'fietste', 'fietsde'], ['maken', 'maakte', 'maakde'], ['hopen', 'hoopte', 'hoopde'], ['lachen', 'lachte', 'lachde'], ['stoppen', 'stopte', 'stopde'], ['praten', 'praatte', 'praatde'], ['dansen', 'danste', 'dansde'], ['spelen', 'speelde', 'speelte'], ['horen', 'hoorde', 'hoorte'], ['bouwen', 'bouwde', 'bouwte'], ['leven', 'leefde', 'leefte'], ['reizen', 'reisde', 'reiste'], ['branden', 'brandde', 'brande'], ['wonen', 'woonde', 'woonte'], ['schilderen', 'schilderde', 'schilderte']];
  const VD = [['werken', 'gewerkt', 'gewerkd'], ['fietsen', 'gefietst', 'gefietsd'], ['spelen', 'gespeeld', 'gespeelt'], ['wonen', 'gewoond', 'gewoont'], ['branden', 'gebrand', 'gebrandt'], ['praten', 'gepraat', 'gepraatt'], ['zetten', 'gezet', 'gezett'], ['leven', 'geleefd', 'geleeft'], ['reizen', 'gereisd', 'gereist'], ['dansen', 'gedanst', 'gedansd'], ['bouwen', 'gebouwd', 'gebouwt'], ['antwoorden', 'geantwoord', 'geantwoordt']];
  const STERK = [['lopen', 'liep', 'loopte'], ['zingen', 'zong', 'zingde'], ['schrijven', 'schreef', 'schrijfde'], ['eten', 'at', 'eette'], ['drinken', 'dronk', 'drinkte'], ['slapen', 'sliep', 'slaapte'], ['vinden', 'vond', 'vindde'], ['kijken', 'keek', 'kijkte'], ['zwemmen', 'zwom', 'zwemde'], ['geven', 'gaf', 'geefde'], ['lezen', 'las', 'leesde'], ['komen', 'kwam', 'koomde']];
  const blank = '<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>';

  /* ---- Engels (groep 7–9) ---- */
  const EN = [
    [['kat', 'cat', '🐱'], ['hond', 'dog', '🐶'], ['paard', 'horse', '🐴'], ['koe', 'cow', '🐄'], ['vogel', 'bird', '🐦'], ['vis', 'fish', '🐟'], ['varken', 'pig', '🐷'], ['schaap', 'sheep', '🐑'], ['muis', 'mouse', '🐭'], ['konijn', 'rabbit', '🐰'], ['beer', 'bear', '🐻'], ['kip', 'chicken', '🐔']],
    [['rood', 'red', '🟥'], ['blauw', 'blue', '🟦'], ['geel', 'yellow', '🟨'], ['groen', 'green', '🟩'], ['zwart', 'black', '⬛'], ['wit', 'white', '⬜'], ['paars', 'purple', '🟪'], ['oranje', 'orange', '🟧'], ['bruin', 'brown', '🟫'], ['roze', 'pink', '🩷']],
    [['één', 'one', '1'], ['twee', 'two', '2'], ['drie', 'three', '3'], ['vier', 'four', '4'], ['vijf', 'five', '5'], ['zes', 'six', '6'], ['zeven', 'seven', '7'], ['acht', 'eight', '8'], ['negen', 'nine', '9'], ['tien', 'ten', '10'], ['elf', 'eleven', '11'], ['twaalf', 'twelve', '12'], ['twintig', 'twenty', '20']],
    [['brood', 'bread', '🍞'], ['kaas', 'cheese', '🧀'], ['appel', 'apple', '🍎'], ['melk', 'milk', '🥛'], ['ei', 'egg', '🥚'], ['banaan', 'banana', '🍌'], ['aardbei', 'strawberry', '🍓'], ['taart', 'cake', '🎂'], ['soep', 'soup', '🍲'], ['ijsje', 'ice cream', '🍦'], ['koekje', 'cookie', '🍪']],
    [['potlood', 'pencil', '✏️'], ['stoel', 'chair', '🪑'], ['deur', 'door', '🚪'], ['huis', 'house', '🏠'], ['raam', 'window', '🪟'], ['fiets', 'bike', '🚲'], ['auto', 'car', '🚗'], ['klok', 'clock', '🕒'], ['tas', 'bag', '👜'], ['boek', 'book', '📖'], ['school', 'school', '🏫'], ['boom', 'tree', '🌳']],
  ];

  /* ---- Begrijpend lezen (groep 4–9): [tekst, vraag, goed, fout, fout] ---- */
  const BL = [
    [['Sam heeft een hond. De hond heet Bob. Bob is bruin en heeft een lange staart. Elke middag gaat Sam met Bob naar het park.', 'Welke kleur heeft Bob?', 'bruin', 'zwart', 'wit'],
     ['Sam heeft een hond. De hond heet Bob. Bob is bruin en heeft een lange staart. Elke middag gaat Sam met Bob naar het park.', 'Waar gaan Sam en Bob elke middag heen?', 'naar het park', 'naar school', 'naar de winkel'],
     ['Het regent. Lisa trekt haar laarzen aan en pakt haar paraplu. Op straat springt ze in een grote plas. Haar sokken worden toch nat!', 'Waarom worden de sokken van Lisa nat?', 'Ze springt in een plas.', 'Ze gaat zwemmen.', 'Ze vergeet haar laarzen.'],
     ['Opa bakt pannenkoeken. Hij doet meel, melk en eieren in een kom. Tim mag de eerste pannenkoek omdraaien. Oeps, hij valt op de grond!', 'Wat valt er op de grond?', 'de pannenkoek', 'de kom', 'de melk']],
    [['Egels slapen in de winter. In de herfst eten ze veel, zodat ze dik worden. Dan zoeken ze een warm plekje onder de bladeren. Pas in het voorjaar worden ze weer wakker.', 'Waarom eten egels veel in de herfst?', 'Zo worden ze dik voor de winter.', 'Dan zijn ze jarig.', 'In de herfst is er geen eten.'],
     ['Noor en Daan bouwen een hut in het bos. Daan haalt takken en Noor bindt ze vast met touw. Als de hut klaar is, begint het te regenen. Gelukkig blijven ze droog.', 'Waarom blijven Noor en Daan droog?', 'Ze zitten in de hut.', 'Ze hebben een paraplu.', 'Het stopt met regenen.'],
     ['In de bibliotheek mag je boeken lenen. Je hebt dan een pasje nodig. Na drie weken breng je de boeken terug. Ben je te laat, dan moet je soms een boete betalen.', 'Wat gebeurt er soms als je te laat bent?', 'Je betaalt een boete.', 'Je krijgt een nieuw boek.', 'Je pasje is weg.']],
    [['De Waddenzee valt twee keer per dag bijna droog. Dat komt door eb en vloed. Bij eb kun je op sommige plekken over de bodem lopen. Dat heet wadlopen. Ga nooit alleen, want het water komt snel terug.', 'Waarom mag je niet alleen gaan wadlopen?', 'Het water komt snel terug.', 'Het is er te koud.', 'Er zwemmen haaien.'],
     ['Mira wilde graag op voetbal, maar ze durfde niet. Haar buurmeisje Fleur nam haar mee naar een training. Na tien minuten was Mira haar zenuwen vergeten. Nu traint ze elke woensdag.', 'Hoe voelde Mira zich eerst?', 'zenuwachtig', 'boos', 'moe'],
     ['Bijen zijn heel belangrijk. Als ze van bloem naar bloem vliegen, nemen ze stuifmeel mee. Zo kunnen planten vruchten maken, zoals appels en aardbeien. Zonder bijen zou er veel minder fruit zijn.', 'Wat is de belangrijkste gedachte van de tekst?', 'Bijen zijn nodig voor fruit.', 'Bijen kunnen steken.', 'Appels zijn gezond.']],
    [['In 1839 reed de eerste trein van Nederland, tussen Amsterdam en Haarlem. De reis duurde ongeveer een half uur. Veel mensen waren eerst bang voor het snelle ijzeren paard.', 'Wat bedoelt de schrijver met het ijzeren paard?', 'de trein', 'een robot', 'een standbeeld'],
     ['Plastic in zee is een groot probleem. Vissen en vogels denken dat stukjes plastic eten zijn. Vrijwilligers ruimen daarom stranden op. Toch is het beter om minder plastic te gebruiken, want dan komt het er niet eens.', 'Wat vindt de schrijver de beste oplossing?', 'minder plastic gebruiken', 'stranden opruimen', 'meer vissen vangen'],
     ['Joris kreeg een slecht cijfer voor zijn toets. Thuis vertelde hij het meteen aan zijn moeder. Ze was niet boos. "Fijn dat je eerlijk bent," zei ze. "Zullen we samen oefenen?"', 'Waarom was moeder niet boos?', 'Joris was eerlijk.', 'Het cijfer was goed.', 'Ze had het niet gehoord.']],
    [['Sinds een paar jaar leven er weer wolven in Nederland. Sommige mensen vinden dat mooi, want de wolf hoort bij de natuur. Boeren maken zich zorgen, omdat wolven soms schapen doden. De overheid betaalt daarom mee aan stevige hekken.', 'Waarom betaalt de overheid mee aan hekken?', 'om schapen te beschermen', 'om wolven te vangen', 'om toeristen te lokken'],
     ['Rond 1913 bedacht Gideon Sundback een rits die echt goed werkte. Eerst werd de rits vooral voor laarzen gebruikt. Pas later kwamen er ritsen in jassen en broeken.', 'Wat is het doel van deze tekst?', 'informeren', 'overtuigen', 'amuseren'],
     ['Esra twijfelde. Zou ze haar vriendin vertellen dat ze het geheim had doorverteld? Ze wist dat het ruzie kon geven. Toch belde ze haar op. Na een stilte zei haar vriendin: "Dank je dat je het zelf zegt."', 'Waarom belde Esra haar vriendin?', 'Ze wilde eerlijk zijn.', 'Ze wilde ruzie maken.', 'Ze verveelde zich.']],
  ];

  /* Every lesson: from = groep where level 1 fits, step = levels per groep */
  const LESSONS = {
    lezen: { title: 'Woorden lezen', pic: '📖', sub: 'Lees en kies', col: '#ff9330', from: 3, step: 3,
      levels: ['Korte woorden', 'Lange klanken (aa, oo)', 'Tweetekenklanken (oe, ui, ij)', 'Medeklinkers samen (tr, sl)', 'Woorden van twee stukjes'],
      make(L) { const set = LEES[L - 1], w = pk(set), other = pk(set.filter(x => x !== w)); return { q: 'Welk woord hoort bij het plaatje?', say: 'Lees de woordjes. Welk woord hoort bij het plaatje?', show: big(w[1]), ans: w[0], opts: uniq([w[0], swapVowel(w[0]), other[0]]), after: w[0] }; } },
    spelling: { title: 'Spelling', pic: '✍️', sub: 'ei/ij, d/t, -je', col: '#d7263d', from: 4, step: 2,
      levels: ['ei of ij', 'au of ou', 'd of t aan het eind', 'g of ch', 'Verkleinwoorden', '-ig of -lijk'],
      make(L) {
        if (L === 1) return gapQ(SP.eiij, 'Ei of ij: dat moet je onthouden. Probeer nog eens.');
        if (L === 2) return gapQ(SP.auou, 'Au of ou: dat moet je onthouden. Probeer nog eens.');
        if (L === 3) return gapQ(SP.dt, 'Tip: maak het woord langer. Hoor je dan een d of een t?');
        if (L === 4) return gapQ(SP.gch, 'Tip: maak het woord langer. Hoor je dan een g of ch?');
        if (L === 5) { const [w, a] = pk(VERKL), last = w.slice(-1); return { q: `Eén ${w}, twee kleine …`, say: `Eén ${w}. Hoe schrijf je het verkleinwoord?`, show: big(w + ' → ?'), ans: a, opts: uniq([a, w + 'je', w + 'tje', w + 'pje', w + last + 'etje']).slice(0, 4), after: a }; }
        const [w, s] = pk(IGLIJK), stem = w.slice(0, w.length - s.length);
        return { q: `Hoe schrijf je <b>${w}</b>?`, say: `Hoe schrijf je ${w}?`, show: big(stem + '<u>__</u>'), ans: s, opts: ['ig', 'ich', 'lijk', 'luk'], after: w };
      } },
    ww: { title: 'Werkwoorden', pic: '📝', sub: 'd, t of dt', col: '#5b74c2', from: 6, step: 1.5,
      levels: ['Ik, jij, hij (d, t, dt)', 'Jij en hij achter het werkwoord', 'Verleden tijd (\'t kofschip)', 'Voltooid deelwoord', 'Sterke werkwoorden'],
      make(L) {
        if (L <= 2) {
          const [inf, s] = pk(WW);
          if (L === 1) { const who = pk(['Ik', 'Jij', 'Hij', 'Mijn zus']), a = who === 'Ik' ? s : wwT(s); return { q: `${who} ${blank} elke dag. (${inf})`, say: `Kies de goede vorm van ${inf}.`, show: big(`${who} … <small>(${inf})</small>`), ans: a, opts: uniq([s, wwT(s), wwWrong(s)]), after: `${who} ${a} elke dag.` }; }
          const who = pk(['jij', 'hij']), a = who === 'jij' ? s : wwT(s);
          return { q: `${blank} ${who} morgen ook? (${inf})`, say: `Kies de goede vorm van ${inf}. Let op: ${who} staat achter het werkwoord.`, show: big(`… ${who} morgen ook? <small>(${inf})</small>`), ans: a[0].toUpperCase() + a.slice(1), opts: uniq([s, wwT(s), wwWrong(s)]).map(x => x[0].toUpperCase() + x.slice(1)), hint: who === 'jij' ? 'Staat jij achter het werkwoord? Dan geen t.' : 'Bij hij komt er wel een t achter.' };
        }
        if (L === 3) { const [inf, a, b] = pk(VT); return { q: `Gisteren ${blank} ik heel lang. (${inf})`, say: `Wat is de verleden tijd van ${inf}?`, show: big(`Gisteren … ik. <small>(${inf})</small>`), ans: a, opts: [a, b], hint: 'Denk aan \'t kofschip: t, k, f, s, ch, p. Dan -te, anders -de.' }; }
        if (L === 4) { const [inf, a, b] = pk(VD); return { q: `Ik heb gisteren ${blank}. (${inf})`, say: `Wat is het voltooid deelwoord van ${inf}?`, show: big(`Ik heb … <small>(${inf})</small>`), ans: a, opts: [a, b], hint: 'Maak het langer: gewerkte, gespeelde. Hoor je een t of een d?' }; }
        const [inf, a, b] = pk(STERK);
        return { q: `Gisteren ${blank} ik. (${inf})`, say: `Wat is de verleden tijd van ${inf}?`, show: big(`Gisteren … ik. <small>(${inf})</small>`), ans: a, opts: uniq([a, b, 'ge' + a]) };
      } },
    lezen2: { title: 'Begrijpend lezen', pic: '🔍', sub: 'Lees en snap het', col: '#2fae63', from: 4, step: 1, long: true,
      levels: ['Korte verhaaltjes', 'Wat gebeurt er en waarom?', 'Gevoelens en hoofdgedachte', 'Wat bedoelt de schrijver?', 'Doel en mening'],
      make(L) { const [t, q, a, b, c] = pk(BL[L - 1]); return { q, say: 'Lees de tekst. ' + q, show: `<div class="les-text">${t}</div>`, ans: a, opts: [a, b, c] }; } },
    breuk: { title: 'Breuken', pic: '🍕', sub: 'Halven en kwarten', col: '#ff7ccf', from: 5, step: 1.5,
      levels: ['Welk deel is gekleurd?', 'Deel van een getal', 'Welke is groter?', 'Breuken optellen', 'Vereenvoudigen', 'Breuk als kommagetal'],
      make(L) {
        if (L === 1) { const d = pk([2, 3, 4, 5, 6, 8]), n = R(1, d - 1); return { q: 'Welk deel is roze?', say: 'Welk deel van de cirkel is roze?', show: pie(n, d), ans: fr(n, d), opts: around(fr(n, d), 3, () => { const dd = pk([2, 3, 4, 5, 6, 8]); return fr(R(1, dd - 1), dd); }), after: frSay(n, d) }; }
        if (L === 2) { const d = pk([2, 3, 4, 5, 10]), k = R(2, 10), n = R(1, d - 1), a = n * k; return { q: `Hoeveel is ${fr(n, d)} van ${d * k}?`, say: `Hoeveel is ${frSay(n, d)} van ${numNl(d * k)}?`, show: eq(`${fr(n, d)} van ${d * k} = ?`), ans: String(a), opts: around(String(a), 3, () => String(Math.max(1, a + (rnd(5) - 2) * k || a + 1))) }; }
        if (L === 3) {
          let a, b, c, d;
          if (Math.random() < .25) { a = R(1, 3); b = a + R(1, 3); const m = R(2, 3); c = a * m; d = b * m; } else { do { b = pk([2, 3, 4, 5, 6, 8, 10]); d = pk([2, 3, 4, 5, 6, 8, 10]); a = R(1, b - 1); c = R(1, d - 1); } while (a * d === c * b); }
          const x = fr(a, b), y = fr(c, d), ans = a * d === c * b ? 'Even groot' : a * d > c * b ? x : y;
          return { q: 'Welke breuk is het grootst?', say: `Wat is groter: ${frSay(a, b)} of ${frSay(c, d)}?`, show: eq(`${x} &nbsp;of&nbsp; ${y}`), ans, opts: [x, y, 'Even groot'] };
        }
        if (L === 4) { const d = R(3, 10), a = R(1, d - 2), b = R(1, d - 1 - a); const s = a + b; return { q: 'Tel de breuken op.', say: `Hoeveel is ${frSay(a, d)} plus ${frSay(b, d)}?`, show: eq(`${fr(a, d)} + ${fr(b, d)} = ?`), ans: fr(s, d), opts: uniq([fr(s, d), fr(s, 2 * d), fr(s + 1, d), fr(a * b, d)]).slice(0, 3) }; }
        if (L === 5) { const m = R(2, 5), d0 = pk([2, 3, 4, 5, 7]), n0 = R(1, d0 - 1); if (gcd(n0, d0) !== 1) return this.make(5); const n = n0 * m, d = d0 * m; return { q: 'Maak de breuk zo eenvoudig mogelijk.', say: `Maak ${frSay(n, d)} zo eenvoudig mogelijk.`, show: eq(`${fr(n, d)} = ?`), ans: fr(n0, d0), opts: uniq([fr(n0, d0), fr(n - 1, d - 1), fr(n0, d), fr(n, d0 + 1)]).slice(0, 3) }; }
        const P = [[1, 2, '0,5'], [1, 4, '0,25'], [3, 4, '0,75'], [1, 5, '0,2'], [2, 5, '0,4'], [1, 10, '0,1'], [3, 10, '0,3'], [7, 10, '0,7'], [1, 8, '0,125'], [3, 5, '0,6']], [n, d, a] = pk(P);
        return { q: 'Welk kommagetal is even groot?', say: `Welk kommagetal is even groot als ${frSay(n, d)}?`, show: eq(`${fr(n, d)} = ?`), ans: a, opts: around(a, 3, () => pk(P)[2]) };
      } },
    komma: { title: 'Kommagetallen', pic: '🔢', sub: '0,5 en 2,25', col: '#14a89b', from: 6, step: 1.5,
      levels: ['Welk getal is groter?', 'Optellen en aftrekken', 'Keer en gedeeld door 10, 100, 1000', 'Afronden', 'Rekenen met geld'],
      make(L) {
        if (L === 1) { const i = R(0, 9), a = i + R(1, 9) / 10, b = i + R(10, 99) / 100; if (Math.abs(a - b) < 1e-9) return this.make(1); const A = kom(a), B = kom(b); return { q: 'Welk getal is het grootst?', say: `Wat is groter: ${sayKom(A)} of ${sayKom(B)}?`, show: eq(`${A} &nbsp;of&nbsp; ${B}`), ans: a > b ? A : B, opts: [A, B], hint: 'Kijk eerst naar het cijfer direct achter de komma.' }; }
        if (L === 2) { const a = R(10, 500) / 10, b = R(5, 400) / 100, plus = Math.random() < .6 || a < b, s = plus ? a + b : a - b; const S = kom(s); return { q: 'Reken het uit.', say: `Hoeveel is ${sayKom(kom(a))} ${plus ? 'plus' : 'min'} ${sayKom(kom(b))}?`, show: eq(`${kom(a)} ${plus ? '+' : '−'} ${kom(b)} = ?`), ans: S, opts: around(S, 4, () => kom(Math.max(0, s + pk([-1, -.1, .1, 1, .01, -.01, .9])))) }; }
        if (L === 3) { const v = R(11, 999) / 100, p = pk([10, 100, 1000]), mul = Math.random() < .6, r = mul ? v * p : v / p; const A = kom(r); return { q: 'Reken het uit.', say: `Hoeveel is ${sayKom(kom(v))} ${mul ? 'keer' : 'gedeeld door'} ${numNl(p)}?`, show: eq(`${kom(v)} ${mul ? '×' : ':'} ${p} = ?`), ans: A, opts: around(A, 4, () => kom(mul ? v * pk([10, 100, 1000]) : v / pk([10, 100, 1000]))) }; }
        if (L === 4) { const v = R(10, 999) / 10; if (Math.abs(v % 1 - .5) < 1e-9) return this.make(4); const a = String(Math.round(v)); return { q: 'Rond af op een heel getal.', say: `Rond ${sayKom(kom(v))} af op een heel getal.`, show: eq(`${kom(v)} ≈ ?`), ans: a, opts: uniq([a, String(Math.floor(v)), String(Math.ceil(v)), String(Math.round(v) + 1)]).slice(0, 3), hint: 'Is het cijfer achter de komma 5 of meer? Dan naar boven.' }; }
        const p = R(105, 495) / 100, pay = p < 2 ? 2 : p < 5 ? 5 : 10, back = Math.round((pay - p) * 100) / 100, e2 = v => '€ ' + v.toFixed(2).replace('.', ',');
        return { q: `Je koopt iets van ${e2(p)} en betaalt met ${e2(pay)}. Hoeveel krijg je terug?`, say: `Iets kost ${sayKom(kom(p))} euro. Je betaalt met ${numNl(pay)} euro. Hoeveel krijg je terug?`, show: big('🛒'), ans: e2(back), opts: around(e2(back), 3, () => e2(Math.max(.05, Math.round((back + pk([-1, -.1, .1, 1, .5])) * 100) / 100))) };
      } },
    procent: { title: 'Procenten', pic: '💯', sub: 'Korting en delen', col: '#8c5cff', from: 7, step: 1.5,
      levels: ['50% en 25%', '10% en 1%', '75%, 20% en 5%', 'Korting', 'Breuk en procent'],
      make(L) {
        const of = (p, t) => { const a = String(p * t / 100); return { q: `Hoeveel is ${p}% van ${t}?`, say: `Hoeveel is ${numNl(p)} procent van ${numNl(t)}?`, show: eq(`${p}% van ${t} = ?`), ans: a, opts: around(a, 3, () => String(Math.max(1, Math.round(+a + pk([-2, -1, 1, 2, 5, -5]) * Math.max(1, t / 20))))) }; };
        if (L === 1) return of(pk([50, 25]), R(2, 30) * 4);
        if (L === 2) return of(pk([10, 1]), R(2, 40) * 100);
        if (L === 3) return of(pk([75, 20, 5]), R(1, 20) * 20);
        if (L === 4) { const p = R(2, 20) * 10, k = pk([10, 20, 25, 50]), a = eur(p - p * k / 100); return { q: `Een spel kost € ${p}. Je krijgt ${k}% korting. Wat betaal je?`, say: `Een spel kost ${numNl(p)} euro. Je krijgt ${numNl(k)} procent korting. Wat betaal je?`, show: big('🏷️'), ans: a, opts: uniq([a, eur(p * k / 100), eur(p - k), eur(p - p * k / 100 + 5)]).slice(0, 3) }; }
        const P = [[1, 2, '50%'], [1, 4, '25%'], [3, 4, '75%'], [1, 5, '20%'], [2, 5, '40%'], [1, 10, '10%'], [3, 10, '30%'], [1, 100, '1%']], [n, d, a] = pk(P);
        return { q: 'Hoeveel procent is dat?', say: `Hoeveel procent is ${frSay(n, d)}?`, show: eq(`${fr(n, d)} = ?`), ans: a, opts: around(a, 3, () => pk(P)[2]) };
      } },
    meten: { title: 'Meten', pic: '📏', sub: 'cm, kg, liter', col: '#f2b705', from: 5, step: 1,
      levels: ['Centimeter en meter', 'Meter en kilometer', 'Gram en kilogram', 'Milliliter en liter', 'Minuten en uren', 'Oppervlakte'],
      make(L) {
        const conv = (small, bigU, f, unitSay) => {
          if (Math.random() < .5) { const v = R(1, 9) * (Math.random() < .5 ? 1 : 1.5); const a = kom(v * f); return { q: `${kom(v)} ${bigU} = ? ${small}`, say: `Hoeveel ${unitSay[0]} is ${sayKom(kom(v))} ${unitSay[1]}?`, show: eq(`${kom(v)} ${bigU} = ? ${small}`), ans: a, opts: around(a, 3, () => kom(v * pk([f / 10, f * 10, f / 100]))) }; }
          const v = R(2, 40) * f / 4, a = kom(v / f); return { q: `${v} ${small} = ? ${bigU}`, say: `Hoeveel ${unitSay[1]} is ${numNl(v)} ${unitSay[0]}?`, show: eq(`${v} ${small} = ? ${bigU}`), ans: a, opts: around(a, 3, () => kom(v / pk([f / 10, f * 10, 10]))) };
        };
        if (L === 1) return conv('cm', 'm', 100, ['centimeter', 'meter']);
        if (L === 2) return conv('m', 'km', 1000, ['meter', 'kilometer']);
        if (L === 3) return conv('g', 'kg', 1000, ['gram', 'kilogram']);
        if (L === 4) return conv('ml', 'l', 1000, ['milliliter', 'liter']);
        if (L === 5) { const T = [['2 uur', 120, 'twee uur'], ['een half uur', 30, 'een half uur'], ['anderhalf uur', 90, 'anderhalf uur'], ['3 kwartier', 45, 'drie kwartier'], ['1 kwartier', 15, 'een kwartier'], ['3 uur', 180, 'drie uur'], ['2,5 uur', 150, 'twee en een half uur']], [t, m, s] = pk(T), a = String(m); return { q: `Hoeveel minuten is ${t}?`, say: `Hoeveel minuten is ${s}?`, show: big('⏱️'), ans: a, opts: around(a, 3, () => String(pk(T)[1])) }; }
        const l = R(2, 12), b = R(2, 9), area = Math.random() < .6, a = area ? `${l * b} cm²` : `${2 * (l + b)} cm`;
        return { q: area ? 'Hoe groot is de oppervlakte?' : 'Hoe lang is de omtrek?', say: area ? `Een rechthoek is ${numNl(l)} bij ${numNl(b)} centimeter. Hoe groot is de oppervlakte?` : `Een rechthoek is ${numNl(l)} bij ${numNl(b)} centimeter. Hoe lang is de omtrek?`,
          show: `<svg class="les-rect" viewBox="0 0 160 110"><rect x="20" y="10" width="120" height="70" rx="4" fill="#ffd23c" stroke="#1d2a4d" stroke-width="3"/><text x="80" y="100" text-anchor="middle" font-size="16" font-weight="800" fill="#1d2a4d">${l} cm</text><text x="148" y="50" font-size="16" font-weight="800" fill="#1d2a4d">${b}</text></svg>`,
          ans: a, opts: uniq([a, area ? `${2 * (l + b)} cm²` : `${l * b} cm`, area ? `${l + b} cm²` : `${l + b} cm`]) };
      } },
    brug: { title: 'Brugklas rekenen', pic: '🧮', sub: 'Negatief, x, machten', col: '#1d2a4d', from: 8, step: 2,
      levels: ['Negatieve getallen', 'Voorrangsregels', 'Machten', 'Vergelijkingen (x + 7 = 12)', 'Vergelijkingen (3x + 1 = 13)', 'Wortels'],
      make(L) {
        const o = (a, gen) => around(num(a), 4, () => num(gen()));
        if (L === 1) { const a = R(-12, 12), b = R(-12, 12), plus = Math.random() < .5, r = plus ? a + b : a - b; const sb = b < 0 ? `(${num(b)})` : b; return { q: 'Reken het uit.', say: `Hoeveel is ${a < 0 ? 'min ' + numNl(-a) : numNl(a)} ${plus ? 'plus' : 'min'} ${b < 0 ? 'min ' + numNl(-b) : numNl(b)}?`, show: eq(`${num(a)} ${plus ? '+' : '−'} ${sb} = ?`), ans: num(r), opts: o(r, () => r + pk([-2, 2, -2 * b, 2 * b, -1, 1]) || r + 3), hint: 'Min en min is plus. Stel je een getallenlijn voor.' }; }
        if (L === 2) { const a = R(2, 9), b = R(2, 9), c = R(2, 9), k = rnd(3); const [t, r, w] = k === 0 ? [`${a} + ${b} × ${c}`, a + b * c, (a + b) * c] : k === 1 ? [`${a} × ${b} − ${c}`, a * b - c, a * (b - c)] : [`(${a} + ${b}) × ${c}`, (a + b) * c, a + b * c]; return { q: 'Let op de voorrangsregels.', say: 'Reken het uit. Eerst haakjes, dan keer en delen, dan plus en min.', show: eq(`${t} = ?`), ans: num(r), opts: uniq([num(r), num(w), num(r + 1), num(r - 2)]).slice(0, 4), hint: 'Eerst haakjes, dan keer en delen, dan plus en min.' }; }
        if (L === 3) { const b = R(2, 10), e = b <= 3 ? R(2, 4) : b === 10 ? R(2, 4) : 2, r = b ** e; return { q: 'Hoeveel is de macht?', say: `Hoeveel is ${numNl(b)} tot de macht ${numNl(e)}?`, show: eq(`${b}<sup>${e}</sup> = ?`), ans: String(r), opts: uniq([String(r), String(b * e), String(b + e), String(r + b)]).slice(0, 4), hint: `${b} tot de macht ${e} is ${e} keer ${b} met zichzelf vermenigvuldigen.` }; }
        if (L === 4) { const x = R(1, 20), a = R(1, 15), plus = Math.random() < .5, rhs = plus ? x + a : x - a; return { q: 'Welk getal is x?', say: `Welk getal is x? x ${plus ? 'plus' : 'min'} ${numNl(a)} is ${rhs < 0 ? 'min ' + numNl(-rhs) : numNl(rhs)}.`, show: eq(`x ${plus ? '+' : '−'} ${a} = ${num(rhs)}`), ans: num(x), opts: o(x, () => x + pk([-2 * a, 2 * a, 1, -1, 2])) }; }
        if (L === 5) { const x = R(1, 12), a = R(2, 9), c = R(0, 15), rhs = a * x + c; return { q: 'Welk getal is x?', say: `Welk getal is x? ${numNl(a)} x ${c ? 'plus ' + numNl(c) + ' ' : ''}is ${numNl(rhs)}.`, show: eq(`${a}x${c ? ' + ' + c : ''} = ${rhs}`), ans: num(x), opts: o(x, () => x + pk([-2, -1, 1, 2, 3])), hint: 'Haal eerst het losse getal weg, deel dan door het getal voor de x.' }; }
        const k = R(2, 15), r = String(k); return { q: 'Hoeveel is de wortel?', say: `Hoeveel is de wortel uit ${numNl(k * k)}?`, show: eq(`√${k * k} = ?`), ans: r, opts: uniq([r, String(k + 1), String(k - 1), String(k * 2)]).slice(0, 4) };
      } },
    engels: { title: 'Engels', pic: '🇬🇧', sub: 'Woordjes', col: '#2f8cff', from: 7, step: 1.5,
      levels: ['Dieren', 'Kleuren', 'Getallen', 'Eten', 'Huis en school'],
      make(L) { const set = EN[L - 1], w = pk(set); return { q: `Wat is <b>${w[0]}</b> in het Engels?`, say: `Wat is ${w[0]} in het Engels?`, show: big(w[2]), ans: w[1], opts: around(w[1], 3, () => pk(set)[1]) }; } },
  };

  function lesson(id, s) {
    const cfg = LESSONS[id], key = 'les-' + id, NEED = 5;
    s = s || { n: LV.get(key), done: 0, lives: 3 };
    const L = Math.min(s.n, cfg.levels.length), Q = cfg.make(L);
    const opts = shuffle(uniq(Q.opts.includes(Q.ans) ? Q.opts : [Q.ans, ...Q.opts]));
    const cols = cfg.long ? 1 : opts.length === 4 ? 2 : opts.length;
    mount(`${prompt(Q.q)}${lvBar(s.done, NEED, s.lives)}<div class="sum-kind">${cfg.levels[L - 1]}</div>
      <div class="les-show">${Q.show || ''}</div>
      <div class="answers les-ans${cfg.long ? ' les-long' : ''}" style="grid-template-columns:repeat(${cols},1fr)">${opts.map((o, i) => `<button class="ans" data-i="${i}">${o}</button>`).join('')}</div>`, lvTitle(cfg.title, s.n));
    wireAgain(Q.say || Q.q.replace(/<[^>]+>/g, ''));
    let done = false;
    app.querySelectorAll('.les-ans .ans').forEach(b => b.onclick = () => {
      if (done) return;
      if (opts[+b.dataset.i] === Q.ans) {
        done = true; b.classList.add('right'); s.done++; setProgress(s.done, NEED); sfx.good(); addStar();
        if (Q.after) later(() => say(Q.after), 300);
        if (s.done >= NEED) levelUp(key, s.n, () => lesson(id)); else { later(() => cheer(), 1500); later(() => lesson(id, s), 2900); }
      } else {
        sfx.bad(); wiggle(b); s.lives--; setHearts(s.lives);
        if (s.lives <= 0) { done = true; gameOver(s.n, () => lesson(id, { n: s.n, done: 0, lives: 3 })); }
        else say(Q.hint || 'Probeer nog eens.');
      }
    });
  }

  /* Start levels per groep for the games that were already there */
  const FLOOR = {
    'sum-+': { 3: 1, 4: 6, 5: 10, 6: 12, 7: 13, 8: 14, 9: 15 }, 'sum--': { 3: 1, 4: 6, 5: 10, 6: 12, 7: 13, 8: 14, 9: 15 },
    'sum-×': { 4: 1, 5: 3, 6: 6, 7: 8, 8: 9, 9: 10 }, 'sum-:': { 4: 1, 5: 3, 6: 6, 7: 8, 8: 9, 9: 10 },
    clock: { 3: 1, 4: 3, 5: 4, 6: 5, 7: 5, 8: 5, 9: 5 }, money: { 3: 1, 4: 3, 5: 4, 6: 5, 7: 5, 8: 5, 9: 5 },
  };
  function groepFloor(k) {
    const g = groep();
    if (FLOOR[k]) return FLOOR[k][g] || 1;
    const id = k.startsWith('les-') ? k.slice(4) : null, L = id && LESSONS[id];
    return L ? Math.max(1, Math.min(L.levels.length, 1 + Math.floor((g - L.from) * L.step))) : 1;
  }
  const LEER_BY_GROEP = {
    3: ['Plussommen', 'Minsommen', 'lezen', 'Letters schrijven', 'Welke letter?', 'Klokkijken', 'Geld tellen'],
    4: ['Plussommen', 'Minsommen', 'Keersommen', 'Deelsommen', 'lezen', 'spelling', 'lezen2', 'Klokkijken', 'Geld tellen'],
    5: ['Plussommen', 'Minsommen', 'Keersommen', 'Deelsommen', 'breuk', 'meten', 'spelling', 'lezen2', 'Klokkijken', 'Geld tellen'],
    6: ['Plussommen', 'Minsommen', 'Keersommen', 'Deelsommen', 'breuk', 'komma', 'meten', 'spelling', 'ww', 'lezen2', 'Klokkijken'],
    7: ['Plussommen', 'Minsommen', 'Keersommen', 'Deelsommen', 'breuk', 'komma', 'procent', 'meten', 'ww', 'engels', 'lezen2'],
    8: ['Keersommen', 'Deelsommen', 'breuk', 'komma', 'procent', 'meten', 'brug', 'ww', 'engels', 'lezen2'],
    9: ['Keersommen', 'Deelsommen', 'breuk', 'komma', 'procent', 'meten', 'brug', 'ww', 'engels', 'lezen2'],
  };
  function leerTiles(base) {
    return LEER_BY_GROEP[groep()].map(n => {
      const L = LESSONS[n];
      if (L) return { pic: L.pic, name: L.title, sub: L.sub, col: L.col, go: () => lesson(n) };
      return base.find(t => t.name === n);
    }).filter(Boolean);
  }
  const groepRow = () => `<div class="groep-row" role="group" aria-label="Groep"><span>Groep</span>${GROEPEN.map(g => `<button class="groep${g === groep() ? ' on' : ''}" data-g="${g}">${g}</button>`).join('')}</div>`;
  function wireGroep() {
    app.querySelectorAll('.groep').forEach(b => b.onclick = () => {
      const g = +b.dataset.g; if (g === groep()) return;
      store('groep', g);
      [...Object.keys(FLOOR), ...Object.keys(LESSONS).map(id => 'les-' + id)].forEach(k => LV.set(k, groepFloor(k)));
      sfx.tap(); say(g === 9 ? 'Groep negen, de brugklas!' : `Groep ${numNl(g)}!`); menu();
    });
  }
