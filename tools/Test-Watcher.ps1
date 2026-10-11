$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Push-Location $root
try {
    $jdk = 'C:/Program Files/BellSoft/LibericaJDK-8/bin'
    $classpath = (Get-Content 'build/pillager159/classpath.txt' -Raw).Trim()
    $output = Join-Path $root 'build/watcher/classes'
    $empty = Join-Path $root 'build/watcher/empty'
    New-Item -ItemType Directory -Force -Path $output,$empty | Out-Null
    $sources = @(Get-ChildItem 'src/main/java/com/mod/htgt6/watcher' -Filter '*.java' -Recurse | ForEach-Object FullName)
    $jdkTools = Join-Path $jdk '../lib/tools.jar'
    & "$jdk/javac.exe" -encoding UTF-8 -source 8 -target 8 -cp "$classpath;$jdkTools" -sourcepath $empty -d $output @sources tools/WatcherStateFixture.java tools/WatcherChecksFixture.java
    if ($LASTEXITCODE -ne 0) { throw 'Watcher compilation failed' }
    & "$jdk/java.exe" -cp $output WatcherStateFixture
    if ($LASTEXITCODE -ne 0) { throw 'Watcher state test failed' }
    $agentClasses = Join-Path $root 'build/watcher/agent-classes'
    New-Item -ItemType Directory -Force -Path $agentClasses | Out-Null
    & "$jdk/javac.exe" -encoding UTF-8 -source 8 -target 8 -d $agentClasses tools/watcher/ProbeAgent.java
    if ($LASTEXITCODE -ne 0) { throw 'Probe compilation failed' }
    $agentManifest = Join-Path $root 'build/watcher/agent-manifest.mf'
    [System.IO.File]::WriteAllText($agentManifest, "Manifest-Version: 1.0`nPremain-Class: watchertest.ProbeAgent`nAgent-Class: watchertest.ProbeAgent`n`n")
    $agentJar = Join-Path $root 'build/watcher/probe-agent.jar'
    & "$jdk/jar.exe" cfm $agentJar $agentManifest -C $agentClasses .
    if ($LASTEXITCODE -ne 0) { throw 'Probe packaging failed' }
    & "$jdk/java.exe" "-javaagent:$agentJar" -cp "$output;$jdkTools" WatcherChecksFixture --startup
    if ($LASTEXITCODE -ne 0) { throw 'Startup agent test failed' }
    & "$jdk/java.exe" -cp "$output;$jdkTools" WatcherChecksFixture $agentJar
    if ($LASTEXITCODE -ne 0) { throw 'Watcher checks failed' }
} finally {
    Pop-Location
}
