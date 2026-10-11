"""Print palettes and marker blocks from modern vanilla structure NBT files.

This intentionally has no third-party dependencies so the imported templates can
be audited again when the source Minecraft version is changed.
"""

import gzip
import pathlib
import struct
import sys


def read_string(stream):
    length = struct.unpack(">H", stream.read(2))[0]
    return stream.read(length).decode("utf-8")


def read_payload(stream, tag_type):
    if tag_type == 0:
        return None
    if tag_type == 1:
        return struct.unpack(">b", stream.read(1))[0]
    if tag_type == 2:
        return struct.unpack(">h", stream.read(2))[0]
    if tag_type == 3:
        return struct.unpack(">i", stream.read(4))[0]
    if tag_type == 4:
        return struct.unpack(">q", stream.read(8))[0]
    if tag_type == 5:
        return struct.unpack(">f", stream.read(4))[0]
    if tag_type == 6:
        return struct.unpack(">d", stream.read(8))[0]
    if tag_type == 7:
        return list(stream.read(struct.unpack(">i", stream.read(4))[0]))
    if tag_type == 8:
        return read_string(stream)
    if tag_type == 9:
        item_type = struct.unpack(">b", stream.read(1))[0]
        return [read_payload(stream, item_type) for _ in range(struct.unpack(">i", stream.read(4))[0])]
    if tag_type == 10:
        result = {}
        while True:
            child_type = struct.unpack(">b", stream.read(1))[0]
            if child_type == 0:
                return result
            child_name = read_string(stream)
            result[child_name] = read_payload(stream, child_type)
    if tag_type == 11:
        return [struct.unpack(">i", stream.read(4))[0] for _ in range(struct.unpack(">i", stream.read(4))[0])]
    if tag_type == 12:
        return [struct.unpack(">q", stream.read(8))[0] for _ in range(struct.unpack(">i", stream.read(4))[0])]
    raise ValueError("unsupported NBT tag type: %d" % tag_type)


def load_nbt(path):
    with gzip.open(str(path), "rb") as stream:
        root_type = struct.unpack(">b", stream.read(1))[0]
        read_string(stream)
        return read_payload(stream, root_type)


root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "src/main/resources/assets/htgt6/structures/shipwreck")
states = set()
markers = set()
for path in sorted(root.glob("*.nbt")):
    nbt = load_nbt(path)
    palettes = nbt.get("palettes", [nbt.get("palette", [])])
    palette = palettes[0]
    for candidate in palettes:
        states.update((entry["Name"], tuple(sorted(entry.get("Properties", {}).items()))) for entry in candidate)
    for block in nbt["blocks"]:
        if "nbt" in block:
            markers.add(tuple(sorted((key, str(value)) for key, value in block["nbt"].items())))
            if "metadata" in block["nbt"]:
                print("  marker %-16s at %s (chest expected at y-1)" %
                      (block["nbt"]["metadata"], block["pos"]))
    print("%-42s size=%s blocks=%d palettes=%d states=%d" %
          (path.name, nbt["size"], len(nbt["blocks"]), len(palettes), len(palette)))

print("\nBlock states:")
for name, properties in sorted(states):
    print(name, dict(properties))
print("\nBlock NBT markers:")
for marker in sorted(markers):
    print(dict(marker))
