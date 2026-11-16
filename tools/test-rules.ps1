$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
$classesPath = Join-Path $PSScriptRoot 'test-classes'
New-Item -ItemType Directory -Force -Path $classesPath | Out-Null
$studioJava = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr\bin'
$javaBin = if (Test-Path -LiteralPath (Join-Path $studioJava 'javac.exe')) { $studioJava } elseif ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin' } else { throw 'Install JDK 17 or newer and set JAVA_HOME.' }
& (Join-Path $javaBin 'javac.exe') --release 17 -encoding UTF-8 -d $classesPath (Join-Path $projectPath 'app\src\main\java\nl\alina\tovertuin\GameRules.java') (Join-Path $PSScriptRoot 'GameRulesTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
& (Join-Path $javaBin 'java.exe') -cp $classesPath nl.alina.tovertuin.GameRulesTest
if ($LASTEXITCODE -ne 0) { throw 'Game rule checks failed' }
