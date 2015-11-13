__author__ = 'Hwaipy'

from LabAtlas import Session, Message
import time

if __name__ == '__main__':
    print('Controller')
    session = Session('LoopholeFree_PockelsCellController', 20001, 'localhost', [], {})
    session.start()

    session.sendMessageLater(
        Message.createRequest('Status', {Message.KEY_TARGET: 'LoopholeFree_PockelsCell_DCSupply_Alice_1'}))

#    session.

    time.sleep(1000)
