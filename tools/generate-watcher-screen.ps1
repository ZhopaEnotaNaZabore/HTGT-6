$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$assetPath = Join-Path $PSScriptRoot '../src/main/resources/assets/htgt6/textures/gui/watcher/stop.png'
[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($assetPath)) | Out-Null
$bitmap = New-Object System.Drawing.Bitmap 1600, 900
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$graphics.Clear([System.Drawing.Color]::FromArgb(0, 120, 215))
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$brush = [System.Drawing.Brushes]::White
function Write-ScreenLine([string] $text, [float] $size, [float] $x, [float] $y) {
    $font = New-Object System.Drawing.Font 'Segoe UI', $size, ([System.Drawing.FontStyle]::Regular), ([System.Drawing.GraphicsUnit]::Pixel)
    try { $graphics.DrawString($text, $font, $brush, $x, $y) } finally { $font.Dispose() }
}
try {
    # Windows 10-style fictional stop screen. No diagnostics, restart or dump occurs.
    Write-ScreenLine ':(' 156 140 96
    Write-ScreenLine 'Your PC ran into a problem and needs to restart.' 38 148 322
    Write-ScreenLine "We're just collecting some error info, and then we'll" 38 148 376
    Write-ScreenLine 'restart for you.' 38 148 430
    Write-ScreenLine '0% complete' 32 150 525
    Write-ScreenLine 'If you call a support person, give them this info:' 23 151 652
    Write-ScreenLine 'Stop code: VIDEO_ADAPTER_THERMAL_DAMAGE' 23 151 699
    Write-ScreenLine 'What failed: gpu_thermal.sys' 23 151 739
    Write-ScreenLine 'Graphics adapter: unrecoverable thermal damage detected.' 23 151 779
    $bitmap.Save($assetPath, [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose()
    $bitmap.Dispose()
}
Write-Output $assetPath
