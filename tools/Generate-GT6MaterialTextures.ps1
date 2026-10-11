param(
    [string] $ProjectRoot = (Split-Path -Parent $PSScriptRoot),
    [string] $PythonExecutable = 'python',
    [switch] $ComponentsOnly
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.IO.Compression.FileSystem
if (-not ('ComponentTextureComposer' -as [type])) {
    $drawingReferences = @('System.Drawing')
    if ($PSVersionTable.PSEdition -eq 'Core') {
        $drawingReferences = @('System.Drawing.Common','System.Drawing.Primitives','System.Runtime')
    }
    Add-Type -Path (Join-Path $PSScriptRoot 'ComponentTextureComposer.cs') -ReferencedAssemblies $drawingReferences
}

$gtJar = Get-ChildItem -LiteralPath (Join-Path $ProjectRoot 'libs') -Filter '*gregtech*.jar' |
    Select-Object -First 1 -ExpandProperty FullName
if (-not $gtJar) { throw 'GregTech jar was not found in libs.' }

$assetRoot = Join-Path $ProjectRoot 'src/main/resources/assets/htgt6/textures'
$deepslate = Join-Path $assetRoot 'blocks/1_20_backport/deepslate.png'
$templateRoot = Join-Path $assetRoot 'material_templates'
$bedrock = Join-Path $templateRoot 'bedrock.png'
$etherniumTemplate = Join-Path $templateRoot 'ethernium.png'
$transcendentTemplate = Join-Path $templateRoot 'transcendent_alloy.png'
$stableTimeSpaceTemplate = Join-Path $templateRoot 'stable_time_space.png'
$oivTemplate = Join-Path $templateRoot 'oiv_rift.png'
$maxTemplate = Join-Path $templateRoot 'max_energy.png'
$uxInfinityFrames = Join-Path $templateRoot 'ux_infinity'

function Open-Bitmap([System.IO.Stream] $stream) {
    $temporary = [System.Drawing.Bitmap]::FromStream($stream)
    try { return [System.Drawing.Bitmap]::new($temporary) } finally { $temporary.Dispose() }
}

function Open-FileBitmap([string] $path) {
    $temporary = [System.Drawing.Bitmap]::FromFile($path)
    try { return [System.Drawing.Bitmap]::new($temporary) } finally { $temporary.Dispose() }
}

function Get-PatternFrames([string] $path) {
    $image = Open-FileBitmap $path
    $frameSize = $image.Width
    $count = [Math]::Max(1, [int]($image.Height / $frameSize))
    $frames = @()
    for ($frame = 0; $frame -lt $count; $frame++) {
        $piece = [System.Drawing.Bitmap]::new($frameSize, $frameSize)
        $graphics = [System.Drawing.Graphics]::FromImage($piece)
        try {
            $graphics.DrawImage($image,
                    [System.Drawing.Rectangle]::new(0, 0, $frameSize, $frameSize),
                    [System.Drawing.Rectangle]::new(0, $frame * $frameSize, $frameSize, $frameSize),
                    [System.Drawing.GraphicsUnit]::Pixel)
        } finally { $graphics.Dispose() }
        $frames += $piece
    }
    $image.Dispose()
    return ,$frames
}

function Get-BounceFramesFromDirectory([string] $path) {
    $files = Get-ChildItem -LiteralPath $path -Filter '*.png' | Sort-Object Name
    if ($files.Count -eq 0) { throw "No animation frames found in $path" }
    $frames = @()
    foreach ($file in $files) {
        $frame = Open-FileBitmap $file.FullName
        if ($frame.Width -gt 64 -or $frame.Height -gt 64) {
            $frame.Dispose()
            throw "Material template must not exceed 64x64: $($file.FullName)"
        }
        $frames += $frame
    }
    for ($index = $frames.Count - 2; $index -ge 1; $index--) {
        $frames += [System.Drawing.Bitmap]::new($frames[$index])
    }
    return ,$frames
}

function Convert-Hue([System.Drawing.Color] $color, [double] $angle) {
    $cos = [Math]::Cos($angle)
    $sin = [Math]::Sin($angle)
    $red = ($color.R * (0.213 + $cos * 0.787 - $sin * 0.213)) +
            ($color.G * (0.715 - $cos * 0.715 - $sin * 0.715)) +
            ($color.B * (0.072 - $cos * 0.072 + $sin * 0.928))
    $green = ($color.R * (0.213 - $cos * 0.213 + $sin * 0.143)) +
              ($color.G * (0.715 + $cos * 0.285 + $sin * 0.140)) +
              ($color.B * (0.072 - $cos * 0.072 - $sin * 0.283))
    $blue = ($color.R * (0.213 - $cos * 0.213 - $sin * 0.787)) +
             ($color.G * (0.715 - $cos * 0.715 + $sin * 0.715)) +
             ($color.B * (0.072 + $cos * 0.928 + $sin * 0.072))
    return [System.Drawing.Color]::FromArgb($color.A,
            [Math]::Max(0, [Math]::Min(255, [int]$red)),
            [Math]::Max(0, [Math]::Min(255, [int]$green)),
            [Math]::Max(0, [Math]::Min(255, [int]$blue)))
}

function Get-AnimatedPatternFrames([string] $path, [string] $style, [int] $count = 16) {
    $source = Open-FileBitmap $path
    try {
        if ($source.Width -gt 64 -or $source.Height -gt 64) {
            throw "Material template must not exceed 64x64: $path"
        }
        $frames = @()
        for ($frame = 0; $frame -lt $count; $frame++) {
            $phase = 2.0 * [Math]::PI * $frame / $count
            $piece = [System.Drawing.Bitmap]::new($source.Width, $source.Height)
            for ($y = 0; $y -lt $source.Height; $y++) {
                for ($x = 0; $x -lt $source.Width; $x++) {
                    switch ($style) {
                        'ethernium' {
                            $sx = ($x + 4 * $frame) % $source.Width
                            $sy = ($y + 2 * $frame) % $source.Height
                            $pixel = Convert-Hue $source.GetPixel($sx, $sy) $phase
                        }
                        'transcendent' {
                            $sx = ($x + 4 * $frame) % $source.Width
                            $pixel = $source.GetPixel($sx, $y)
                            $luma = ($pixel.R + $pixel.G + $pixel.B) / 765.0
                            $twinkle = 0.82 + 0.18 * [Math]::Sin($phase + $x * 0.55 + $y * 0.37)
                            $starBoost = 1.0 + $luma * (0.30 + 0.25 * [Math]::Sin($phase * 2.0 + $x))
                            $pixel = [System.Drawing.Color]::FromArgb($pixel.A,
                                    [Math]::Min(255, [int]($pixel.R * $twinkle * $starBoost)),
                                    [Math]::Min(255, [int]($pixel.G * $twinkle * $starBoost)),
                                    [Math]::Min(255, [int]($pixel.B * (0.92 + 0.08 * $twinkle) * $starBoost)))
                        }
                        'spacetime' {
                            $sx = ($x + 4 * $frame) % $source.Width
                            $sy = ($y + [int](4 * (1.0 + [Math]::Sin($phase)))) % $source.Height
                            $pixel = $source.GetPixel($sx, $sy)
                            $pulse = 0.68 + 0.32 * (0.5 + 0.5 * [Math]::Sin($phase))
                            $pixel = [System.Drawing.Color]::FromArgb($pixel.A,
                                    [Math]::Min(255, [int]($pixel.R * $pulse)),
                                    [Math]::Min(255, [int]($pixel.G * (0.82 + 0.25 * $pulse))),
                                    [Math]::Min(255, [int]($pixel.B * (0.90 + 0.18 * $pulse))))
                        }
                        'bedrock' {
                            $pixel = $source.GetPixel($x, $y)
                            $pulse = 0.78 + 0.22 * (0.5 + 0.5 * [Math]::Sin($phase + $x * 0.32 - $y * 0.27))
                            $pixel = [System.Drawing.Color]::FromArgb($pixel.A,
                                    [Math]::Min(255, [int]($pixel.R * $pulse + 10 * (1.0 - $pulse))),
                                    [Math]::Min(255, [int]($pixel.G * $pulse + 8 * (1.0 - $pulse))),
                                    [Math]::Min(255, [int]($pixel.B * $pulse + 14 * (1.0 - $pulse))))
                        }
                        'oiv' {
                            $sx = ($x + 4 * $frame) % $source.Width
                            $sy = ($y + 2 * $frame) % $source.Height
                            $pixel = $source.GetPixel($sx, $sy)
                            $pulse = 0.72 + 0.28 * (0.5 + 0.5 * [Math]::Sin($phase * 2.0 + $x * 0.18))
                            $pixel = [System.Drawing.Color]::FromArgb($pixel.A,
                                    [Math]::Min(255, [int]($pixel.R * (0.90 + 0.25 * $pulse))),
                                    [Math]::Min(255, [int]($pixel.G * $pulse)),
                                    [Math]::Min(255, [int]($pixel.B * (0.85 + 0.25 * $pulse))))
                        }
                        'max' {
                            $sx = ($x + 4 * $frame) % $source.Width
                            $pixel = Convert-Hue $source.GetPixel($sx, $y) ([Math]::Sin($phase) * 0.22)
                            $pulse = 0.80 + 0.20 * (0.5 + 0.5 * [Math]::Sin($phase))
                            $pixel = [System.Drawing.Color]::FromArgb($pixel.A,
                                    [Math]::Min(255, [int]($pixel.R * (0.95 + 0.10 * $pulse))),
                                    [Math]::Min(255, [int]($pixel.G * (0.90 + 0.15 * $pulse))),
                                    [Math]::Min(255, [int]($pixel.B * (0.88 + 0.18 * $pulse))))
                        }
                        default { throw "Unknown animation style: $style" }
                    }
                    $piece.SetPixel($x, $y, $pixel)
                }
            }
            $frames += $piece
        }
        return ,$frames
    } finally { $source.Dispose() }
}

function Convert-PatternColor([System.Drawing.Color] $color, [string] $mode) {
    switch ($mode) {
        'star' {
            return [System.Drawing.Color]::FromArgb($color.A,
                    [Math]::Min(255, [int]($color.B * 1.15 + 35)),
                    [Math]::Min(255, [int]($color.R * 0.75 + 20)),
                    [Math]::Min(255, [int]($color.G * 1.20 + 45)))
        }
        'cosmos' {
            return [System.Drawing.Color]::FromArgb($color.A,
                    [Math]::Min(255, [int]($color.R * 0.45 + 12)),
                    [Math]::Min(255, [int]($color.B * 0.85 + 24)),
                    [Math]::Min(255, [int]($color.G * 1.35 + 50)))
        }
        default { return $color }
    }
}

function Write-MaskedTexture([System.Drawing.Bitmap] $mask, [System.Drawing.Bitmap[]] $patterns,
        [string] $destination, [string] $mode, [bool] $animated) {
    $width = $mask.Width
    $height = [Math]::Min($mask.Height, $mask.Width)
    $frameCount = if ($animated) { $patterns.Count } else { 1 }
    $output = [System.Drawing.Bitmap]::new($width, $height * $frameCount)
    try {
        for ($frame = 0; $frame -lt $frameCount; $frame++) {
            $pattern = $patterns[$frame % $patterns.Count]
            for ($y = 0; $y -lt $height; $y++) {
                for ($x = 0; $x -lt $width; $x++) {
                    $maskPixel = $mask.GetPixel($x, $y)
                    $px = [Math]::Min($pattern.Width - 1, [int]($x * $pattern.Width / $width))
                    $py = [Math]::Min($pattern.Height - 1, [int]($y * $pattern.Height / $height))
                    $patternPixel = Convert-PatternColor $pattern.GetPixel($px, $py) $mode
                    $shade = ($maskPixel.R + $maskPixel.G + $maskPixel.B) / 765.0
                    $red = [Math]::Min(255, [int]($patternPixel.R * (0.35 + 0.85 * $shade)))
                    $green = [Math]::Min(255, [int]($patternPixel.G * (0.35 + 0.85 * $shade)))
                    $blue = [Math]::Min(255, [int]($patternPixel.B * (0.35 + 0.85 * $shade)))
                    # The GT6 image supplies the component silhouette. A source pattern may
                    # contain transparent pixels (notably Avaritia's Infinity Ingot), but
                    # those must not punch holes into motors, pumps or sensors.
                    $alpha = $maskPixel.A
                    $output.SetPixel($x, $y + $frame * $height,
                            [System.Drawing.Color]::FromArgb($alpha, $red, $green, $blue))
                }
            }
        }
        $directory = Split-Path -Parent $destination
        [System.IO.Directory]::CreateDirectory($directory) | Out-Null
        $output.Save($destination, [System.Drawing.Imaging.ImageFormat]::Png)
        if ($animated) {
            [System.IO.File]::WriteAllText($destination + '.mcmeta',
                    "{`n  `"animation`": { `"frametime`": 2, `"interpolate`": true }`n}`n")
        }
    } finally { $output.Dispose() }
}

function Export-Palette([System.IO.Compression.ZipArchive] $archive, [string] $kind,
        [string] $sourceSet, [string] $targetSet, [System.Drawing.Bitmap[]] $patterns,
        [string] $mode, [bool] $animated, [string[]] $onlyNames = @()) {
    $prefix = "assets/gregtech/textures/$kind/materialicons/$sourceSet/"
    $entries = $archive.Entries | Where-Object {
        $_.FullName.StartsWith($prefix) -and $_.Name.EndsWith('.png') -and
        -not $_.Name.Contains('_OVERLAY')
    }
    foreach ($entry in $entries) {
        $name = [System.IO.Path]::GetFileNameWithoutExtension($entry.Name)
        if ($onlyNames.Count -gt 0 -and $onlyNames -notcontains $name) { continue }
        $stream = $entry.Open()
        try { $mask = Open-Bitmap $stream } finally { $stream.Dispose() }
        try {
            $destination = Join-Path $assetRoot "$kind/materialicons/$targetSet/$($entry.Name)"
            Write-MaskedTexture $mask $patterns $destination $mode $animated
        } finally { $mask.Dispose() }

        $overlayEntry = $archive.GetEntry($prefix + $name + '_OVERLAY.png')
        if ($overlayEntry) {
            $stream = $overlayEntry.Open()
            try { $overlay = Open-Bitmap $stream } finally { $stream.Dispose() }
            try {
                $overlayDestination = Join-Path $assetRoot "$kind/materialicons/$targetSet/${name}_OVERLAY.png"
                $overlayFrame = [System.Drawing.Bitmap]::new($overlay.Width, [Math]::Min($overlay.Height, $overlay.Width))
                try {
                    $graphics = [System.Drawing.Graphics]::FromImage($overlayFrame)
                    try { $graphics.DrawImageUnscaled($overlay, 0, 0) } finally { $graphics.Dispose() }
                    $overlayFrame.Save($overlayDestination, [System.Drawing.Imaging.ImageFormat]::Png)
                } finally { $overlayFrame.Dispose() }
            } finally { $overlay.Dispose() }
        }
    }
}

function Export-HighTierComponents([System.IO.Compression.ZipArchive] $archive,
        [System.Collections.IDictionary] $tierPatterns) {
    $components = [ordered]@{
        electric_motor = '12010'
        electric_piston = '12070'
        electric_pump = '12030'
        conveyor_module = '12050'
        robot_arm = '12090'
        emitter = '12130'
        sensor = '12150'
        field_generator = '12110'
    }
    foreach ($tier in $tierPatterns.Keys) {
        foreach ($component in $components.GetEnumerator()) {
            $entry = $archive.GetEntry(
                    "assets/gregtech/textures/items/gt.multiitem.technological/$($component.Value).png")
            if (-not $entry) { throw "GT6 component mask was not found: $($component.Value)" }
            $stream = $entry.Open()
            try { $mask = Open-Bitmap $stream } finally { $stream.Dispose() }
            try {
                $destination = Join-Path $assetRoot "items/high_tier_components/$($tier.ToLower())/$($component.Key).png"
                $composed = [ComponentTextureComposer]::Compose($mask, $tierPatterns[$tier], ($tier -eq 'UX'))
                try {
                    [IO.Directory]::CreateDirectory((Split-Path -Parent $destination)) | Out-Null
                    $composed.Save($destination, [Drawing.Imaging.ImageFormat]::Png)
                    if ($tier -eq 'UX') {
                        # Avaritia resource_infinity_ingot: 0..8..1, with original per-frame timing.
                        # Get-BounceFramesFromDirectory already expands that sequence to 16 frames.
                        $times=@(3,3,3,2,2,30,30,30,30,30,30,30,2,2,3,3)
                        $sequence=@();for($i=0;$i -lt $times.Count;$i++){$sequence+=@{index=$i;time=$times[$i]}}
                        $meta=@{animation=@{frametime=30;interpolate=$true;frames=$sequence}}|ConvertTo-Json -Depth 5 -Compress
                    } else { $meta='{"animation":{"frametime":2,"interpolate":true}}' }
                    [IO.File]::WriteAllText($destination + '.mcmeta', $meta)
                } finally { $composed.Dispose() }
            } finally { $mask.Dispose() }
        }
    }
}

$deepslateFrames = Get-PatternFrames $deepslate
$bedrockFrames = Get-PatternFrames $bedrock
$etherniumFrames = Get-AnimatedPatternFrames $etherniumTemplate 'ethernium'
$transcendentFrames = Get-AnimatedPatternFrames $transcendentTemplate 'transcendent'
$stableTimeSpaceFrames = Get-AnimatedPatternFrames $stableTimeSpaceTemplate 'spacetime'
$bedrockAnimatedFrames = Get-AnimatedPatternFrames $bedrock 'bedrock'
$uxFrames = Get-BounceFramesFromDirectory $uxInfinityFrames
$oivFrames = Get-AnimatedPatternFrames $oivTemplate 'oiv'
$maxFrames = Get-AnimatedPatternFrames $maxTemplate 'max'
$archive = [System.IO.Compression.ZipFile]::OpenRead($gtJar)
try {
    if (-not $ComponentsOnly) {
    Export-Palette $archive 'items' 'STONE' 'HTGT_DEEPSLATE' $deepslateFrames 'normal' $false @('dust', 'dustImpure')
    foreach ($kind in @('items', 'blocks')) {
        Export-Palette $archive $kind 'METALLIC' 'HTGT_BEDROCK' $bedrockFrames 'normal' $false
        Export-Palette $archive $kind 'SHINY' 'HTGT_ETERNITY' $etherniumFrames 'normal' $true
        Export-Palette $archive $kind 'SPACE' 'HTGT_TRANSCENDENT' $transcendentFrames 'normal' $true
        Export-Palette $archive $kind 'SPACE' 'HTGT_COSMOS' $stableTimeSpaceFrames 'normal' $true
    }
    }
    Export-HighTierComponents $archive ([ordered]@{
        UX = $uxFrames
        OLV = $bedrockAnimatedFrames
        OMV = $transcendentFrames
        OHV = $stableTimeSpaceFrames
        OEV = $etherniumFrames
        OIV = $oivFrames
        MAX = $maxFrames
    })
} finally {
    $archive.Dispose()
    @($deepslateFrames + $bedrockFrames + $etherniumFrames + $transcendentFrames +
            $stableTimeSpaceFrames + $bedrockAnimatedFrames + $uxFrames + $oivFrames +
            $maxFrames) | ForEach-Object { $_.Dispose() }
}

& $PythonExecutable (Join-Path $PSScriptRoot 'optimize_component_textures.py') --root $ProjectRoot
if ($LASTEXITCODE -ne 0) { throw 'Component texture compression failed.' }
if ($ComponentsOnly) { Write-Output 'Regenerated only 56 high-tier component animations.'; return }
& $PythonExecutable (Join-Path $PSScriptRoot 'deduplicate_materialicons.py') --root $ProjectRoot
if ($LASTEXITCODE -ne 0) { throw 'Material icon deduplication failed (Python with Pillow required).' }
Write-Output 'Generated and deduplicated GT6 material palettes in assets/htgt6/textures.'
