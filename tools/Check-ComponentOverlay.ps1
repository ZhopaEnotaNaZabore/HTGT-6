$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$refs=@('System.Drawing')
if($PSVersionTable.PSEdition -eq 'Core'){$refs=@('System.Drawing.Common','System.Drawing.Primitives','System.Runtime')}
Add-Type -Path (Join-Path $PSScriptRoot 'ComponentTextureComposer.cs') -ReferencedAssemblies $refs
$mask=New-Object Drawing.Bitmap 16,16
$first=New-Object Drawing.Bitmap 16,16
$second=New-Object Drawing.Bitmap 16,16
try {
    # Same colour distribution in each band, but different spatial patterns.
    # A palette-only implementation must fail this check.
    for($y=0;$y -lt 16;$y++){for($x=0;$x -lt 16;$x++){
        $mask.SetPixel($x,$y,[Drawing.Color]::FromArgb(255,140,140,140))
        $a=[Drawing.Color]::Blue;$b=[Drawing.Color]::Blue
        if($y%2 -eq 0){$a=[Drawing.Color]::White}
        if([int][Math]::Floor($y/2)%2 -eq 0){$b=[Drawing.Color]::White}
        $first.SetPixel($x,$y,$a);$second.SetPixel($x,$y,$b)
    }}
    $a=[ComponentTextureComposer]::Compose($mask,[Drawing.Bitmap[]]@($first),$false)
    $b=[ComponentTextureComposer]::Compose($mask,[Drawing.Bitmap[]]@($second),$false)
    try{
        $different=0
        for($y=0;$y -lt 16;$y++){for($x=0;$x -lt 16;$x++){if($a.GetPixel($x,$y).ToArgb() -ne $b.GetPixel($x,$y).ToArgb()){$different++}}}
        if($different -eq 0){throw 'Material pattern was reduced to a palette'}
    }finally{$a.Dispose();$b.Dispose()}
    $dir=Join-Path (Split-Path $PSScriptRoot -Parent) 'src/main/resources/assets/htgt6/textures/material_templates/ux_infinity'
    $frames=@(Get-ChildItem $dir -Filter *.png|Sort-Object Name|ForEach-Object{[Drawing.Bitmap]::FromFile($_.FullName)})
    try{
        $rect=[ComponentTextureComposer]::Interior([Drawing.Bitmap[]]$frames)
        Write-Output "PASS: spatial material patterns retained; Infinity uses opaque interior $rect across all nine Avaritia source frames."
    }finally{$frames|ForEach-Object{$_.Dispose()}}
}finally{$mask.Dispose();$first.Dispose();$second.Dispose()}
