
# AudioController ![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/realPaulsen/audiocontroller?include_prereleases) ![Beta](https://img.shields.io/badge/Status-Beta-yellow) ![GitHub](https://img.shields.io/github/license/realPaulsen/AudioController) ![GitHub top language](https://img.shields.io/github/languages/top/realPaulsen/AudioController)

- Beta In Development: ![GitHub last commit (branch)](https://img.shields.io/github/last-commit/realPaulsen/audiocontroller/v2_in_development)
- Releasebranch: ![GitHub last commit (branch)](https://img.shields.io/github/last-commit/realPaulsen/audiocontroller/Release)

Controls audio of different processes (Can connect to Serial/Arduino)

___

Reimplementation of the original v1-AudioController with similar UI, but new Linux-Support and a more stable **UI-** and **USB-Serial-Library** 

> *Tip: **Connect an Arduino** to it, so you don't have to tab out of your **Game/Application** to change the volume.*

## Usage:

If you want to edit the configs manually (at your own risk), you can find them **inside a Folder** in your **Home-Directory** at `%HOME%/.jaudiocontroller`

### Linux

1. Before **using USB-Control** you might need to **add your User** to some of those **4 groups**<br>
   Don't worry if some of the commands fail. All of these groups may not exist on every Linux distro.
    ```shell
    sudo usermod -a -G uucp,dialout,tty <your_username>
    ```
    If you are **using SUSE 11.3 or higher**, replace the **'-a -G'** flags with a single **'-A'** flag.


2. **Log out** and you should have **access** to the serial port **after logging back in**

*If Problems still occur check the [**Troubleshooting-Wiki**](https://github.com/Fazecast/jSerialComm/wiki/Troubleshooting) of the [USB-Library](https://github.com/Fazecast/jSerialComm)*

### Windows

Just download the latest `.jar` and run it.<br>
It should automatically download/update any files needed.

## Build-Note

* Import latest build of [Java-Project-Library](https://github.com/realPaulsen/Java-Project-Library) into Project before building
* Build python-binary with:
  > pyinstaller --onefile .\Python\WinAudioControl.py


## TODOs

* add Min- & Max-Values for Processes
* Serial-Protocol for directly controlling a processes' Volume