"""Haalt Spelen met LOU op en zet het offline in de Android-app als Queen Alina.

Gebruik:  python tools/import-lou.py [map-met-eerder-gedownloade-voice-clips]

Schrijft naar app/src/main/assets/web/. Elke aanpassing moet precies het verwachte
aantal keren passen, anders stopt het script: zo merk je het als LOU verandert.
"""
import json
import os
import re
import shutil
import sys
import urllib.request
from concurrent.futures import ThreadPoolExecutor

SITE = "https://spelenmetlou.web.app/"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "web")
UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36"


def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    return urllib.request.urlopen(req, timeout=60).read()


def patch(html, old, new, count=1):
    found = html.count(old)
    if found != count:
        raise SystemExit(f"Aanpassing past niet ({found}x, verwacht {count}x): {old[:80]!r}")
    return html.replace(old, new)


PATCHES = [
    # Naam van de app
    ("<title>Spelen en Leren met LOU</title>", "<title>Queen Alina</title>", 1),
    ('<meta name="apple-mobile-web-app-title" content="LOU">',
     '<meta name="apple-mobile-web-app-title" content="Queen Alina">', 1),
    ('<h1>Spelen en Leren <span class="h1-lou">met LOU</span></h1>',
     '<h1>Spelen en Leren <span class="h1-lou">met Queen Alina</span></h1>', 2),

    # Offline: lettertypen uit de app, geen service worker, Android-spraak
    ('<link rel="preconnect" href="https://fonts.googleapis.com">\n', "", 1),
    ('<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@600;800&family=Nunito:wght@600;800&display=swap">',
     '<link rel="stylesheet" href="fonts/fonts.css">', 1),
    ("</head>", '<script src="qa-bridge.js"></script>\n</head>', 1),
    ("if ('serviceWorker' in navigator) addEventListener('load', () => navigator.serviceWorker.register('sw.js').catch(() => {}));",
     "/* Queen Alina: geen service worker, alles zit in de app */", 1),
    ("const online = location.protocol.startsWith('http')",
     "const online = !window.QAHost && location.protocol.startsWith('http')", 1),
    ("msg.textContent = 'Deze versie wordt vanzelf bijgewerkt.';",
     "msg.textContent = window.QAHost ? 'Een nieuwe versie installeer je met de downloadlink van Queen-Alina.apk.' : 'Deze versie wordt vanzelf bijgewerkt.';", 1),
    ("<small>De app haalt updates ook vanzelf op als er internet is.</small>",
     "<small>${window.QAHost ? 'Een nieuwe versie installeer je met de downloadlink.' : 'De app haalt updates ook vanzelf op als er internet is.'}</small>", 1),

    # Getallen boven de 1000 uitspreken
    ("const numNl = n => n > 100 && n < 1000 ?",
     "const numNl = n => n > 1000 ? (Math.floor(n / 1000) === 1 ? 'duizend' : numNl(Math.floor(n / 1000)) + 'duizend') + (n % 1000 ? ' ' + numNl(n % 1000) : '') : n > 100 && n < 1000 ?", 1),

    # Moeilijkere levels: sommen
    ("'Grote sommen tot 1000'],", "'Grote sommen tot 1000', 'Tot 5000', 'Tot 10.000', 'Wat mist er? Tot 1000'],", 2),
    ("'Grote keersommen'],", "'Grote keersommen', 'Tafel van 11 en 12', 'Keersommen tot 240'],", 1),
    ("'Grote deelsommen'],", "'Grote deelsommen', 'Delen door 11 en 12', 'Grote deelsommen tot 240'],", 1),
    ("const L = Math.min(n, 8), T = [[1, 2, 3], [5, 10], [3, 4], [6, 7], [8, 9], [2, 3, 4, 5, 6, 7, 8, 9, 10], [2, 3, 4, 5, 6, 7, 8, 9, 10], [11, 12, 15, 20, 25]][L - 1];",
     "const L = Math.min(n, 10), T = [[1, 2, 3], [5, 10], [3, 4], [6, 7], [8, 9], [2, 3, 4, 5, 6, 7, 8, 9, 10], [2, 3, 4, 5, 6, 7, 8, 9, 10], [11, 12, 15, 20, 25], [11, 12], [13, 14, 15, 16, 18, 20]][L - 1];", 1),
    ("k = L === 8 ? r(2, 9) : L === 1 ? r(1, 5) : r(1, 10)",
     "k = L === 8 ? r(2, 9) : L === 1 ? r(1, 5) : L === 9 ? r(2, 12) : L === 10 ? r(3, 12) : r(1, 10)", 1),
    ("const L = Math.min(n, 13);", "const L = Math.min(n, 16);", 1),
    ("else { a = r(100, 850); b = r(100, 999 - a); }",
     "else if (L === 13) { a = r(100, 850); b = r(100, 999 - a); }\n"
     "      else if (L === 14) { a = r(1000, 4000); b = r(100, 5000 - a); }\n"
     "      else if (L === 15) { a = r(1000, 8000); b = r(500, 10000 - a); }\n"
     "      else { a = r(100, 800); b = r(50, 999 - a); miss = true; }", 1),
    ("else { a = r(300, 999); b = r(100, a - 50); }",
     "else if (L === 13) { a = r(300, 999); b = r(100, a - 50); }\n"
     "      else if (L === 14) { a = r(1500, 5000); b = r(100, a - 500); }\n"
     "      else if (L === 15) { a = r(5000, 10000); b = r(1000, a - 1000); }\n"
     "      else { a = r(300, 999); b = r(50, a - 50); miss = true; }", 1),
    ("sub: 'Tot 1000', col: '#2f8cff'", "sub: 'Tot 10.000', col: '#2f8cff'", 1),
    ("sub: 'Tot 1000', col: '#ff6b5b'", "sub: 'Tot 10.000', col: '#ff6b5b'", 1),

    # Moeilijkere levels: tellen, memory, puzzel
    ("const max = [5, 10, 15, 20][Math.min(s.n, 4) - 1];",
     "const max = [5, 10, 15, 20, 25, 30][Math.min(s.n, 6) - 1];", 1),
    ("pairs = Math.min(8, lv + 2);", "pairs = Math.min(10, lv + 2);", 1),
    ("LVLS = [[2, 2], [3, 2], [3, 3], [4, 3], [4, 4], [5, 4], [5, 5]];",
     "LVLS = [[2, 2], [3, 2], [3, 3], [4, 3], [4, 4], [5, 4], [5, 5], [6, 5], [6, 6]];", 1),

    # Moeilijkere levels: klok op 5 minuten en op de minuut
    ("const mins = [[0], [0, 30], [0, 30, 15, 45]][Math.min(s.n, 3) - 1];",
     "const mins = [[0], [0, 30], [0, 30, 15, 45], Array.from({ length: 12 }, (_, i) => i * 5), Array.from({ length: 60 }, (_, i) => i)][Math.min(s.n, 5) - 1];", 1),
    ("const timeLabel = (hh, mm) => mm === 0 ? `${hh} uur` : mm === 30 ? `half ${nextH(hh)}` : mm === 15 ? `kwart over ${hh}` : `kwart voor ${nextH(hh)}`;",
     "const timeLabel = (hh, mm) => mm === 0 ? `${hh} uur` : mm === 30 ? `half ${nextH(hh)}` : mm === 15 ? `kwart over ${hh}` : mm === 45 ? `kwart voor ${nextH(hh)}`"
     " : mm < 15 ? `${numNl(mm)} over ${hh}` : mm < 30 ? `${numNl(30 - mm)} voor half ${nextH(hh)}` : mm < 45 ? `${numNl(mm - 30)} over half ${nextH(hh)}` : `${numNl(60 - mm)} voor ${nextH(hh)}`;", 1),
    (": 'Grote wijzer rechts: kwart over. Links: kwart voor.';",
     ": s.n === 3 ? 'Grote wijzer rechts: kwart over. Links: kwart voor.' : s.n === 4 ? 'Van cijfer naar cijfer is vijf minuten.' : 'Elk klein streepje is één minuut.';", 1),

    # Moeilijkere levels: geld tot 100 euro
    ("const [lo, hi, coins] = [[1, 5, [1, 2]], [2, 10, [1, 2, 5]], [5, 20, [1, 2, 5, 10]]][Math.min(s.n, 3) - 1];",
     "const [lo, hi, coins] = [[1, 5, [1, 2]], [2, 10, [1, 2, 5]], [5, 20, [1, 2, 5, 10]], [10, 50, [1, 2, 5, 10, 20]], [20, 100, [1, 2, 5, 10, 20, 50]]][Math.min(s.n, 5) - 1];", 1),
    (".note.n10 {",
     ".note.n20 { background: linear-gradient(135deg, #5f8fd8, #a9c6f2); border: 3px solid #3d6bb5; }\n"
     ".note.n50 { background: linear-gradient(135deg, #f0a040, #f8cf95); border: 3px solid #c77a1f; }\n"
     ".note.n10 {", 1),

    # Ouders: moeilijke levels meteen aanzetten
    ('<button class="pbtn" id="pStats">',
     '<button class="pbtn" id="pHard">🚀 Moeilijke levels<small>Sommen tot 10.000, tafels van 11 en 12, klok op de minuut en geld tot 100 euro.</small></button>\n'
     '          <button class="pbtn" id="pStats">', 1),
    ("document.getElementById('pStats').onclick = () => { sfx.tap(); parentStats(pbox, options); };",
     "document.getElementById('pStats').onclick = () => { sfx.tap(); parentStats(pbox, options); };\n"
     "      document.getElementById('pHard').onclick = () => {\n"
     "        [['sum-+', 14], ['sum--', 14], ['sum-×', 9], ['sum-:', 9], ['clock', 4], ['money', 4], ['cnt-Tellen', 5], ['cnt-Pepernoten tellen', 5],"
     " ['mem-Memory', 8], ['mem-Sprookjes-memory', 8], ['jig', 8], ['jig-lou', 8]].forEach(([k, v]) => { if (LV.get(k) < v) LV.set(k, v); });\n"
     "        sfx.good(); document.getElementById('pmsg').textContent = 'De moeilijke levels staan aan.'; say('De moeilijke levels staan aan.');\n"
     "      };", 1),
]


def fonts(out):
    css = get("https://fonts.googleapis.com/css2?family=Baloo+2:wght@600;800&family=Nunito:wght@600;800&display=swap").decode()
    blocks = re.findall(r"/\* (\S+) \*/\s*(@font-face \{.*?\})", css, re.S)
    keep = []
    os.makedirs(out, exist_ok=True)
    for subset, block in blocks:
        if subset != "latin":
            continue
        fam = re.search(r"font-family: '([^']+)'", block).group(1)
        weight = re.search(r"font-weight: (\d+)", block).group(1)
        url = re.search(r"url\((https://[^)]+)\)", block).group(1)
        name = f"{fam.replace(' ', '')}-{weight}.woff2"
        open(os.path.join(out, name), "wb").write(get(url))
        keep.append(block.replace(url, name))
    if len(keep) != 4:
        raise SystemExit(f"Verwacht 4 lettertypen, kreeg {len(keep)}")
    open(os.path.join(out, "fonts.css"), "w", encoding="utf-8").write("\n".join(keep) + "\n")


def voices(out, cache):
    os.makedirs(out, exist_ok=True)
    table = get(SITE + "voice/voice.json")
    open(os.path.join(out, "voice.json"), "wb").write(table)
    files = sorted(set(json.loads(table).values()))

    def one(name):
        dst = os.path.join(out, name)
        if os.path.exists(dst) and os.path.getsize(dst):
            return True
        if cache and os.path.exists(os.path.join(cache, name)):
            shutil.copyfile(os.path.join(cache, name), dst)
            return True
        for _ in range(3):
            try:
                open(dst, "wb").write(get(SITE + "voice/" + name))
                return True
            except Exception:
                pass
        return False

    with ThreadPoolExecutor(24) as ex:
        ok = sum(ex.map(one, files))
    if ok != len(files):
        raise SystemExit(f"Niet alle stemfragmenten opgehaald: {ok}/{len(files)}")
    for f in os.listdir(out):
        if f.endswith(".mp3") and f not in files:
            os.remove(os.path.join(out, f))
    return len(files)


def main():
    cache = sys.argv[1] if len(sys.argv) > 1 else None
    os.makedirs(OUT, exist_ok=True)
    html = get(SITE).decode("utf-8")
    for old, new, count in PATCHES:
        html = patch(html, old, new, count)
    with open(os.path.join(OUT, "index.html"), "w", encoding="utf-8", newline="") as f:
        f.write(html)
    fonts(os.path.join(OUT, "fonts"))
    n = voices(os.path.join(OUT, "voice"), cache)
    print(f"Klaar: index.html ({len(html)} tekens), 4 lettertypen, {n} stemfragmenten in {OUT}")


if __name__ == "__main__":
    main()
