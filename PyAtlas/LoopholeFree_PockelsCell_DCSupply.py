__author__ = 'Hwaipy'

from LabAtlas import Session, Message
import time

if __name__ == '__main__':
    print('Controller')
    session = Session('LoopholeFree_PockelsCell_DCSupply_Alice_1', 20001, 'localhost', [], {})
    session.start()

    time.sleep(1000)
