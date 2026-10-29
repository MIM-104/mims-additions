"""Original 16 px precision tools with small GregTech-style pixel adjustments."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / 'src/main/resources/assets/modid/textures/item'


def iron():
    image = Image.new('RGBA', (16, 16))
    d = ImageDraw.Draw(image)
    # Keep the original slim diagonal silhouette and simple grip.
    d.line((3,13,9,7), fill='#192631', width=4)
    d.line((3,12,8,7), fill='#577E89', width=2)
    d.line((9,7,13,3), fill='#BF8F62', width=3)
    d.line((10,6,13,3), fill='#C8C9B7')
    d.rectangle((2,12,4,14), fill='#28353F')
    d.point((8,7), fill='#D1BF7A')
    # A pointed tip and a few flat steel pixels, like GT's small tool heads.
    d.point((14,2), fill='#D8D8D8')
    d.point((13,3), fill='#B8B8B8')
    d.point((12,5), fill='#808080')
    return image


def strippers():
    image = Image.new('RGBA', (16, 16))
    d = ImageDraw.Draw(image)
    d.line((3,13,10,6), fill='#263642', width=3)
    d.line((12,13,5,6), fill='#263642', width=3)
    # One pixel closer on the right jaw; flat steel highlights instead of extra detail.
    d.line((7,7,5,2), fill='#C0C0C0', width=2)
    d.line((8,7,10,2), fill='#909090', width=2)
    d.line((3,13,6,10), fill='#59A59C')
    d.line((12,13,9,10), fill='#59A59C')
    d.point((7,8), fill='#DDBD72')
    d.point((5,3), fill='#808080')
    d.point((10,3), fill='#D0D0D0')
    return image


def main():
    DEST.mkdir(parents=True, exist_ok=True)
    images = [iron(), strippers()]
    for name, image in zip(['precision_soldering_iron', 'precision_wire_strippers'], images):
        image.save(DEST / (name + '.png'))
    preview = Image.new('RGBA', (144, 80), '#29332E')
    for x, image in zip([4,76], images):
        preview.alpha_composite(image.resize((64,64), Image.Resampling.NEAREST), (x,8))
    path = ROOT / 'build/precision-tools-preview.png'
    path.parent.mkdir(parents=True, exist_ok=True)
    preview.resize((576,320), Image.Resampling.NEAREST).save(path)
    print('Painted both tools at their original 16 x 16 resolution.')


if __name__ == '__main__': main()
