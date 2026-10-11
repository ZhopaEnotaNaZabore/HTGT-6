$ErrorActionPreference = 'Stop'

$moduleRoot = (Resolve-Path (Join-Path $PSScriptRoot '../src/main/java/com/mod/htgt6/minecraft1202/Module')).Path

# Members that used package-private access before the historical packages were
# separated into Module categories. This is the explicit cross-category API.
$targets = @{
    'EntitySeaTurtle' = @('setHome','MOVEMENT_SPEED')
    'EntityFallingPointedDripstone' = @('EntityFallingPointedDripstone')
    'ModelTrident' = @('REFERENCE_GEOMETRY','REFERENCE_MIRROR')
    'RenderThrownTrident' = @('REFERENCE_YAW_OFFSET','REFERENCE_PITCH_OFFSET')
    'TridentItemRenderer' = @('FIRST_PERSON','THIRD_PERSON_HELD','THIRD_PERSON_THROWING','GROUND')
    'TurtleEggRenderHandler120' = @('REFERENCE_CUBES')
    'ItemTrident' = @('THROW_THRESHOLD_TICKS','ATTACK_DAMAGE_MODIFIER')
    'EntityThrownTrident' = @('BASE_DAMAGE','IMPALING_DAMAGE_PER_LEVEL','GRAVITY','WATER_INERTIA')
    'EntityDrowned' = @('TRIDENT_EQUIPMENT_CHANCE','TRIDENT_DROP_CHANCE','OCEAN_SPAWN_ROLL','RIVER_SPAWN_ROLL','MINIMUM_DEPTH_BELOW_SEA','TRIDENT_CHARGE_TICKS')
    'ZombieDrowningHandler' = @('CONVERSION_TICKS')
    'RiptideAttackHandler' = @('AUTO_SPIN_TICKS','AUTO_SPIN_DAMAGE','start')
    'BlockTurtleEgg120' = @('metadata')
    'ItemTurtleHelmet120' = @('shouldRefreshWaterBreathing')
    'BlockCoralBlock120' = @('isLive')
    'WorldGenCoralFeature120' = @('TREE','CLAW','MUSHROOM')
    'BlockKelp' = @('isOpenWater','isWater','isKelp')
    'OceanAquaticHandler' = @('aquaticVegetationCount')
    'VanillaTreasureMap120' = @('MAP_SCALE','RED_X_ICON')
    'BuriedTreasureHandler' = @('EXPLORATION_MAP_SEARCH_RADIUS')
    'AquaticChestLoot120' = @('SHIPWRECK_MAP_MISC_WEIGHT','COAST_TRIM_DENOMINATOR','SHIPWRECK_SUPPLY_WEIGHT','SHIPWRECK_TREASURE_MAIN_WEIGHT','SHIPWRECK_TREASURE_NUGGET_WEIGHT','BURIED_TREASURE_COMMON_WEIGHT','BURIED_TREASURE_RARE_WEIGHT','uniform')
    'WorldGenOceanRuin' = @('BIG_MAP_WEIGHT','BIG_SECOND_POOL_WEIGHT','SMALL_MAP_WEIGHT','SMALL_SECOND_POOL_WEIGHT','isBigTemplateName')
    'WoodlandMansionGenerator112' = @('replayFingerprint','replayChunks')
    'WoodlandMansionLayout112' = @('quarters')
    'FuturaBubbleColumn120' = @('velocityAfter')
    'Backport120AxeStrippingHandler' = @('isAxe','isAuthoritativeSide')
    'AquiferSampler120' = @('preliminarySurfaceModern')
    'CaveDensityRuntime120' = @('rarityType1','rarityType2','spaghetti2dVerticalDensity','finalDensityShapeForFixture')
    'FuturaBiomeZoomer120' = @('obfuscateSeed')
    'FuturaClimateSampler120' = @('Point')
    'OverworldBiomeParameterMap120' = @('selectLinear','min','max')
    'FuturaDeepDarkRuntime120' = @('DEEP_DARK','ANCIENT_CITY','PLACEMENT_COUNT','chargeCount','amountPerCharge','spreadAttempts','growthRounds','spreadRounds','extraMin','extraMax','catalystChance')
    'ChunkProviderFutura120' = @('copyLowerLegacyVolume','isSurfaceReplaceable','WAY_BELOW_MIN_Y_1202','findStoneSequenceBottom')
    'FuturaLushCavesRuntime120' = @('FEATURE_MARGIN','acceptsUndergroundPosition','sampleDepth')
    'FuturaAncientCityGenerator120' = @('layout','cachedLayoutCount','mappedMetadataForFixture','mappedBlockForFixture','villageLootProfile','shouldIgnoreTemplateState','villageBlockEntityFingerprint')
    'FuturaVillageGenerator120' = @('styleForBiomeId','runtimeAuditForFixture','loadTemplateNbtForFixture')
    'FuturaWorldChunkManager120' = @('getSurfaceBiomeAt','loadSurfaceBiomeData')
    'CommandFindBiome120' = @('SEARCH_RADIUS','resolve','isCaveBiomeId','isCaveBiome','findNearestUnderground')
    'VillagePoiHandler120' = @('mayAdoptPoiProfession')
    'VillagerSleep120' = @('assertFixture','isRestTime','wake','maySleep','bedAvailable','sleep')
    'TileEntityVillageBell120' = @('assertRoundTripFixture')
    'BlockVillagePoi120' = @('compostChanceForKey')
    'VillageWorkstationLogic120' = @('assertBehaviorFixture')
    'TileEntityVillageLectern120' = @('assertStateFixture')
    'VillagePoiData120' = @('claimNearest','ownedBy','homeOwnedBy','releaseJob','releaseHome','releaseOwner','claimNearestHome','migrateChunkOnLoad','type','x','y','z','loaded')
    'PacketBackport120DeathPosition' = @('PacketBackport120DeathPosition')
    'BlockBackport120SculkCatalyst' = @('bloom')
    'BlockBackport120CaveVines' = @('BERRIES')
    'BlockBackport120PointedDripstone' = @('canSurvive')
    'BlockBackport120SmallDripleaf' = @('isSoil','UPPER')
    'OffhandSystem' = @('setClientShieldCooldown','setClientShieldBlocking')
    'BlockBackport120Door' = @('setDoorItem')
    'BlockBackport120Sign' = @('setSignItem')
    'BlockBackport120HangingSign' = @('setSignItem')
    'BlockBackport120Chain' = @('setChainItem')
    'BlockBackport120Slab' = @('setSingle')
    'ItemSuspiciousStew120' = @('create')
}

function Add-PublicModifier([string]$content, [string]$owner, [string]$member) {
    $escaped = [regex]::Escape($member)
    $publicPattern = '(?m)^\s*public\s+[^\r\n]*\b' + $escaped + '\b'
    if ([regex]::IsMatch($content, $publicPattern)) { return $content }
    # Constructor (including constructors of nested API value types).
    $pattern = '(?m)^(\s*)(?!public\b|protected\b|private\b)(' + $escaped + '\s*\()'
    $updated = [regex]::Replace($content, $pattern, '$1public $2')
    if ($updated -ne $content) { return $updated }

    # Method with a return type and optional Java modifiers/type parameters.
    $pattern = '(?m)^(\s*)(?!public\b|protected\b|private\b)((?:(?:static|final|synchronized|abstract|native|strictfp)\s+)*(?:<[^>]+>\s+)?[\w$<>\[\].?, ]+\s+' + $escaped + '\s*\()'
    $updated = [regex]::Replace($content, $pattern, '$1public $2')
    if ($updated -ne $content) { return $updated }

    # Field, including comma-separated declarations such as "long min,max".
    $pattern = '(?m)^(\s*)(?!public\b|protected\b|private\b)((?:(?:static|final|transient|volatile)\s+)*[\w$<>\[\].?, ]+\b' + $escaped + '\b[^\r\n]*(?:;|=))'
    return [regex]::Replace($content, $pattern, '$1public $2')
}

$changed = 0
foreach ($owner in $targets.Keys) {
    $files = @(Get-ChildItem -LiteralPath $moduleRoot -Recurse -File -Filter ($owner + '.java'))
    if ($files.Count -ne 1) { throw "Expected one source for $owner, found $($files.Count)" }
    $path = $files[0].FullName
    $content = [IO.File]::ReadAllText($path)
    $updated = $content
    foreach ($member in $targets[$owner]) {
        $next = Add-PublicModifier $updated $owner $member
        if ($next -eq $updated -and $updated -notmatch '(?m)^\s*public\s+[^\r\n]*\b' + [regex]::Escape($member) + '\b') {
            throw "Could not publicize $owner.$member"
        }
        $updated = $next
    }
    if ($updated -ne $content) {
        [IO.File]::WriteAllText($path, $updated, (New-Object Text.UTF8Encoding($false)))
        ++$changed
    }
}

"CHANGED_FILES`t$changed"
