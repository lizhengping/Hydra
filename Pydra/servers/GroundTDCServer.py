__author__ = 'Hwaipy'

import time
import jpype

'''
class GroundTDCParser:
    def __init__(self):
        self.unitBuffer = ByteBuffer([0] * 8)

    def offer(self, data):
        dataBuffer = ByteBuffer(data)
        while dataBuffer.remaining() > 0:
            self.unitBuffer.fill(dataBuffer)
            if self.unitBuffer.remaining() == 0:
                self.unitBuffer.flip()


class ByteBuffer:
    def __init__(self, array):
        self.array = array
        self.capacity = len(array)
        self.position = 0
        self.limit = self.capacity

    def remaining(self):
        return self.limit - self.position

    def fill(self, source):
        len = min(self.remaining(), source.remaining())
        for i in range(len):
            self.array[self.position + i] = source.array[source.position + i]
        self.position += len
        source.position += len

    def flip(self):
        self.limit = self.position
        self.position = 0
'''


class DataProcessor:
    def __init__(self):
        self.c1 = []
        self.c2 = []

    def process(self, data):
        '''
        if data == None:
            return
        it = data.iterator()
        while it.hasNext():
            te = it.next()
            channel = te[0]
            time = te[1]
            if channel == 2:
                self.c1.append(time)
            if channel == 3:
                self.c2.append(time)
        '''
        pass

    def calc(self, delay=0, gate=2000):
        c = 0
        t2Start = 0
        for t1 in self.c1:
            for index2 in range(t2Start, len(self.c2), 1):
                t2 = self.c2[index2] + delay
                diff = t2 - t1
                if diff < -gate:
                    t2Start += 1
                    continue
                if diff > gate:
                    break
                c += 1
        return c


if __name__ == '__main__':
    try:
        jpype.startJVM(jpype.getDefaultJVMPath(), '-ea',
                       '-Djava.class.path=/Users/Hwaipy/Documents/GitHub/LabAtlas/JAtlas/JTDC/target/classes')
        tester = jpype.JClass('com.hwaipy.jatlas.groundtdcserver._Tester')()
        tester.testParseTime()
        '''
        groundTDCAdapter = jpype.JClass('com.hwaipy.vi.tdc.adapters.GroundTDCDataAdapter')([0, 2, 3])
        bufferedOrderTDCDataAdapter = jpype.JClass('com.hwaipy.vi.tdc.adapters.BufferedOrderTDCDataAdapter')()
        serializingTDCDataAdapter = jpype.JClass('com.hwaipy.vi.tdc.adapters.SerializingTDCDataAdapter')(4, 20)
        processor = DataProcessor()
        jProcessor = jpype.JProxy("com.hwaipy.vi.tdc.TDCDataProcessor", inst=processor)
        parser = jpype.JClass('com.hwaipy.vi.tdc.TDCParser')(jProcessor,
                                                             [groundTDCAdapter])
        sampleFile = open('/users/hwaipy/documents/data/samples/20151129114403-帧错误示例.dat', 'r+b')
        # sampleFile = open('/users/hwaipy/documents/data/samples/Ground_TDC_1.dat', 'r+b')
        data = sampleFile.read()
        print('Data size: {}'.format(len(data)))
        dataSection = []
        randomSeed = 198917
        position = 0
        while position < len(data):
            nextPosition = min(len(data), position + randomSeed)
            randomSeed = (randomSeed * 2) % 3711 + 7
            dataSection.append(data[position: nextPosition])
            position = nextPosition

        startTime = time.time()
        for section in dataSection:
            parser.offer(section)
        endTime = time.time()
        print((endTime - startTime))
        print('----In GroundTDCDataAdapter----')
        print('Frame readed: {}'.format(groundTDCAdapter.getFrameCount()))
        print('Frame valid: {}'.format(groundTDCAdapter.getValidFrameCount()))
        print('Skipped in seeking head: {}'.format(groundTDCAdapter.getSkippedInSeekingHead()))
        print('Unknown channel events: {}'.format(groundTDCAdapter.getUnknownChannelEventCount()))
        print('Valid events: {} {}'.format(sum(groundTDCAdapter.getValidEventCount()),
                                           groundTDCAdapter.getValidEventCount()))
        print('Remaining: {}'.format(groundTDCAdapter.getDataRemaining()))
        print('Addressed bytes: {}'.format(
            groundTDCAdapter.getFrameCount() * 2048 + groundTDCAdapter.getSkippedInSeekingHead() + groundTDCAdapter.getDataRemaining()))
        print('----In bufferedOrderTDCDataAdapter----')
        print('SortOurttedCount: {}'.format(bufferedOrderTDCDataAdapter.getSortOuttedCount()))
        print('----In SerializingTDCDataAdapter----')
    # print('Unmapped events: {} {}'.format(sum(serializingTDCDataAdapter.getSkippedEventCounts()),
    #                                             serializingTDCDataAdapter.getSkippedEventCounts()))

    # for delay in range(3000 - 100 * 13160, 3000 + 100 * 13160, 13160):
    #    c = processor.calc(delay, 2000)
    #    print('{}: {}'.format(delay, c))
        '''
    except jpype.JavaException as e:
        print(e.stacktrace())
