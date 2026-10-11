# Normalize the generated 4x2 sprite sheet into eight 32x32 atlas cells.
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$source = [Drawing.Image]::FromFile((Join-Path $root 'ToDo/OreHelper/microplanets-source.png'))
$atlas = New-Object Drawing.Bitmap 256,32
$graphics = [Drawing.Graphics]::FromImage($atlas)
$graphics.InterpolationMode = [Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$graphics.PixelOffsetMode = [Drawing.Drawing2D.PixelOffsetMode]::Half
try {
 $cw = [int]($source.Width/4); $ch = [int]($source.Height/2)
 for ($i=0; $i -lt 8; $i++) {
  $graphics.DrawImage($source,[Drawing.Rectangle]::new($i*32,0,32,32),($i%4)*$cw,[Math]::Floor($i/4)*$ch,$cw,$ch,[Drawing.GraphicsUnit]::Pixel)
 }
 $atlas.Save((Join-Path $root 'src/main/resources/assets/htgt6/textures/gui/orehelper/microplanets.png'),[Drawing.Imaging.ImageFormat]::Png)
} finally { $graphics.Dispose(); $atlas.Dispose(); $source.Dispose() }
