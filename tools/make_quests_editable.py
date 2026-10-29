"""Inline quest translations without resetting saved edits; merge the firmware-parts tutorial."""
import copy
import json
import shutil
from datetime import datetime
from pathlib import Path
from firmware_quest_text import update_descriptions

ROOT=Path(__file__).resolve().parents[1]
LANG_PATHS=[ROOT/'run/resources/questbook/lang/en_us.lang',
            ROOT/'src/main/resources/assets/questbook/lang/en_us.lang',
            ROOT/'run/resourcepacks/Nomi-CEu-1.7.5-Resources/assets/questbook/lang/en_us.lang']
translations={}
for path in LANG_PATHS:
    for line in path.read_text(encoding='utf-8').splitlines():
        if '=' in line and not line.lstrip().startswith('#'):
            key,value=line.split('=',1);translations[key]=value

paths=[ROOT/'run/config/betterquesting/DefaultQuests.json']
paths+=list((ROOT/'run/config/betterquesting/saved_quests').glob('*.json'))
paths+=list((ROOT/'run/saves').glob('*/betterquesting/QuestDatabase.json'))
backup=ROOT/'build/quest-editable-backup'/datetime.now().strftime('%Y%m%d-%H%M%S')
counts={}


def inline(value):
    if isinstance(value,dict):
        for key,child in list(value.items()):
            if key in ('name:8','desc:8') and isinstance(child,str):
                if child in translations:counts['fields']=counts.get('fields',0)+1
                # Translation formatting expands %n; literal editable fields need real newlines.
                value[key]=translations.get(child,child).replace('%n','\n')
            else:inline(child)
    elif isinstance(value,list):
        for child in value:inline(child)


def props(quest):return quest['properties:10']['betterquesting:10']


def replace_paragraphs(text,changes):
    result=[]
    for paragraph in text.split('\n\n'):
        replacement=next((new for marker,new in changes if marker in paragraph),paragraph)
        result.append(replacement)
    return '\n\n'.join(result)


wear_note=('§6Fine Soldering Alloy Wire§r reduces tool wear when programming firmware: '
           '§e1 durability per soldered branch§r instead of §e2 with Fine Tin Wire§r. '
           'Both precision tools have 320 durability: one fresh set handles §e64 five-branch circuits with alloy§r '
           'or §e32 with tin§r. Earlier boards have fewer branches and use less durability.')

for path in paths:
    original=json.loads(path.read_text(encoding='utf-8'))
    data=copy.deepcopy(original);inline(data)
    quests=data.get('questDatabase:9',{})
    # Match the addon quests by their original identity/title, never by an expert-mode ID alone.
    bench=quests.get('1101:10')
    if bench and 'Firmware Workbench' in props(bench)['name:8']:
        desc=props(bench)['desc:8']
        desc=replace_paragraphs(desc,[('Claim this quest before',
            'Start with §6Fine Tin Wire§r; no Mixer is needed to program your first circuits. '
            'Both wire supply slots accept §6Fine Tin or Fine Soldering Alloy Wire§r. '
            'The feed slot supplies each soldered branch; the other slot supplies one wire when you flash the completed board.')])
        if '320 durability' not in desc:desc+='\n\n'+wear_note
        props(bench)['desc:8']=desc
        # The temporary starter alloy is unnecessary now that tin is a valid early-game option.
        for reward in bench.get('rewards:9',{}).values():
            for stack in reward.get('rewards:9',{}).values():
                if stack.get('id:8')=='gregtech:meta_wire_fine' and stack.get('Damage:2')==320:
                    stack['Damage:2']=112
    first=quests.get('1102:10')
    if first and props(first)['name:8']=='Your First Firmware':
        props(first)['desc:8']=replace_paragraphs(props(first)['desc:8'],[
            ('Fill the terminal slots',
             'Fill the terminal slots with the parts shown by their ghost icons. An LV board needs §6Fine Copper Wire§r '
             'and an §6Iron Bolt§r in its two terminals. Put 2 §6Fine Tin or Fine Soldering Alloy Wires§r '
             'in the feed slot and one wire of either type in the finishing slot.'),
            ('Click the trace tiles',
             'Click the trace tiles to rotate them until each terminal connects continuously to the central circuit. '
             'Press §aSOLDER§r for each connected branch. Each successful branch consumes its terminal part and one feed wire. '
             '§6Tin costs 2 durability per tool; Soldering Alloy costs 1.§r Failed attempts cost nothing.'),
            ('Once every branch is soldered',
             'Once every branch is soldered, press §aFLASH§r. This consumes the circuit and one '
             '§6Fine Tin or Fine Soldering Alloy Wire§r from the finishing slot, returning the original circuit with its purpose in the tooltip. '
             'Use it in the matching recipe. §eTick this quest after programming your first circuit.§r')])
    for quest in quests.values():
        title=props(quest).get('name:8','')
        if title.replace('§2','').replace('§r','').strip()=='Soldering Alloy':
            if 'reduces tool wear when programming firmware' not in props(quest)['desc:8']:
                props(quest)['desc:8']+='\n\n'+wear_note
    parts=quests.get('1104:10');main=quests.get('1105:10')
    if parts and main and props(parts)['name:8']=='Parts for the Firmware Array' and 'Firmware Synthesis Array' in props(main)['name:8']:
        intro,*body=props(main)['desc:8'].split('\n\n')
        preparation=props(parts)['desc:8'].replace('The next quest lists the blocks needed for the complete structure,',
                                                  'The checklist below lists the blocks needed for the complete structure,')
        props(main)['desc:8']='\n\n'.join([intro,preparation,*body])
        for quest in quests.values():
            prerequisites=quest.get('preRequisites:11',[])
            if 1104 in prerequisites:
                rewired=[]
                for parent in prerequisites:
                    for replacement in (parts['preRequisites:11'] if parent==1104 else [parent]):
                        if replacement!=quest['questID:3'] and replacement not in rewired:rewired.append(replacement)
                quest['preRequisites:11']=rewired
        # Parts' item requirements are already covered by the complete structure checklist.
        main_reward=next(r for r in main['rewards:9'].values() if r['rewardID:8']=='bq_standard:item')
        for reward in parts['rewards:9'].values():
            if reward['rewardID:8']=='bq_standard:item':
                for stack in reward['rewards:9'].values():
                    values=main_reward['rewards:9'];index=max([int(k.split(':')[0]) for k in values]+[-1])+1
                    values[f'{index}:10']=copy.deepcopy(stack)
        del quests['1104:10']
        for chapter in data['questLines:9'].values():
            chapter['quests:9']={k:v for k,v in chapter['quests:9'].items() if v['id:3']!=1104}
        counts['merged']=counts.get('merged',0)+1
    update_descriptions(data)
    if data!=original:
        settings=data.get('questSettings:10',{}).get('betterquesting:10',{})
        if 'pack_version:3' in settings:settings['pack_version:3']+=1
        saved=backup/path.relative_to(ROOT);saved.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(path,saved)
        temp=path.with_suffix('.editable.tmp')
        temp.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
        temp.replace(path);counts['books']=counts.get('books',0)+1
print(json.dumps({'updated':counts,'backup':str(backup)},indent=2))
