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

:: Set local variable scope.
setlocal

:: Check args.
if /i "%~1"=="legacy" (
    :: Build in legacy.
    echo SCRIPT: Building in legacy mode...
    cmd.exe /c gradlew.bat -Dru.vidtu.hcscr.legacy=true assemble
    echo SCRIPT: Building in legacy mode exited with code %ERRORLEVEL%.
    goto :end
)
if /i "%~1"=="normal" (
    :: Build in normal.
    echo SCRIPT: Building in normal mode...
    cmd.exe /c gradlew.bat assemble
    echo SCRIPT: Building in normal mode exited with code %ERRORLEVEL%.
    goto :end
)
echo ERROR: You must specify the mode of execution.
echo Normal (Beta/Active): compileall.cmd normal
echo Legacy (Beta/Active/Legacy): compileall.cmd legacy

:end
:: End local variable scope.
endlocal
