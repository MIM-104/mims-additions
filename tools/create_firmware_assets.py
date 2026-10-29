"""Hand-paint native pixel textures and a Minecraft element model for the firmware bench."""
from pathlib import Path
from PIL import Image, ImageDraw
import json

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'src/main/resources/assets/modid'

def write_json(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')

def save(name,image,folder='blocks'):
    path=ASSETS/'textures'/folder/(name+'.png');path.parent.mkdir(parents=True,exist_ok=True);image.save(path)

def panel():
    im=Image.new('RGBA',(32,32),'#30414e');d=ImageDraw.Draw(im)
    d.rectangle((0,0,31,31),outline='#16222d');d.line((1,30,1,1,30,1),fill='#758691')
    d.line((30,2,30,30,2,30),fill='#1c2b36')
    for x,y in [(3,3),(27,3),(3,27),(27,27)]:
        d.rectangle((x,y,x+1,y+1),fill='#a4a8a5');d.point((x+1,y+1),fill='#48535d')
    return im,d

# Keep GT's steel bevel and grain; the etching adds finer, one-pixel cuts.
im=Image.open(ROOT/'tools/texture_sources/machine_casing_solid_steel.png').convert('RGBA')
for y in range(16):
    for x in range(16):
        r,g,b,a=im.getpixel((x,y));v=(r+g+b)//3
        im.putpixel((x,y),(int(v*.68),int(v*.78),int(v*.84),a))
im=im.resize((32,32),Image.Resampling.NEAREST)
d=ImageDraw.Draw(im)
d.rectangle((7,7,24,24),fill='#3b484e',outline='#65777c')
d.line((8,23,23,23,23,8),fill='#29383e')
d.rectangle((13,13,18,18),fill='#27363d',outline='#87958d')
d.rectangle((14,14,17,17),fill='#48685e')
for flip_x in [False,True]:
    for flip_y in [False,True]:
        def mirrored(x,y):return (31-x if flip_x else x,31-y if flip_y else y)
        d.line([mirrored(x,y) for x,y in [(9,10),(12,10),(12,14),(13,14)]],fill='#aaa47b')
        d.point(mirrored(9,10),fill='#d0c79d')
save('etched_logic_casing',im)
im,d=panel()
for y in [5,13,21]:
    d.rectangle((5,y,26,y+5),fill='#111d29',outline='#56727b')
    for x in [8,12,16,20]:d.rectangle((x,y+2,x+1,y+3),fill='#344c5c')
    d.point((24,y+2),fill='#76cdb2');d.point((24,y+3),fill='#ddbb6b')
save('firmware_memory_bank',im)
# Reuse the base EBF coil winding and corner brackets, recoloured to Inconel.
im=Image.open(ROOT/'tools/texture_sources/machine_coil_kanthal.png').convert('RGBA')
for y in range(16):
    for x in range(16):
        r,g,b,a=im.getpixel((x,y));v=(r*3+g*6+b)//10
        im.putpixel((x,y),(min(255,int(v*1.10)),min(255,int(v*1.12)),min(255,int(v*1.10)),a))
d=ImageDraw.Draw(im)
# Small copper tie points and steel fasteners add detail without replacing the windings.
for x in [1,14]:
    d.line((x,6,x,9),fill='#394342')
    d.point((x,6),fill='#c7ceca');d.point((x,9),fill='#697571')
    d.point((x,7),fill='#aa8863');d.point((x,8),fill='#d0b18b')
save('noetic_induction_coil',im)
im,d=panel();d.rectangle((4,4,27,27),fill='#225044',outline='#a9a36b')
for x in [7,11,20,24]:
    d.line([(x,5),(x,10),(15 if x<16 else 17,14)],fill='#c0a975')
    d.line([(x,26),(x,22),(15 if x<16 else 17,18)],fill='#b19863')
d.rectangle((12,12,20,20),fill='#152432',outline='#93ac9c');d.rectangle((14,14,18,18),fill='#3b7c71')
save('firmware_workbench_top',im)
im,d=panel()
for y in [8,12,16,20,24]:d.line((7,y,24,y),fill='#52616c')
save('firmware_workbench_metal',im)
im=Image.new('RGBA',(32,32),'#1a2833');d=ImageDraw.Draw(im)
d.rectangle((2,2,29,29),outline='#7c9098');d.rectangle((4,4,27,27),fill='#09272d')
d.line([(7,20),(10,20),(10,13),(14,13),(14,9),(22,9),(22,18),(25,18)],fill='#72d5bd',width=2)
d.rectangle((13,15,20,22),fill='#2a5a5a',outline='#c4b87c')
for x,y in [(7,20),(22,9),(25,18)]:d.rectangle((x-1,y-1,x+1,y+1),fill='#e6cb83')
d.line((7,25,18,25),fill='#547a81');d.line((22,25,25,25),fill='#91a166')
save('firmware_workbench_screen',im)

textures={k:'modid:blocks/firmware_workbench_'+k for k in ['top','metal','screen']}
textures['particle']=textures['metal']
elements=[]
def element(a,b,face='metal',top=None):
    faces={side:{'texture':'#'+face,'uv':[0,0,16,16]} for side in ['north','south','west','east','up','down']}
    if top:faces['up']['texture']='#'+top
    elements.append({'from':a,'to':b,'faces':faces})
for x in [1,13]:
    for z in [1,13]:element([x,0,z],[x+2,10,z+2])
element([2,2,2],[14,3,14]);element([0,10,0],[16,12,16],top='top')
element([6,12,13],[14,16,15],face='screen');element([8,12,12],[12,13,14])
element([1,12,10],[4,13,14]);element([1.5,13,10.5],[3.5,14,13.5]);element([1,14,10],[4,14.5,14])
element([12,12,2],[15,13,6]);element([13,13,3],[14,14,6])
write_json(ASSETS/'models/block/precision_firmware_workbench.json',{
    'parent':'block/block','ambientocclusion':True,'textures':textures,'elements':elements,
    'display':{'gui':{'rotation':[30,225,0],'translation':[0,0,0],'scale':[0.75,0.75,0.75]}}})
write_json(ASSETS/'blockstates/precision_firmware_workbench.json',{'variants':{
    f'facing={direction}':{'model':'modid:precision_firmware_workbench','y':rotation}
    for direction,rotation in [('north',0),('east',90),('south',180),('west',270)]}})
for name in ['etched_logic_casing','firmware_memory_bank','noetic_induction_coil']:
    write_json(ASSETS/'models/block'/f'{name}.json',{'parent':'block/cube_all','textures':{'all':f'modid:blocks/{name}'}})
    write_json(ASSETS/'blockstates'/f'{name}.json',{'variants':{'normal':{'model':f'modid:{name}'}}})
for name in ['precision_firmware_workbench','etched_logic_casing','firmware_memory_bank','noetic_induction_coil']:
    write_json(ASSETS/'models/item'/f'{name}.json',{'parent':f'modid:block/{name}'})

import runpy
runpy.run_path(str(ROOT/'tools/paint_precision_tools.py'), run_name='__main__')
for name in ['precision_soldering_iron','precision_wire_strippers']:
    write_json(ASSETS/'models/item'/f'{name}.json',{'parent':'item/handheld','textures':{'layer0':f'modid:item/{name}'}})

lang=ASSETS/'lang/en_us.lang'
entries={
 'tile.modid.precision_firmware_workbench.name':'Precision Firmware Workbench',
 'tile.modid.etched_logic_casing.name':'Etched Logic Casing',
 'tile.modid.firmware_memory_bank.name':'Firmware Memory Bank',
 'tile.modid.noetic_induction_coil.name':'Inconel 600 Induction Coil',
 'modid.material.noetic_alloy':'Inconel 600',
 'modid.fluid.liquid_inconel':'Liquid Inconel 600',
 'item.modid.precision_soldering_iron.name':'Precision Soldering Iron',
 'item.modid.precision_wire_strippers.name':'Precision Wire Strippers',
 'modid.machine.firmware_synthesis_array.name':'Firmware Synthesis Array',
 'recipemap.firmware_synthesis.name':'Firmware Synthesis',
 'modid.firmware.jei.title':'Firmware Replication',
}
lines=lang.read_text(encoding='utf-8').splitlines();lines=[s for s in lines if s.split('=',1)[0] not in entries
    and not s.startswith('modid.firmware.info.') and not s.startswith('material.modid.noetic_alloy=')]
lang.write_text('\n'.join(lines)+'\n\n'+'\n'.join(k+'='+v for k,v in entries.items())+'\n',encoding='utf-8')
print('Created native pixel textures, workbench element model, block/item models, and firmware translations.')
import runpy
runpy.run_path(str(ROOT/'tools/rebuild_firmware_workbench.py'), run_name='__main__')
