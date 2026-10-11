$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$art = Join-Path $root 'docs/assets/singleblock-machines'
$out = Join-Path $root 'src/main/resources/assets/htgt6/textures/blocks/atlases'
New-Item -ItemType Directory -Force $out | Out-Null
$names = @('assembler_side','assembler_top','assembler_front','machine_front','em_processor_front','metal_former_front','MEG_Front','RIG_Front','UGS_Front','UEfront')
$source = [Drawing.Bitmap]::FromFile((Join-Path $art 'atlas-source.png'))
$atlas = New-Object Drawing.Bitmap 320,128
$preview = New-Object Drawing.Bitmap 1280,512
try {
    # Sample each cell independently: no borders from neighbouring panels enter the slice.
    for ($i=0; $i -lt $names.Count; $i++) {
        $col=$i%5; $row=[int][Math]::Floor($i/5)
        for ($y=0; $y -lt 64; $y++) { for ($x=0; $x -lt 64; $x++) {
            $sx=[int][Math]::Floor(($col+($x+0.5)/64)*$source.Width/5)
            $sy=[int][Math]::Floor(($row+($y+0.5)/64)*$source.Height/2)
            $color=$source.GetPixel($sx,$sy)
            if ($color.A -ne 255) { throw 'Block surfaces must be opaque.' }
            $atlas.SetPixel($col*64+$x,$row*64+$y,$color)
            for($dy=0;$dy -lt 4;$dy++){for($dx=0;$dx -lt 4;$dx++){
                $preview.SetPixel(($col*64+$x)*4+$dx,($row*64+$y)*4+$dy,$color)
            }}
        }}
    }
    $atlas.Save((Join-Path $out 'singleblock.png'))
    $preview.Save((Join-Path $art 'preview-4x.png'))
} finally { $source.Dispose(); $atlas.Dispose(); $preview.Dispose() }
# Reuse the validated 32px base-grid decoder; each 64px block face occupies 2x2 cells.
$index=@('tileSize=32','columns=10','rows=4')
for($i=0;$i -lt $names.Count;$i++) {
    $cell=($i%5)*2+[int][Math]::Floor($i/5)*20
    $index+='item.'+$names[$i]+'='+$cell
    $index+='size.'+$names[$i]+'=64'
}
[IO.File]::WriteAllLines((Join-Path $out 'singleblock.properties'),$index,[Text.Encoding]::ASCII)
Write-Output 'Exported 10 opaque 64x64 machine faces in one 320x128 atlas.'
