; Define the installer name, output, and installation directory
OutFile "MyAppInstaller.exe"
InstallDir $PROGRAMFILES\MyApp

; Define the default folder for the application
Var APPDATAFOLDER
Var CREATE_SHORTCUTS

; Define the sections of the installer
Section "MainSection" SEC01

    ; Request the installation directory
    InstallDirRegKey HKLM "Software\MyApp" "Install_Dir"

    ; Ask the user for the installation directory
    nsDialogs::Create 1018
    Pop $0

    ${NSD_CreateDirRequest} 0 0 100% 12u "$INSTDIR"
    Pop $INSTDIR

    nsDialogs::Show

    ; Set the output path to the chosen installation directory
    SetOutPath $INSTDIR

    ; Copy the Python scripts to the installation directory
    File /r "myapp\*.*"

    ; Copy the portable Python interpreter to the installation directory
    File /r "portable_python\*.*"

    ; Set the Python interpreter path
    SetEnvVar "PYTHONPATH" "$INSTDIR\portable_python"

    ; Run pip to install dependencies from requirements.txt
    ExecWait '"$INSTDIR\portable_python\python.exe" -m pip install -r "$INSTDIR\myapp\requirements.txt"'

    ; Write the installation directory to the registry
    WriteRegStr HKLM "Software\MyApp" "Install_Dir" "$INSTDIR"
    WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

; Section to ask user whether to create shortcuts
Section "ShortcutOptions" SEC02

    ; Create a checkbox for creating shortcuts
    nsDialogs::Create 1018
    Pop $0

    ${NSD_CreateCheckbox} 0 0 100% 12u "Create Desktop and Start Menu Shortcuts"
    Pop $CREATE_SHORTCUTS
    ${NSD_Check} $CREATE_SHORTCUTS

    nsDialogs::Show

    ; If the user checked the box, create shortcuts
    ${If} ${NSD_GetState} $CREATE_SHORTCUTS
        ; Create Desktop shortcut
        CreateShortCut "$DESKTOP\MyApp.lnk" "$INSTDIR\portable_python\python.exe" "$INSTDIR\myapp\main.py" "" "$INSTDIR\myapp\app_icon.ico"

        ; Create Start Menu shortcut
        CreateDirectory "$SMPROGRAMS\MyApp"
        CreateShortCut "$SMPROGRAMS\MyApp\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
        CreateShortCut "$SMPROGRAMS\MyApp\MyApp.lnk" "$INSTDIR\portable_python\python.exe" "$INSTDIR\myapp\main.py" "" "$INSTDIR\myapp\app_icon.ico"
    ${EndIf}

SectionEnd

; Section for the user to select the default data folder
Section "SelectDataFolder" SEC03

    ; Ask the user to select the data folder
    nsDialogs::Create 1018
    Pop $0

    ${NSD_CreateDirRequest} 0
    Pop $APPDATAFOLDER

    nsDialogs::Show

    ; Write the selected folder path to a config file
    FileOpen $0 "$INSTDIR\config.json" w
    FileWrite $0 '{"data_folder": "$APPDATAFOLDER"}'
    FileClose $0

SectionEnd

; Section to create an uninstaller
Section "Uninstall"

    ; Remove the installation directory and its contents
    RMDir /r "$INSTDIR"

    ; Remove the registry entry
    DeleteRegKey HKLM "Software\MyApp"

    ; Remove the desktop shortcut if it was created
    Delete "$DESKTOP\MyApp.lnk"

    ; Remove Start Menu shortcuts
    RMDir /r "$SMPROGRAMS\MyApp"

SectionEnd
