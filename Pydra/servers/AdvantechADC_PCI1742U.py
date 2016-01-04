__author__ = 'Hwaipy'
import sys
import jpype
from InstrumentServers import InstrumentServer

class AdvantechADC_PCI1742U:
    def __init__(self):
        jpype.startJVM(jpype.getDefaultJVMPath(),'-Djava.class.path=..\\..\\Jydra\\AdvantechADCPCI\\build\\classes\\;..\\..\\Jydra\\AdvantechADCPCI\\lib\\jna-3.0.9.jar')
        deviceManager = jpype.JClass('com.hwaipy.unifieddeviceinterface.adc.advantech.AdvantechADCDeviceManager').getManager()
        self.jpypeObject = deviceManager.getDeviceList()[0]
        self.jpypeObject.open()

    def readAnalogVoltage(self,channel):
        if channel>=0 and channel<16:
            return self.jpypeObject.analogIOReadVoltage(channel)
        else:
            raise

def runAsService():
    jpype.startJVM(jpype.getDefaultJVMPath(),'-Djava.class.path=E:\\Coding\\Examples\\Programs\\CODE\\AdvantechADCPCI\\build\\classes\\;E:\\Coding\\Examples\\Programs\\CODE\\Lib\\Native\\jna-3.0.9.jar')
    deviceManager = jpype.JClass('com.hwaipy.unifieddeviceinterface.adc.advantech.AdvantechADCDeviceManager').getManager()
    device0 = deviceManager.getDeviceList()[0]
    device0.open()
    device = AdvantechADC_PCI1742U(device0)
    server = InstrumentServer('R22_AdvantechADC_PCI1742U',address=('172.16.60.199',20102),invokers=[device])
    server.start()

if __name__ == '__main__':
    runAsService()
    try:
        jpype.startJVM(jpype.getDefaultJVMPath(),
                       '-Djava.class.path=E:\\Coding\\Examples\\Programs\\CODE\\AdvantechADCPCI\\build\\classes\\;E:\\Coding\\Examples\\Programs\\CODE\\Lib\\Native\\jna-3.0.9.jar')
        deviceManager = jpype.JClass('com.hwaipy.unifieddeviceinterface.adc.advantech.AdvantechADCDeviceManager').getManager()
        deviceList = deviceManager.getDeviceList()
        device0 = deviceList[0]
        device0.open()
        print(device0.analogIOReadVoltage(0))
        print(device0.analogIOReadVoltage(2))
        device0.close()
        sys.exit(0)
    except jpype.JavaException as e:
        print(e.message())
        print(e.stacktrace())