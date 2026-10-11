param([switch]$ImportOriginals)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $root 'docs/assets/armor-blanks'
New-Item -ItemType Directory -Path $dir -Force | Out-Null
. (Join-Path $PSScriptRoot 'atlas_pixels.ps1')
if ($ImportOriginals) {
    if (Test-Path (Join-Path $dir 'nano-source.png')) { throw 'Nano source atlas already exists.' }
    $source = New-Object Drawing.Bitmap 2000,500
    $entries = @()
    try {
        $names = @('itemCarbonHelmet','itemCarbonChestplate','itemCarbonleggins','itemCarbonBoots')
        for ($i=0; $i -lt 4; $i++) {
            $file = Get-Item (Join-Path $root "src/main/resources/assets/htgt6/textures/items/$($names[$i]).png")
            $original = [Drawing.Bitmap]::FromFile($file.FullName)
            try {
                if ($original.Width -gt 500 -or $original.Height -gt 500) { throw 'Oversized nano source' }
                [ItemAtlasPixels]::Copy($original,$source,($i*500),0)
                $entries += [ordered]@{name=$names[$i];row=0;column=$i;source='nano-source.png';left=$i*500;top=0;width=$original.Width;height=$original.Height;originalBytes=$file.Length}
            } finally { $original.Dispose() }
        }
        $source.Save((Join-Path $dir 'nano-source.png'),[Drawing.Imaging.ImageFormat]::Png)
    } finally { $source.Dispose() }
    $quantum = [Drawing.Bitmap]::FromFile((Join-Path $dir 'quantum-source.png'))
    try {
        if ($quantum.Width%2 -ne 0 -or $quantum.Height%2 -ne 0) { throw 'Expected even 2x2 quantum source grid' }
        $w=[int]($quantum.Width/2); $h=[int]($quantum.Height/2)
        $names=@('itemQuantumPartHelmet','itemQuantumPartChestplate','itemQuantumPartLeggins','itemQuantumPartBoots')
        for ($i=0; $i -lt 4; $i++) {
            $entries += [ordered]@{name=$names[$i];row=1;column=$i;source='quantum-source.png';left=($i%2)*$w;top=[int][Math]::Floor($i/2)*$h;width=$w;height=$h;originalBytes=0}
        }
    } finally { $quantum.Dispose() }
    [ordered]@{tileSize=32;columns=4;rows=2;entries=$entries} | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $dir 'atlas.json') -Encoding UTF8
}
$manifest = Get-Content (Join-Path $dir 'atlas.json') -Raw | ConvertFrom-Json
$packed = New-Object Drawing.Bitmap 128,64
try {
    foreach ($entry in $manifest.entries) {
        $source=[Drawing.Bitmap]::FromFile((Join-Path $dir $entry.source))
        try { [ItemAtlasPixels]::Fit($source,$packed,$entry.left,$entry.top,$entry.width,$entry.height,($entry.column*32),($entry.row*32)) }
        finally { $source.Dispose() }
    }
    $packed.Save((Join-Path $dir 'atlas-32.png'),[Drawing.Imaging.ImageFormat]::Png)
    $preview = New-Object Drawing.Bitmap 768,384
    $g = [Drawing.Graphics]::FromImage($preview)
    try {
        $g.Clear([Drawing.Color]::FromArgb(255,70,75,85))
        $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
        $g.DrawImage($packed,(New-Object Drawing.Rectangle 0,0,768,384),(New-Object Drawing.Rectangle 0,0,128,64),[Drawing.GraphicsUnit]::Pixel)
        $preview.Save((Join-Path $dir 'preview-6x.png'),[Drawing.Imaging.ImageFormat]::Png)
    } finally { $g.Dispose(); $preview.Dispose() }
} finally { $packed.Dispose() }
& (Join-Path $PSScriptRoot 'pack_item_atlas.ps1')
