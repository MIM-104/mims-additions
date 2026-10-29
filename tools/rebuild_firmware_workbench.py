"""Paint a pixel atlas and build a small electronics bench from native model elements."""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/modid'
atlas = Image.new('RGBA', (64, 64))
tiles = {}


def tile(name, index, base):
    image = Image.new('RGBA', (16, 16), base)
    tiles[name] = (index % 4 * 16, index // 4 * 16)
    return image, ImageDraw.Draw(image)


def put(name, image):
    atlas.paste(image, tiles[name])


im,d = tile('steel',0,'#687b80')
for y in range(16): d.line((0,y,15,y),fill=['#687b80','#73868a','#607378','#6b7e83'][y%4])
d.line((0,0,15,0),fill='#a4b2b1');d.line((0,15,15,15),fill='#3c5058')
put('steel',im)
im,d = tile('paint',1,'#354953')
for x in range(2,16,5):d.line((x,0,x,15),fill='#394e58')
d.line((0,0,15,0),fill='#687d84');d.line((0,15,15,15),fill='#1f323d')
for x,y in [(1,1),(14,1),(1,14),(14,14)]:d.point((x,y),fill='#a4aaa3')
put('paint',im)
im,d = tile('mat',2,'#245348')
for x in range(1,16,3):d.line((x,1,x,14),fill='#2a5b4e')
for y in range(1,16,3):d.line((1,y,14,y),fill='#2a5b4e')
d.rectangle((0,0,15,15),outline='#193d38');d.line((1,1,14,1),fill='#437968')
put('mat',im)
im,d = tile('pcb',3,'#1f4c35')
for coords in [[1,3,5,3,5,7,8,7],[14,2,11,2,11,5,9,5],[1,12,6,12,6,9],[14,12,10,12,10,9]]:
    d.line(coords,fill='#bea16a')
for x,y in [(1,3),(14,2),(1,12),(14,12)]:d.rectangle((x,y,x+1,y+1),fill='#e1c488')
for x in range(5,11):d.point((x,6),fill='#8eaa8e');d.point((x,10),fill='#8eaa8e')
d.rectangle((5,7,10,9),fill='#1d2930');d.point((6,7),fill='#768b82')
put('pcb',im)
im,d = tile('station',4,'#3b4c55')
d.rectangle((1,2,14,8),fill='#13272b',outline='#71858b')
# A tiny three-digit temperature readout, painted one pixel at a time.
for x in [3,7,11]:
    for y in [3,5,7]:d.line((x,y,x+1,y),fill='#a4cf9d')
    d.point((x+1,4),fill='#a4cf9d');d.point((x+1,6),fill='#a4cf9d')
d.rectangle((2,11,4,13),fill='#1c3038');d.point((3,11),fill='#a7b2ae')
d.rectangle((9,11,13,12),fill='#1c3038');d.point((11,11),fill='#79bba0')
put('station',im)
im,d = tile('spool',5,'#637577')
d.ellipse((0,0,15,15),fill='#9da9a2',outline='#40565c')
for r in [2,4,6]:d.ellipse((r,r,15-r,15-r),outline='#c3cac0' if r%4 else '#7f918f')
d.ellipse((6,6,9,9),fill='#273d45');d.point((7,6),fill='#d5d5c4')
put('spool',im)
im,d = tile('rubber',6,'#274c57')
for y in range(2,16,3):d.line((1,y,14,y),fill='#47747a')
d.line((1,1,1,14),fill='#78a29e');d.line((14,1,14,14),fill='#1b323f')
put('rubber',im)
im,d = tile('silver',7,'#a5b3af')
for y in range(16):d.line((0,y,15,y),fill=['#8b9e9d','#c6cdc0','#adbdb5','#9bada7'][y%4])
put('silver',im)
im,d = tile('screen',8,'#112d32')
for x in range(2,16,3):d.line((x,1,x,14),fill='#1d4141')
for y in range(2,16,3):d.line((1,y,14,y),fill='#1d4141')
d.line((1,10,4,10,4,5,7,5,7,9,10,9,10,3,14,3),fill='#8bd0ad')
d.line((2,13,7,13),fill='#c1b57a');d.point((12,13),fill='#9dc995')
d.rectangle((0,0,15,15),outline='#778f91')
put('screen',im)
im,d = tile('tray',9,'#53686c')
d.rectangle((0,0,15,15),outline='#99aaa2');d.rectangle((2,2,13,13),fill='#293f46')
d.line((1,14,14,14,14,1),fill='#203840')
for x,y in [(4,4),(10,4),(4,10),(10,10)]:
    d.rectangle((x,y,x+1,y+1),fill='#b2b8a0');d.point((x+1,y+1),fill='#7b8e84')
put('tray',im)
im,d = tile('chip',10,'#273234')
d.line((1,1,14,1),fill='#64716a');d.line((1,1,1,14),fill='#4c5b56')
for y in [4,7,10]:d.line((4,y,10,y),fill='#a5a88c')
d.point((3,12),fill='#a5a88c')
put('chip',im)
im,d = tile('ceramic',11,'#b3a583')
for y,c in [(2,'#8b5e46'),(5,'#b89b52'),(8,'#4b6c61'),(12,'#795844')]:d.line((0,y,15,y),fill=c,width=2)
put('ceramic',im)
im,d = tile('copper',12,'#aa8760')
for y in range(1,16,3):d.line((0,y,15,y),fill='#d1b57f')
put('copper',im)
im,d = tile('drawer',13,'#40545c')
d.rectangle((0,0,15,15),outline='#233b44');d.line((1,1,14,1),fill='#71878b')
d.rectangle((5,5,10,7),fill='#1b313b');d.line((5,5,10,5),fill='#9aaba6')
put('drawer',im)
im,d = tile('dark',14,'#233840');put('dark',im)
im,d = tile('tin',15,'#b8c5bd')
for y in range(0,16,2):d.line((0,y,15,y),fill='#899d9a')
put('tin',im)
atlas.save(ASSETS / 'textures/blocks/firmware_workbench_details.png')

elements=[]


def uv(name):
    x,y=tiles[name]
    return [x/4,y/4,(x+16)/4,(y+16)/4]


def box(a,b,material='paint',top=None,front=None,rotation=None):
    faces={side:{'texture':'#details','uv':uv(material)} for side in ['north','south','west','east','up','down']}
    if top:faces['up']['uv']=uv(top)
    if front:faces['north']['uv']=uv(front)
    element={'from':a,'to':b,'faces':faces}
    if rotation:element['rotation']=rotation
    elements.append(element)


# A single steel frame: four slim legs, side rails, low shelf and a front drawer.
for x in [1,13.5]:
    for z in [1,13.5]:
        box([x,.5,z],[x+1.5,10,z+1.5]);box([x-.15,0,z-.15],[x+1.65,.5,z+1.65],'dark')
for x in [1.3,13.3]:box([x,2,1.5],[x+1,3,14.5])
box([2,2.3,3],[14,2.7,13],'steel')
# The apron wraps just outside the legs; their visible faces must not share a plane.
box([.9,7.3,.9],[15.1,9.8,2.6],front='drawer')
box([6,8.3,.7],[10,8.7,1.15],'silver')
box([.3,9.8,.3],[15.7,10.8,15.7],'steel')
box([.8,10.8,.8],[15.2,11,15.2],'dark')
box([4.4,11,2.2],[11.3,11.13,11.5],'mat')
# The central board has raised chips, pins and discrete components.
box([5.2,11.13,4.2],[10.5,11.3,9.5],'pcb',top='pcb')
box([7,11.3,6.1],[8.6,11.7,7.6],'chip')
for x in [6.8,8.6]:
    for z in [6.2,6.6,7,7.4]:box([x,11.3,z],[x+.2,11.46,z+.15],'tin')
for x,z in [(6,5),(9.2,8),(6.1,8.2)]:box([x,11.3,z],[x+.65,11.57,z+.35],'ceramic')
for x,z in [(9.4,5.2),(9.4,6.2)]:
    box([x,11.3,z],[x+.45,12.05,z+.45],'dark',top='silver')
# Compact soldering station at the back, with its controls facing the worker.
box([1,11,11.9],[4.7,13.05,15],'paint',front='station')
box([1.3,13.05,12.3],[4.4,13.2,14.6],'steel')
for x in [1.7,2.4,3.1,3.8]:box([x,13.2,12.7],[x+.2,13.25,14.2],'dark')
# Small upright monitor on one narrow stand; its screen is not repeated on other faces.
box([6.5,11,13.4],[10.5,11.25,14.9],'paint')
box([8.1,11.25,14],[8.8,12.2,14.6],'steel')
box([5.8,12.2,14],[11.4,15.7,14.65],'paint',front='screen')
# Soldering iron in a shallow metal cradle to the left of the mat.
box([1.5,11,5.8],[3.5,11.25,9.8],'paint')
for z in [7.2,9]:
    box([1.7,11.25,z],[2,11.85,z+.3],'silver');box([3,11.25,z],[3.3,11.85,z+.3],'silver')
box([2.15,11.4,3],[2.85,12.05,6.4],'rubber')
box([2.28,11.55,6.4],[2.72,11.9,8.7],'silver')
box([2.39,11.6,8.7],[2.61,11.85,9.8],'copper')
# Fine-wire reels with separate rims, wound wire and a short loose end.
for x,z,wire in [(12.3,12.3,'tin'),(12.3,9.4,'silver')]:
    box([x,11,z],[x+2.2,11.2,z+2.2],'silver',top='spool')
    box([x+.35,11.2,z+.35],[x+1.85,12.2,z+1.85],wire)
    box([x,12.2,z],[x+2.2,12.4,z+2.2],'silver',top='spool')
    box([x-.7,11.03,z+.8],[x+.4,11.13,z+1],wire)
# Wire strippers, two rubber grips and two steel jaws around a visible pivot.
for angle in [-22.5,22.5]:
    rot={'origin':[12.7,11.4,5.4],'axis':'y','angle':angle,'rescale':False}
    # Plier arms sit above one another at their hinge instead of drawing two coplanar tops.
    lift=.06 if angle>0 else 0
    box([12.4,11.12+lift,2.5],[13,11.65+lift,5.4],'rubber',rotation=rot)
    box([12.45,11.2+lift,5.4],[12.95,11.55+lift,7.2],'silver',rotation=rot)
box([12.45,11.5,5.1],[12.95,11.75,5.6],'copper')
# Parts tray and loose microcomponents along the front edge.
box([5,11,11.9],[10.8,11.3,13],'paint',top='tray')
for x in [5.4,6.4,7.4,8.4,9.4]:
    box([x,11.3,12.1],[x+.45,11.5,12.55],'chip')
for x in [5.5,6.8,8.3,9.5]:box([x,11.13,2.7],[x+.6,11.37,3],'ceramic')

model={'parent':'block/block','ambientocclusion':True,
       'textures':{'details':'modid:blocks/firmware_workbench_details','particle':'modid:blocks/firmware_workbench_metal'},
       'elements':elements,'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[.8,.8,.8]}}}
# Coplanar outward faces flicker even when their enclosing boxes otherwise intersect correctly.
for i,a in enumerate(elements):
    if a.get('rotation'):continue
    for j,b in enumerate(elements[i+1:],i+1):
        if b.get('rotation'):continue
        for side,axis,edge in [('up',1,'to'),('down',1,'from'),('north',2,'from'),
                               ('south',2,'to'),('east',0,'to'),('west',0,'from')]:
            if abs(a[edge][axis]-b[edge][axis])>1e-6:continue
            axes=[n for n in range(3) if n!=axis]
            assert not all(min(a['to'][n],b['to'][n])-max(a['from'][n],b['from'][n])>1e-6
                           for n in axes), f'Coplanar overlapping {side} faces: elements {i}, {j}'
(ASSETS/'models/block/precision_firmware_workbench.json').write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
print(f'Painted 64×64 workbench atlas; built {len(elements)} textured model elements.')
