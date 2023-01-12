# AudioController ![Beta](https://img.shields.io/badge/Status-Beta-yellow) ![Lines of code](https://img.shields.io/tokei/lines/github/realPaulsen/AudioController)


Controls audio of different processes (Can connect to Serial/Arduino)

Currently in **Beta-Version b2**<br>
Reimplementation of the original v1-AudioController with similar UI, but new Linux-Support and a more stable **UI-** and **Serial-Library** 

> *Tip: **Connect an Arduino** to it, so you don't have to tab out of your **Game/Application** to change the volume.*

## IMPORTANT Usage-Notes:

If you want to edit the configs manually (at your own risk), you can find them **inside a Folder** in your **Home-Directory** at `%HOME%/.jaudiocontroller`

### Linux
*Checklist is derived from [jSerialComm-Wiki](https://github.com/Fazecast/jSerialComm/wiki/Troubleshooting)*

1. Before **using USB-Control** you might need to **add your User** to some of those **4 groups**<br>
   Don't worry if some of the commands fail. All of these groups may not exist on every Linux distro.
    ```shell
    sudo usermod -a -G uucp,dialout,tty <your_username>
    ```
    If you are **using SUSE 11.3 or higher**, replace the **'-a -G'** flags with a single **'-A'** flag.


2. **Log out** and you should have **access** to the serial port **after logging back in**

**If Problems still occur check the [_Troubleshooting-Wiki_](https://github.com/Fazecast/jSerialComm/wiki/Troubleshooting)** of the [USB-Library](https://github.com/Fazecast/jSerialComm)

### Windows (***!! Currently Not Working !!***)

*Autoinstall of Python-Script for controlling audio comming soon...*


## Build-Note

* Import latest build of [Java-Project-Library](https://github.com/realPaulsen/Java-Project-Library) into Project before building
* Build python-binary with:
  > pyinstaller --onefile .\Python\WinAudioControl.py

## TODOs

* Reimplement **Windows**-Python-Script into `AudioControllerWin`
* Package Python-Script into binary (pyinstaller)
* add Min- & Max-Values for Processes
* Serial-Protocol for directly controlling a processes' Volume