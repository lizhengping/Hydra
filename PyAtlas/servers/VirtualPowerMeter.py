__author__ = 'Hwaipy'

from InstrumentServers import PowerMeterServer

__clientName__ = 'VirtualPowerMeter_Hwaipy'
__serverPort__ = 20102
__serverAddress__ = 'localhost'

if __name__ == '__main__':
    print('{} is started.'.format(__clientName__))
    VPM = PowerMeterServer(__clientName__)
    VPM.start()
