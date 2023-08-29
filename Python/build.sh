#!/bin/bash
pipenv run pyinstaller --onefile WinAudioControl.py --distpath target --workpath target/pybuild
exit 0