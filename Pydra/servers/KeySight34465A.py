__author__ = 'Hwaipy'

import time
import jpype
import os
import math
import random

class KeySight34465A:
    def __init__(self, visa):
        self.visa = visa

    def getVersion(self):
        return self.visa.query('SYSTem:VERSion', [])

    def measure(self):
        self.visa.write('CONF:VOLT:DC',False, [1])
        self.visa.write('TRIG:SOURCE',False, ['BUS'])
        self.visa.write('VOLT:APER',False, [0.001])
        self.visa.write('SAMP:COUN',False, [200])
        self.visa.write('INIT',False, [])
        self.visa.write('*TRG',False, [])
        return self.visa.query('FETC', [])


if __name__ == '__main__':
    try:
        print('begin')
        jpype.startJVM(jpype.getDefaultJVMPath(),
                       '-Djava.class.path=F:\\AD-DA\\Code\\LabAtlas\\JAtlas\\VISA\\target\\classes')
        VISA = jpype.JClass('com.hwaipy.jatlas.visa.VISA')
        visa = VISA.openSocketResource('192.168.99.156', 5025)
        dm = KeySight34465A(visa)
        print('Software version: {}'.format(dm.getVersion()))
        st = time.time()
        for i in range(1):
            (dm.measure())
        print(time.time()-st)
        #print('There are {} waveforms: {}'.format(awg.getWaveformListSize(), awg.listWaveforms()))
        #print('Waveform {} {}.'.format('"{}"'.format(name), 'exists' if awg.isWaveformExists(name) else 'not exists'))
    except jpype.JavaException as e:
        print(e.message())
        print(e.stacktrace())

    time.sleep(2)