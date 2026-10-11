import io
import struct
import sys
import zlib
from pathlib import Path


def read(fmt, stream):
    return struct.unpack(">" + fmt, stream.read(struct.calcsize(">" + fmt)))[0]


def string(stream):
    return stream.read(read("H", stream)).decode("utf-8")


def payload(kind, stream):
    if kind == 1: return read("b", stream)
    if kind == 2: return read("h", stream)
    if kind == 3: return read("i", stream)
    if kind == 4: return read("q", stream)
    if kind == 5: return read("f", stream)
    if kind == 6: return read("d", stream)
    if kind == 7: return list(stream.read(read("i", stream)))
    if kind == 8: return string(stream)
    if kind == 9:
        element = read("b", stream)
        return [payload(element, stream) for _ in range(read("i", stream))]
    if kind == 10:
        result = {}
        while True:
            child = read("b", stream)
            if child == 0: return result
            result[string(stream)] = payload(child, stream)
    if kind == 11: return [read("i", stream) for _ in range(read("i", stream))]
    if kind == 12: return [read("q", stream) for _ in range(read("i", stream))]
    raise ValueError("unknown NBT tag " + str(kind))


def chunk(region, chunk_x, chunk_z):
    data = region.read_bytes()
    index = (chunk_x & 31) + (chunk_z & 31) * 32
    location = int.from_bytes(data[index * 4:index * 4 + 3], "big") * 4096
    if not location: return None
    size = int.from_bytes(data[location:location + 4], "big")
    compression = data[location + 4]
    compressed = data[location + 5:location + 4 + size]
    raw = zlib.decompress(compressed) if compression == 2 else compressed
    # The diagnostic only needs the unique TAG_Int_Array named BiomesExt.
    marker = b"\x0b\x00\x09BiomesExt"
    offset = raw.find(marker)
    if offset < 0: return None
    offset += len(marker)
    length = struct.unpack(">i", raw[offset:offset + 4])[0]
    offset += 4
    values = list(struct.unpack(">" + "i" * length, raw[offset:offset + length * 4]))
    return {"Level": {"BiomesExt": values}}


region = Path(sys.argv[1])
center_x, center_z = int(sys.argv[2]), int(sys.argv[3])
for cz in range(center_z - 3, center_z + 4):
    line = []
    for cx in range(center_x - 3, center_x + 4):
        root = chunk(region, cx, cz)
        ids = None if root is None else root.get("Level", {}).get("BiomesExt")
        if not ids:
            line.append("   ---")
            continue
        warm = ids.count(350)
        deep = ids.count(351)
        unset = ids.count(255)
        line.append("%3d/%3d/%3d" % (warm, deep, unset))
    print(" ".join(line))

root = chunk(region, center_x, center_z)
ids = None if root is None else root.get("Level", {}).get("BiomesExt")
if ids:
    print("center chunk biome grid:")
    for z in range(16):
        print(" ".join("W" if ids[(z << 4) | x] == 350 else
                       "D" if ids[(z << 4) | x] == 351 else
                       "?" if ids[(z << 4) | x] == 255 else "." for x in range(16)))
