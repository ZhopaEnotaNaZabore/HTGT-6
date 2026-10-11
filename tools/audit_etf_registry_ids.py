"""Read-only Forge 1.7 registry audit; never modifies a save.

Matching names are candidates, not proof of compatible metadata or TileEntity NBT.
Use --after to enforce preservation of every saved block and item ID.
"""
import argparse
import json
from pathlib import Path

from inspect_world_nbt import load


def registry(path):
    root = load(path)
    if not isinstance(root, dict) or len(root) != 1:
        raise ValueError("Expected one NBT root compound")
    entries = next(iter(root.values()))["FML"]["ItemData"]
    result = {}
    occupied = set()
    for entry in entries:
        key, number = entry["K"], entry["V"]
        if not key or key[0] not in ("\x01", "\x02"):
            raise ValueError("Unknown Forge registry entry: %r" % key)
        identity = ("block" if key[0] == "\x01" else "item", key[1:])
        slot = (identity[0], number)
        if identity in result or slot in occupied:
            raise ValueError("Duplicate registry name or ID: %r" % (identity,))
        result[identity] = number
        occupied.add(slot)
    return result


def audit(before, after=None):
    candidates = []
    for (kind, name), number in sorted(before.items()):
        if not name.startswith("etfuturum:"):
            continue
        path = name.split(":", 1)[1]
        for prefix in ("Minecraft_1.20.2:", "Minecraft_1.20.2:backport_1_20_"):
            target = prefix + path
            if (kind, target) in before:
                candidates.append(dict(kind=kind, source=name, source_id=number,
                    target=target, target_id=before[kind, target],
                    status="two_registered_identities_require_compatibility_adapter"))
    changed = []
    if after is not None:
        for identity, number in sorted(before.items()):
            actual = after.get(identity)
            if actual != number:
                changed.append(dict(kind=identity[0], name=identity[1],
                                    before=number, after=actual))
    return dict(entries=len(before), candidates=candidates,
                id_check="not_run" if after is None else ("failed" if changed else "passed"),
                changed_or_missing=changed)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("before", type=Path, help="Saved level.dat")
    parser.add_argument("--after", type=Path, help="level.dat after a control launch")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    report = audit(registry(args.before), registry(args.after) if args.after else None)
    text = json.dumps(report, ensure_ascii=False, indent=2)
    if args.output:
        args.output.write_text(text + "\n", encoding="utf-8")
    else:
        print(text)
    return 1 if report["changed_or_missing"] else 0


if __name__ == "__main__":
    raise SystemExit(main())
