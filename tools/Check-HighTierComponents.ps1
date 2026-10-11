$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
$root=Split-Path $PSScriptRoot -Parent
$jar=Get-ChildItem (Join-Path $root 'libs') -Filter '*gregtech*.jar' | Select-Object -First 1
$archive=[IO.Compression.ZipFile]::OpenRead($jar.FullName)
$components=[ordered]@{electric_motor=12010;electric_piston=12070;electric_pump=12030;conveyor_module=12050;robot_arm=12090;emitter=12130;sensor=12150;field_generator=12110}
$tiers=@('ux','olv','omv','ohv','oev','oiv','max')
$preview=New-Object Drawing.Bitmap 768,768
$g=[Drawing.Graphics]::FromImage($preview)
$g.Clear([Drawing.Color]::FromArgb(255,40,40,48));$g.InterpolationMode=[Drawing.Drawing2D.InterpolationMode]::NearestNeighbor;$g.PixelOffsetMode=[Drawing.Drawing2D.PixelOffsetMode]::Half
$checked=0;$col=0;$rows=@()
try {
    foreach($component in $components.GetEnumerator()) {
        $entry=$archive.GetEntry("assets/gregtech/textures/items/gt.multiitem.technological/$($component.Value).png")
        $stream=$entry.Open();$mask=[Drawing.Bitmap]::FromStream($stream)
        try {
            $g.DrawImage($mask,[Drawing.Rectangle]::new($col*96,0,96,96),[Drawing.Rectangle]::new(0,0,16,16),[Drawing.GraphicsUnit]::Pixel)
            $row=1
            foreach($tier in $tiers) {
                $path=Join-Path $root "src/main/resources/assets/htgt6/textures/items/high_tier_components/$tier/$($component.Key).png"
                $image=[Drawing.Bitmap]::FromFile($path)
                try {
                    if($image.Width -ne 16 -or $image.Height -ne 256){throw "Unexpected frame size: $path"}
                    $metadata=Get-Content ($path+'.mcmeta') -Raw|ConvertFrom-Json
                    if(!$metadata.animation.interpolate){throw 'Animation interpolation missing'}
                    if($tier -eq 'ux') {
                        $times=@(3,3,3,2,2,30,30,30,30,30,30,30,2,2,3,3)
                        if($metadata.animation.frames.Count -ne 16 -or $metadata.animation.frametime -ne 30){throw 'Infinity animation sequence missing'}
                        for($i=0;$i -lt 16;$i++){if($metadata.animation.frames[$i].index -ne $i -or $metadata.animation.frames[$i].time -ne $times[$i]){throw 'Infinity timing differs from Avaritia'}}
                    } elseif($metadata.animation.frametime -ne 2){throw 'Animation settings changed'}
                    for($frame=0;$frame -lt 16;$frame++){for($y=0;$y -lt 16;$y++){for($x=0;$x -lt 16;$x++){
                        $a=$mask.GetPixel($x,$y);$b=$image.GetPixel($x,($y+$frame*16))
                        if($a.A -ne $b.A){throw "Silhouette changed: $tier/$($component.Key) frame $frame"}
                        $high=[Math]::Max($a.R,[Math]::Max($a.G,$a.B));$low=[Math]::Min($a.R,[Math]::Min($a.G,$a.B))
                        if(($a.A -gt 0 -and ($high -lt 64 -or $high-$low -gt 40)) -and $a.ToArgb() -ne $b.ToArgb()){throw 'Mechanical detail or coloured accent changed'}
                    }}}
                    $g.DrawImage($image,[Drawing.Rectangle]::new($col*96,$row*96,96,96),[Drawing.Rectangle]::new(0,0,16,16),[Drawing.GraphicsUnit]::Pixel)
                    $rows+=[pscustomobject]@{tier=$tier;component=$component.Key;bytes=(Get-Item $path).Length;frames=16}
                    $checked++;$row++
                } finally {$image.Dispose()}
            }
        } finally {$mask.Dispose();$stream.Dispose()}
        $col++
    }
    $preview.Save((Join-Path $root 'docs/assets/high-tier-components/after.png'))
    $rows|ConvertTo-Json|Set-Content (Join-Path $root 'docs/assets/high-tier-components/after-files.json')
} finally {$g.Dispose();$preview.Dispose();$archive.Dispose()}
Write-Output "PASS: $checked components, 896 frames; exact GT6 alpha, preserved outlines/contacts/coloured accents and animation metadata."
