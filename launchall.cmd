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

:: Iterate.
echo SCRIPT: Launching all versions...
for /D %%F in (versions\*) do (
    :: Launch.
    echo SCRIPT: Launching '%%~nxF'...
    cmd.exe /c gradlew.bat "-Dru.vidtu.hcscr.only=%%~nxF" "%%~nxF:runClient"
    echo SCRIPT: Launch for '%%~nxF' exited with code !ERRORLEVEL!.
    if not !ERRORLEVEL!==0 (
        echo SCRIPT: Non-zero exit code. Press any key to continue, terminate ^(CTRL+C^) to cancel.
        pause >nul
    )
)
echo SCRIPT: Done launching all versions.

:: End local variable scope.
endlocal
