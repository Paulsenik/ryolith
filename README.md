# AudioController ![Beta](https://img.shields.io/badge/Status-Beta-yellow)
Controls audio of different processes (Can connect to Serial/Arduino)

Currently in **Beta-Version b2**<br>
Reimplementation of the original v1-AudioController with similar UI, but new Linux-Support and a more stable **UI-** and **Serial-Library** 


## IMPORTANT Usage-Notes:

### Linux
*Checklist is derived from [jSerialComm-Wiki](https://github.com/Fazecast/jSerialComm/wiki/Troubleshooting)*

1. Before **using USB-Control** you might need to **add your User** to some of those **4 groups**<br>
   Don't worry if some of the commands fail. All of these groups may not exist on every Linux distro.
    ```shell
    sudo usermod -a -G uucp <your_username>
    sudo usermod -a -G dialout <your_username>
    sudo usermod -a -G lock <your_username>
    sudo usermod -a -G tty <your_username>
    ```
    If you are **using SUSE 11.3 or higher**, replace the **'-a -G'** flags with a single **'-A'** flag.


2. **Log out** and you should have **access** to the serial port **after logging back in**

**If Problems still occur check the [_Troubleshooting-Wiki_](https://github.com/Fazecast/jSerialComm/wiki/Troubleshooting)** of the [USB-Library](https://github.com/Fazecast/jSerialComm)

### Windows (***!! Currently Not Working !!***)



1. Pyhton-Script needs **additional modules**. Just try to run in console, see what's missing and install it via `pip`😅🤦

2. Create those Folders and put the `pycaw`-Folder and `WinAudioControl.py` in the `Python`-Folder
   > AudioController.jar <br>
   > > AudioController/Python/ <br>
   > > *--put pycaw and script in here--*


*Autoinstall of modules in Python-Script comming soon...*


## Build-Note

* Import latest build of [Java-Project-Library](https://github.com/realPaulsen/Java-Project-Library) into Project before building

## TODO

* implement **communication** between `UI` and `AudioManager`
* reimplement **Windows**-Python-Script into `AudioControllerWin`
* implement **Windows**-Python-Install that installs all Python-Modules
