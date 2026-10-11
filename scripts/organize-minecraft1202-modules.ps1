$ErrorActionPreference = 'Stop'

$workspace = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$javaRoot = (Resolve-Path (Join-Path $workspace 'src/main/java')).Path
$moduleRoot = Join-Path $javaRoot 'com/mod/htgt6/minecraft1202/Module'

if (-not $moduleRoot.StartsWith($workspace, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Module target escaped workspace: $moduleRoot"
}

$sources = New-Object 'System.Collections.Generic.Dictionary[string,string]' ([System.StringComparer]::OrdinalIgnoreCase)

function Add-Tree([string]$relativeRoot, [string]$update) {
    $root = Join-Path $workspace $relativeRoot
    if (-not (Test-Path -LiteralPath $root)) { return }
    Get-ChildItem -LiteralPath $root -Recurse -File -Filter '*.java' | ForEach-Object {
        $sources[$_.FullName] = $update
    }
}

function Add-File([string]$relativePath, [string]$update) {
    $path = Join-Path $workspace $relativePath
    if (Test-Path -LiteralPath $path) { $sources[(Resolve-Path -LiteralPath $path).Path] = $update }
}

Add-Tree 'src/main/java/com/mod/htgt6/init' 'Registry'
Add-Tree 'src/main/java/com/mod/htgt6/common/backport_1_20' 'Modern'
Add-Tree 'src/main/java/com/mod/htgt6/common/bountiful' 'Bountiful'
Add-Tree 'src/main/java/com/mod/htgt6/common/combat' 'Combat'
Add-Tree 'src/main/java/com/mod/htgt6/common/exploration' 'Exploration'
Add-Tree 'src/main/java/com/mod/htgt6/common/frostburn' 'Frostburn'
Add-Tree 'src/main/java/com/mod/htgt6/common/inventory112' 'Inventory'
Add-Tree 'src/main/java/com/mod/htgt6/common/villagepillage' 'VillageAndPillage'
Add-Tree 'src/main/java/com/mod/htgt6/common/handler/worldgeneration/oceanbiome' 'Aquatic'
Add-Tree 'src/main/java/com/mod/htgt6/common/handler/worldgeneration/oceancontent' 'Aquatic'
Add-Tree 'src/main/java/com/mod/htgt6/common/handler/worldgeneration/shipwerk' 'Aquatic'
Add-Tree 'src/main/java/com/mod/htgt6/client/backport_1_20' 'Modern'
Add-Tree 'src/main/java/com/mod/htgt6/client/model/backport' 'Shared'
Add-Tree 'src/main/java/com/mod/htgt6/client/combat' 'Combat'

$clientFiles = @(
    'src/main/java/com/mod/htgt6/client/audio/GuardianBeamSound112.java',
    'src/main/java/com/mod/htgt6/client/audio/GuardianBeamSoundHandler112.java',
    'src/main/java/com/mod/htgt6/client/render/AquaticClientContractFixture120.java',
    'src/main/java/com/mod/htgt6/client/render/EntityBackportValidator.java',
    'src/main/java/com/mod/htgt6/client/render/ModelBountifulArmorStand.java',
    'src/main/java/com/mod/htgt6/client/render/ModelBountifulGuardian.java',
    'src/main/java/com/mod/htgt6/client/render/ModelBountifulRabbit.java',
    'src/main/java/com/mod/htgt6/client/render/ModelFrostburnPolarBear.java',
    'src/main/java/com/mod/htgt6/client/render/OceanWaterRenderHandler.java',
    'src/main/java/com/mod/htgt6/client/render/RenderBountifulArmorStand.java',
    'src/main/java/com/mod/htgt6/client/render/RenderBountifulEndermite.java',
    'src/main/java/com/mod/htgt6/client/render/RenderBountifulGuardian.java',
    'src/main/java/com/mod/htgt6/client/render/RenderBountifulRabbit.java',
    'src/main/java/com/mod/htgt6/client/render/RenderFrostburnHusk.java',
    'src/main/java/com/mod/htgt6/client/render/RenderFrostburnPolarBear.java',
    'src/main/java/com/mod/htgt6/client/render/RenderFrostburnStray.java',
    'src/main/java/com/mod/htgt6/client/render/TileEntityBountifulBannerRenderer.java',
    'src/main/java/com/mod/htgt6/client/render/TridentPlayerPoseHooks.java'
)
foreach ($file in $clientFiles) { Add-File $file 'Shared' }

$combatClientFiles = @(
    'src/main/java/com/mod/htgt6/client/render/ChorusRenderHandler.java',
    'src/main/java/com/mod/htgt6/client/render/ElytraPlayerRenderer.java',
    'src/main/java/com/mod/htgt6/client/render/EndRodRenderHandler.java',
    'src/main/java/com/mod/htgt6/client/render/ModelCombatShulker.java',
    'src/main/java/com/mod/htgt6/client/render/ModelCombatZombieVillager.java',
    'src/main/java/com/mod/htgt6/client/render/RenderCombatInvisible.java',
    'src/main/java/com/mod/htgt6/client/render/RenderCombatShulker.java',
    'src/main/java/com/mod/htgt6/client/render/RenderCombatShulkerBullet.java',
    'src/main/java/com/mod/htgt6/client/render/RenderCombatZombieVillager.java',
    'src/main/java/com/mod/htgt6/client/render/ShieldItemRenderer.java'
)
foreach ($file in $combatClientFiles) { Add-File $file 'Combat' }

$aquaticClientFiles = @(
    'src/main/java/com/mod/htgt6/client/render/KelpRenderHandler.java',
    'src/main/java/com/mod/htgt6/client/render/ModelDrowned.java',
    'src/main/java/com/mod/htgt6/client/render/ModelRiptideEffect.java',
    'src/main/java/com/mod/htgt6/client/render/ModelSeaTurtle.java',
    'src/main/java/com/mod/htgt6/client/render/ModelTrident.java',
    'src/main/java/com/mod/htgt6/client/render/RenderDrowned.java',
    'src/main/java/com/mod/htgt6/client/render/RenderSeaTurtle.java',
    'src/main/java/com/mod/htgt6/client/render/RenderThrownTrident.java',
    'src/main/java/com/mod/htgt6/client/render/RiptideEffectRenderer.java',
    'src/main/java/com/mod/htgt6/client/render/TreasureMapItemRenderer120.java',
    'src/main/java/com/mod/htgt6/client/render/TreasureMapScrollItemRenderer120.java',
    'src/main/java/com/mod/htgt6/client/render/TridentGlintRenderer.java',
    'src/main/java/com/mod/htgt6/client/render/TridentItemRenderer.java',
    'src/main/java/com/mod/htgt6/client/render/TurtleEggRenderHandler120.java'
)
foreach ($file in $aquaticClientFiles) { Add-File $file 'Aquatic' }

$explorationClientFiles = @(
    'src/main/java/com/mod/htgt6/client/render/ModelIllager112.java',
    'src/main/java/com/mod/htgt6/client/render/ModelLlama112.java',
    'src/main/java/com/mod/htgt6/client/render/ModelVex112.java',
    'src/main/java/com/mod/htgt6/client/render/RenderEvokerFangs112.java',
    'src/main/java/com/mod/htgt6/client/render/RenderIllager112.java',
    'src/main/java/com/mod/htgt6/client/render/RenderLlama112.java',
    'src/main/java/com/mod/htgt6/client/render/RenderStateGuard112.java',
    'src/main/java/com/mod/htgt6/client/render/RenderVex112.java',
    'src/main/java/com/mod/htgt6/client/render/TotemActivationRenderer112.java'
)
foreach ($file in $explorationClientFiles) { Add-File $file 'Exploration' }

$villageClientFiles = @(
    'src/main/java/com/mod/htgt6/client/render/ModelPillager.java',
    'src/main/java/com/mod/htgt6/client/render/ModelRavager.java',
    'src/main/java/com/mod/htgt6/client/render/RenderPillager.java',
    'src/main/java/com/mod/htgt6/client/render/RenderRavager.java',
    'src/main/java/com/mod/htgt6/client/render/RenderVillageBell120.java',
    'src/main/java/com/mod/htgt6/client/render/RenderVillager120.java'
)
foreach ($file in $villageClientFiles) { Add-File $file 'VillageAndPillage' }

$sharedFiles = @(
    'src/main/java/com/mod/htgt6/common/BackportStrengthFixture120.java',
    'src/main/java/com/mod/htgt6/common/command/CommandFindBiome120.java',
    'src/main/java/com/mod/htgt6/common/command/CommandFindBiomeFixtures120.java',
    'src/main/java/com/mod/htgt6/common/GTHooks/WorldHooks/BlockGT6DeepslateOre.java',
    'src/main/java/com/mod/htgt6/common/GTHooks/WorldHooks/PherithiumDeepslateWorldGenerator.java',
    'src/main/java/com/mod/htgt6/common/handler/worldgeneration/AquaticFuturaStructureFixture120.java',
    'src/main/java/com/mod/htgt6/common/GTHooks/WorldHooks/GT6DeepslateOreIntegration.java',
    'src/main/java/com/mod/htgt6/minecraft1202/MinecraftBackportGuiHandler.java',
    'src/main/java/com/mod/htgt6/minecraft1202/content/BackportCategory.java'
)
foreach ($file in $sharedFiles) { Add-File $file 'Shared' }

function Get-Category([System.IO.FileInfo]$file) {
    $name = $file.BaseName
    $path = $file.FullName.Replace('\', '/').ToLowerInvariant()
    if ($path.Contains('/client/') -or $name -match '(Client|Render|Renderer|Model|Gui|Sound)') { return 'Client' }
    if ($name -match '^ItemBlock') { return 'Items' }
    if ($name -match '^(Block|TileEntity)') { return 'Blocks' }
    if ($name -match '^Item') { return 'Items' }
    if ($name -match '^Entity') { return 'Entity' }
    if ($name -match '^(Packet|Network)' -or $path.Contains('/network/')) { return 'Network' }
    if ($name -match 'Recipe') { return 'Recipes' }
    if ($name -match '(Fixture|Contract|Data|Storage|Template|Processor|Plan)') { return 'Data' }
    if ($path.Contains('/world/') -or $path.Contains('/worldgeneration/') -or
        $name -match '(World|Biome|Chunk|MapGen|Generator|Generation|Structure|Futura|Noise|Density|Jigsaw|Aquifer|Cave|Surface|Climate|Terrain|VillageTemplate)') { return 'World' }
    if ($name -match '(Handler|Event|Tracker|Hooks)') { return 'Events' }
    if ($name -match '(Content|Update|MinecraftBackports|Minecraft120Backport|BackportCategory|Registry)') { return 'Registry' }
    return 'Gameplay'
}

function Get-Update([System.IO.FileInfo]$file, [string]$fallback) {
    if ($fallback -ne 'Registry') { return $fallback }
    $name = $file.BaseName
    if ($name -match 'Aquatic') { return 'Aquatic' }
    if ($name -match 'Bountiful') { return 'Bountiful' }
    if ($name -match 'Combat') { return 'Combat' }
    if ($name -match 'Exploration') { return 'Exploration' }
    if ($name -match 'Frostburn') { return 'Frostburn' }
    if ($name -match 'Inventory') { return 'Inventory' }
    if ($name -match 'VillageAndPillage') { return 'VillageAndPillage' }
    if ($name -match 'Minecraft') { return 'Modern' }
    return 'Shared'
}

$moves = New-Object System.Collections.Generic.List[object]
foreach ($entry in $sources.GetEnumerator()) {
    $file = Get-Item -LiteralPath $entry.Key
    $category = Get-Category $file
    $destinationDirectory = Join-Path (Join-Path $moduleRoot $category) $entry.Value
    $destination = Join-Path $destinationDirectory $file.Name
    if (-not $destination.StartsWith($moduleRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Destination escaped Module root: $destination"
    }
    if ((Test-Path -LiteralPath $destination) -and
        -not $destination.Equals($file.FullName, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Destination collision: $destination"
    }
    $moves.Add([pscustomobject]@{ Source=$file.FullName; Directory=$destinationDirectory; Destination=$destination; Category=$category; Update=$entry.Value })
}

foreach ($move in $moves) {
    if (-not (Test-Path -LiteralPath $move.Directory)) {
        New-Item -ItemType Directory -Path $move.Directory -Force | Out-Null
    }
    Move-Item -LiteralPath $move.Source -Destination $move.Destination
}

# Normalize files already under Module as well. This makes the script safely
# rerunnable after category rules are refined.
$normalizations = New-Object System.Collections.Generic.List[object]
Get-ChildItem -LiteralPath $moduleRoot -Recurse -File -Filter '*.java' |
    Where-Object { $_.Name -ne 'package-info.java' } | ForEach-Object {
        $relative = $_.FullName.Substring($moduleRoot.Length).TrimStart('\','/').Replace('\','/')
        $segments = $relative.Split('/')
        $fallback = if ($segments.Length -ge 2) { $segments[1] } else { 'Shared' }
        $category = Get-Category $_
        $update = Get-Update $_ $fallback
        $directory = Join-Path (Join-Path $moduleRoot $category) $update
        $destination = Join-Path $directory $_.Name
        if (-not $destination.Equals($_.FullName, [System.StringComparison]::OrdinalIgnoreCase)) {
            if (Test-Path -LiteralPath $destination) { throw "Normalization collision: $destination" }
            $normalizations.Add([pscustomobject]@{ Source=$_.FullName; Directory=$directory; Destination=$destination })
        }
    }

foreach ($move in $normalizations) {
    if (-not (Test-Path -LiteralPath $move.Directory)) {
        New-Item -ItemType Directory -Path $move.Directory -Force | Out-Null
    }
    Move-Item -LiteralPath $move.Source -Destination $move.Destination
}

# Remove only verified empty directories below the exact Module root.
Get-ChildItem -LiteralPath $moduleRoot -Recurse -Directory |
    Sort-Object { $_.FullName.Length } -Descending | ForEach-Object {
        if (-not $_.FullName.StartsWith($moduleRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
            throw "Cleanup target escaped Module root: $($_.FullName)"
        }
        if ((Get-ChildItem -LiteralPath $_.FullName -Force | Measure-Object).Count -eq 0) {
            Remove-Item -LiteralPath $_.FullName
        }
    }

Get-ChildItem -LiteralPath $moduleRoot -Directory | Sort-Object Name | ForEach-Object {
    "{0}`t{1}" -f $_.Name,(Get-ChildItem -LiteralPath $_.FullName -Recurse -File -Filter '*.java').Count
}
"MOVED`t$($moves.Count)"
"NORMALIZED`t$($normalizations.Count)"
