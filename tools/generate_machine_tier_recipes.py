"""Fill T1-T4 and existing electrical IV gaps from existing HiTech6 grids; preserve authored higher-tier recipes.

Run from the repository root. Outputs a reviewed Java catalog and an audit manifest.
All GT6 output IDs are explicit: material variants are not inferred by incrementing IDs.
"""
import json
import re
from pathlib import Path

ROOT = Path('src/main/java/com/mod/htgt6/common/handler/recipes/modpackgenerated/crafting3x3')
HULL = ['ULVhull', 'hullLV', 'hullMV', 'hullHV', 'HullEV', 'HullIV']
COIL = ['ULVcoil', 'LVCOIL', 'MVcoil', 'HVcoil', 'EVcoil', 'IVcoil']
CIRCUIT = ['<IC2:itemPartCircuit>', '<gregtech:gt.multiitem.technological:30301>',
           '<gregtech:gt.multiitem.technological:30302>', '<IC2:itemPartCircuitAdv>',
           '<gregtech:gt.multiitem.technological:30304>', '<gregtech:gt.multiitem.technological:30305>']
CABLE = [28100, 28350, 28650, 28600, 28900, 29400]
KINETIC_LEVELS = [0, 2, 3, 4]  # Bronze ULV, steel MV, titanium HV, tungsten steel EV.
MATERIAL = {'kinetic': [8610, 8630, 220, 8635], 'electric': [8651, 130, 8636, 240, 220],
            'heat': [8630, 8661, 220, 8638]}


def multi(n): return '<gregtech:gt.multitileentity:%d>' % n


def read_recipes():
    result = {}
    for path in sorted(ROOT.glob('CraftingFrom*.java')):
        for line in path.read_text(encoding='utf-8-sig').splitlines():
            m = re.search(r'addTokens\("([^"]+)", "<gregtech:gt.multitileentity:(\d+)>\*1", new String\[\]\[\] \{(.+)\}, settings, logger\)', line)
            if not m: continue
            rows = [json.loads('[' + row + ']') for row in re.findall(r'new String\[\] \{([^{}]*)\}', m[3])]
            identity = int(m[2])
            if identity in result: raise ValueError('ambiguous template %d' % identity)
            result[identity] = dict(source=m[1], grid=rows)
    return result


def component_level(grid):
    levels = {HULL.index(t[7:-1]) for row in grid for t in row if t and t.startswith('<HTGT6:') and t[7:-1] in HULL}
    if not levels:
        for row in grid:
            for token in row:
                m = re.fullmatch(r'<gregtech:gt.multiitem.technological:(120\d\d)>',token or '')
                if m and int(m[1]) % 20 <= 5: levels.add(int(m[1]) % 20)
    assert len(levels) == 1, grid
    return levels.pop()


def promote(token, old, new, old_mat, new_mat, source_output, target_output, kind):
    if token is None or token == 'null': return token
    if token == CIRCUIT[old]: return CIRCUIT[new]
    for parts in [HULL, COIL]:
        if token == '<HTGT6:%s>' % parts[old]: return '<HTGT6:%s>' % parts[new]
    m = re.fullmatch(r'<gregtech:gt.multiitem.technological:(\d+)>', token)
    if m:
        number = int(m[1])
        # Motors, pumps, conveyors, pistons, arms, fields, emitters and sensors.
        for base in [12000, 12020, 12040, 12060, 12080, 12100, 12120, 12140]:
            if number == base + old: return '<gregtech:gt.multiitem.technological:%d>' % (base + new)
        # Gas laser cartridges (110xx) are types, not tier numbers.
    m = re.fullmatch(r'<gregtech:gt.multitileentity:(\d+)>', token)
    if m:
        number = int(m[1])
        if CABLE[old] <= number <= CABLE[old] + 19:
            return multi(CABLE[new] + number - CABLE[old])
        # Electric motors use the matching transmission axle, starting with bronze at LV.
        if 24810 <= number <= 24843 and old >= 1:
            base = 24800 + old * 10
            if base <= number <= base + 3: return multi(number + (new-old)*10)
        if source_output in range(10001,10005):
            heating_wire = [None,28300,28600,28900,29100,29350]
            if heating_wire[old] <= number <= heating_wire[old]+5:
                return multi(heating_wire[new]+number-heating_wire[old])
        # Metal fluid pipes retain their diameter. Plastic/acid-resistant pipes stay unchanged.
        pipe_tiers = {'kinetic':[26120,26140,26180,26220],
                      'electric':[0,26140,26160,26180,26220,26440],
                      'heat':[0,26140,26400,26180,26240]}
        for base in [26100,26120,26140,26160,26180,26220,26400,26240]:
            if base <= number <= base+4: return multi(pipe_tiers[kind][KINETIC_LEVELS.index(new) if kind == 'kinetic' else new]+number-base)
        # An electric mixer incorporates the mechanical mixer of the same physical machine tier.
        if source_output in range(20351,20355) and number in range(20181,20185):
            return multi(min(20184, number + target_output-source_output))
        # Strong steam engine upgrade retains the corresponding normal engine.
        engines = [1302,1304,1306,1308]
        if source_output in [1352,1354,1356,1358] and number in engines: return multi(engines[KINETIC_LEVELS.index(new)])
    m = re.fullmatch(r'<gregtech:gt.meta.([^:]+):(\d+)>', token)
    if m:
        prefix, material = m[1], int(m[2])
        # Only structural material advances. Diamond abrasives, lenses, electrodes,
        # ceramic insulation and other functional ingredients keep their identity.
        if material in {old_mat,8610,8630,8651,8667,130,220,240,8636,8635} and prefix in {'plate','plateDouble','plateTriple','plateQuintuple',
                'gearGt','gearGtSmall','spring','rotor','stick','stickLong','casingSmall'}:
            return '<gregtech:gt.meta.%s:%d>' % (prefix,new_mat)
    if token == '<ore:plateQuintupleBronze>' and old_mat == 8610:
        return '<gregtech:gt.meta.plateQuintuple:%d>' % new_mat
    return token


def generate():
    recipes = read_recipes()
    families = []
    def linear(kind, bases):
        for base in bases: families.append((str(base),kind,list(range(base,base+(5 if kind == 'electric' else 4)))))
    linear('kinetic', [20011,20021,20041,20051,20061,20101,20111,20141,20151,20181,20211,20231,20291,20511])
    # Centrifuges already have a complete dedicated ULV-HV progression.
    linear('heat', [20001,20171,20191,20311,20391])
    linear('electric', [10001,10011,10021,10031,10101,10111,20091,20161,20221,20261,
                        20301,20321,20331,20351,20501,20506,20601,20621,20631])
    families += [('steam engine','kinetic',[1302,1304,1306,1308]),
                 ('strong steam engine','kinetic',[1352,1354,1356,1358]),
                 ('boiler','kinetic',[1202,1204,1206,1208]),
                 ('gas turbine','kinetic',[9160,9161,9163,9164]),
                 ('steam turbine bronze-steel','kinetic',[1512,1522])]
    added, preserved = [], []
    for name, kind, outputs in families:
        assert outputs[0] in recipes, name
        for index, output in enumerate(outputs):
            expected_level = KINETIC_LEVELS[index] if kind == 'kinetic' else index + 1
            if output in recipes:
                current = recipes[output]
                actual_level = component_level(current['grid'])
                if actual_level == expected_level:
                    preserved.append(dict(output=output, source=current['source']))
                    continue
                # Existing kinetic grids must follow the corrected material-to-component mapping.
                assert kind == 'kinetic', (output,actual_level,expected_level)
                source_index = index
            else:
                source_index = max(i for i in range(index) if outputs[i] in recipes)
                current = recipes[outputs[source_index]]
            old_level = component_level(current['grid'])
            old_mat = MATERIAL[kind][source_index]
            new_mat = MATERIAL[kind][index]
            grid = [[promote(t,old_level,expected_level,old_mat,new_mat,outputs[source_index],output,kind)
                     for t in row] for row in current['grid']]
            assert component_level(grid) == expected_level
            added.append(dict(output=output, kind=kind, tier=index+1, voltage=['ULV','LV','MV','HV','EV','IV'][expected_level],
                              source=current['source'], grid=grid, replaces_authored=output in recipes))
    # Complete material variants which are not linear metadata tiers.
    known = {e['output'] for e in added} | {e['output'] for e in preserved}
    def part(prefix, mat): return '<gregtech:gt.meta.%s:%d>' % (prefix,mat)
    def comp(base, tier): return '<gregtech:gt.multiitem.technological:%d>' % (base+tier)
    def add_variant(output, tier, mat, family, grid):
        if output in known: return
        if output in recipes:
            assert component_level(recipes[output]['grid']) == tier, ('wrong authored tier',output)
            preserved.append(dict(output=output,source=recipes[output]['source']))
            known.add(output)
            return
        added.append(dict(output=output, kind='generator', tier=tier+1,
                          voltage=['ULV','LV','MV','HV','EV','IV'][tier],
                          source=family, grid=grid, replaces_authored=False))
        known.add(output)
    gas=[(9162,1,8661),(9165,4,740),(9166,5,770)]
    for out,t,m in gas:
        add_variant(out,t,m,'gas turbine material variants',[
            [comp(12000,t),part('rotor',m),comp(12000,t)],
            ['<ore:pipeSmall%s>'%{8661:'Invar',740:'Tungsten',770:'Iridium'}[m],'<HTGT6:%s>'%HULL[t],'<ore:pipeSmall%s>'%{8661:'Invar',740:'Tungsten',770:'Iridium'}[m]],
            [comp(12000,t),CIRCUIT[t],comp(12000,t)]])
    steam=[(1515,0,8620),(1518,0,8661),(1525,2,240),(1527,2,8672),
           (1528,2,8673),(1529,2,8679),(1530,3,220),(1531,3,8675),
           (1535,3,130),(1538,3,8665),(1540,4,8681),(1545,4,8790),(1548,4,9175)]
    for out,t,m in steam:
        add_variant(out,t,m,'steam turbine material variants',[
            [part('rotor',m)]*3,
            [comp(12000,t),CIRCUIT[t],comp(12000,t)],
            [multi(CABLE[t]+16),'<HTGT6:%s>'%HULL[t],multi(CABLE[t]+16)]])
    diesel=[(9147,0,8610),(9149,1,8661),(9148,2,8630),(9197,3,220),
            (9198,4,8635),(9199,4,740),(9120,5,770)]
    for out,t,m in diesel:
        add_variant(out,t,m,'diesel engine component progression',[
            [part('plate',m),comp(12060,t),part('plate',m)],
            [comp(12000,t),'<HTGT6:%s>'%HULL[t],comp(12000,t)],
            [comp(12020,t),CIRCUIT[t],'<ore:itemLubricant>']])
    engine=[(0,0,820),(1,0,8667),(9,0,8620),(3,1,8661),(10,1,8672),
            (11,2,8675),(5,2,240),(7,4,740)]
    for offset,t,m in engine:
        add_variant(1300+offset,t,m,'steam engine material variants',[
            [part('plate',m),part('plateTriple',820),part('plate',m)],
            [comp(12000,t),CIRCUIT[t],comp(12060,t)],
            [part('plate',m),'<HTGT6:%s>'%HULL[t],part('plate',m)]])
        add_variant(1350+offset,t,m,'strong steam engine material variants',[
            [part('plateQuintuple',m),comp(12060,t),part('plateQuintuple',m)],
            [comp(12000,t),multi(1300+offset),comp(12000,t)],
            [part('plateQuintuple',m),comp(12060,t),part('plateQuintuple',m)]])
    return added, preserved


def main():
    added, preserved = generate()
    lines = ['// Generated by tools/generate_machine_tier_recipes.py; preserve authored higher-tier recipes.',
             'package com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3;', '',
             'import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;',
             'import org.apache.logging.log4j.Logger;', '',
             'public final class MissingMachineTierRecipes {',
             '    private MissingMachineTierRecipes() {}',
             '    public static void register(GeneratedRecipeSettings settings, Logger logger) {']
    for entry in added:
        rows = ', '.join('new String[] {'+', '.join(json.dumps(x) for x in row)+'}' for row in entry['grid'])
        source = 'HTGT6 T%d/%s from %s' % (entry['tier'],entry['voltage'],entry['source'])
        lines.append('        GeneratedCrafting3x3.replaceTokens("%s", "%s*1", new String[][] {%s}, settings, logger);' % (source,multi(entry['output']),rows))
    lines += ['    }','}']
    (ROOT/'MissingMachineTierRecipes.java').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    path=Path('docs/audits/machine-tier-recipes.json')
    path.write_text(json.dumps(dict(changed=added,preserved=preserved),ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('Changed:',len(added),'Preserved:',len(preserved),'Corrected authored:',sum(e['replaces_authored'] for e in added))


if __name__ == '__main__': main()
