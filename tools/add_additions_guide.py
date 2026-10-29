"""Add an optional Genesis guide without moving or recreating existing quests."""
import copy
import json
import shutil
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / 'run/config/betterquesting/DefaultQuests.json'
INTRO_TITLE = 'What Changes?'

# Each entry is a short, separately searchable checkbox quest.
ENTRIES = [
    ('Machines Need Neighbours', 'minecraft:redstone',
     'The §eearly-game penalty§r starts when any player carries an ULV circuit or better. '
     'A powered GregTech singleblock with fewer than §e2 directly adjacent machines of the same voltage tier§r '
     'uses §e50% more recipe power§r.\n\n'
     'Only the six block faces count. The neighbours must be singleblocks with recipe logic; '
     'cables, chests and multiblock controllers do not count. Three suitable machines in a line only protect '
     'the middle one. Multiblock controllers themselves are exempt.\n\n'
     'This penalty multiplies with wear and the late-game world penalty. Plan the layout before powering it.'),
    ('Machines Wear Out', 'minecraft:clock',
     'The §6mid-game penalty§r begins when any player carries an §dEV circuit§r or better. '
     'Every affected machine accumulates wear while a power-consuming recipe is in progress, '
     'including time spent stalled with that recipe still in progress.\n\n'
     'Wear adds §e5% of the original recipe power per operating hour§r: '
     '20 hours means 2× power, and 50 hours means 3.5×. This is linear growth, not repeated compounding. '
     'It affects singleblocks and multiblock recipe controllers.\n\n'
     'Picking up and replacing the same machine §cdoes not remove its wear§r. '
     'There is currently no overhaul item: a newly crafted replacement starts fresh. '
     'Keep spare power and check older production lines.'),
    ('The World Gets Worse', 'minecraft:ender_eye',
     'The §clate-game penalty§r begins when any player carries a §dZPM circuit§r or better. '
     'Every §e50 ticking world hours§r after that adds another §e10% of base power demand§r. '
     'Without mitigation, 500 hours means 2× power and 1,000 hours means 3×.\n\n'
     'It counts world time, including AFK time, rather than individual machine runtime. '
     'All dimensions share the penalty. Once a stage unlocks it stays unlocked, '
     'even if the circuit is put away or its owner leaves. A closed or paused world does not advance it.\n\n'
     'Different §aactively running, formed multiblock controller types§r reduce the added tax. '
     'The first type removes 2% of the tax, the second another 1%, the third another 0.67%, '
     'with diminishing returns. Duplicate controllers do not add another type. '
     'Twenty types remove about 7.2% of the tax, not 7.2% of total power.\n\n'
     'World tax multiplies with machine wear and the neighbour penalty. '
     'A machine with 50 hours of wear after 500 post-ZPM world hours can demand §c7× power§r '
     'before the neighbour penalty.'),
    ('Keep the Power On', 'minecraft:tnt',
     '§cRunning out of power can explode a GregTech recipe machine.§r '
     'The addon checks the machine\'s insufficient-energy flag, including failed attempts to run a recipe. '
     'This danger exists from the start; it does not wait for EV.\n\n'
     'The power-failure blast has explosion strength §e8§r and breaks blocks. '
     'A battery only helps if its output and the cables can actually deliver the required EU/t. '
     'Allow for simultaneous recipes, overclocks, wear, world tax and lonely machines.\n\n'
     'Normal GregTech machine explosions are also enlarged dramatically: '
     'their explosion strength is multiplied by §e100§r, with a minimum of §e200§r, '
     'and living entities are thrown away from the blast. '
     'These are separate from the strength-8 power-failure explosion.'),
    ('Careful With That Mallet', 'minecraft:iron_ingot',
     'Each server-side §6Soft Mallet§r click on a GregTech machine or controller has a '
     '§e5% chance§r to break something permanently. You will hear an anvil break and see debris.\n\n'
     'A crippled tiered machine adds its full tier voltage to recipe power demand, '
     'on top of the other penalties. Crippled controllers without a normal machine tier are effectively unusable. '
     'The damage is saved when the machine is picked up.\n\n'
     'There is currently no repair recipe. Avoid unnecessary toggles and keep replacements for important machines.'),
    ('No Time to Run', 'minecraft:flint_and_steel',
     '§cPrimed TNT has no normal fuse§r: it detonates on its next update. '
     'That includes TNT created by other addon events.\n\n'
     'A §3Draconic Reactor§r meltdown also loses its usual delay. '
     'Once its explosion process begins, calculation and detonation are forced immediately. '
     'Do not plan around having time to escape after the reactor has already failed.'),
    ('Blood Moons', 'minecraft:iron_sword',
     'Each Overworld night has a §e1 in 8 chance§r to be a Blood Moon. '
     'You cannot sleep through it.\n\n'
     'Extra zombies and skeletons spawn on suitable surfaces in loaded chunks. '
     'They wear iron armour, carry weapons and receive Strength. '
     'The extra waves spawn two mobs every two seconds, up to 40 loaded Blood Moon mobs. '
     'Their armour has no normal equipment drop chance.\n\n'
     '§cPeaceful does not protect you from these mobs§r: they remain alive and can damage players. '
     'Build shelter and secure the area around your factory.'),
    ('Who Moved My Items?', 'minecraft:chest',
     'Every §e5–60 minutes§r, eligible loaded storage inventories can have '
     'up to §e20 occupied stacks§r moved to different slots. Items are rearranged inside each inventory; '
     'this event does not destroy them or swap them between chests.\n\n'
     'Ordinary and trapped chests are affected, along with supported chest, crate and barrel inventories. '
     'Open inventories are skipped. §aAE2 storage is excluded§r.\n\n'
     'Do not depend on a particular chest slot staying put. Use item filters for automation.'),
    ('The Machine Went for a Walk', 'minecraft:leather_boots',
     'Very rarely, a loaded GregTech recipe singleblock can grow legs, leave its position '
     'and settle elsewhere. Multiblock controllers are not candidates.\n\n'
     'The machine takes its saved contents and state with it. Its old cables and pipes do not follow. '
     'If a production line suddenly has a machine-shaped gap, look around before crafting a replacement.\n\n'
     'The very rare roll is world-wide, not once per machine. '
     'It is random, not a countdown or an event that happens at a fixed interval.'),
    ('The ME System Ate It', 'minecraft:ender_pearl',
     'AE2 autocrafting has a §e1 in 5,000 chance per real ingredient-extraction attempt§r '
     'to destroy one ingredient and deliver nothing to that crafting step. '
     'This is not one roll per complete crafting job: a large job can make many attempts.\n\n'
     'A chat message announces the loss. The job can then remain waiting for an ingredient that is gone. '
     'Check stuck jobs and be prepared to cancel and request them again with replacement materials.\n\n'
     'Passive stockpiles help absorb losses. Keep especially expensive ingredients out of unnecessarily large requests.'),
    ('Mind the Molten Metal', 'minecraft:bucket',
     'Fluid containers in your inventory or offhand holding fluid §eabove 1,812 K§r '
     'melt after about §e5 seconds§r. The container is consumed and can spill its fluid at your feet. '
     'Contact with these very hot placed fluids also damages players.\n\n'
     'A GregTech fluid pipe carrying fluid above its temperature rating, or otherwise incompatible fluid, '
     'can break and spill a source block. Nearby flammable surroundings can catch fire. '
     'Use pipes rated for the actual fluid, not just its throughput.\n\n'
     'Placed fluids at §e2,274 K or hotter§r can destroy blocks underneath them, '
     'except bedrock, unbreakable blocks and liquids. A hot spill can keep eating downward.\n\n'
     '§eNon-water fluids cannot replenish source blocks from neighbouring sources.§r '
     'Vanilla water keeps its normal source-generation behaviour.'),
    ('Watch What You Type', 'minecraft:paper',
     'Chat is not harmless here. Messages containing §cgex, pex, grex, lex or dex§r '
     'trigger a Draconic Reactor explosion at the speaker after a short delay. '
     'Matching is case-insensitive and also catches those letters inside longer words.\n\n'
     'The §dIridium God§r is another gamble. A request such as '
     '§eDear Iridium God, please give me 10 iridium§r has a 50% chance of granting the requested amount, '
     'capped at 1,000. The other outcome spawns ten TNT entities around you. '
     'A successful request also summons lightning.\n\n'
     'Neither event is made safe by reaching a higher voltage tier.'),
    ('Every Circuit Has a Job', 'modid:precision_firmware_workbench',
     'Raw circuits are §eUnconfigured§r. Recipes that use circuits for a machine, controller or component '
     'need firmware for that exact output, including less obvious items such as filters and prospectors. '
     'Circuit-manufacturing recipes still use raw circuits. Circuit-free recipes need no firmware.\n\n'
     'At §6Steel§r, use the §3Precision Firmware Workbench§r and precision tools. '
     'Follow the ghost slots, connect the traces, solder each branch, then flash the board. '
     'Fine Tin Wire costs 2 durability per tool per branch; Fine Soldering Alloy Wire costs 1. '
     'A fresh tool set handles 32 five-branch boards with tin or 64 with alloy.\n\n'
     'At §dIV§r, the §3Firmware Synthesis Array§r automates programming without puzzles or a cleanroom. '
     'Fine wire material plus a numbered GregTech Programmed Circuit selects the purpose; '
     'the raw circuit and wire are consumed, while the numbered selector remains.\n\n'
     '§eCraft a firmware circuit alone to wipe its firmware.§r The same physical circuit returns Unconfigured. '
     'The detailed tutorials are in The Beginning and Mid Game.'),
    ('Check the Circuit Tier', 'minecraft:comparator',
     'The addon changes several familiar circuit recipes. The §6LV Microprocessor§r '
     'now needs §dUHV§r power in the Circuit Assembler, including machines that share its recipe map. '
     'Do not assume an item\'s LV circuit rating means it is an early-game recipe.\n\n'
     'The §6Integrated Processor§r keeps its normal Circuit Assembler recipes '
     'because it is needed for later circuits. Use JEI to choose a circuit implementation '
     'you can actually make at your current stage.\n\n'
     'Machine recipes accept §aany circuit item of their required tier§r with the right firmware. '
     'The workbench will let you put an unsuitable purpose on a higher- or lower-tier circuit, '
     'but that does not bypass the final recipe\'s tier requirement.'),
    ('Epsteinium and Infinity', 'modid:stabilized_epsteinium',
     'Baby villagers can drop §6Baby Villager Essence§r. Extract it at IV to obtain the fluid '
     'used with Drilling Fluid in the addon\'s UV fusion route to §6Epsteinium§r. '
     'Each Stabilized Epsteinium needs §e1,000 L§r in a Fluid Solidifier.\n\n'
     'The §6Epsteinium-Infused Wetware Mainframe§r is a §dUEV-tier circuit§r, '
     'so it follows the same firmware rules as the other circuits.\n\n'
     'Extract §6Infinity Ingots§r at §dUV§r for §6Molten Infinity§r. '
     'UHV component Assembly Line recipes use this fluid and research; '
     'Robot Arms need more Infinity than the other components.\n\n'
     '§6Inconel 600§r supplies the firmware array coils and conductors. '
     'Its ABS-only molten route, separate liquid form and IV electrical wires are covered in Mid Game. '
     'JEI has the exact recipes for all these materials.'),
    ('Bigger Machines, Bigger Bills', 'gregtech:machine',
     '§3Supreme§r singleblocks extend the machine lineup into §dUHV§r. '
     'Their processing machines have perfect overclocks and selectable §e1×, 16× or 64×§r recipe parallelism. '
     'More parallel work needs more ingredients and power; the penalties still apply.\n\n'
     'The addon also adds the §3Microverse Projector IV§r, a §6Microminer Microminer§r '
     'and the §6Titanium Coil Micro Miner§r. Projector IV uses a miner, '
     'a Microminer Microminer and Quantum Flux to produce large batches of miners. '
     'Projector II gains additional coil recipes. Check JEI for the research and power requirements.\n\n'
     '§cThe Projector IV Creative Chest recipe is a trap.§r '
     'Its apparent chest output is erased when the recipe completes, and fluid is placed in front '
     'of the output bus if that space is empty. Do not treat its JEI output as a free completion route.'),
    ('A Few Things in Your Favour', 'minecraft:diamond',
     'There are a few useful changes among the penalties. §6Omnium conductors§r '
     'have greatly increased amperage capacity. Ordinary GregTech multiblocks allow at most '
     '§e2 energy hatches§r again. Other structure requirements still apply.\n\n'
     'There are additional metal-forming and fine-wire routes, including the addon\'s Omnium plates '
     'and wires needed for firmware. Check JEI rather than assuming only the original pack recipes exist.\n\n'
     'Dropped §6Coal§r can also turn into Diamonds when caught in the addon\'s piston-crushing setup. '
     'Charcoal does not qualify.\n\n'
     'Custom miners, coils, casings and the workbench have their own models or textures. '
     'Molten Infinity has an animated aura. Those visual changes do not protect anything from penalties.\n\n'
     '§6Omnium is the only superconducting wire material.§r Every other GregTech wire or cable loses at least '
     '§e1 EU per block§r. Existing higher losses remain; voltage and amperage ratings are unchanged.'),
]


def overlaps(a, b):
    return (a['x:3'] < b['x:3'] + b['sizeX:3'] and b['x:3'] < a['x:3'] + a['sizeX:3']
            and a['y:3'] < b['y:3'] + b['sizeY:3'] and b['y:3'] < a['y:3'] + a['sizeY:3'])


def main():
    original = json.loads(PATH.read_text(encoding='utf-8'))
    data = copy.deepcopy(original)
    quests = data['questDatabase:9']
    properties = lambda q: q['properties:10']['betterquesting:10']
    parent = next(q for q in quests.values() if 'So, what the heck is this?' in properties(q)['name:8'])
    genesis = next(c for c in data['questLines:9'].values()
                   if c['properties:10']['betterquesting:10']['name:8'].replace('§2', '').replace('§r', '') == 'Genesis')
    template = next(q for q in quests.values() if properties(q)['name:8'] == 'Your First Firmware')
    if any(properties(q)['name:8'] == INTRO_TITLE for q in quests.values()):
        print('Genesis additions guide already exists; preserving saved edits.')
        return
    before_positions = list(genesis['quests:9'].values())
    next_id = max(q['questID:3'] for q in quests.values()) + 1
    new_ids = []

    def add(title, desc, icon, parent_id, x, y, size=24):
        nonlocal next_id
        q = copy.deepcopy(template)
        qid = next_id
        next_id += 1
        q['questID:3'] = qid
        q['preRequisites:11'] = [parent_id]
        q['rewards:9'] = {}
        properties(q).update({'name:8': title, 'desc:8': desc, 'visibility:8': 'UNLOCKED',
                              'icon:10': {'Count:3': 1, 'Damage:2': 0, 'OreDict:8': '', 'id:8': icon}})
        if title == 'Bigger Machines, Bigger Bills':
            properties(q)['icon:10']['Damage:2'] = 32000
        assert '%n' not in desc and '/n' not in desc and '\\n' not in desc
        quests[f'{qid}:10'] = q
        pos = {'id:3': qid, 'sizeX:3': size, 'sizeY:3': size, 'x:3': x, 'y:3': y}
        assert all(not overlaps(pos, other) for other in genesis['quests:9'].values()), title
        key = max(int(k.split(':')[0]) for k in genesis['quests:9']) + 1
        genesis['quests:9'][f'{key}:10'] = pos
        new_ids.append(qid)
        return qid

    intro_id = add(INTRO_TITLE,
                   '§cMIM’s Additions§r changes factory planning from your first circuits through the endgame. '
                   'These optional checkbox quests explain the rules; ticking them does not activate or disable anything.\n\n'
                   '§eULV:§r machines need neighbours. §6EV:§r machines accumulate wear. '
                   '§cZPM:§r the world adds a growing power tax. These stages unlock for the whole world '
                   'when any player carries a qualifying circuit and remain unlocked permanently.\n\n'
                   'The other branches cover hazards that can happen from the start, firmware and changed recipes, '
                   'and the new endgame machines and materials. Read the ones you need, then return as you progress. '
                   'JEI remains the source for exact ingredient lists.',
                   'minecraft:book', parent['questID:3'], 300, 30, 36)
    for index, (title, icon, desc) in enumerate(ENTRIES):
        add(title, desc, icon, intro_id, 456, -360 + index * 48)
    old_desc = properties(parent)['desc:8']
    properties(parent)['desc:8'] = old_desc.replace(
        'This mod adds a handful of things to the pack, almost none of which will be listed here, but there are new quests and additions. '
        'You might spot unusual behaviour, or changes that only become obvious when you keep playing.',
        'This mod adds new machines, materials, firmware and a collection of increasingly unpleasant penalties. '
        'The §eWhat Changes?§r branch explains them. Those quests are optional reading; the rules apply whether you tick them or not.')
    data['questSettings:10']['betterquesting:10']['pack_version:3'] += 1
    assert list(genesis['quests:9'].values())[:len(before_positions)] == before_positions
    for key, q in original['questDatabase:9'].items():
        if q['questID:3'] != parent['questID:3']:
            assert quests[key] == q, 'Existing quest changed'
    for qid in new_ids:
        q = quests[f'{qid}:10']
        assert q['tasks:9'] == {'0:10': {'index:3': 0, 'taskID:8': 'bq_standard:checkbox'}}
        assert all(any(q2['questID:3'] == p for q2 in quests.values()) for p in q['preRequisites:11'])
    backup = ROOT / 'build/quest-guide-backup' / datetime.now().strftime('%Y%m%d-%H%M%S')
    backup.mkdir(parents=True, exist_ok=True)
    shutil.copy2(PATH, backup / PATH.name)
    PATH.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    print(f'Added {len(new_ids)} checkbox quests ({new_ids[0]}–{new_ids[-1]}) in Genesis; no node overlaps.')
    print(f'Preserved existing quest tasks, dependencies, rewards and layout. Backup: {backup}')


if __name__ == '__main__':
    main()
