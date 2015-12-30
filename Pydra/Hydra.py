__author__ = 'Hwaipy'
import socket
import time
import msgpack
import enum
import Utils
import threading


class Session:
    def __init__(self, name, address, services, invokers):
        self.address = address
        self.name = name
        if type(services) == str:
            services = [services]
        self.services = services
        if not isinstance(invokers, list):
            invokers = [invokers]
        self.invokers = invokers
        self.semaphores = {}

    def start(self, async=False):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.socket.connect(self.address)
        self.unpacker = msgpack.Unpacker(encoding='utf-8')
        self.communicator = Utils.BlockingCommunicator(self.socket, self.__dataFetcher, self.__dataSender)
        self.communicator.start()
        connectionResponse = self.request(Message.creator.Connection(Name=self.name))
        self.clientID = connectionResponse.ClientID
        print('client registered: ID={}'.format(self.clientID))
        if self.services:
            for servive in self.services:
                serviceRegistrationResponse = self.request(Message.creator.ServiceRegistration(Service=servive))
        if not async:
            while True:
                time.sleep(1000)

    # def sendMessageLater(self, message):
    # self.communicator.sendLater(message)

    def request(self, message, async=False):
        if message.getType() is not Message.Type.Request:
            raise
        if async:
            # TODO sync only for now
            raise
        else:
            self.communicator.sendLater(message)
            semaphore = threading.Semaphore(0)
            self.semaphores[str(message.MessageID)] = semaphore
            semaphore.acquire()
            response = self.semaphores.pop(str(message.MessageID), None)
            if response is None:
                raise RuntimeError('None response is got.')
            if response.getType() is Message.Type.Error:
                raise ProtocolException('Error response is got.', response)
            if response.getType() is Message.Type.Unknown:
                raise RuntimeError('Unknown response is got.', response)
            return response

    def response(self, message):
        if (message.getType() is not Message.Type.Response) and (message.getType() is not Message.Type.Error):
            raise
        self.communicator.sendLater(message)

    def messageDeal(self, message):
        type = message.getType()
        if type is Message.Type.Request:
            command = message.Request
            args, kwargs = message.invokeArgs()
            for invoker in self.invokers:
                try:
                    method = invoker.__getattribute__(command)
                    if callable(method):
                        try:
                            result = method(*args, **kwargs)
                            response = message.response()
                            response.Result = result
                            self.response(response)
                        except Exception as ex:
                            response = message.error('InvokeError')
                            response.ErrorMessage = str(ex)
                            self.response(response)
                        return
                except AttributeError as ae:
                    pass
            response = message.error('InvokeError')
            response.ErrorMessage = 'Command {} not found.'.format(command)
            self.response(response)
        elif (type is Message.Type.Response) or (type is Message.Type.Error):
            semaphore = self.semaphores.get(str(message.ResponseID))
            if semaphore is not None:
                self.semaphores[str(message.ResponseID)] = message
                semaphore.release()
        else:
            print('A Wrong Message: {}'.format(message))

    def __dataFetcher(self, socket):
        data = self.socket.recv(10000000)
        self.unpacker.feed(data)
        for packed in self.unpacker:
            message = Message(packed, False)
            self.messageDeal(message)

    def __dataSender(self, socket, message):
        self.socket.send(msgpack.packb(message.pack()))


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
    keyWords = ['MessageID', 'Request', 'Response', 'Error', 'Target', 'From']

    def __init__(self, content=None, counting=True):
        if content == None:
            content = {}
        self.__content__ = content
        self.__mutex__ = threading.Lock()
        if counting:
            self.__mutex__.acquire()
            self.MessageID = Message.messageIndex
            Message.messageIndex += 1
            self.__mutex__.release()

    def __getattr__(self, item):
        if item.startswith('__'):
            return self.__dict__.get(item)
        return self.__content__.get(item)

    def __setattr__(self, key, value):
        if key.startswith('__'):
            self.__dict__[key] = value
        else:
            self.__content__[key] = value

    def __str__(self):
        return "Message: {}".format(self.__content__)

    def pack(self):
        return self.__content__

    def invokeArgs(self):
        kwargs = self.__content__.copy()
        args = kwargs.pop('Arguments', [])
        if not isinstance(args, list):
            args = [args]
        for keyWord in Message.keyWords:
            kwargs.pop(keyWord, None)
        return args, kwargs

    def response(self):
        response = Message()
        response.ResponseID = self.MessageID
        response.Response = self.Request
        if self.From:
            response.Target = self.From
        return response

    def error(self, errorType):
        response = Message()
        response.ResponseID = self.MessageID
        response.Error = self.Request
        response.ErrorType = errorType
        if self.From:
            response.Target = self.From
        return response

    def getType(self):
        if self.__type__ == None:
            requestCommandO = self.Request
            responseCommandO = self.Response
            errorCommandO = self.Error
            if requestCommandO is not None:
                self.__type__ = Message.Type.Request
            elif responseCommandO is not None:
                self.__type__ = Message.Type.Response
            elif errorCommandO is not None:
                self.__type__ = Message.Type.Error
            else:
                self.__type__ = Message.Type.Unkonwn
        return self.__type__

    class MessageCreator:
        def __getattr__(self, item):
            def creatorMethod(*args, **kwargs):
                args = [arg for arg in args]
                if len(args) > 0:
                    kwargs['Arguments'] = args
                message = Message(kwargs)
                message.Request = item
                return message

            return creatorMethod

    creator = MessageCreator()

    class Type(enum.Enum):
        Request = 1
        Response = 2
        Error = 3
        Unknown = 0


if __name__ == '__main__':
    session = Session('SessionTest', ('localhost', 20102), ['S1', 'S2'], {})
    session.start()
