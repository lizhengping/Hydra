__author__ = 'Hwaipy'

from LabAtlas import Session


class InstrumentServer:
    def __init__(self, name, address=('localhost', 20102), services=[], commanders={}):
        self.name = name
        self.address = address
        self.services = services
        self.commanders = commanders

    def start(self, async=False):
        self.session = Session(self.name, self.address, self.services, self.commanders)
        self.session.start(async)

    def sendMessageLaser(self,messae):
        raise

class PowerMeterServer(InstrumentServer):
    def __init__(self, name, address=('localhost', 20102)):
        super().__init__(name, address, ['PowerMeter'], {})
