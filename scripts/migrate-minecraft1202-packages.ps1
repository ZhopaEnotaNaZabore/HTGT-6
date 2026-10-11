$ErrorActionPreference = 'Stop'

$workspace = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$javaRoot = (Resolve-Path (Join-Path $workspace 'src/main/java')).Path
$moduleRoot = (Resolve-Path (Join-Path $javaRoot 'com/mod/htgt6/minecraft1202/Module')).Path
$modulePackage = 'com.mod.htgt6.minecraft1202.Module'

if (-not $moduleRoot.StartsWith($workspace, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Module root escaped workspace: $moduleRoot"
}

$entries = New-Object System.Collections.Generic.List[object]
Get-ChildItem -LiteralPath $moduleRoot -Recurse -File -Filter '*.java' | ForEach-Object {
    $content = [IO.File]::ReadAllText($_.FullName)
    $packageMatch = [regex]::Match($content, '(?m)^package\s+([A-Za-z_][\w]*(?:\.[A-Za-z_][\w]*)*)\s*;')
    if (-not $packageMatch.Success) { throw "Missing package declaration: $($_.FullName)" }

    $relative = $_.FullName.Substring($moduleRoot.Length).TrimStart('\','/')
    $relativeDirectory = Split-Path -Parent $relative
    $newPackage = $modulePackage
    if ($relativeDirectory) {
        $newPackage += '.' + $relativeDirectory.Replace('\','.').Replace('/','.')
    }

    $entries.Add([pscustomobject]@{
        Path = $_.FullName
        Name = $_.BaseName
        OldPackage = $packageMatch.Groups[1].Value
        NewPackage = $newPackage
        OldFqn = $packageMatch.Groups[1].Value + '.' + $_.BaseName
        NewFqn = $newPackage + '.' + $_.BaseName
        IsPackageInfo = $_.Name -eq 'package-info.java'
    })
}

$typeEntries = @($entries | Where-Object { -not $_.IsPackageInfo })
$duplicateOldNames = $typeEntries | Group-Object OldFqn | Where-Object Count -gt 1
if ($duplicateOldNames) {
    throw "Duplicate original FQCN(s): $($duplicateOldNames.Name -join ', ')"
}
$duplicateNewNames = $typeEntries | Group-Object NewFqn | Where-Object Count -gt 1
if ($duplicateNewNames) {
    throw "Duplicate target FQCN(s): $($duplicateNewNames.Name -join ', ')"
}

$byOldPackage = @{}
foreach ($entry in $typeEntries) {
    if (-not $byOldPackage.ContainsKey($entry.OldPackage)) {
        $byOldPackage[$entry.OldPackage] = New-Object System.Collections.Generic.List[object]
    }
    $byOldPackage[$entry.OldPackage].Add($entry)
}

# Registries instantiate implementation types from sibling categories.
foreach ($entry in $typeEntries) {
    $content = [IO.File]::ReadAllText($entry.Path)
    if ([regex]::IsMatch($content, '(?m)^\s*public\s+enum\s+' + [regex]::Escape($entry.Name) + '\b')) { continue }
    $constructorPattern = '(?m)^(\s*)(?!public\b|protected\b|private\b)(' + [regex]::Escape($entry.Name) + '\s*\()'
    $updated = [regex]::Replace($content, $constructorPattern, '$1public $2')
    if ($updated -ne $content) {
        [IO.File]::WriteAllText($entry.Path, $updated, (New-Object Text.UTF8Encoding($false)))
        ++$changed
    }
}

$replacements = @($typeEntries | Sort-Object { $_.OldFqn.Length } -Descending)
$allJavaFiles = Get-ChildItem -LiteralPath $javaRoot -Recurse -File -Filter '*.java'
$changed = 0

foreach ($file in $allJavaFiles) {
    $original = [IO.File]::ReadAllText($file.FullName)
    $content = $original
    $packageMatch = [regex]::Match($original, '(?m)^package\s+([A-Za-z_][\w]*(?:\.[A-Za-z_][\w]*)*)\s*;')
    if (-not $packageMatch.Success) { continue }
    $oldCurrentPackage = $packageMatch.Groups[1].Value

    $moduleEntry = $entries | Where-Object { $_.Path -eq $file.FullName } | Select-Object -First 1
    $newCurrentPackage = if ($moduleEntry) { $moduleEntry.NewPackage } else { $oldCurrentPackage }

    foreach ($replacement in $replacements) {
        $content = $content.Replace($replacement.OldFqn, $replacement.NewFqn)
        $content = $content.Replace($replacement.OldFqn.Replace('.','/'), $replacement.NewFqn.Replace('.','/'))
    }

    if ($moduleEntry) {
        $content = [regex]::Replace(
            $content,
            '(?m)^package\s+[A-Za-z_][\w]*(?:\.[A-Za-z_][\w]*)*\s*;',
            'package ' + $newCurrentPackage + ';',
            1)
    }

    # Moving one historical package into several Module categories breaks the
    # implicit visibility of sibling types. Add explicit imports for every
    # referenced sibling that now lives in another target package.
    $neededImports = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::Ordinal)
    if ($byOldPackage.ContainsKey($oldCurrentPackage)) {
        foreach ($candidate in $byOldPackage[$oldCurrentPackage]) {
            if ($moduleEntry -and $candidate.Path -eq $moduleEntry.Path) { continue }
            if ($candidate.NewPackage -eq $newCurrentPackage) { continue }
            if ([regex]::IsMatch($original, '(?<![\w$])' + [regex]::Escape($candidate.Name) + '(?![\w$])')) {
                [void]$neededImports.Add($candidate.NewFqn)
            }
        }
    }

    if ($neededImports.Count -gt 0) {
        $existingImports = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::Ordinal)
        [regex]::Matches($content, '(?m)^import\s+(?:static\s+)?([^;]+);') | ForEach-Object {
            [void]$existingImports.Add($_.Groups[1].Value.Trim())
        }
        $toInsert = @($neededImports | Where-Object { -not $existingImports.Contains($_) } | Sort-Object)
        if ($toInsert.Count -gt 0) {
            $updatedPackageMatch = [regex]::Match($content, '(?m)^package\s+[^;]+;')
            $importBlock = "`r`n`r`n" + (($toInsert | ForEach-Object { 'import ' + $_ + ';' }) -join "`r`n")
            $content = $content.Insert($updatedPackageMatch.Index + $updatedPackageMatch.Length, $importBlock)
        }
    }

    if ($content -ne $original) {
        [IO.File]::WriteAllText($file.FullName, $content, (New-Object Text.UTF8Encoding($false)))
        ++$changed
    }
}

# Category separation turns types that historically shared a package into a
# public Module API. Make each file's primary type accessible to sibling
# categories; constructors/members remain as restrictive as the compiler allows.
foreach ($entry in $typeEntries) {
    $content = [IO.File]::ReadAllText($entry.Path)
    $typePattern = '(?m)^(\s*)(?!public\s)((?:(?:abstract|final|strictfp)\s+)*)(class|interface|enum)\s+' + [regex]::Escape($entry.Name) + '\b'
    $updated = [regex]::Replace($content, $typePattern, '$1public $2$3 ' + $entry.Name, 1)
    if ($updated -ne $content) {
        [IO.File]::WriteAllText($entry.Path, $updated, (New-Object Text.UTF8Encoding($false)))
        ++$changed
    }
}

"MAPPED_TYPES`t$($typeEntries.Count)"
"CHANGED_FILES`t$changed"
