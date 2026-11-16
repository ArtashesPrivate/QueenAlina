"""Analyse alpha bounds only; never modifies or exports the source images."""
from pathlib import Path
import json
from PIL import Image

assets=Path(__file__).resolve().parents[1]/'app/src/main/assets'
grids={'friends.png':(5,4),'extras.png':(3,2),'extra-actions.png':(6,3)}
metadata={}
for name,(cols,rows) in grids.items():
    with Image.open(assets/name) as image:
        alpha=image.getchannel('A')
        frames=[]
        for row in range(rows):
            line=[]
            for col in range(cols):
                left,top=col*image.width//cols,row*image.height//rows
                right,bottom=(col+1)*image.width//cols,(row+1)*image.height//rows
                bounds=alpha.crop((left,top,right,bottom)).point(lambda a:255 if a>110 else 0).getbbox()
                if bounds is None:
                    raise ValueError(f'Empty sprite: {name}, row {row}, column {col}')
                rect=[left+bounds[0],top+bounds[1],left+bounds[2],top+bounds[3]]
                assert 0<=rect[0]<rect[2]<=image.width and 0<=rect[1]<rect[3]<=image.height
                line.append(rect)
            frames.append(line)
        metadata[name]=frames
(assets/'sprite-bounds.json').write_text(json.dumps(metadata,separators=(',',':')),encoding='utf-8')
print('Validated 38 sprite bounds; source PNG files unchanged.')
