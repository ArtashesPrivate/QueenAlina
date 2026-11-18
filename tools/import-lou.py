"""Haalt de nieuwste Spelen met LOU op en voegt die samen met Queen Alina.

Gebruik:  python tools/import-lou.py [map-met-eerder-gedownloade-voice-clips]

Het spel in app/src/main/assets/web/index.html is LOU met onze aanpassingen. web-src/lou-original.html is
de LOU-versie waarop dat gebaseerd is. Bij een nieuwe LOU doet dit script een drieweg-samenvoeging
(git merge-file): wat LOU veranderde én wat wij veranderden blijft allebei bewaard. Botsen ze, dan stopt
het script en staan de botsingen met <<<<<<< in index.html.merge, zodat je ze met de hand oplost.
Daarna zet tools/build-web.py de bestanden uit web-src/ weer in het spel.
"""
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import urllib.request
from concurrent.futures import ThreadPoolExecutor

SITE = "https://spelenmetlou.web.app/"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "web")
GAME = os.path.join(OUT, "index.html")
BASE = os.path.join(ROOT, "web-src", "lou-original.html")
UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Mobile Safari/537.36"


def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    data = urllib.request.urlopen(req, timeout=60).read()
    if not data:
        raise IOError("leeg antwoord van " + url)
    return data


def write(path, data):
    with open(path, "wb") as f:
        f.write(data)


def merge(new_lou):
    with open(BASE, "rb") as f:
        if f.read() == new_lou:
            return "LOU is niet veranderd"
    with tempfile.TemporaryDirectory() as tmp:
        theirs = os.path.join(tmp, "lou-new.html")
        write(theirs, new_lou)
        r = subprocess.run(["git", "merge-file", "-p", "-L", "Queen Alina", "-L", "LOU (oud)", "-L", "LOU (nieuw)", GAME, BASE, theirs],
                           capture_output=True)
        if r.returncode < 0 or r.returncode > 127:
            raise SystemExit("git merge-file is mislukt: " + r.stderr.decode(errors="replace"))
        if r.returncode:
            write(GAME + ".merge", r.stdout)
            raise SystemExit(f"{r.returncode} botsing(en). Los ze op in {GAME}.merge, hernoem dat naar index.html en zet "
                             f"web-src/lou-original.html op de nieuwe LOU.")
        write(GAME, r.stdout)
    write(BASE, new_lou)
    return "nieuwe LOU samengevoegd"


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
        write(os.path.join(out, name), get(url))
        keep.append(block.replace(url, name))
    if len(keep) != 4:
        raise SystemExit(f"Verwacht 4 lettertypen, kreeg {len(keep)}")
    write(os.path.join(out, "fonts.css"), ("\n".join(keep) + "\n").encode())


def voices(out, cache):
    os.makedirs(out, exist_ok=True)
    table = get(SITE + "voice/voice.json")
    write(os.path.join(out, "voice.json"), table)
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
                write(dst, get(SITE + "voice/" + name))
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
    what = merge(get(SITE))
    subprocess.run([sys.executable, os.path.join(ROOT, "tools", "build-web.py")], check=True)
    fonts(os.path.join(OUT, "fonts"))
    n = voices(os.path.join(OUT, "voice"), cache)
    print(f"Klaar: {what}, 4 lettertypen, {n} stemfragmenten in {OUT}")


if __name__ == "__main__":
    main()
