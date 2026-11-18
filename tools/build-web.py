"""Puts the Queen Alina additions from web-src/ into the game (app/src/main/assets/web/index.html).

Each file sits between /* QA:BEGIN name */ and /* QA:END name */.
  python tools/build-web.py          rewrite the blocks from web-src/
  python tools/build-web.py --check  fail if index.html and web-src/ differ (used in CI)
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GAME = os.path.join(ROOT, "app", "src", "main", "assets", "web", "index.html")
SRC = os.path.join(ROOT, "web-src")
FILES = ["alina.css", "leren.css", "fit.css", "alina.js", "leren.js", "fit.js"]


def read(path):
    with open(path, encoding="utf-8") as f:
        return f.read().replace("\r\n", "\n")


def build(html):
    for name in FILES:
        body = read(os.path.join(SRC, name)).strip("\n")
        pat = re.compile(r"(/\* QA:BEGIN %s \*/\n).*?(\n[ \t]*/\* QA:END %s \*/)" % (re.escape(name), re.escape(name)), re.S)
        if len(pat.findall(html)) != 1:
            raise SystemExit(f"Marker voor {name} niet precies één keer gevonden in index.html")
        html = pat.sub(lambda m: m.group(1) + body + m.group(2), html)
    return html


def main():
    html = read(GAME)
    out = build(html)
    if "--check" in sys.argv:
        if out != html:
            raise SystemExit("index.html loopt niet gelijk met web-src/. Draai: python tools/build-web.py")
        print("index.html is gelijk met web-src/")
        return
    with open(GAME, "w", encoding="utf-8", newline="") as f:
        f.write(out)
    print("web-src/ in index.html gezet")


if __name__ == "__main__":
    main()
