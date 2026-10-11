"""Read-only BQ 3 quest references audit against a world's saved Forge registry.

Saved DefaultFluidList proves fluid names, not their current numeric IDs.
Never guess display metadata from list order or rewrite the quest database.
"""
import argparse
import collections
import hashlib
import json
from pathlib import Path
from inspect_world_nbt import load


def audit(database, level):
    data = json.loads(database.read_text(encoding="utf-8-sig"))
    root = next(iter(load(level).values()))
    item_names = {e["K"][1:] for e in root["FML"]["ItemData"] if e["K"].startswith("\x02")}
    fluid_names = {s.split(":", 1)[1] for s in root["Forge"]["DefaultFluidList"]}
    report = dict(database=str(database), registry=str(level), quests=len(data.get("questDatabase:9", {})),
                  database_sha256=hashlib.sha256(database.read_bytes()).hexdigest(),
                  registry_sha256=hashlib.sha256(level.read_bytes()).hexdigest(),
                  item_stacks=0, numeric_item_ids=[], missing_saved_items=[], placeholders=[],
                  fluid_displays=[], named_fluid_stacks=[], missing_saved_fluids=[], ore_dictionary_references=[])

    def visit(node, path, context):
        if not isinstance(node, dict):
            if isinstance(node, list):
                for i, value in enumerate(node): visit(value, path + "/" + str(i), context)
            return
        if path.count("/") == 2 and path.startswith("/questDatabase:9/"):
            context = dict(quest=path.rsplit("/", 1)[1],
                           name=node.get("properties:10", {}).get("betterquesting:10", {}).get("name:8", ""),
                           uuid_high=node.get("questIDHigh:4"), uuid_low=node.get("questIDLow:4"))
        location = dict(context, path=path)
        # Count distinguishes item stacks from enchantments, entity IDs and quest IDs.
        if any(k.startswith("Count:") for k in node):
            ids = [k for k in node if k.startswith("id:")]
            if ids:
                key = ids[0]; identity = node[key]; report["item_stacks"] += 1
                if key != "id:8" or str(identity).isdigit():
                    report["numeric_item_ids"].append(dict(location, id=identity))
                elif identity not in item_names:
                    report["missing_saved_items"].append(dict(location, id=identity))
                if identity == "betterquesting:placeholder":
                    original = node.get("tag:10", {}).get("orig_id:8", "")
                    report["placeholders"].append(dict(location, original=original, registered=original in item_names))
                if node.get("OreDict:8"):
                    report["ore_dictionary_references"].append(dict(location, ore=node["OreDict:8"]))
                if identity == "gregapi:gt.display.fluid":
                    name = node.get("tag:10", {}).get("f:8", "")
                    report["fluid_displays"].append(dict(location, fluid=name, saved_metadata=node.get("Damage:2")))
                    if name not in fluid_names:
                        report["missing_saved_fluids"].append(dict(location, fluid=name))
        if "FluidName:8" in node:
            name = node["FluidName:8"]
            report["named_fluid_stacks"].append(dict(location, fluid=name))
            if name not in fluid_names: report["missing_saved_fluids"].append(dict(location, fluid=name))
        for key, value in node.items(): visit(value, path + "/" + key, context)

    visit(data, "", {})
    by_name, by_id = collections.defaultdict(set), collections.defaultdict(set)
    for entry in report["fluid_displays"]:
        by_name[entry["fluid"]].add(entry["saved_metadata"])
        by_id[entry["saved_metadata"]].add(entry["fluid"])
    report["same_fluid_multiple_ids"] = {k: sorted(v) for k, v in by_name.items() if len(v) > 1}
    report["same_id_multiple_fluids"] = {str(k): sorted(v) for k, v in by_id.items() if len(v) > 1}
    report["limits"] = ["Saved registry is not a live registry check.",
                         "Item metadata semantics require the originating mod/version; not globally remappable.",
                         "Fluid display numeric IDs must be resolved by name in the receiving runtime."]
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("database", type=Path)
    parser.add_argument("--level", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    report = audit(args.database, args.level)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: len(v) if isinstance(v, (list, dict)) else v for k, v in report.items()}, ensure_ascii=True))


if __name__ == "__main__":
    main()
