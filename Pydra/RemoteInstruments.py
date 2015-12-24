__author__ = 'Hwaipy'

from Hydra import Session, Message
import time


class RemoteInstrument:
    def __init__(self, name, address=('localhost', 20102), commanders={}):
        self.name = name
        self.address = address
        self.commanders = commanders

    def start(self, async=False):
        self.session = Session(self.name, self.address, [], self.commanders)
        self.session.start(async)

    def request(self, message, async=True):
        if not isinstance(message, Message):
            raise RuntimeError('Not a message.')
        if async:
            self.session.sendMessageLater(message)
        else:
            raise

# class RemotePowerMeter(RemoteInstrument):
#    def __init__(self):
#        super().__init__()
