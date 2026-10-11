"""Independent integer model of dlr/dlt/dlh/arw in the pinned 1.20.2 JAR.

No calls to the production Java implementation. Generates reproducible test vectors.
"""
import hashlib
import json
from pathlib import Path

MASK = (1 << 64) - 1
def signed(x, bits=64):
    x &= (1 << bits) - 1
    return x - (1 << bits) if x >> (bits - 1) else x
def mix(x):
    x = ((x ^ (x >> 30)) * 0xbf58476d1ce4e5b9) & MASK
    x = ((x ^ (x >> 27)) * 0x94d049bb133111eb) & MASK
    return x ^ (x >> 31)
def rotate(x, n):
    return ((x << n) | (x >> (64 - n))) & MASK
class Xoro:
    def __init__(self, seed, hi=None):
        if hi is None:
            low = (seed & MASK) ^ 0x6a09e667f3bcc909
            self.lo, self.hi = mix(low), mix((low + 0x9e3779b97f4a7c15) & MASK)
        else:
            self.lo, self.hi = seed & MASK, hi & MASK
    def draw(self):
        lo, hi = self.lo, self.hi
        result = (rotate((lo + hi) & MASK, 17) + lo) & MASK
        hi ^= lo
        self.lo = (rotate(lo, 49) ^ hi ^ (hi << 21)) & MASK
        self.hi = rotate(hi, 28)
        return result
    def worldgen_long(self):
        return signed((signed(self.draw() >> 32, 32) << 32) + signed(self.draw() >> 32, 32))
def coordinate(x, y, z):
    a = (signed(x * 3129871, 32) ^ signed(z * 116129781) ^ y) & MASK
    return signed(a * a * 42317861 + a * 11) >> 16
def positional(seed, name):
    r = Xoro(seed)
    lo, hi = r.draw(), r.draw()
    if name is not None:
        md5 = hashlib.md5(name.encode()).digest()
        r = Xoro(lo ^ int.from_bytes(md5[:8], 'big'), hi ^ int.from_bytes(md5[8:], 'big'))
        lo, hi = r.draw(), r.draw()
    return lo, hi

vectors = {'source': 'Minecraft 1.20.2 dlr, dlt$a, dlh, arw bytecode', 'decoration': [], 'position': []}
for seed in [0, 1, -1, 0x1202dec0, -9223372036854775808]:
    for x, z in [(0, 0), (112, -176), (-30000000, 29999984)]:
        r = Xoro(seed)
        a, b = r.worldgen_long() | 1, r.worldgen_long() | 1
        decoration = signed((x * a + z * b) ^ seed)
        r = Xoro(decoration)
        vectors['decoration'].append({'seed': seed, 'x': x, 'z': z, 'value': decoration,
                                      'draws': [r.worldgen_long() for _ in range(4)]})
    for name in [None, 'minecraft:bedrock_floor', 'minecraft:aquifer', 'minecraft:ore']:
        lo, hi = positional(seed, name)
        for x, y, z in [(1, -64, -1), (-4097, -61, 8193), (30000000, 319, -30000000)]:
            h = coordinate(x, y, z)
            r = Xoro(lo ^ (h & MASK), hi)
            vectors['position'].append({'seed': seed, 'name': name, 'x': x, 'y': y, 'z': z,
                                        'coordinate': h, 'draw': signed(r.draw()), 'float': (r.draw() >> 40) / (1 << 24)})
path = Path(__file__).resolve().parents[1] / 'src/main/resources/assets/htgt6/worldgen/futura_random_oracle_1202.json'
path.write_text(json.dumps(vectors, indent=2) + '\n', encoding='utf-8')
print('Wrote', len(vectors['decoration']), 'decoration and', len(vectors['position']), 'positional vectors')
