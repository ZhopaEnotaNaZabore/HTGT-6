param([switch]$ImportOriginals)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $root 'docs/assets/coins'
New-Item -ItemType Directory -Path $dir -Force | Out-Null
. (Join-Path $PSScriptRoot 'atlas_pixels.ps1')
if ($ImportOriginals) {
    if (Test-Path (Join-Path $dir 'atlas-source.png')) { throw 'Original source atlas already exists; refusing overwrite.' }
    $source = New-Object Drawing.Bitmap 2500,1500
    $entries = @()
    try {
        $names = @('BeeCoin','coinExplorer','coinBlackSmith','coinFarmer','coinSteamPunk','coinWizardy','coinSlayer','GymMCCoin','coinSpaceTraveler','coinChemicalist','coinTech','coinChunkload12h','AE2coin','DEcoin','coinInfinity')
        for ($i=0; $i -lt $names.Count; $i++) {
            $name=$names[$i]; $column=$i%5; $row=[int][Math]::Floor($i/5)
            $file = Get-Item (Join-Path $root "src/main/resources/assets/htgt6/textures/items/$name.png")
            $original = [Drawing.Bitmap]::FromFile($file.FullName)
            try {
                if ($original.Width -gt 500 -or $original.Height -gt 500) { throw "Oversized source: $name" }
                [ItemAtlasPixels]::Copy($original,$source,($column*500),($row*500))
                $entries += [ordered]@{name=$name;row=$row;column=$column;sourceWidth=$original.Width;sourceHeight=$original.Height;originalBytes=$file.Length}
            } finally { $original.Dispose() }
        }
        $source.Save((Join-Path $dir 'atlas-source.png'),[Drawing.Imaging.ImageFormat]::Png)
        [ordered]@{source='atlas-source.png';tileSize=64;columns=5;rows=3;entries=$entries} | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $dir 'atlas.json') -Encoding UTF8
    } finally { $source.Dispose() }
}
$manifest = Get-Content (Join-Path $dir 'atlas.json') -Raw | ConvertFrom-Json
$manifest.tileSize = 64
$manifest | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $dir 'atlas.json') -Encoding UTF8
$source = [Drawing.Bitmap]::FromFile((Join-Path $dir $manifest.source))
$packed = New-Object Drawing.Bitmap 320,192
try {
    foreach ($entry in $manifest.entries) {
        if ($entry.name -eq 'coinInfinity') {
            $infinity=[Drawing.Bitmap]::FromFile((Join-Path $dir 'infinity-source.png'))
            try { [ItemAtlasPixels]::FitSize($infinity,$packed,0,0,$infinity.Width,$infinity.Height,($entry.column*64),($entry.row*64),64) }
            finally { $infinity.Dispose() }
        } else {
            [ItemAtlasPixels]::FitSize($source,$packed,($entry.column*500),($entry.row*500),$entry.sourceWidth,$entry.sourceHeight,($entry.column*64),($entry.row*64),64)
        }
    }
    $packed.Save((Join-Path $dir 'atlas-64.png'),[Drawing.Imaging.ImageFormat]::Png)
    $preview = New-Object Drawing.Bitmap 960,576
    $g = [Drawing.Graphics]::FromImage($preview)
    try {
        $g.Clear([Drawing.Color]::FromArgb(255,36,40,46))
        $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
        $g.DrawImage($packed,(New-Object Drawing.Rectangle 0,0,960,576),(New-Object Drawing.Rectangle 0,0,320,192),[Drawing.GraphicsUnit]::Pixel)
        $preview.Save((Join-Path $dir 'preview-3x.png'),[Drawing.Imaging.ImageFormat]::Png)
    } finally { $g.Dispose(); $preview.Dispose() }
} finally { $source.Dispose(); $packed.Dispose() }
& (Join-Path $PSScriptRoot 'pack_item_atlas.ps1')
