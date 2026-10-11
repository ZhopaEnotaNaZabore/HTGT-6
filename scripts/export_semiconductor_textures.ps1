param([string]$AssetDirectory = 'docs/assets/semiconductors')
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$assetDir = Join-Path $root $AssetDirectory
$manifest = Get-Content -LiteralPath (Join-Path $assetDir 'atlas.json') -Raw | ConvertFrom-Json
$tileSize = [int]$manifest.tileSize
if ($tileSize -notin @(32,64)) { throw 'Unsupported tile size' }
$source = [System.Drawing.Bitmap]::FromFile((Join-Path $assetDir $manifest.source))
$packedWidth = [int]$manifest.columns * $tileSize
$packedHeight = [int]$manifest.rows * $tileSize
$packed = New-Object System.Drawing.Bitmap $packedWidth,$packedHeight
try {
    if ($source.Width -ne $manifest.sourceWidth -or $source.Height -ne $manifest.sourceHeight) {
        throw 'Source size differs from reviewed atlas.json crop coordinates.'
    }
    foreach ($entry in $manifest.entries) {
        $left = [int]$manifest.columnBoundaries[$entry.column]
        $right = [int]$manifest.columnBoundaries[$entry.column + 1]
        $top = [int]$manifest.rowBoundaries[$entry.row]
        $bottom = [int]$manifest.rowBoundaries[$entry.row + 1]
        $minX = $right; $minY = $bottom; $maxX = -1; $maxY = -1
        for ($y = $top; $y -lt $bottom; $y++) {
            for ($x = $left; $x -lt $right; $x++) {
                if ($source.GetPixel($x, $y).A -lt 128) { continue }
                $minX = [Math]::Min($minX, $x); $maxX = [Math]::Max($maxX, $x)
                $minY = [Math]::Min($minY, $y); $maxY = [Math]::Max($maxY, $y)
            }
        }
        if ($maxX -lt 0) { throw "Empty atlas cell: $($entry.name)" }
        $width = $maxX - $minX + 1; $height = $maxY - $minY + 1
        # Retain a transparent margin proportional to icon size (2px or 4px).
        $scale = ($tileSize * 0.875) / [Math]::Max($width, $height)
        $outWidth = [int][Math]::Round($width * $scale)
        $outHeight = [int][Math]::Round($height * $scale)
        $tile = New-Object System.Drawing.Bitmap $tileSize,$tileSize
        $graphics = [System.Drawing.Graphics]::FromImage($tile)
        try {
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
            $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
            $srcRect = New-Object System.Drawing.Rectangle $minX,$minY,$width,$height
            $dstRect = New-Object System.Drawing.Rectangle ([int][Math]::Floor(($tileSize-$outWidth)/2)),([int][Math]::Floor(($tileSize-$outHeight)/2)),$outWidth,$outHeight
            $graphics.DrawImage($source,$dstRect,$srcRect,[System.Drawing.GraphicsUnit]::Pixel)
            # Copy RGBA exactly: GDI+ compositing can round colours at translucent edges.
            for ($copyY = 0; $copyY -lt $tileSize; $copyY++) {
                for ($copyX = 0; $copyX -lt $tileSize; $copyX++) {
                    $packed.SetPixel(([int]$entry.column * $tileSize + $copyX),([int]$entry.row * $tileSize + $copyY),$tile.GetPixel($copyX,$copyY))
                }
            }
        } finally { $graphics.Dispose(); $tile.Dispose() }
    }
    $packed.Save((Join-Path $assetDir "atlas-$tileSize.png"),[System.Drawing.Imaging.ImageFormat]::Png)
    $previewScale = [int](192 / $tileSize)
    $previewWidth = $packedWidth * $previewScale
    $previewHeight = $packedHeight * $previewScale
    $preview = New-Object System.Drawing.Bitmap $previewWidth,$previewHeight
    $previewGraphics = [System.Drawing.Graphics]::FromImage($preview)
    try {
        $previewGraphics.Clear([System.Drawing.Color]::FromArgb(255,36,40,46))
        $previewGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $previewGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $previewGraphics.DrawImage($packed,(New-Object System.Drawing.Rectangle 0,0,$previewWidth,$previewHeight),(New-Object System.Drawing.Rectangle 0,0,$packedWidth,$packedHeight),[System.Drawing.GraphicsUnit]::Pixel)
        $preview.Save((Join-Path $assetDir "preview-${previewScale}x.png"),[System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $previewGraphics.Dispose(); $preview.Dispose() }
    Write-Output "Packed $($manifest.entries.Count) cells at ${tileSize}x${tileSize} into atlas-$tileSize.png (no individual PNGs)."
} finally { $packed.Dispose(); $source.Dispose() }
& (Join-Path $PSScriptRoot 'pack_item_atlas.ps1')
