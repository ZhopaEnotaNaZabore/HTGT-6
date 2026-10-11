param([switch]$ImportOriginals)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = Split-Path $PSScriptRoot -Parent
$dir = Join-Path $root 'docs/assets/misc'
New-Item -ItemType Directory -Path $dir -Force | Out-Null
. (Join-Path $PSScriptRoot 'atlas_pixels.ps1')
if ($ImportOriginals) {
    if (Test-Path (Join-Path $dir 'atlas-source.png')) { throw 'Original misc atlas already exists.' }
    # Explicit addon-only allowlist: no backport items, tools or animated resources.
    $names=@('HullBatteryIC2','HullBatteryIC2_2','itemNanoCrystall','itemQuantumCrystall','NovaCrystall','Qeye',
        'basicMesh','advancedMesh','GlassTube','vacuumtube','Resistor','itemDiode','itemTransistor','itemInductiveCoil',
        'BoronGlassPane1','BoronGlassDust1','itemDustLapatron','FireClay','UnfFIreClyBrick',
        'itemDisplay','itemMonitor','RocketTerminalT1','ZPMsocket','ZPMCirc','UVCorc','ingotArcanite')
    $entries=@(); $left=0; $top=0; $rowHeight=0; $i=0
    foreach ($name in $names) {
        $file=Get-Item (Join-Path $root "src/main/resources/assets/htgt6/textures/items/$name.png")
        $im=[Drawing.Bitmap]::FromFile($file.FullName)
        try {
            if ($im.Width -gt 2048) { throw "Oversized input: $name" }
            if ($left+$im.Width -gt 2048) { $top+=$rowHeight; $left=0; $rowHeight=0 }
            $entries += [ordered]@{name=$name;row=[int][Math]::Floor($i/6);column=$i%6;left=$left;top=$top;sourceWidth=$im.Width;sourceHeight=$im.Height;originalBytes=$file.Length}
            $left+=$im.Width; $rowHeight=[Math]::Max($rowHeight,$im.Height); $i++
        } finally { $im.Dispose() }
    }
    $source=[Drawing.Bitmap]::new(2048,($top+$rowHeight))
    try {
        foreach ($entry in $entries) {
            $im=[Drawing.Bitmap]::FromFile((Join-Path $root "src/main/resources/assets/htgt6/textures/items/$($entry.name).png"))
            try { [ItemAtlasPixels]::Copy($im,$source,$entry.left,$entry.top) } finally { $im.Dispose() }
        }
        $source.Save((Join-Path $dir 'atlas-source.png'),[Drawing.Imaging.ImageFormat]::Png)
        [ordered]@{source='atlas-source.png';tileSize=64;columns=6;rows=5;entries=$entries} | ConvertTo-Json -Depth 5 | Set-Content (Join-Path $dir 'atlas.json') -Encoding UTF8
    } finally { $source.Dispose() }
}
$manifest=Get-Content (Join-Path $dir 'atlas.json') -Raw | ConvertFrom-Json
$source=[Drawing.Bitmap]::FromFile((Join-Path $dir $manifest.source))
$packed=[Drawing.Bitmap]::new(384,320)
try {
    foreach ($entry in $manifest.entries) {
        if ($entry.name -in @('Qeye','itemDustLapatron')) {
            $fixes=[Drawing.Bitmap]::FromFile((Join-Path $root 'docs/assets/engraved-chips/corrections-64.png'))
            try {
                $left=0; if ($entry.name -eq 'itemDustLapatron') { $left=64 }
                for($y=0;$y -lt 64;$y++){for($x=0;$x -lt 64;$x++){
                    $packed.SetPixel(($entry.column*64+$x),($entry.row*64+$y),$fixes.GetPixel(($left+$x),$y))
                }}
            } finally { $fixes.Dispose() }
        } elseif ($entry.name -in @('basicMesh','advancedMesh')) {
            # Dense wires need filtered reduction; nearest-neighbour drops entire strands.
            $g=[Drawing.Graphics]::FromImage($packed)
            $attributes=[Drawing.Imaging.ImageAttributes]::new()
            $mesh=$source.Clone([Drawing.Rectangle]::new($entry.left,$entry.top,$entry.sourceWidth,$entry.sourceHeight),[Drawing.Imaging.PixelFormat]::Format32bppArgb)
            try {
                $g.CompositingMode=[Drawing.Drawing2D.CompositingMode]::SourceCopy
                $g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
                $g.PixelOffsetMode=[Drawing.Drawing2D.PixelOffsetMode]::Half
                $attributes.SetWrapMode([Drawing.Drawing2D.WrapMode]::TileFlipXY)
                $rect=[Drawing.Rectangle]::new(($entry.column*64+4),($entry.row*64+4),56,56)
                $g.DrawImage($mesh,$rect,0,0,$mesh.Width,$mesh.Height,[Drawing.GraphicsUnit]::Pixel,$attributes)
            } finally { $mesh.Dispose(); $attributes.Dispose(); $g.Dispose() }
        } else {
            [ItemAtlasPixels]::FitSize($source,$packed,$entry.left,$entry.top,$entry.sourceWidth,$entry.sourceHeight,($entry.column*64),($entry.row*64),64)
        }
    }
    $packed.Save((Join-Path $dir 'atlas-64.png'),[Drawing.Imaging.ImageFormat]::Png)
    $preview=[Drawing.Bitmap]::new(1152,960); $g=[Drawing.Graphics]::FromImage($preview)
    try {
        $g.Clear([Drawing.Color]::FromArgb(255,50,55,62))
        $g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $g.PixelOffsetMode=[Drawing.Drawing2D.PixelOffsetMode]::Half
        $g.DrawImage($packed,[Drawing.Rectangle]::new(0,0,1152,960),[Drawing.Rectangle]::new(0,0,384,320),[Drawing.GraphicsUnit]::Pixel)
        $preview.Save((Join-Path $dir 'preview-3x.png'),[Drawing.Imaging.ImageFormat]::Png)
    } finally { $g.Dispose(); $preview.Dispose() }
} finally { $packed.Dispose(); $source.Dispose() }
& (Join-Path $PSScriptRoot 'pack_item_atlas.ps1')
