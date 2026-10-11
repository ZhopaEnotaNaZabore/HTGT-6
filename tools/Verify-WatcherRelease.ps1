param(
    [string] $Jar = (Join-Path $PSScriptRoot '../build/watcher-release/release/HiTech6_V_1.0.4_Build_Watcher_03.jar')
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $Jar).Path)
try {
    $classes = @('Watcher', 'WatcherState', 'WatcherChecks', 'WatcherRuntime', 'WatcherProxy', 'client/WatcherClient', 'client/GuiWatcherStop')
    foreach ($class in $classes) {
        if ($null -eq $archive.GetEntry("com/mod/htgt6/watcher/$class.class")) { throw "Missing class: $class" }
    }
    foreach ($resource in @('assets/htgt6/textures/gui/watcher/stop.png')) {
        $entry = $archive.GetEntry($resource)
        if ($null -eq $entry) { throw "Missing resource: $resource" }
        $stream = $entry.Open()
        $digest = [System.Security.Cryptography.SHA256]::Create()
        try { $packed = ([System.BitConverter]::ToString($digest.ComputeHash($stream))).Replace('-', '') }
        finally { $stream.Dispose(); $digest.Dispose() }
        $expected = (Get-FileHash -LiteralPath (Join-Path $PSScriptRoot "../src/main/resources/$resource") -Algorithm SHA256).Hash
        if ($packed -ne $expected) { throw "Stale packaged resource: $resource" }
    }
    if (@($archive.Entries | Where-Object { $_.FullName -like 'META-INF/htgt6-watcher*.properties' -or $_.FullName -eq 'com/mod/htgt6/watcher/WatcherTransformers.class' }).Count -ne 0) {
        throw 'Obsolete startup allowlists leaked into the session-based release'
    }
    if (@($archive.Entries | Where-Object { $_.FullName -like 'watchertest/*' -or $_.FullName -like '*WatcherChecksFixture*' -or $_.FullName -like '*WatcherStateFixture*' }).Count -ne 0) {
        throw 'Test agent or fixture leaked into the release'
    }
} finally { $archive.Dispose() }
Write-Output 'Watcher production JAR: PASS (classes, exact resources, no test agents)'
Get-Item -LiteralPath $Jar | Select-Object FullName,Length
