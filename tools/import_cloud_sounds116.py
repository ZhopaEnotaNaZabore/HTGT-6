"""Import official sound event metadata from a local launcher index; never copy OGG."""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = pathlib.Path.home() / "AppData/Roaming/.minecraft/assets"


def run(prefixes):
    index = json.loads((ASSETS / "indexes/5.json").read_text(encoding="utf-8"))["objects"]
    sound_hash = index["minecraft/sounds.json"]["hash"]
    official = json.loads((ASSETS / "objects" / sound_hash[:2] / sound_hash).read_text(encoding="utf-8"))
    path = ROOT / "src/main/resources/htgt6-external-sounds.json"
    manifest = json.loads(path.read_text(encoding="utf-8"))
    events_path = ROOT / "src/main/resources/assets/htgt6/sounds.json"
    events = json.loads(events_path.read_text(encoding="utf-8"))
    seen = {entry["resource"] for entry in manifest["entries"]}
    seen.update(entry["resource"] for entry in manifest.get("officialIndex116", {}).get("entries", []))
    visited = set()
    before = len(seen)

    def event(name):
        if name in visited:
            return
        visited.add(name)
        source = dict(official[name])
        sounds = []
        for raw in source["sounds"]:
            sound = {"name": raw} if isinstance(raw, str) else dict(raw)
            sound_name = sound["name"].removeprefix("minecraft:")
            if sound.get("type") == "event":
                event(sound_name)
            else:
                original = "minecraft/sounds/" + sound_name + ".ogg"
                entry = index[original]
                resource = "assets/htgt6/sounds/" + sound_name + ".ogg"
                if resource not in seen:
                    manifest["entries"].append({"resource": resource, "sha1": entry["hash"], "size": entry["size"], "officialResource": original})
                    seen.add(resource)
            sound["name"] = "htgt6:" + sound_name
            sounds.append(sound)
        source["sounds"] = sounds
        events[name] = source

    for name in official:
        if any(name.startswith(prefix) for prefix in prefixes):
            event(name)
    path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    events_path.write_text(json.dumps(events, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(len(visited), "events;", len(seen) - before, "new cloud entries")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        raise SystemExit("Pass official sound event prefixes")
    run(sys.argv[1:])
