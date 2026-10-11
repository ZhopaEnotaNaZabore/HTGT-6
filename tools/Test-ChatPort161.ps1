$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
Push-Location $root
try {
    $jdk = 'C:/Program Files/BellSoft/LibericaJDK-8/bin'
    $baseClasspath = (Get-Content 'build/pillager159/classpath.txt' -Raw).Trim()
    $outputDir = Join-Path $root 'build/chatport161/classes'
    $emptyDir = Join-Path $root 'build/chatport161/empty'
    New-Item -ItemType Directory -Force -Path $outputDir,$emptyDir | Out-Null
    $sourceDir = 'src/main/java/com/mod/htgt6/minecraft1202/Module/Client/VillageAndPillage'
    & "$jdk/javac.exe" -encoding UTF-8 -source 8 -target 8 -cp $baseClasspath -sourcepath $emptyDir -d $outputDir "$sourceDir/RaidHudState160.java" "$sourceDir/RaidHud150.java" tools/Gemma160Fixture.java tools/Gemma160ReviewFixture.java
    if ($LASTEXITCODE -ne 0) { throw 'Java8 compilation failed' }
    $testClasspath = "$outputDir;$baseClasspath"
    $testClasspath | Set-Content 'build/chatport161/classpath.txt'
    foreach ($fixture in @('Gemma160Fixture','Gemma160ReviewFixture','com.mod.htgt6.minecraft1202.Module.Data.Bountiful.Pillager159Fixture')) {
        & "$jdk/java.exe" -cp $testClasspath $fixture
        if ($LASTEXITCODE -ne 0) { throw "Failed: $fixture" }
    }
} finally {
    Pop-Location
}
