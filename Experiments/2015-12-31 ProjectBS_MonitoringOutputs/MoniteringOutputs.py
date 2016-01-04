__author__ = 'Hwaipy'

import os
import sys
import pyvisa
import time

def loadDevices():
    import servers.Thorlabs_PowerMeter as tpm
    devicePM320 = tpm.PM320('USB0::0x1313::0x8022::M00236664::INSTR')
    devicePM320.setWavelength(1,895)
    devicePM320.setWavelength(2,895)
    devicePM320.setAutoRange(1)
    devicePM320.setAutoRange(2)
    devicePM200_A = tpm.PM200('USB0::0x1313::0x80B0::P3000934::INSTR')
    devicePM200_A.setWavelength(895)
    devicePM200_A.setAutoRange(True)
    devicePM200_B = tpm.PM200('USB0::0x1313::0x80B0::P3000997::INSTR')
    devicePM200_B.setWavelength(895)
    devicePM200_B.setAutoRange(True)
    import servers.AdvantechADC_PCI1742U as adc
    deviceADC = adc.AdvantechADC_PCI1742U()

    dc320_1 = (devicePM320.measure, [1])
    dc320_2 = (devicePM320.measure, [2])
    dc200_A = (devicePM200_A.measure, [])
    dc200_B = (devicePM200_B.measure, [])
    dcAdc_1 = (deviceADC.readAnalogVoltage, [0])
    dcAdc_2 = (deviceADC.readAnalogVoltage, [4])
    dcAdc_3 = (deviceADC.readAnalogVoltage, [2])
    dcAdc_4 = (deviceADC.readAnalogVoltage, [3])
    return [dc200_A,dc200_B,dc320_1,dc320_2,dcAdc_1,dcAdc_2,dcAdc_3,dcAdc_4]

def measureLoop(invokers):
    fileName = 'E://Data//2015-12-31 MonitoringOutputs//data{}.txt'.format(time.time())
    for i in range(3600*24*365):
        time.sleep(1)
        powers = []
        for invoker in invokers:
            value = invoker[0](*invoker[1])
            powers.append(value)
        tp = sum(powers)
        print(powers)
        file = open(fileName,'a')
        file.write('{}\n'.format(powers))
        file.close()

if __name__ == '__main__':
    print('Experiment ProjectBS Monitering Outputs.')
    print(pyvisa.ResourceManager().list_resources())
    sys.path.append('..\\..\\Pydra\\')
    deviceChannels = loadDevices()
    print('{} divece channels are loaded.'.format(len(deviceChannels)))
    measureLoop(deviceChannels)
