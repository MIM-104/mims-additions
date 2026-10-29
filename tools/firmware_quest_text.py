"""Keep firmware tutorials focused on mechanics; ingredient lists belong in JEI."""

DESCRIPTIONS = {
    'The Firmware Workbench': (
        'Circuits start §eUnconfigured§r. Before using one in a machine, controller or component recipe, '
        'program it for that exact item. Circuit recipes themselves still use unconfigured circuits.\n\n'
        'The §3Precision Firmware Workbench§r needs no power and becomes available with §6Steel§r. '
        'Make both precision tools, then continue to §eYour First Firmware§r.\n\n'
        'Use §6Fine Tin Wire§r to get started; §6Fine Soldering Alloy Wire§r works too. '
        'Tin costs §e2 durability per tool per soldered branch§r; alloy costs §e1§r. '
        'Both tools have §e320 durability§r, enough for 32 five-branch boards with tin or 64 with alloy. '
        'Earlier boards have fewer branches.\n\n'
        'Automatic programming becomes available at §dIV§r with the §3Firmware Synthesis Array§r.'
    ),
    'Your First Firmware': (
        'Put an unconfigured circuit in the centre of the workbench and install both precision tools. '
        'Choose the §eexact item and voltage tier§r from the search list. An LV machine is a good first job.\n\n'
        'Follow the ghost icons for the terminal parts. Put §6Fine Tin or Fine Soldering Alloy Wire§r '
        'in the single wire supply slot. It supplies one wire per soldered branch '
        'and one extra wire when you flash the board.\n\n'
        'Rotate the traces to connect each terminal to the circuit. Press §aSOLDER§r to join the next ready branch. '
        'A successful branch consumes its terminal part, one feed wire and tool durability. '
        'Failed attempts cost nothing; resetting does not refund soldered parts. '
        'Once every branch is soldered, press §aFLASH§r and check the purpose in the circuit tooltip.\n\n'
        'Boards have 2 branches at ULV/LV, 3 at MV, 4 at HV and 5 at EV and above. '
        'The table allows any purpose on any tier, but the final recipe still needs its normal circuit tier. '
        'Components such as Robot Arms and filters need their own firmware when their recipes use circuits.\n\n'
        '§eCraft a firmware circuit by itself to wipe it back to Unconfigured.§r '
        'The numbered GregTech §6Programmed Circuit§r is a separate recipe selector. '
        'Tick this quest after programming your first board.'
    ),
    'Inconel 600': (
        '§6Inconel 600§r is the nickel-chromium-iron alloy used in the firmware array coils and wiring. '
        'It is made only in the §3Alloy Blast Smelter§r, requiring §dIV§r power and §e3,600 K§r.\n\n'
        'Cast the molten alloy into ingots in a §3Vacuum Freezer§r. '
        'One ABS batch gives §e20 ingots§r. JEI shows the recipes.\n\n'
        '§eMolten and Liquid Inconel 600 are different fluids.§r The ABS produces Molten Inconel; '
        'fluid-extracting alloy items produces Liquid Inconel. Use the matching casting recipe. '
        'Its electrical wires and cables carry §dIV§r power.'
    ),
    'Parts for the Firmware Array': (
        'Make the array parts in an §3IV Assembling Machine§r. No cleanroom is required.\n\n'
        'Use the workbench to program the first circuits: each §6Firmware Memory Bank§r needs '
        '§e2 IV circuits§r programmed for the bank, and the controller needs §e4 IV circuits§r '
        'programmed for the §3Firmware Synthesis Array§r. Any circuit item of IV tier works. '
        'Circuits used in its Robot Arms, Emitters and Sensors need those components\' firmware instead.\n\n'
        'Check JEI for the part recipes and the controller\'s multiblock page for the structure.'
    ),
    'Firmware Synthesis Array': (
        'The §3Firmware Synthesis Array§r programs circuits automatically at §dIV§r, '
        'without a puzzle or a cleanroom. Make its special parts in an §3IV Assembling Machine§r.\n\n'
        'Use the workbench to get started: each §6Firmware Memory Bank§r needs '
        '§e2 IV circuits§r programmed for the bank, and the controller needs §e4 IV circuits§r '
        'programmed for the array. Any circuit item of IV tier works. '
        'Program circuits for the Robot Arms, Emitters and Sensors separately when making those components.\n\n'
        'Follow the controller\'s §eJEI multiblock page§r or §esneak-right-click preview§r to build it. '
        'Include power, item input/output and a §3Maintenance Hatch§r; fix maintenance before running it. '
        'No operating fluids are needed. Allow extra power for machine wear and world penalties.\n\n'
        'Each recipe uses an §6unconfigured circuit§r and a §6fine wire§r, selected by a numbered '
        '§6Programmed Circuit§r. §eWire material and circuit number together choose the exact firmware§r; '
        'check JEI for the combination. The numbered selector is retained. '
        'Any circuit of the required tier works, and the output keeps its original circuit type. '
        'No pre-programmed master circuit is needed.\n\n'
        'Automate the input materials and collect firmware circuits from the output bus. '
        'Keep one selector in place for a dedicated recipe, or supply the matching selector for each job. '
        'Unwanted firmware can still be wiped in your crafting grid.'
    ),
}


def update_descriptions(book):
    for quest in book.get('questDatabase:9', {}).values():
        properties = quest.get('properties:10', {}).get('betterquesting:10', {})
        title = properties.get('name:8', '').replace('§2', '').replace('§r', '').strip()
        if title in DESCRIPTIONS:
            properties['desc:8'] = DESCRIPTIONS[title]
