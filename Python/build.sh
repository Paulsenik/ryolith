#!/bin/bash
pipenv run pyinstaller WinAudioControl.py --clean --noconfirm --onefile --onedir --log-level=WARN --distpath target --workpath target/build
exit 0