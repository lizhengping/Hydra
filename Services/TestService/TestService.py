import msgpack
import socket
import time

if __name__ == "__main__":
    print("begin with TestService")

    socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    socket.connect(("localhost", 9997))

    ##### Register client
    messageRegisterClient = {"Request": "Connection", "ID": 0, "Name": "VirtualPowerMeter"}
    socket.send(msgpack.packb(messageRegisterClient))
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******1")

    ##### Register services
    messageRegisterService = {"Request": "ServiceRegistration", "ID": 1, "Service": ["PowerMeter", "DC Supply"]}
    socket.send(msgpack.packb(messageRegisterService))
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******2")

    ##### Test
    # messageTest = "TestMessage"
    # socket.send(msgpack.packb(messageTest))
    # data = socket.recv(1000)

    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******3")

    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******4")

    ##### Response to version Request
    messageRegisterService = {"Response": "Version", "ID": 1, "Version": "1.0.0.20150912",
                              'To': {'Name': 'PowerMeterMonitor', 'ClientID': 1}}
    socket.send(msgpack.packb(messageRegisterService))
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******5")

    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******6")

    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******7")

    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    print("******")
