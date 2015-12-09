__author__ = 'Hwaipy'

import time
import jpype
import os.path
import numpy as np

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

if __name__ == '__main__':
    try:
        jpype.startJVM(jpype.getDefaultJVMPath(), '-ea',
                       '-Djava.class.path=/Users/Hwaipy/Documents/GitHub/LabAtlas/JAtlas/JTDC/target/classes:')
        parser = jpype.JClass('com.hwaipy.vi.tdc.GroundTDCParser')()
        sampleFile = open('/users/hwaipy/documents/data/samples/20151129114403-帧错误示例.dat', 'r+b')
        # sampleFile = open('/users/hwaipy/documents/data/samples/Ground_TDC_1.dat', 'r+b')
        data = sampleFile.read()
        print('Data size: {}'.format(len(data)))
        dataSection = []
        randomSeed = 198917
        position = 0
        while position < len(data):
            nextPosition = min(len(data), position + randomSeed)
            randomSeed = (randomSeed * 2) % 376111 + 7
            dataSection.append(data[position: nextPosition])
            position = nextPosition

        startTime = time.time()
        for section in dataSection:
            te = parser.offer(section)
        endTime = time.time()
        print((endTime - startTime))
        print('Frame readed: {}'.format(parser.getFrameCount()))
        print('Frame valid: {}'.format(parser.getValidFrameCount()))
        print('Skipped in seeking head: {}'.format(parser.getSkippedInSeekingHead()))
        print('Remaining: {}'.format(parser.getDataRemaining()))

        print('Addressed bytes: {}'.format(
            parser.getFrameCount() * 2048 + parser.getSkippedInSeekingHead() + parser.getDataRemaining()))
    except jpype.JException(jpype.java.lang.RuntimeException) as e:
        print(e.message())