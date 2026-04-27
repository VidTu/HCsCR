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
    echo SCRIPT: You must specify the version to launch.
    echo Example: launch.cmd 1.16.5-fabric
    exit /B 2
)

:: Launch.
echo SCRIPT: Launching '%1'...
cmd.exe /C gradlew.bat "-Dru.vidtu.hcscr.only=%1" "%1:runClient"
echo SCRIPT: Launch for '%1' exited with code %ERRORLEVEL%.
