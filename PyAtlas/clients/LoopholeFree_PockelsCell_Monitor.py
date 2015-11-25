__author__ = 'Hwaipy'

from LabAtlas import Session, Message
from RemoteInstruments import RemoteInstrument
import time
import threading


class PCControllerMonitor(RemoteInstrument):
    def __init__(self, name, address, targetA, targetB):
        super().__init__(name, address, {'Measure': self.__remoteMeasure__})
        self.targetA = targetA
        self.targetB = targetB

    def measure(self):
        message = Message.createRequest('Measure', {'Target': self.targetA})
        super().request(message)

    def output(self, on):
        if not isinstance(on, bool):
            raise RuntimeError('Should be boolean.')
        message = Message.createRequest('Output', {'Target': self.targetA, 'Status': '{}'.format(on).lower()})
        super().request(message)

    def __remoteMeasure__(self, message):
        voltages = message.content.__getitem__('Voltages')
        currents = message.content.__getitem__('Currents')
        source = message.content.__getitem__('Source')
        print('--------{}------------'.format(source))
        for i in range(6):
            print('{}V, {}A'.format(voltages[i], currents[i]))
        print('--------------------------------------------')


__clientName__ = 'PC Monitor 2'
__serverPort__ = 20102
__serverAddress__ = '172.16.60.199'

if __name__ == '__main__':
    print('{} started.'.format(__clientName__))
    monitor = PCControllerMonitor(__clientName__, (__serverAddress__, __serverPort__), 'PC Controller Alice',
                                  'PC Controller Bob')
    monitor.start(True)
    monitor.measure()
    monitor.output(False)
    for i in range (20):
        time.sleep(0.5)
        monitor.measure()


    time.sleep(100000)
