"""Inspect gzip-compressed Minecraft NBT files without external dependencies."""

import argparse
import gzip
import json
import struct
import zlib
from pathlib import Path


class Reader:
    def __init__(self, data):
        self.data = memoryview(data)
        self.offset = 0

    def read(self, count):
        result = self.data[self.offset:self.offset + count]
        if len(result) != count:
            raise EOFError("truncated NBT payload")
        self.offset += count
        return result

    def number(self, fmt):
        return struct.unpack(">" + fmt, self.read(struct.calcsize(">" + fmt)))[0]

    def string(self):
        return bytes(self.read(self.number("H"))).decode("utf-8")

    def payload(self, kind):
        if kind == 1:
            return self.number("b")
        if kind == 2:
            return self.number("h")
        if kind == 3:
            return self.number("i")
        if kind == 4:
            return self.number("q")
        if kind == 5:
            return self.number("f")
        if kind == 6:
            return self.number("d")
        if kind == 7:
            return list(bytes(self.read(self.number("i"))))
        if kind == 8:
            return self.string()
        if kind == 9:
            element = self.number("b")
            return [self.payload(element) for _ in range(self.number("i"))]
        if kind == 10:
            result = {}
            while True:
                child = self.number("b")
                if child == 0:
                    return result
                name = self.string()
                result[name] = self.payload(child)
        if kind == 11:
            return [self.number("i") for _ in range(self.number("i"))]
        if kind == 12:
            return [self.number("q") for _ in range(self.number("i"))]
        raise ValueError("unknown NBT tag %d" % kind)


def load(path):
    raw = Path(path).read_bytes()
    if raw[:2] == b"\x1f\x8b":
        raw = gzip.decompress(raw)
    reader = Reader(raw)
    kind = reader.number("b")
    if kind == 0:
        return None
    name = reader.string()
    return {name: reader.payload(kind)}


def load_region_chunk(path, chunk_x, chunk_z):
    """Load one chunk from an Anvil region file, or return None if absent."""
    raw = Path(path).read_bytes()
    index = (chunk_x & 31) + (chunk_z & 31) * 32
    header = index * 4
    sector = int.from_bytes(raw[header:header + 3], "big")
    if sector == 0:
        return None
    offset = sector * 4096
    length = int.from_bytes(raw[offset:offset + 4], "big")
    compression = raw[offset + 4]
    compressed = raw[offset + 5:offset + 4 + length]
    if compression == 1:
        payload = gzip.decompress(compressed)
    elif compression == 2:
        payload = zlib.decompress(compressed)
    elif compression == 3:
        payload = compressed
    else:
        raise ValueError("unknown region compression %d" % compression)
    reader = Reader(payload)
    kind = reader.number("b")
    name = reader.string()
    return {name: reader.payload(kind)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("files", nargs="+")
    args = parser.parse_args()
    for filename in args.files:
        print("=== %s ===" % filename)
        print(json.dumps(load(filename), ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
