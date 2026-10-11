"""Import official JSON/PNG resources for owned Nether wood; never copies audio."""
import json
import pathlib
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
DEST = ROOT / "src/main/resources/assets/htgt6"
NAMES = []
ITEM_NAMES = []
for family in ("crimson", "warped"):
    NAMES += [family + "_" + suffix for suffix in (
        "stem", "hyphae", "planks", "stairs", "slab", "fence", "fence_gate",
        "door", "trapdoor", "button", "pressure_plate", "sign", "wall_sign",
        "hanging_sign", "wall_hanging_sign")]
    NAMES += ["stripped_" + family + "_" + suffix for suffix in ("stem", "hyphae")]


def run():
    seen = set()
    with zipfile.ZipFile(ROOT / "tmp/minecraft-1.20.2-client.jar") as jar:
        def texture(source):
            source = source.removeprefix("minecraft:")
            if source.startswith("#"):
                return source
            original = "assets/minecraft/textures/" + source + ".png"
            name = source.split("/", 1)[-1]
            target = DEST / "textures/blocks/1_20_backport" / (name + ".png")
            target.parent.mkdir(parents=True, exist_ok=True)
            if not target.exists():
                target.write_bytes(jar.read(original))
            if original + ".mcmeta" in jar.namelist():
                target.with_suffix(".png.mcmeta").write_bytes(jar.read(original + ".mcmeta"))
            # Legacy Item.registerIcons uses textures/items rather than the block atlas.
            if source.startswith("item/"):
                item = DEST / "textures/items/1_20_backport" / (name + ".png")
                item.parent.mkdir(parents=True, exist_ok=True)
                item.write_bytes(jar.read(original))
            return "htgt6:1_20_backport/" + name

        def model(source):
            source = source.removeprefix("minecraft:")
            if source.startswith("builtin/"):
                return "minecraft:" + source
            kind, name = source.split("/", 1)
            location = "htgt6:" + kind + "/1_20_backport/" + name
            if source in seen:
                return location
            seen.add(source)
            data = json.loads(jar.read("assets/minecraft/models/" + source + ".json"))
            if "parent" in data:
                data["parent"] = model(data["parent"])
            if "textures" in data:
                data["textures"] = {key: texture(value) for key, value in data["textures"].items()}
            target = DEST / "models" / kind / "1_20_backport" / (name + ".json")
            target.parent.mkdir(parents=True, exist_ok=True)
            if not target.exists():
                target.write_text(json.dumps(data, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8")
            return location

        def remap(data):
            if isinstance(data, list):
                return [remap(value) for value in data]
            if isinstance(data, dict):
                return {key: model(value) if key == "model" else remap(value) for key, value in data.items()}
            return data

        for name in NAMES:
            path = "assets/minecraft/blockstates/" + name + ".json"
            target = DEST / "blockstates/1_20_backport" / (name + ".json")
            target.write_text(json.dumps(remap(json.loads(jar.read(path))), separators=(",", ":")) + "\n", encoding="utf-8")
            if "assets/minecraft/models/item/" + name + ".json" in jar.namelist():
                model("item/" + name)
        for name in ITEM_NAMES:
            model("item/" + name)
        for family in ("crimson", "warped"):
            for suffix in (family, "hanging/" + family):
                target = DEST / "textures/entity/1_20_backport/signs" / (suffix + ".png")
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(jar.read("assets/minecraft/textures/entity/signs/" + suffix + ".png"))
        for language in ("en_US", "ru_RU"):
            official = json.loads(jar.read("assets/minecraft/lang/" + language.lower() + ".json")) if language == "en_US" else None
            # Translations other than English are distributed through the asset index.
            if official is None:
                continue
            target = DEST / "lang" / (language + ".lang")
            existing = target.read_text(encoding="utf-8")
            entries = []
            for name in NAMES:
                title = official.get("block.minecraft." + name)
                if title:
                    prefix = "item" if name.endswith(("_sign", "_door")) else "tile"
                    key = prefix + ".backport_1_20." + name + ".name="
                    if key not in existing:
                        entries.append(key + title)
            with target.open("a", encoding="utf-8") as out:
                out.write("\n" + "\n".join(entries) + "\n")
    print("Nether wood resources:", len(NAMES), "blockstates,", len(seen), "models")


if __name__ == "__main__":
    run()
