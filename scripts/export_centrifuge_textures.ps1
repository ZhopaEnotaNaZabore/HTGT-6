param([Parameter(Mandatory=$true)][string]$Source)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$textureDir = Join-Path $root 'src/main/resources/assets/htgt6/textures/blocks'
$image = [System.Drawing.Bitmap]::FromFile((Resolve-Path -LiteralPath $Source))
try {
    $names = @('industrial_centrifuge_casing','industrial_centrifuge_rib','industrial_centrifuge_controller_front','industrial_centrifuge_rotor')
    for ($i = 0; $i -lt 4; $i++) {
        $tile = New-Object System.Drawing.Bitmap 64,64
        $graphics = [System.Drawing.Graphics]::FromImage($tile)
        try {
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
            $src = New-Object System.Drawing.Rectangle ([int](($i % 2) * $image.Width / 2)),([int]([Math]::Floor($i / 2) * $image.Height / 2)),([int]($image.Width / 2)),([int]($image.Height / 2))
            $dst = New-Object System.Drawing.Rectangle 0,0,64,64
            $graphics.DrawImage($image,$dst,$src,[System.Drawing.GraphicsUnit]::Pixel)
            $tile.Save((Join-Path $textureDir ($names[$i] + '.png')),[System.Drawing.Imaging.ImageFormat]::Png)
        } finally { $graphics.Dispose(); $tile.Dispose() }
    }
} finally { $image.Dispose() }
