$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$root=Split-Path $PSScriptRoot -Parent
$dir=Join-Path $root 'docs/assets/engraved-chips'
. (Join-Path $PSScriptRoot 'atlas_pixels.ps1')
$source=[Drawing.Bitmap]::FromFile((Join-Path $dir 'atlas-source.png'))
$fixes=New-Object Drawing.Bitmap 128,64
try {
    # The two reviewed corrections occupy row 2, columns 2 and 3 of the source sheet.
    for($i=0;$i -lt 2;$i++) {
        $left=[int][Math]::Floor(($i+1)*$source.Width/4)
        $right=[int][Math]::Floor(($i+2)*$source.Width/4)
        $top=[int][Math]::Floor($source.Height/2)
        [ItemAtlasPixels]::FitSize($source,$fixes,$left,$top,($right-$left),($source.Height-$top),($i*64),0,64)
    }
    $fixes.Save((Join-Path $dir 'corrections-64.png'))
} finally {$fixes.Dispose();$source.Dispose()}
& (Join-Path $PSScriptRoot 'export_semiconductor_textures.ps1') -AssetDirectory 'docs/assets/engraved-chips'
& (Join-Path $PSScriptRoot 'export_misc_textures.ps1')
