$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'export_semiconductor_textures.ps1') -AssetDirectory 'docs/assets/tier-modules'
