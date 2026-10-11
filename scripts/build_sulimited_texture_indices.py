"""Build SU-Limited's compact 64 px texture set.

The ten terrain cells come from the ImageGen-authored 5x2 atlas supplied with
``--atlas``.  Celestial indices and animated catalogue art are resized in
place because Minecraft only displays them as small GUI icons.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageEnhance, ImageOps


STYLE_NAMES = (
    "smooth_sandy",
    "rough_rocky",
    "magmatic",
    "chipped_stone",
    "smooth_snowy",
    "broken_ice",
    "suede_stone",
    "rough_regolith",
    "smooth_regolith",
    "dense_core",
)


def save_png(image: Image.Image, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    image.save(destination, "PNG", optimize=True, compress_level=9)


def build_indices(atlas_path: Path, output_dir: Path) -> None:
    atlas = Image.open(atlas_path).convert("RGB")
    width, height = atlas.size
    for index, name in enumerate(STYLE_NAMES):
        column, row = index % 5, index // 5
        left = round(width * column / 5)
        right = round(width * (column + 1) / 5)
        top = round(height * row / 2)
        bottom = round(height * (row + 1) / 2)
        cell = atlas.crop((left, top, right, bottom)).resize((64, 64), Image.Resampling.LANCZOS)
        cell = ImageOps.autocontrast(ImageOps.grayscale(cell), cutoff=1)
        # Keep the masks predominantly white so a single RGB multiplier can
        # reproduce both very pale ice and dark rock without crushed shadows.
        cell = cell.point(lambda value: 192 + value * 63 // 255)
        cell = ImageEnhance.Contrast(cell).enhance(1.18)
        save_png(cell.convert("RGBA"), output_dir / f"{name}.png")


def resize_to_64(path: Path) -> bool:
    image = Image.open(path)
    if image.width <= 64 and image.height <= 64:
        return False
    resized = image.convert("RGBA").resize((64, 64), Image.Resampling.LANCZOS)
    save_png(resized, path)
    return True


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--atlas", type=Path, required=True)
    parser.add_argument("--resources", type=Path, default=Path("src/main/resources/assets/htgt6/textures"))
    args = parser.parse_args()

    build_indices(args.atlas, args.resources / "blocks" / "index")

    resized = 0
    targets = list((args.resources / "gui" / "celestialbodies").rglob("*.png"))
    targets.extend((args.resources / "sulimited").rglob("*.png"))
    for target in targets:
        resized += int(resize_to_64(target))
    print(f"built {len(STYLE_NAMES)} terrain indices; resized {resized} icons")


if __name__ == "__main__":
    main()
