__author__ = 'Hwaipy'

from LabAtlas import Session, Message
import time

if __name__ == "__main__":
    print("This is monitor-hwaipy")
    client = Session('Monitor[Hwaipy]', ('localhost', 20102), [], {})
    client.start(async=True)
    client.sendMessageLater(Message.createRequest('SummaryRegistration'))
    client.sendMessageLater(Message.createRequest('SummaryRegistration'))

    time.sleep(20)
