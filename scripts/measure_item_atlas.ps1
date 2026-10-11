$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$doc = Join-Path $root 'docs/assets/item-atlas'
$runtime = Join-Path $root 'src/main/resources/assets/htgt6/textures/items/atlases'
$cells = Get-Content (Join-Path $doc 'cells.json') -Raw | ConvertFrom-Json
$bitmap = [Drawing.Bitmap]::FromFile((Join-Path $runtime 'electronics.png'))
$atlasSize = "$($bitmap.Width)x$($bitmap.Height)"
$separateBytes = 0L
try {
    foreach ($cell in $cells) {
        $tile = $bitmap.Clone((New-Object Drawing.Rectangle ($cell.column*32),($cell.row*32),$cell.size,$cell.size),[Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $stream = New-Object IO.MemoryStream
        try { $tile.Save($stream,[Drawing.Imaging.ImageFormat]::Png); $separateBytes += $stream.Length }
        finally { $stream.Dispose(); $tile.Dispose() }
    }
} finally { $bitmap.Dispose() }
$pngBytes = (Get-Item (Join-Path $runtime 'electronics.png')).Length
$indexBytes = (Get-Item (Join-Path $runtime 'electronics.properties')).Length
$after = $pngBytes + $indexBytes
$before = Get-Content (Join-Path $doc 'size-report-before-engraved-chips.json') -Raw | ConvertFrom-Json
$report = [ordered]@{
    icons=$cells.Count; gridCellSize=32; iconSizes=@(32,64); atlasSize=$atlasSize; runtimeFiles=2
    icons32=@($cells|Where-Object size -eq 32).Count; icons64=@($cells|Where-Object size -eq 64).Count
    pngBytes=$pngBytes; indexBytes=$indexBytes; atlasWithIndexBytes=$after
    sameIconsAsSeparatePngBytes=$separateBytes
    packingSavedBytes=$separateBytes-$after
    packingSavedPercent=[Math]::Round(100.0*($separateBytes-$after)/$separateBytes,2)
    previousAtlasBytes=$before.atlasWithIndexBytes
    atlasGrowthBytes=$after-$before.atlasWithIndexBytes
    change='12 ZPM-INFI circuit sprites at 32px: 3 replacements and 9 new items'
}
$report | ConvertTo-Json | Set-Content (Join-Path $doc 'size-report.json') -Encoding UTF8
$report | ConvertTo-Json
