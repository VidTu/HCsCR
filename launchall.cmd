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

:: Set local variable scope. (enable delayed expansions)
setlocal enabledelayedexpansion

:: Iterate.
echo SCRIPT: Launching all versions...
for /D %%f in (versions\*) do (
    :: Skip, if ".ignored" exists.
    if exist %%f\.ignored (
        echo SCRIPT: Skipping '%%~nxf' because of the '.ignored' file.
    ) else (
        :: Launch.
        echo SCRIPT: Launching '%%~nxf'...
        cmd.exe /c gradlew.bat "-Dru.vidtu.hcscr.only=%%~nxf" "%%~nxf:runClient"
        echo SCRIPT: Launch for '%%~nxf' exited with code !ERRORLEVEL!.
        if not !ERRORLEVEL!==0 (
            echo SCRIPT: Non-zero exit code.
            pause
        )
    )
)
echo SCRIPT: Done launching all versions.

:: End local variable scope.
endlocal
