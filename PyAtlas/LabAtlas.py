__author__ = 'Hwaipy'
import socket
import time
import msgpack
import enum
import Utils
import threading
import random


class Client:
    def __init__(self, name, messagePort, broadcastPort, services, commander):
        self.messagePort = messagePort
        self.broadcastPort = broadcastPort
        self.services = services
        self.commander = commander
        self.name = name
        self.session = None

    def start(self):
        threading._start_new_thread(self.run, ())

    def run(self):
        while True:
            try:
                self.startSession()
            finally:
                print("one client failed")
                self.session.running = False
                time.sleep(random.Random().randint(100, 1000) / 1000)

    def startSession(self):
        self.session = Session(self.name, self.messagePort, self.broadcastPort, self.commander)

    def send(self, message):
        self.session.sendMessageLater(message)


class Session:
    def __init__(self, name, address, services, commander):
        self.address = address
        self.name = name
        self.messageID = 0
        self.messageIndex = 0
        if type(services) == str:
            services = [services]
        self.services = services

        def connectionHandler(message):
            if message.type == Message.Type.Response:
                self.clientID = message.content.get(Message.KEY_CLIENT_ID)
                print('client registered: ID={}'.format(self.clientID))
                if self.services:
                    serv = []
                    serv += self.services
                    self.sendMessageLater(
                        Message.createRequest(Message.COMMAND_SERVICE_REGISTRATION, {Message.KEY_SERVICE: serv}))
            elif message.type == Message.Type.Error:
                errorMessage = message.content.get(Message.KEY_ERROR_MESSAGE)
                raise ProtocolException(errorMessage)
            else:
                raise ProtocolException('Unrecognized Message.', message)

        def serviceRegistrationHandler(message):
            if message.type == Message.Type.Response:
                pass
            else:
                raise ProtocolException('Unrecognized Message.', message)

        self.commander = {Message.COMMAND_CONNECTION: connectionHandler,
                          Message.COMMAND_SERVICE_REGISTRATION: serviceRegistrationHandler}
        self.commander.update(commander)

    def start(self, async=False):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect(self.address)
        self.unpacker = msgpack.Unpacker(encoding='utf-8')
        self.communicator = Utils.BlockingCommunicator(self.socket, self.__dataFetcher, self.__dataSender)
        self.communicator.start()
        self.sendMessageLater(Message.createRequest(Message.COMMAND_CONNECTION, {Message.KEY_NAME: self.name}))
        if not async:
            while True:
                time.sleep(1000)

    def registerCommand(self, command, function):
        if self.commander.__contains__(command):
            pass
        self.commander.__setitem__(command, function)

    def sendMessageLater(self, message):
        self.communicator.sendLater(message)

    def __dataFetcher(self, socket):
        data = self.socket.recv(10000000)
        self.unpacker.feed(data)
        for packed in self.unpacker:
            message = Message.wrap(packed)
            self.messageDeal(message)

    def __dataSender(self, socket, message):
        self.socket.send(msgpack.packb(message.content))

    def messageDeal(self, message):
        command = message.command
        commander = self.commander.get(command)
        if commander != None:
            commander(message)
        else:
            print('Undealed message: {}'.format(message))


class ProtocolException(Exception):
    def __init__(self, description, message=None):
        Exception.__init__(self)
        self.description = description
        self.message = message

    def __str__(self):
        if self.message:
            return '{} - {}'.format(self.description, self.message)
        else:
            return self.description


class Message:
    messageIndex = 0
    KEY_REQUEST = "Request"
    KEY_RESPONSE = "Response"
    KEY_MESSAGE_ID = "MessageID"
    KEY_NAME = "Name"
    KEY_CLIENT_ID = "ClientID"
    KEY_ERROR = "Error"
    KEY_ERROR_MESSAGE = "ErrorMessage"
    KEY_STATUS = "Status"
    KEY_TARGET = "Target"
    KEY_FROM = "From"
    KEY_CONTINUES = "Continues"
    KEY_SERVICE = "Service"
    VALUE_STATUS_OK = "Ok"
    COMMAND_CONNECTION = "Connection"
    COMMAND_SERVICE_REGISTRATION = "ServiceRegistration"

    def __init__(self, content=None):
        if not content:
            content = {}
        self.content = content

    def response(self):
        response = Message()
        response.content.__setitem__(Message.KEY_MESSAGE_ID, self.content.get(Message.KEY_MESSAGE_ID))
        response.content.__setitem__(Message.KEY_RESPONSE, self.content.get(Message.KEY_REQUEST))
        if (self.content.__contains__(Message.KEY_FROM)):
            response.content.__setitem__(Message.KEY_TARGET, self.content.get(Message.KEY_FROM))
        return response

    def __str__(self):
        return "Message: {}".format(self.content)

    def __extractEssentialFields(self):
        requestCommandO = self.content.get(Message.KEY_REQUEST)
        responseCommandO = self.content.get(Message.KEY_RESPONSE)
        errorCommandO = self.content.get(Message.KEY_ERROR)
        IDO = self.content.get(Message.KEY_MESSAGE_ID)
        if not requestCommandO == None:
            typeCommand = requestCommandO
            self.type = Message.Type.Request
        elif not responseCommandO == None:
            typeCommand = responseCommandO
            self.type = Message.Type.Response
        elif not errorCommandO == None:
            typeCommand = errorCommandO
            self.type = Message.Type.Error
        else:
            raise ProtocolException('Command should be assigned.', self)
        if isinstance(typeCommand, str):
            self.command = typeCommand
        else:
            raise ProtocolException("Command should be String.", self)
        if IDO == None:
            raise ProtocolException("ID should be assigned.", self)
        elif isinstance(IDO, int):
            self.ID = IDO
        else:
            raise ProtocolException("ID should be Integer.", self)

    @staticmethod
    def createRequest(command, content=None):
        if not content:
            content = {}
        message = Message(content)
        message.content.__setitem__(Message.KEY_REQUEST, command)
        message.content.__setitem__(Message.KEY_MESSAGE_ID, Message.messageIndex)
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
