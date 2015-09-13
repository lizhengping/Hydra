__author__ = 'Hwaipy'
import socket
from io import BytesIO
import time
import threading
import msgpack


class Client:
    def __init__(self, messagePort, broadcastPort):
        self.messagePort = messagePort
        self.broadcastPort = broadcastPort

    def start(self):
        threading._start_new_thread(self.run, ())

    def run(self):
        while True:
#            try:
            self.loop()
#            except:
#                pass

    def loop(self):
        addressSeeker = AddressSeeker(self.broadcastPort)
        address = addressSeeker.seek()
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect((address[0],self.messagePort))

        ##### Register client
        messageRegisterClient = {"Request": "Connection", "ID": 0, "Name": "VirtualPowerMeter"}
        self.socket.send(msgpack.packb(messageRegisterClient))
        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******1")

        ##### Register services
        messageRegisterService = {"Request": "ServiceRegistration", "ID": 1, "Service": ["PowerMeter", "DC Supply"]}
        self.socket.send(msgpack.packb(messageRegisterService))
        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******2")

        ##### Test
        # messageTest = "TestMessage"
        # socket.send(msgpack.packb(messageTest))
        # data = socket.recv(1000)

        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******3")

        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******4")

        ##### Response to version Request
        messageRegisterService = {"Response": "Version", "ID": 1, "Version": "1.0.0.20150912",
                                  'To': {'Name': 'PowerMeterMonitor', 'ClientID': 1}}
        self.socket.send(msgpack.packb(messageRegisterService))
        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******5")

        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******6")

        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******7")

        data = self.socket.recv(1000)
        message = msgpack.unpackb(data, encoding='utf-8')
        print(message)
        print("******")


class AddressSeeker:
    def __init__(self, port):
        self.port = port
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        self.socket.setblocking(False)
        self.message = bytes("Connection?", encoding="UTF-8");
        self.ip = "192.168.1.255"

    def seek(self):
        while True:
            self.broadcastConnectionRequest()
            address = self.tryReceive()
            if address:
                return address

    def broadcastConnectionRequest(self):
        self.socket.sendto(self.message, (self.ip, self.port))

    def tryReceive(self):
        begin = time.time()
        timeout = 1
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


if __name__ == "__main__":
    client = Client(50001, 50051)
    client.start()

    time.sleep(3000)