from __future__ import print_function
import sys
from pycaw import pycaw
from pycaw.pycaw import ISimpleAudioVolume, AudioUtilities

protocolName: str = "wac"


def mainLoop():
    while True:
        message = getProtocolMessage(sys.stdin.readline().strip())

        if message.__eq__("exit"):
            outputAudioInfo()
            exit(0)

        if not message.__eq__("null"):
            processMessage(message)


def processMessage(message):
    volume = ""
    processName = ""
    hasBeenEnd = False

    for c in message[::-1]:

        if not hasBeenEnd:
            if c.__eq__("|"):
                hasBeenEnd = True
            else:
                volume = c + volume
        else:
            processName = c + processName

    try:
        newVol = float(volume)

        if processName:
            setVolume(processName, newVol)
    except:
        print(volume + " is no volume")


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


# Prints all Processes with their volume
def outputAudioInfo():
    sessions = pycaw.AudioUtilities.GetAllSessions()
    for session in sessions:
        volume = session._ctl.QueryInterface(pycaw.ISimpleAudioVolume)
        if session.Process:
            print(protocolName + "[" + str(session.Process.name()).replace("[", "^<<^").replace("]", "^>>^") + "|"
                  + str(volume.GetMasterVolume()) + "]")


def getProtocolMessage(input: str):
    if input.__eq__(protocolName + "[exit]"):  # AudioProtocolRequest
        return "exit"
    if input.startswith(protocolName + "[") and input.endswith("]"):
        # Convert placeholder to "[" "]" in message
        return input[4:-1].replace("^<<^", "[").replace("^>>^", "]")

    return "null"


if __name__ == "__main__":
    mainLoop()
