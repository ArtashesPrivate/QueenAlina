# Rebuilds the web game into a debug APK, installs it on the running emulator and checks that every game fits one screen.
# Usage: powershell tools/dev-audit.ps1 [-Landscape] [-Tab spel] [-Level 6] [-NoBuild]
param([switch]$Landscape, [string]$Tab = "", [int]$Level = 0, [switch]$NoBuild)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not $NoBuild) {
    python "$root\tools\build-web.py"
    if ($LASTEXITCODE) { throw "build-web failed" }
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    $ErrorActionPreference = "Continue"
    & "$root\gradlew.bat" -q -p $root "-PalinaBuildDir=$env:LOCALAPPDATA\alina-build" assembleDebug 2>$null
    $ErrorActionPreference = "Stop"
    if ($LASTEXITCODE) { throw "build failed" }
    & $adb install -r "$env:LOCALAPPDATA\alina-build\outputs\apk\debug\app-debug.apk" | Out-Null
}
& $adb shell settings put system accelerometer_rotation 0
$rotation = if ($Landscape) { "1" } else { "0" }
& $adb shell settings put system user_rotation $rotation
& $adb shell input keyevent 224
& $adb shell am force-stop nl.alina.tovertuin
& $adb shell am start -n nl.alina.tovertuin/.MainActivity | Out-Null
Start-Sleep 7
$env:ADB = $adb
$a = @(); if ($Tab) { $a += $Tab }; if ($Level) { $a += "--level"; $a += "$Level" }
node "$root\tools\fit-audit.js" @a
