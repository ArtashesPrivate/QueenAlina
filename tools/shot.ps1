# Opens one game in the running debug app and saves a small screenshot: powershell tools/shot.ps1 "Vrij kleuren" out.png
param([string]$Name, [string]$Out, [int]$Level = 0)
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$env:ADB = $adb
if ($Name) { if ($Level) { node "$PSScriptRoot\fit-audit.js" --open $Name --level $Level } else { node "$PSScriptRoot\fit-audit.js" --open $Name } }
& $adb shell screencap -p /sdcard/qa.png
& $adb pull /sdcard/qa.png "$env:TEMP\qa-full.png" | Out-Null
python -c "from PIL import Image;im=Image.open(r'$env:TEMP\qa-full.png');im.thumbnail((900,900));im.save(r'$Out')"
