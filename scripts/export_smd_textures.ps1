param([switch]$ImportOriginals)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $root 'docs/assets/smd'
New-Item -ItemType Directory -Path $dir -Force | Out-Null
. (Join-Path $PSScriptRoot 'atlas_pixels.ps1')
if ($ImportOriginals) {
    if (Test-Path (Join-Path $dir 'atlas-source.png')) { throw 'Original source atlas already exists; refusing overwrite.' }
    $source = New-Object Drawing.Bitmap 2500,1500
    $entries = @()
    try {
        $row=0
        foreach ($tier in @('','ADV','OP')) {
            $column=0
            foreach ($part in @('Resistor','diode','Transistor','Capacitor','InductiveCoil')) {
                $name = "item${tier}SMD${part}"
                $file = Get-Item (Join-Path $root "src/main/resources/assets/htgt6/textures/items/$name.png")
                $original = [Drawing.Bitmap]::FromFile($file.FullName)
                try {
                    if ($original.Width -gt 500 -or $original.Height -gt 500) { throw "Oversized source: $name" }
                    [ItemAtlasPixels]::Copy($original,$source,($column*500),($row*500))
                    $entries += [ordered]@{name=$name;row=$row;column=$column;sourceWidth=$original.Width;sourceHeight=$original.Height;originalBytes=$file.Length}
                } finally { $original.Dispose() }
                $column++
            }
            $row++
        }
        $source.Save((Join-Path $dir 'atlas-source.png'),[Drawing.Imaging.ImageFormat]::Png)
        [ordered]@{source='atlas-source.png';tileSize=32;columns=5;rows=3;entries=$entries} | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $dir 'atlas.json') -Encoding UTF8
    } finally { $source.Dispose() }
}
$manifest = Get-Content (Join-Path $dir 'atlas.json') -Raw | ConvertFrom-Json
$source = [Drawing.Bitmap]::FromFile((Join-Path $dir $manifest.source))
$packed = New-Object Drawing.Bitmap 160,96
try {
    foreach ($entry in $manifest.entries) {
        [ItemAtlasPixels]::Fit($source,$packed,($entry.column*500),($entry.row*500),$entry.sourceWidth,$entry.sourceHeight,($entry.column*32),($entry.row*32))
    }
    $packed.Save((Join-Path $dir 'atlas-32.png'),[Drawing.Imaging.ImageFormat]::Png)
    $preview = New-Object Drawing.Bitmap 960,576
    $g = [Drawing.Graphics]::FromImage($preview)
    try {
        $g.Clear([Drawing.Color]::FromArgb(255,36,40,46))
        $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
        $g.DrawImage($packed,(New-Object Drawing.Rectangle 0,0,960,576),(New-Object Drawing.Rectangle 0,0,160,96),[Drawing.GraphicsUnit]::Pixel)
        $preview.Save((Join-Path $dir 'preview-6x.png'),[Drawing.Imaging.ImageFormat]::Png)
    } finally { $g.Dispose(); $preview.Dispose() }
} finally { $source.Dispose(); $packed.Dispose() }
& (Join-Path $PSScriptRoot 'pack_item_atlas.ps1')
