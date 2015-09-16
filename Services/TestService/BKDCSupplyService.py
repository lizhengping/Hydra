__author__ = 'Hwaipy'
import serial
import queue
import threading
import Utils
import time
import LabAtlas


class BKDCSupplyService:
    def __init__(self, name, com):
        self.runner = LabAtlas.ClientRunner(name, services=["DC Supply"],
                                            commander={"Set": self.cmdSet,
                                                       "Output": self.cmdOutput,
                                                       "Measure": self.cmdMeasure,
                                                       "Remote": self.cmdRemote})
        self.dcSupply = BKDCSupply(com)
        self.name = name

    def start(self):
        self.runner.start()

    def cmdSet(self, message):
        print(message.content)
        voltages = message.content.get("Voltages")
        currents = message.content.get("Currents")
        for i in range(3):
            self.dcSupply.setVoltage(i, voltages[i])
            self.dcSupply.setCurrent(i, currents[i])
        self.dcSupply.__applyVoltage__()
        time.sleep(0.5)
        self.dcSupply.__applyCurrent__()
        time.sleep(0.5)

    def cmdOutput(self, message):
        print(message.content)
        outputs = message.content.get("Outputs")
        for i in range(3):
            if outputs[i]:
                self.dcSupply.turnOn(i)
            else:
                self.dcSupply.turnOff(i)
        self.dcSupply.__applyOutput__()
        time.sleep(1)

    def cmdMeasure(self, message):
        results = self.dcSupply.measureAll()
        response = message.response()
        response.content.__setitem__("Voltages", results[0])
        response.content.__setitem__("Currents", results[1])
        response.content.__setitem__("Source", self.name)
        self.runner.send(response)

    def cmdRemote(self, message):
        self.dcSupply.remote()


class BKDCSupply:
    def __init__(self, port):
        self.underlying = DCSupplyBKUnderlying(port)
        self.voltages = [0, 0, 0]
        self.currents = [0, 0, 0]
        self.outputs = [0, 0, 0]
        threading._start_new_thread(self.underlying.start, ())

    def remote(self):
        self.underlying.remote()

    def setVoltage(self, channel, level):
        if channel >= 0 & channel < 3:
            self.voltages[channel] = level

    def setCurrent(self, channel, level):
        if channel >= 0 & channel < 3:
            self.currents[channel] = level

    def turnOn(self, channel):
        if channel >= 0 & channel < 3:
            self.outputs[channel] = True

    def turnOff(self, channel):
        if channel >= 0 & channel < 3:
            self.outputs[channel] = False

    def __applyVoltage__(self):
        self.underlying.setVoltage(self.voltages)

    def __applyCurrent__(self):
        self.underlying.setCurrent(self.currents)

    def __applyOutput__(self):
        self.underlying.setOutput(self.outputs)

    def measureAll(self):
        return self.underlying.measure()


class DCSupplyBKUnderlying:
    def __init__(self, port):
        self.serial = serial.Serial(port, 9600, timeout=1)
        self.communicator = Utils.BlockingCommunicator(self.serial, self.dataFetcher, self.dataSender)

    def start(self):
        self.communicator.start()

    def dataFetcher(self, serialChannel):
        while True:
            data = serialChannel.readline()
            if data.__len__() > 0:
                return str(data, encoding='utf-8')

    def dataSender(self, serialChannel, message):
        serialChannel.write(message.encode('utf-8'))

    def version(self):
        return self.communicator.query('SYSTem:VERSion?\n')

    def remote(self):
        self.communicator.sendLater('SYSTem:REMote\n')

    def setOutput(self, outputs):
        outputCodeString = ['1' if o else '0' for o in outputs]
        outputCode = ','.join(outputCodeString)
        self.communicator.sendLater('APP:OUT {}\n'.format(outputCode))

    def setVoltage(self, voltages):
        outputCodeString = ['{}'.format(v) for v in voltages]
        outputCode = ','.join(outputCodeString)
        self.communicator.sendLater('APP:VOLT {}\n'.format(outputCode))

    def setCurrent(self, currents):
        outputCodeString = ['{}'.format(i) for i in currents]
        outputCode = ','.join(outputCodeString)
        self.communicator.sendLater('APP:CURR {}\n'.format(outputCode))

    def measure(self):
        voltageString = self.communicator.query('MEAS:VOLT:ALL?\n')[:-1].split(', ')
        currentString = self.communicator.query('MEAS:CURR:ALL?\n')[:-1].split(', ')
        voltages = [float(vs) for vs in voltageString]
        currents = [float(cs) for cs in currentString]
        return [voltages, currents]


if __name__ == "__main__":
    service = BKDCSupplyService('DC Supply Alice 1', 'COM6')
    service.start()
    while True:
        time.sleep(1000)