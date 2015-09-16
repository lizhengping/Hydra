__author__ = 'Hwaipy'
import socket
import time
import threading
import msgpack
import random
import queue
import enum


class ClientRunner:
    def __init__(self, name, messagePort=20101, broadcastPort=20151, services=None, commander=None):
        self.messagePort = messagePort
        self.broadcastPort = broadcastPort
        self.services = services
        self.commander = commander
        self.name = name

    def start(self):
        threading._start_new_thread(self.run, ())

    def run(self):
        while True:
            try:
                self.startClient()
            finally:
                print("one client failed")
                self.client.running = False
                time.sleep(random.Random().randint(100, 1000) / 1000)

    def startClient(self):
        self.client = Client(self.name, self.messagePort, self.broadcastPort, self.commander)
        if self.services:
            self.client.registerServices(self.services)
        self.client.start()


class Client:
    def __init__(self, name, messagePort, broadcastPort, commander={}):
        self.messagePort = messagePort
        self.broadcastPort = broadcastPort
        self.name = name
        self.messageID = 0
        self.messageIndex = 0
        self.sendQueue = queue.Queue()

        def connectionHandler(message):
            if message.type == Message.Type.Response:
                self.clientID = message.content.get("ClientID")
                if self.services:
                    serv = []
                    serv += (self.services)
                    self.sendMessageLater(Message.createRequest("ServiceRegistration", {"Service": serv}))
            else:
                raise

        self.unregistratedService = 3

        def serviceRegistrationHandler(message):
            if message.type == Message.Type.Response:
                self.unregistratedService -= 1
                print(self.unregistratedService)
            else:
                raise

        self.commander = {"Connection": connectionHandler, "ServiceRegistration": serviceRegistrationHandler}
        self.commander.update(commander)

    def start(self):
        addressSeeker = AddressSeeker(self.broadcastPort)
        address = addressSeeker.seek()
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((address[0], self.messagePort))
        self.running = True

        threading._start_new_thread(self.receiveLoop, ())
        threading._start_new_thread(self.sendLoop, ())

        self.sendMessageLater(Message.createRequest("Connection", {"Name": self.name}))
        while self.running:
            time.sleep(1)

    def registerServices(self, services):
        if type(services) == str:
            services = [services]
        self.services = services

    def registerCommand(self, command, function):
        if self.commander.__contains__(command):
            pass
        self.commander.__setitem__(command, function)

    def sendMessageLater(self, message):
        self.sendQueue.put(message)

    def sendLoop(self):
        try:
            while self.running:
                message = self.sendQueue.get()
                self.socket.send(msgpack.packb(message.content))
        finally:
            self.running = False

    def receiveLoop(self):
        self.unpacker = msgpack.Unpacker(encoding='utf-8')
        try:
            while self.running:
                data = self.socket.recv(10000000)
                self.unpacker.feed(data)
                for packed in self.unpacker:
                    message = Message.wrap(packed)
                    self.messageDeal(message)
        finally:
            self.running = False

    def messageDeal(self, message):
        command = message.command
        commander = self.commander.get(command)
        if commander != None:
            commander(message)


class Message:
    messageIndex = 0

    def __init__(self, content={}):
        self.content = content

    def __extractEssentialFields(self):
        requestCommandO = self.content.get("Request")
        responseCommandO = self.content.get("Response")
        IDO = self.content.get("ID")
        if requestCommandO == None:
            if responseCommandO == None:
                self.type = Message.Type.Error
            else:
                self.command = responseCommandO;
                self.type = Message.Type.Response;
        else:
            if responseCommandO == None:
                self.command = requestCommandO;
                self.type = Message.Type.Request;
            else:
                self.type = Message.Type.Error
        self.ID = IDO

    @staticmethod
    def createRequest(command, content={}):
        message = Message(content)
        message.content.__setitem__("Request", command)
        message.content.__setitem__("ID", Message.messageIndex)
        Message.messageIndex += 1
        return message

    @staticmethod
    def wrap(content):
        message = Message(content)
        message.__extractEssentialFields()
        return message

    class Type(enum.Enum):
        Request = 1
        Response = 2
        Error = 3


class AddressSeeker:
    def __init__(self, port):
        self.port = port
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        self.socket.setblocking(False)
        self.message = bytes("Connection?", encoding="UTF-8");
        self.ip = "192.168.1.104"

    def seek(self):
        while True:
            self.broadcastConnectionRequest()
            address = self.tryReceive()
            if address:
                return address

    def broadcastConnectionRequest(self):
        print((self.ip, self.port))
        self.socket.sendto(self.message, (self.ip, self.port))

    def tryReceive(self):
        begin = time.time()
        timeout = 1
        message = None
        while True:
            if time.time() - begin > timeout:
                break
            try:
                message = self.socket.recvmsg(1024)
                if message:
                    break
                else:
                    time.sleep(0.1)
            except:
                time.sleep(0.1)
        if message:
            if "Connection".__eq__(str(message[0], "UTF-8")):
                return message[3]
        time.sleep(timeout)
        return None


class MessageUnpacker:
    def feed(self, data):
        self.unpacker.feed(data)

    def unpack(self):
        return [message for message in self.unpacker]


if __name__ == "__main__":
    runner = ClientRunner("TestService", services=["PowerMeter", "PowerMeter1", "PowerMeter2"],
                          commander={"Version": "hha"})
    runner.start()

    time.sleep(3000)
    # {"Response": "Version", "ID": 1, "Version": "1.0.0.20150912",
    # 'To': {'Name': 'PowerMeterMonitor', 'ClientID': 1}


def test():
    messages = []
    for i in range(10):
        messages.append({"Request": "Connection", "ID": i, "Name": "VirtualPowerMeter"})
    data = [msgpack.packb(message) for message in messages]
    unpacker = MessageUnpacker()
    unpacker.feed(data[0])
    unpacker.feed(data[1])
    msg1 = unpacker.unpack()
    assert msg1.__len__() == 2
    assert msg1[0].get("ID") == 0
    assert msg1[1].get("ID") == 1
    unpacker.feed(data[2][:20])
    msg2 = unpacker.unpack()
    assert msg2.__len__() == 0
    unpacker.feed(data[2][20:])
    unpacker.feed(data[3][:10])
    msg3 = unpacker.unpack()
    assert msg3.__len__() == 1
    assert msg3[0].get("ID") == 2
    unpacker.feed(data[3][10:25])
    msg4 = unpacker.unpack()
    assert msg4.__len__() == 0
    unpacker.feed(data[3][25:])
    unpacker.feed(data[4])
    msg5 = unpacker.unpack()
    assert msg5.__len__() == 2
    assert msg5[0].get("ID") == 3
    assert msg5[1].get("ID") == 4

