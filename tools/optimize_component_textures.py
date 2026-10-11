"""Losslessly compress only generated HiTech6 component animation strips."""
import argparse
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(root / 'scripts'))
from optimize_hitech_block_pngs import optimize

parser = argparse.ArgumentParser()
parser.add_argument('--root', type=Path, default=root)
args = parser.parse_args()
paths = sorted((args.root.resolve() / 'src/main/resources/assets/htgt6/textures/items/high_tier_components').rglob('*.png'))
if len(paths) != 56:
    raise RuntimeError('Expected exactly 56 component animation strips')
results = [optimize(path, True) for path in paths]
print('Compressed 56 component strips; saved', sum(row['saved'] for row in results), 'bytes without pixel changes.')
