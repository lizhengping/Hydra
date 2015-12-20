__author__ = 'Hwaipy'

import time
import jpype
import os
import math
import random

class AWG70K:
    def __init__(self, visa):
        self.visa = visa

    def getVersion(self):
        return self.visa.query('SYSTem:VERSion', [])

    def getWaveformListSize(self):
        return self.visa.query('WLISt:SIZE', [])

    def getWaveformName(self, index):
        assert isinstance(index, int)
        return self.visa.query('WLISt:NAME', [index])

    def listWaveforms(self):
        size = self.getWaveformListSize()
        print(type(size + 1))
        return [self.getWaveformName(i) for i in range(size)]

    def isWaveformExists(self, name):
        assert isinstance(name, str)
        return self.listWaveforms().__contains__(name)

    def createWaveform(self, name, length):
        assert isinstance(name, str)
        assert isinstance(length, int)
        print('c')
        visa.write('WLISt:WAVeform:NEW', False, [name, length])

    def test(self):
        visa.query('WLISt:WAVeform:DATA', [name])


class AWG5002C_E:
    def __init__(self, visa):
        self.visa = visa

    def getVersion(self):
        return self.query('SYSTem:VERSion', [])

    def getWaveformListSize(self):
        return self.query('WLISt:SIZE', []).longValue()

    def getWaveformName(self, index):
        assert isinstance(index, int)
        return self.query('WLISt:NAME', [index])

    def listWaveforms(self):
        size = self.getWaveformListSize()
        return [self.getWaveformName(i) for i in range(size)]

    def isWaveformExists(self, name):
        assert isinstance(name, str)
        return self.listWaveforms().__contains__(name)

    def createWaveform(self, name, length):
        assert isinstance(name, str)
        assert isinstance(length, int)
        self.write('WLISt:WAVeform:NEW', ['"{}"'.format(name), length, 'INTEGER'])

    def setWaveformData(self, name, data):
        dataA = self.visa.argument(data)
        self.write('WLISt:WAVeform:DATA', ['"{}"'.format(name), 0, len(data), dataA])

    def test(self):
        print(self.query('SEQUENCE:LENGTH', []))
        size = 1000
        self.write('SEQUENCE:LENGTH', [0])
        self.write('SEQUENCE:LENGTH', [size])
        for i in range(1,size+1):
            self.write('SEQUENCE:ELEMENT{}:WAVEFORM1'.format(i), ['"HTest-{}"'.format(random.randint(1,10))])
            self.write('SEQUENCE:ELEMENT{}:WAVEFORM2'.format(i), ['"HTest-{}"'.format(random.randint(1,10))])

    def query(self, head, arguments):
        a = self.visa.query(head, arguments)
        return self.visa.query('*IDN',[])

    def write(self, head, arguments):
        self.visa.write(head, False, arguments)

def generateSineWaveform(length, period):
    data = [int((math.sin(i/period*2*math.pi)+1)*4192)  for i in range(length)]
    return data

def generateSqureWaveform(length, high, low):
    highLen = int(length/2)
    data = [high]*highLen+[low]*(length-highLen)
    return data

def generateConstant(length, v):
    return [v]*length

def generateTestWaveform(length):
    assert length%10==0
    src = [0,1,2,3,4,5,6,7,8,9,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30]
    data = [src[random.randint(0,len(src)-1)] for i in range(length)]
    return data

if __name__ == '__main__':
    try:
        print('begin')
        jpype.startJVM(jpype.getDefaultJVMPath(),
                       '-Djava.class.path=F:\\AD-DA\\Code\\LabAtlas\\JAtlas\\VISA\\target\\classes')
        VISA = jpype.JClass('com.hwaipy.jatlas.visa.VISA')
        visa = VISA.openSocketResource('192.168.99.153', 4001)
        awg = AWG5002C_E(visa)
        name = 'HTest'
        length = 100
        print('Software version: {}'.format(awg.getVersion()))
        #print('There are {} waveforms: {}'.format(awg.getWaveformListSize(), awg.listWaveforms()))
        #print('Waveform {} {}.'.format('"{}"'.format(name), 'exists' if awg.isWaveformExists(name) else 'not exists'))


        if not awg.isWaveformExists(name):
            awg.createWaveform(name, length)
            for i in range(10):
                awg.createWaveform('{}-{}'.format(name,i+1), length)

        #waveforms = [generateSqureWaveform(length,16000,192) if i <=5 else generateSqureWaveform(length,8000,192) for i in range(10)]
        waveforms = [generateConstant(length,16000) for i in range(10)]
        print('Starting transfer waveforms')
        for i in range(1):
            awg.setWaveformData('{}-{}'.format(name,i+1),waveforms[i])
            print('Waveform[{}] transfored.'.format(i))

        #awg.test()

    # visa.write('WLISt:WAVeform:DATA', False,
    #          ['ne3ww', 0, 10, jpype.JArray(jpype.JFloat, 1)([0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0])])
    # visa.query('WLISt:WAVeform:DATA', ['ne3ww', 0, 10])
    #    print(visa.query('WLISt:WAVeform:LENGth', ['neww']))
    except jpype.JavaException as e:
        print(e.message())
        print(e.stacktrace())

    time.sleep(2)