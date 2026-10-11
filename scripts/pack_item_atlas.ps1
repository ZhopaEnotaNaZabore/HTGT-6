$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$target = Join-Path $root 'src/main/resources/assets/htgt6/textures/items/atlases'
New-Item -ItemType Directory -Path $target -Force | Out-Null
$groups = @('semiconductors','crystal-substrates','coils','smd','coins','armor-blanks','tier-modules','rocket-composites','misc','engraved-chips','circuit-tiers')
$occupied = [Collections.Generic.HashSet[int]]::new()
$rows = 0
# Allocate a bounded scratch sheet; only occupied rows are saved.
$packed = New-Object Drawing.Bitmap 256,2048
$index = [Collections.Generic.List[string]]::new()
$index.Add('tileSize=32'); $index.Add('columns=8')
$cells = [Collections.Generic.List[object]]::new()
$seen = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
try {
    foreach ($group in $groups) {
        $dir = Join-Path $root ('docs/assets/' + $group)
        $manifest = Get-Content (Join-Path $dir 'atlas.json') -Raw | ConvertFrom-Json
        $size = [int]$manifest.tileSize
        if ($size -notin @(32,64)) { throw "Unsupported tile size: $size" }
        $span = [int]($size/32)
        $sheet = [Drawing.Bitmap]::FromFile((Join-Path $dir "atlas-$size.png"))
        try {
            foreach ($entry in $manifest.entries) {
                if ($group -eq 'misc' -and $entry.name -in @('ZPMsocket','ZPMCirc','UVCorc')) { continue }
                if (!$seen.Add($entry.name)) { throw "Duplicate atlas icon: $($entry.name)" }
                $cell = -1
                for ($candidate=0; $candidate -lt 512; $candidate++) {
                    $column=$candidate%8; $row=[int][Math]::Floor($candidate/8)
                    if ($column+$span -gt 8 -or $row+$span -gt 64) { continue }
                    $free=$true
                    for ($dy=0; $dy -lt $span; $dy++) { for ($dx=0; $dx -lt $span; $dx++) {
                        if ($occupied.Contains($candidate+$dy*8+$dx)) { $free=$false }
                    } }
                    if ($free) { $cell=$candidate; break }
                }
                if ($cell -lt 0) { throw 'Atlas capacity exceeded' }
                for ($dy=0; $dy -lt $span; $dy++) { for ($dx=0; $dx -lt $span; $dx++) {
                    [void]$occupied.Add($cell+$dy*8+$dx)
                } }
                $rows=[Math]::Max($rows,([int][Math]::Floor($cell/8)+$span))
                for ($y=0; $y -lt $size; $y++) { for ($x=0; $x -lt $size; $x++) {
                    $pixel = $sheet.GetPixel(([int]$entry.column*$size+$x),([int]$entry.row*$size+$y))
                    $packed.SetPixel((($cell%8)*32+$x),([int][Math]::Floor($cell/8)*32+$y),$pixel)
                } }
                $index.Add('item.' + $entry.name + '=' + $cell)
                if ($size -ne 32) { $index.Add('size.' + $entry.name + '=' + $size) }
                $cells.Add([pscustomobject]@{name=$entry.name;group=$group;cell=$cell;column=$cell%8;row=[int][Math]::Floor($cell/8);size=$size})
            }
        } finally { $sheet.Dispose() }
    }
    $height=$rows*32
    $cropped=$packed.Clone((New-Object Drawing.Rectangle 0,0,256,$height),[Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try { $cropped.Save((Join-Path $target 'electronics.png'),[Drawing.Imaging.ImageFormat]::Png) }
    finally { $cropped.Dispose() }
    $index.Add("rows=$rows")
    [IO.File]::WriteAllLines((Join-Path $target 'electronics.properties'),$index,[Text.Encoding]::ASCII)
    $docDir = Join-Path $root 'docs/assets/item-atlas'
    New-Item -ItemType Directory -Path $docDir -Force | Out-Null
    $cells | ConvertTo-Json | Set-Content (Join-Path $docDir 'cells.json') -Encoding UTF8
    $preview = New-Object Drawing.Bitmap 1024,($height*4)
    $g = [Drawing.Graphics]::FromImage($preview)
    try {
        $g.Clear([Drawing.Color]::FromArgb(255,36,40,46))
        $g.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $g.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
        $g.DrawImage($packed,(New-Object Drawing.Rectangle 0,0,1024,($height*4)),(New-Object Drawing.Rectangle 0,0,256,$height),[Drawing.GraphicsUnit]::Pixel)
        $preview.Save((Join-Path $docDir 'preview-4x.png'),[Drawing.Imaging.ImageFormat]::Png)
    } finally { $g.Dispose(); $preview.Dispose() }
    Write-Output "Packed $($cells.Count) icons into electronics.png with a runtime cell index."
} finally { $packed.Dispose() }
