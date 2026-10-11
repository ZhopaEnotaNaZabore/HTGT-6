param([string]$Source = "$PSScriptRoot/../docs/assets/multiblock-refresh/atlas.png")
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$textureDir = Join-Path (Split-Path $PSScriptRoot -Parent) 'src/main/resources/assets/htgt6/textures/blocks'
$image = [System.Drawing.Bitmap]::FromFile((Resolve-Path -LiteralPath $Source))
try {
    $machines = @('mining','chemical','mixer','compressor','foundry','centrifuge')
    $roles = @('casing','rib','front','inner')
    # Measured horizontal dividers in the generated 1024x1536 sheet.
    $rows = @(0,240,480,720,960,1200,1536)
    for ($row=0; $row -lt 6; $row++) { for ($col=0; $col -lt 4; $col++) {
        $tile = New-Object System.Drawing.Bitmap 64,64
        $graphics = [System.Drawing.Graphics]::FromImage($tile)
        try {
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
            $sx = [int]($col * $image.Width / 4)
            $sy = [int]($rows[$row] * $image.Height / 1536)
            $sh = [int](($rows[$row+1]-$rows[$row]) * $image.Height / 1536)
            $src = New-Object System.Drawing.Rectangle ($sx+2),($sy+2),([int]($image.Width/4)-4),($sh-4)
            $dst = New-Object System.Drawing.Rectangle 0,0,64,64
            $graphics.DrawImage($image,$dst,$src,[System.Drawing.GraphicsUnit]::Pixel)
            $name = 'multiblock_v2_' + $machines[$row] + '_' + $roles[$col] + '.png'
            $tile.Save((Join-Path $textureDir $name),[System.Drawing.Imaging.ImageFormat]::Png)
        } finally { $graphics.Dispose(); $tile.Dispose() }
    } }
} finally { $image.Dispose() }
