"""Build Queen Alina launcher and store icons from the approved Alina sprite.

Writes only derived images: the source atlas is never modified.
Adaptive icon layout: 108dp canvas, 66dp safe circle -> foreground art fits in the centre ~61%.
Run: python tools/make-icon.py
"""
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw

root = Path(__file__).resolve().parents[1]
source = Image.open(root / 'app/src/main/assets/friends.png').convert('RGBA')

# Head of Alina from the first sprite cell (approved Knuffelwereld sheet).
head = source.crop((40, 26, 244, 178))
# Oval medallion around hair and face; this removes the shoulders without a hard horizontal cut.
oval = Image.new('L', head.size, 0)
ImageDraw.Draw(oval).ellipse((2, 2, head.width - 3, head.height - 3), fill=255)
head.putalpha(ImageChops.multiply(head.getchannel('A'), oval))

BACKGROUND = (251, 235, 224, 255)  # soft peach from the approved palette
SIZE = 432  # xxxhdpi adaptive foreground: 108dp * 4

# Fit head into the safe zone (about 62% of the canvas) and centre it.
target = int(SIZE * 0.62)
scale = min(target / head.width, target / head.height)
resized = head.resize((round(head.width * scale), round(head.height * scale)), Image.LANCZOS)

foreground = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
foreground.alpha_composite(resized, ((SIZE - resized.width) // 2, (SIZE - resized.height) // 2 + 10))
res = root / 'app/src/main/res'
(res / 'drawable-nodpi').mkdir(parents=True, exist_ok=True)
foreground.save(res / 'drawable-nodpi/ic_launcher_foreground.png', optimize=True)

# Store/presentation icon: full colour square with a rounded backdrop, 512px.
store = Image.new('RGBA', (512, 512), BACKGROUND)
mask = Image.new('L', (512, 512), 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, 511, 511), radius=112, fill=255)
store.putalpha(mask)
art = foreground.resize((512, 512), Image.LANCZOS)
store.alpha_composite(art)
store.save(root / 'art/queen-alina-icon-512.png', optimize=True)

print('Wrote ic_launcher_foreground.png (432px) and queen-alina-icon-512.png')

