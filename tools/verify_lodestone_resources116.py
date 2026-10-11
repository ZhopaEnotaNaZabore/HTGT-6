"""Validate actual models, PNG data, predicates and declared cloud resources (stdlib only)."""
import argparse
import json
from pathlib import Path
import struct
import zipfile
import zlib

parser = argparse.ArgumentParser()
parser.add_argument("--reference", type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1] / "src/main/resources"
checks = 0

def check(value, message):
    global checks
    if not value:
        raise AssertionError(message)
    checks += 1

def model(name):
    namespace, path = name.split(":", 1) if ":" in name else ("minecraft", name)
    return json.loads((root / "assets" / namespace / "models" / (path + ".json")).read_text(encoding="utf-8"))

seen = set()
def parents(name, ancestry=()):
    if name in ("builtin/generated", "minecraft:builtin/generated"):
        return
    check(name not in ancestry, "model parent cycle: " + name)
    content = model(name)
    seen.add(name)
    if "parent" in content:
        parents(content["parent"], ancestry + (name,))

base_name = "htgt6:item/1_20_backport/lodestone_compass"
base = model(base_name)
parents(base_name)
for override in base["overrides"]:
    check(set(override["predicate"]) == {"angle"}, "angle predicate only")
    parents(override["model"])
for index in range(16384):
    angle = index / 16384.0
    selected = base_name
    for override in base["overrides"]:
        if angle >= override["predicate"]["angle"]:
            selected = override["model"]
    texture = model(selected)["textures"]["layer0"]
    frame = int(texture.rsplit("_", 1)[1])
    check(frame == (int(angle * 32 + 0.5) + 16) % 32, "actual model angle/frame mismatch")
parents("htgt6:block/1_20_backport/lodestone")
parents("htgt6:item/1_20_backport/lodestone")
state = json.loads((root / "assets/htgt6/blockstates/1_20_backport/lodestone.json").read_text())
check(state["variants"]["normal"]["model"] == "htgt6:block/1_20_backport/lodestone", "blockstate connected")

reference = zipfile.ZipFile(args.reference) if args.reference else None
png_count = 0
for atlas in ("blocks", "items"):
    for i in range(32):
        path = root / ("assets/htgt6/textures/%s/1_20_backport/compass_%02d.png" % (atlas, i))
        data = path.read_bytes()
        check(data[:8] == b"\x89PNG\r\n\x1a\n", "PNG signature")
        check(struct.unpack(">II", data[16:24]) == (16, 16), "compass texture dimensions")
        offset = 8
        while offset < len(data):
            size = struct.unpack(">I", data[offset:offset+4])[0]
            chunk = data[offset+4:offset+8+size]
            expected_crc = struct.unpack(">I", data[offset+8+size:offset+12+size])[0]
            check(zlib.crc32(chunk) & 0xffffffff == expected_crc, "PNG chunk checksum")
            offset += 12 + size
        check(offset == len(data), "PNG chunk boundaries")
        if reference:
            check(data == reference.read("assets/minecraft/textures/item/compass_%02d.png" % i), "official compass bytes")
        png_count += 1
for texture in ("lodestone_top", "lodestone_side"):
    data = (root / ("assets/htgt6/textures/blocks/1_20_backport/" + texture + ".png")).read_bytes()
    check(data[:8] == b"\x89PNG\r\n\x1a\n", "lodestone PNG signature")
    if reference:
        check(data == reference.read("assets/minecraft/textures/block/" + texture + ".png"), "official block bytes")
    png_count += 1

manifest = json.loads((root / "htgt6-external-sounds.json").read_text())
check(manifest["schema"] == 1, "existing manifest schema retained")
imports = manifest["officialIndex116"]
check(imports["id"] == "8" and imports["sha1"] == "117379a04a44c80fc5a5806f2e676ca4ac81f5ce" and imports["size"] == 416952, "pinned official index")
check(imports["url"] == "https://piston-meta.mojang.com/v1/packages/117379a04a44c80fc5a5806f2e676ca4ac81f5ce/8.json", "pinned official metadata origin")
paths = set()
for entry in manifest["entries"] + imports["entries"]:
    check(entry["resource"] not in paths, "unique cloud resource")
    paths.add(entry["resource"])
for entry in imports["entries"]:
    check(entry["officialResource"] == "minecraft/" + entry["resource"].split("assets/htgt6/", 1)[1], "exact official object name")
    check(".." not in entry["resource"] and "//" not in entry["resource"], "safe import path")
sounds = json.loads((root / "assets/htgt6/sounds.json").read_text())
for event in ("block.lodestone.break", "block.lodestone.step", "block.lodestone.hit", "block.lodestone.fall", "block.lodestone.place", "item.lodestone_compass.lock"):
    check(bool(sounds[event]["sounds"]), "event has sounds")
    for sound in sounds[event]["sounds"]:
        name = sound if isinstance(sound, str) else sound["name"]
        namespace, path = name.split(":", 1)
        check("assets/"+namespace+"/sounds/"+path+".ogg" in paths, "event resolves through cloud manifest")
check(not list((root / "assets/htgt6").rglob("*.ogg")), "zero bundled Backport audio")
for language in ("en_US", "ru_RU"):
    lines = (root / ("assets/htgt6/lang/"+language+".lang")).read_text(encoding="utf-8").splitlines()
    names = {line.split("=", 1)[0] for line in lines if "=" in line and not line.startswith("#")}
    for name in ("tile.backport_1_20.lodestone.name", "item.backport_1_20.lodestone_compass.name", "achievement.htgt6.backport.country_lode_take_me_home"):
        check(name in names, "localization: "+language+" / "+name)
if reference:
    reference.close()
print("PASS B116-05 resources: %d checks; %d connected models; %d PNGs; %d index imports; official byte comparison=%s" % (checks, len(seen), png_count, len(imports["entries"]), bool(args.reference)))
