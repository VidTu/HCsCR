:: HCsCR is a third-party mod for Minecraft Java Edition
:: that allows removing the end crystals faster.
::
:: Copyright (c) 2023 Offenderify
:: Copyright (c) 2023-2026 VidTu
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::     http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
::
:: SPDX-License-Identifier: Apache-2.0

:: Disable echo.
@echo off

:: Check args.
if "%~1"=="" (
    :: Fail.
    echo SCRIPT: You must specify the mode of execution or the version to build.
    echo SCRIPT: Normal ^(Beta/Active^): compile.cmd normal
    echo SCRIPT: Legacy ^(Beta/Active/Legacy^): compile.cmd legacy
    echo SCRIPT: Single: compile.cmd ^<version^> ^(e.g., "compile.cmd 1.20.1-fabric"^)
    exit /B 2
)

:: Select execution mode.
if /I "%~1"=="legacy" (
    :: Build in legacy mode.
    echo SCRIPT: Building in legacy mode...
    cmd.exe /C gradlew.bat -Dru.vidtu.hcscr.legacy=true assemble
    echo SCRIPT: Building in legacy mode exited with code %ERRORLEVEL%.
    exit /B 0
)
if /I "%~1"=="normal" (
    :: Build in normal mode.
    echo SCRIPT: Building in normal mode...
    cmd.exe /C gradlew.bat assemble
    echo SCRIPT: Building in normal mode exited with code %ERRORLEVEL%.
    exit /B 0
)
:: Build in single mode.
echo SCRIPT: Building in single ^('%1'^) mode...
cmd.exe /C gradlew.bat "-Dru.vidtu.hcscr.single=%1" "%1:assemble"
echo SCRIPT: Building in single ^('%1'^) mode exited with code %ERRORLEVEL%.
