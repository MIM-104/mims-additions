"""Analyse an effective ProgressionAudit snapshot. Reachability is an optimistic upper bound."""
import collections
import json
import math
import re
from fractions import Fraction
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'build/progression-audit'
data=json.loads((OUT/'runtime-recipes.json').read_text(encoding='utf-8'))
items=data['items'];recipes=list(data['recipes']);mods=data['mods']
# Avaritia's collector produces neutron piles through its tile tick, not a recipe registry.
recipes.append({'id':'worldgen:avaritia_neutron_collector','type':'world_process',
                'inputs':[{'options':['avaritia:neutron_collector@0'],'count':1,'catalyst':True}],
                'outputs':[{'key':'avaritia:resource@2','count':1}]})
by_output=collections.defaultdict(list)
for recipe in recipes:
    for output in recipe['outputs']:by_output[output['key']].append(recipe)


def producing(key,kind=None):
    return [r for r in by_output[key] if kind is None or r['type']==kind]


def name(key):return re.sub('§.','',items.get(key,{}).get('name',key))


def find_named(text):
    return [(k,v) for k,v in items.items() if text.lower() in v['name'].lower() and '#fw:' not in k]


# Give every natural ore and survival vanilla item unlimited availability, allow unlimited
# power and all recipe machines, and ignore research, NBT and crafting-table sizes.
# A failure under this generous model is useful; success is NOT proof of beatability.
creative={'air','barrier','bedrock','command_block','chain_command_block','repeating_command_block',
          'structure_block','structure_void','end_portal_frame','mob_spawner','spawn_egg','knowledge_book',
          'command_block_minecart','lit_furnace','lit_redstone_ore','flowing_water','flowing_lava'}
seed={k for k,v in items.items() if '#fw:' not in k and
      ((k.startswith('minecraft:') and k.split(':',1)[1].split('@')[0] not in creative)
       or any(o.startswith('ore') for o in v['ores'])
       or 'logWood' in v['ores'] or 'treeSapling' in v['ores'])}
seed|={'fluid:water','fluid:lava','fluid:air','modid:baby_villager_essence@0',
        'gregtech:meta_wire_fine@320'}  # Actual mob drop and finite starter reward, relaxed to unlimited.
seed|={f'gregtech:meta_item_1@461#selector:{i}' for i in range(33)}  # Native machine/input-bus ghost settings.
reached=set(seed);witness={};rounds=0
while True:
    added=set();rounds+=1
    for r in recipes:
        if not r['inputs'] or r.get('dynamic'):continue
        if all(any(o in reached for o in i['options']) for i in r['inputs']):
            for o in r['outputs']:
                if o['key'] not in reached:added.add(o['key']);witness.setdefault(o['key'],r['id'])
    if not added:break
    reached.update(added)

# Direct two-way and self exchanges, preserving quantities. No probabilistic outputs,
# no fluid reagents hidden as free inputs, and no extra consumed ingredients.
edges={}
for r in recipes:
    consumed=[i for i in r['inputs'] if not i['catalyst']]
    if len(consumed)!=1 or r.get('dynamic') or r['type']=='manual_firmware':continue
    for i in consumed[0]['options']:
        for o in r['outputs']:
            if o.get('chance',10000)!=10000:continue
            ratio=Fraction(o['count'],consumed[0]['count'])
            k=(i,o['key'])
            if k not in edges or ratio>edges[k][0]:edges[k]=(ratio,r['id'])
gains=[]
for (a,b),(ratio,rid) in edges.items():
    if a==b and ratio>1:gains.append({'from':a,'gain':str(ratio),'recipes':[rid]})
    if a<b and (b,a) in edges and ratio*edges[(b,a)][0]>1:
        gains.append({'from':a,'via':b,'gain':str(ratio*edges[(b,a)][0]),'recipes':[rid,edges[(b,a)][1]]})

questbook=json.loads((ROOT/'run/config/betterquesting/DefaultQuests.json').read_text(encoding='utf-8'))
lang=dict(l.split('=',1) for l in (ROOT/'src/main/resources/assets/questbook/lang/en_us.lang').read_text(encoding='utf-8').splitlines() if '=' in l)
blocked_quests=[]
for q in questbook['questDatabase:9'].values():
    props=q['properties:10']['betterquesting:10'];bad=[]
    for task in q['tasks:9'].values():
        if task.get('entryLogic:8','AND')!='AND':continue
        for stack in task.get('requiredItems:9',{}).values():
            item=stack['id:8']
            if item=='betterquesting:placeholder':item=stack.get('tag:10',{}).get('orig_id:8',item)
            if item.split(':',1)[0] not in mods and not stack.get('OreDict:8'):bad.append(item)
    if bad and props.get('tasklogic:8','AND')=='AND':
        blocked_quests.append({'id':q['questID:3'],'name':lang.get(props['name:8'],props['name:8']), 'missing':sorted(set(bad))})

array_key='gregtech:machine@32039'
assembly_targets={'modid:etched_logic_casing@0':176,'modid:firmware_memory_bank@0':16,
                  'modid:noetic_induction_coil@0':24,array_key:1}
bill=collections.defaultdict(Fraction);batches={};assembly_ticks=0;assembly_eu=0
todo=collections.Counter(assembly_targets)
for key in [array_key,'modid:etched_logic_casing@0','modid:firmware_memory_bank@0','modid:noetic_induction_coil@0']:
    choices=producing(key,'gt:assembler')
    # GT generates a tin alternative; use the authored soldering-alloy route consistently.
    route=next(r for r in choices if any('fluid:soldering_alloy' in i['options'] for i in r['inputs']))
    out=next(o['count'] for o in route['outputs'] if o['key']==key)
    count=math.ceil(todo[key]/out);batches[name(key)]=count
    assembly_ticks+=count*route['ticks'];assembly_eu+=count*route['eu']
    for i in route['inputs']:
        if i['catalyst']:continue
        option=i['options'][0]
        if option in assembly_targets:todo[option]+=i['count']*count
        else:bill[option]+=i['count']*count

inconel_ingots=Fraction(0)
for key,amount in bill.items():
    if key=='modid:wire_double@0':inconel_ingots+=amount
    if key=='modid:meta_wire_fine@0':inconel_ingots+=amount/8
abs_route=next(r for r in recipes if r['type']=='gt:alloy_blast_smelter' and any(o['key']=='fluid:molten.noetic_alloy' for o in r['outputs']))
abs_batches=math.ceil(inconel_ingots/20)
cast=next(r for r in producing('modid:meta_ingot@0','gt:vacuum_freezer') if any('fluid:molten.noetic_alloy' in i['options'] for i in r['inputs']))
cast_count=math.ceil(inconel_ingots)
array_calculation={'bill':{name(k)+((' [firmware: '+k.split('#fw:')[1]+']') if '#fw:' in k else ''):str(v) for k,v in bill.items()},'batches':batches,
 'assembly_eu':assembly_eu,'assembly_seconds':assembly_ticks/20,
 'inconel_ingots':str(inconel_ingots),'abs_batches':abs_batches,
 'nickel_dust':15*abs_batches,'chrome_dust':3*abs_batches,'iron_dust':2*abs_batches,
 'abs_eu':abs_batches*abs_route['eu'],'abs_seconds':abs_batches*abs_route['ticks']/20,
 'casting_eu':cast_count*cast['eu'],'casting_seconds':cast_count*cast['ticks']/20,
 'soldering_alloy_l':int(bill['fluid:soldering_alloy']),
 'note':'Structure preview plus parts consumed to craft its controller. Excludes ports, glass and all upstream part/circuit manufacturing. Nominal non-overclocked times; tool wear not free.'}

# Exact costs of the mod's alternative Creative Quantum Chest route.
chest=next(r for r in producing('gregtech:machine@1668') if r['type']=='gt:microverse_4')
solidifier=producing('modid:stabilized_epsteinium@0','gt:fluid_solidifier')[0]
fusion=next(r for r in by_output['fluid:epsteinium'] if r['type']=='gt:fusion_reactor')
essence=next(r for r in by_output['fluid:liquidbabyvillager'] if r['type']=='gt:extractor')
orbs=64;ep=orbs*1000;fusion_runs=ep//125;baby=fusion_runs*100;essences=math.ceil(baby/1000)
creative_calculation={'orbs':orbs,'epsteinium_l':ep,'fusion_runs':fusion_runs,'baby_fluid_l':baby,
 'drilling_fluid_l':fusion_runs*25,'essences':essences,
 'eu':chest['eu']+orbs*solidifier['eu']+fusion_runs*fusion['eu']+essences*essence['eu'],
 'fusion_seconds':fusion_runs*fusion['ticks']/20,'solidification_seconds':orbs*solidifier['ticks']/20,
 'fusion_startup_eu':fusion['properties'].get('eu_to_start'),
 'note':'Excludes structures, research, generators, power distribution and the upstream resources. Startup energy is a separate reactor-buffer requirement.'}

targets=['modid:precision_firmware_workbench@0','gregtech:machine@620',array_key,
         'modid:meta_ingot@0','modid:epsteinium_infused_wetware_mainframe@0',
         'modid:microminermicrominer@0','nomilabs:tieroneship@0','nomilabs:quantumflux@0',
         'extendedcrafting:material@32','gregtech:machine@32037','gregtech:machine@32102',
         'gregtech:machine@1668','avaritia:resource@6']
reachability=[]
for target in targets:
    rows=producing(target)
    blockers=[]
    for r in rows[:8]:
        blockers.append({'recipe':r['id'],'unavailable_inputs':[
            [name(o) for o in i['options'][:5]] if i['options'] else ['EMPTY INGREDIENT']
            for i in r['inputs'] if not any(o in reached for o in i['options'])]})
    reachability.append({'item':target,'name':name(target),'reachable_in_optimistic_model':target in reached,
                         'direct_recipe_count':len(rows),'witness':witness.get(target),'blocked_recipes':blockers})

ct_log=(OUT/'run/crafttweaker.log').read_text(encoding='utf-8',errors='replace')
script_errors=sorted(set(re.findall(r'Error executing \{\[[^]]+\]: ([^}]+)\}',ct_log)))
summary={'mods':len(mods),'recipe_count':len(data['recipes']),'model_process_count':len(recipes),
 'item_states':len(items),'providers':len(data['providers']),
 'failed_scripts':script_errors,'unique_unresolved_references':len(set(re.findall(r'Could not resolve (.*)',ct_log))),
 'export_errors':data['errors'],'recipe_types':dict(collections.Counter(r['type'] for r in recipes)),
 'optimistic_reachable':len(reached),'optimistic_rounds':rounds,'targets':reachability,
 'unavailable_quest_requirements':blocked_quests,'two_way_conversion_edges':len(edges),'two_way_gains':gains,
 'empty_manual_firmware_by_tier':dict(collections.Counter(items[r['outputs'][0]['key']]['tier'] for r in recipes
    if r['type']=='manual_firmware' and any(not i['options'] for i in r['inputs']))),
 'firmware_array_calculation':array_calculation,'creative_chest_calculation':creative_calculation}
(OUT/'analysis.json').write_text(json.dumps(summary,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in summary.items() if k in ['mods','recipe_count','item_states','optimistic_reachable','optimistic_rounds','two_way_conversion_edges','two_way_gains','empty_manual_firmware_by_tier']},indent=2))
print('Mandatory quest tasks referencing absent mods:',len(blocked_quests))
for entry in reachability:print(entry['name'],entry['reachable_in_optimistic_model'],'recipes',entry['direct_recipe_count'])
print('ARRAY',json.dumps(array_calculation));print('CREATIVE CHEST',json.dumps(creative_calculation))
