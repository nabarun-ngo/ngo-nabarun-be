<#
.SYNOPSIS
  List the N largest files under a directory.

.DESCRIPTION
  Scans a directory (optionally recursive) and returns the top N files by size,
  largest first. Output is a table by default, or objects when piped.

.PARAMETER Path
  Directory to search. Defaults to the current working directory.

.PARAMETER Count
  How many of the largest files to return. Default: 10.

.PARAMETER NoRecurse
  Search only the top-level directory (do not recurse into subdirectories).

.PARAMETER Include
  Optional wildcard filter(s), e.g. '*.log','*.zip'. Applied via -Include on Get-ChildItem.

.PARAMETER Exclude
  Optional wildcard filter(s) to skip, e.g. '*.tmp'.

.EXAMPLE
  .\Get-LargestFiles.ps1 -Path C:\data -Count 20

.EXAMPLE
  .\Get-LargestFiles.ps1 -Path . -Count 5 -NoRecurse

.EXAMPLE
  .\Get-LargestFiles.ps1 -Path C:\logs -Count 15 -Include '*.log','*.txt'
#>
[CmdletBinding()]
param(
  [Parameter(Position = 0)]
  [ValidateNotNullOrEmpty()]
  [string]$Path = (Get-Location).Path,

  [Parameter(Position = 1)]
  [ValidateRange(1, [int]::MaxValue)]
  [int]$Count = 10,

  [switch]$NoRecurse,

  [string[]]$Include,

  [string[]]$Exclude
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Test-Path -LiteralPath $Path -PathType Container)) {
  Write-Error "Directory not found: $Path"
  exit 2
}

$resolvedPath = (Resolve-Path -LiteralPath $Path).Path

$gciParams = @{
  LiteralPath = $resolvedPath
  File        = $true
  Force       = $true
  ErrorAction = 'SilentlyContinue'
}

if (-not $NoRecurse) {
  $gciParams['Recurse'] = $true
}

if ($Include -and $Include.Count -gt 0) {
  $gciParams['Include'] = $Include
  # -Include requires a trailing * when used with -LiteralPath / -Path
  $gciParams['LiteralPath'] = Join-Path $resolvedPath '*'
}

if ($Exclude -and $Exclude.Count -gt 0) {
  $gciParams['Exclude'] = $Exclude
}

$files = Get-ChildItem @gciParams |
  Sort-Object -Property Length -Descending |
  Select-Object -First $Count

if (-not $files) {
  Write-Host "No files found under: $resolvedPath"
  exit 0
}

$files |
  Select-Object @{
      Name       = 'SizeMB'
      Expression = { [math]::Round($_.Length / 1MB, 2) }
    },
    @{
      Name       = 'SizeKB'
      Expression = { [math]::Round($_.Length / 1KB, 1) }
    },
    @{
      Name       = 'SizeBytes'
      Expression = { $_.Length }
    },
    Name,
    FullName,
    LastWriteTime |
  Format-Table -AutoSize
