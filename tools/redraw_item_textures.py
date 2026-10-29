"""Refine original miner pixel art; recolour Nomi's exact stabilized base.

No image generation or smoothing. Native 64px ships preserve the original detail.
Input sprites are retained in texture_sources/ for reproducible edits.
"""
from pathlib import Path
import colorsys
import statistics
from PIL import Image, ImageDraw
ROOT = Path(__file__).resolve().parents[1]
SOURCES = Path(__file__).resolve().parent / 'texture_sources'
TEXTURES = ROOT / 'src/main/resources/assets/modid/textures/item'

def refine_miner(name, advanced=False):
    source = Image.open(SOURCES / f'{name}-original.png').convert('RGBA')
    source = source.crop(source.getbbox())
    source.thumbnail((54, 58), Image.Resampling.NEAREST)
    sprite = Image.new('RGBA', (64, 64))
    sprite.paste(source, ((64-source.width)//2, 61-source.height))
    for y in range(64):
        for x in range(64):
            r,g,b,a = sprite.getpixel((x,y))
            if a < 128:
                sprite.putpixel((x,y),(0,0,0,0))
                continue
            h,s,v = colorsys.rgb_to_hsv(r/255,g/255,b/255)
            if 0.69 < h < 0.96 and s > 0.18:
                if not advanced and (x < 17 or x > 46) and 24 < y < 47:
                    h,s = 0.075,s*0.65  # Warm nichrome windings.
                else:
                    h,s,v = (0.74 if advanced else 0.755),s*(0.67 if advanced else 0.52),min(1,v*1.08)
            elif advanced and s < 0.24 and 33 < y < 53 and (x < 26 or x > 37):
                h,s,v = 0.075,0.20,min(1,v*1.12)  # Duranium armour.
            rgb = colorsys.hsv_to_rgb(h,s,v)
            sprite.putpixel((x,y),(*[round(c*255) for c in rgb],255))
    d = ImageDraw.Draw(sprite)
    for mirror in (False,True):
        def pts(points):
            return [(63-x if mirror else x,y) for x,y in points]
        if advanced:
            # Trinium nose bevels, Omnium rails, Duranium wing ribs.
            for points,color in [([(22,12),(23,16),(25,20)],'#b6cccf'),
                                 ([(21,13),(22,18),(24,21)],'#627f91'),
                                 ([(26,29),(25,39),(22,47)],'#af65a3'),
                                 ([(27,29),(26,39),(23,47)],'#563e69'),
                                 ([(19,38),(13,44),(9,47)],'#a39c9e'),
                                 ([(18,40),(14,45)],'#45404f'),
                                 ([(25,44),(25,47)],'#b99450')]:
                d.line(pts(points),fill=color)
            for x,y in [(22,34),(18,42),(11,48)]:
                xx=63-x if mirror else x
                if sprite.getpixel((xx,y))[3]: d.point((xx,y),fill='#c7b7a0')
        else:
            # Titanium plating follows the original swept silhouette.
            for points,color in [([(24,21),(23,29),(16,37),(13,43)],'#c5bad9'),
                                 ([(25,22),(24,30),(18,37)],'#736883'),
                                 ([(20,37),(18,44),(14,49)],'#9888b0'),
                                 ([(25,12),(27,12)],'#b6f2ef')]:
                d.line(pts(points),fill=color)
            for y in (29,32,35,38,41):
                d.line(pts([(9,y),(14,y)]),fill='#e4b295')
                d.line(pts([(10,y+1),(14,y+1)]),fill='#87536c')
            d.point(pts([(22,34)])[0],fill='#e1d7ec')
            d.point(pts([(17,43)])[0],fill='#d4c6db')
    if advanced:
        # Stabilized cells, multicoloured heart and four rear warp vents.
        d.line((29,40,29,44),fill='#d8b85a'); d.line((34,40,34,44),fill='#a78a41')
        d.point((31,43),fill='#e7aaca'); d.point((32,43),fill='#a3ded7')
        d.line((31,44,32,44),fill='#8c8adb')
        for x in (25,29,34,38): d.line((x,51,x,53),fill='#6baebb')
    else:
        # Cargo chest latch and pump on the central spine.
        d.line((30,38,33,38),fill='#585269'); d.point((31,39),fill='#e6c57f')
        d.line((30,46,33,46),fill='#a5c7cd')
    # Remove the entire old flame/white-hot plume, including its bright fringe.
    for left, right in [(19,28),(35,44)]:
        d.rectangle((left,51,right,63),fill=(0,0,0,0))
    # Inactive, closed engine nozzles; no fire, emissive tips, or glowing exhaust.
    for left in (21,37):
        d.rectangle((left-1,49,left+6,53),fill='#272633')
        d.line((left,50,left+5,50),fill='#80788d')
        d.line((left,51,left+5,51),fill='#4b465a')
        d.line((left+1,52,left+4,52),fill='#181923')
    alpha=sprite.getchannel('A')
    sprite=sprite.convert('RGB').quantize(colors=64,method=Image.Quantize.MEDIANCUT).convert('RGBA')
    sprite.putalpha(alpha)
    # Mirror the finished left half, including shading and alpha, for exact bilateral symmetry.
    for y in range(64):
        for x in range(32):
            sprite.putpixel((63-x,y),sprite.getpixel((x,y)))
    sprite.save(TEXTURES / f'{name}.png')

def stabilized_epsteinium():
    base=Image.open(SOURCES/'stabilized-element-base.png').convert('RGBA')
    original=Image.open(SOURCES/'stabilized-epsteinium-original.png').convert('RGBA')
    old=[colorsys.rgb_to_hsv(r/255,g/255,b/255) for r,g,b,a in original.getdata() if a>200 and max(r,g,b)>60]
    hue=statistics.median(p[0] for p in old); saturation=statistics.median(p[1] for p in old)
    values=[max(r,g,b)/255 for r,g,b,a in base.getdata() if a>200]
    scale=statistics.median(p[2] for p in old)/statistics.median(values)
    for y in range(base.height):
        for x in range(base.width):
            r,g,b,a=base.getpixel((x,y))
            if a:
                _,s,v=colorsys.rgb_to_hsv(r/255,g/255,b/255)
                rgb=colorsys.hsv_to_rgb(hue,min(1,s*saturation),min(1,v*scale))
                base.putpixel((x,y),(*[round(c*255) for c in rgb],a))
    base.save(TEXTURES/'stabilized_epsteinium.png')

if __name__ == '__main__':
    refine_miner('titaniumcoilmicrominer')
    refine_miner('microminermicrominer',advanced=True)
    stabilized_epsteinium()
    print('Refined original miners at 64x64; recoloured the exact 32x32 stabilized base.')
