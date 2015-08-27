import msgpack
import socket
import time

if __name__ == "__main__":
    print("begin with TestService")

    socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    socket.connect(("localhost", 9997))

    ##### Register client
    messageRegisterClient = {"Request": 0, "Name": "TestService"}
    socket.send(msgpack.packb(messageRegisterClient))
    data = socket.recv(1000)
    message = msgpack.unpackb(data, encoding='utf-8')
    print(message.get("Response"))

    ##### Register services
    messageRegisterService = {"ServiceRegister": ["1Service", "2Service"]}
    socket.send(msgpack.packb(messageRegisterService))
    data = socket.recv(1000)

    ##### Register services
    messageRegisterService = "ServiceRegister"
    socket.send(msgpack.packb(messageRegisterService))
    data = socket.recv(1000)

