from __future__ import print_function
import sys

import os

try:
    from ctypes import (HRESULT, POINTER, Structure, Union, c_float, c_longlong,
                    c_uint32)
except ImportError:
    print("importerror")
    os.system('pip install ctypes')
    # TODO check on Windows

from pycaw import pycaw
from pycaw.pycaw import ISimpleAudioVolume, AudioUtilities

def mainLoop():
    while True:
        s = sys.stdin.readline().strip()
        message = getProtocolMessage(s)
        if not message.__eq__("null"):
            processMessage(message)


def processMessage(message):
    number = ""
    processName = ""
    isSplitter = False
    hasBeenEnd = False

    for c in message[::-1]:
        if c.__eq__(">") and (not isSplitter) and (not hasBeenEnd):
            isSplitter = True
            hasBeenEnd = True

        if not hasBeenEnd:
            number = c + number

        if (not isSplitter) and hasBeenEnd:
            processName = c + processName

        if c.__eq__("|") and isSplitter:
            isSplitter = False

    numNew = 0.0
    isFloat = True
    try:
        numNew = float(number)
    except:
        isFloat = False

    if isFloat and processName:
        setVolume(processName, numNew)


def setVolume(processName, value):
    if value > 1:
        value = 1
    if value < 0:
        value = 0

    sessions = AudioUtilities.GetAllSessions()
    for session in sessions:
        volume = session._ctl.QueryInterface(ISimpleAudioVolume)
        if session.Process:
            if session.Process.name().__eq__(processName):
                volume.SetMasterVolume(value, None)


def outputAudioInfo():
    sessions = pycaw.AudioUtilities.GetAllSessions()
    for session in sessions:
        volume = session._ctl.QueryInterface(pycaw.ISimpleAudioVolume)
        if session.Process:
            print("[PAC]::AudioProcess[" + str(session.Process.name()).replace("[", "^<<^").replace("]", "^>>^") + "|=>"
                  + str(volume.GetMasterVolume()) + "]") # PAC = PythonAudioControl


def getProtocolMessage(input):
    if str(input).startswith("[PAC]::AudioProcess[") and str(input).endswith("]"):
        s = input[20:-1]
        return str(s).replace("^<<^", "[").replace("^>>^", "]")
    elif str(input).__eq__("[PAC]::request[exit]"):
        outputAudioInfo()
        sys.exit()
        return exit
    return "null"


if __name__ == "__main__":
    mainLoop()
    input()