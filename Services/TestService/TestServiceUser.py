import msgpack
import socket
import time

if __name__ == "__main__":
    print("begin with TestServiceUser")

    socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    socket.connect(("localhost", 9997))

    ##### Register client
    messageRegisterClient = {"Request": "Connection", "ID": 0, "Name": "PowerMeterMonitor"}
    socket.send(msgpack.packb(messageRegisterClient))
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)

    ##### Quest
    messageRegisterService = {"Request": "Version", "ID": 1, "Target": "PowerMeter"}
    socket.send(msgpack.packb(messageRegisterService))
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)

    ##### Register services
    # messageRegisterService = {"ServiceRegister": ["PowerMeter"]}
    # socket.send(msgpack.packb(messageRegisterService))
    # data = socket.recv(1000)

    ##### Test
    # messageTest = "TestMessage"
    # socket.send(msgpack.packb(messageTest))
    # data = socket.recv(1000)

    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message)
