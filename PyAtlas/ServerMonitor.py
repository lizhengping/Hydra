__author__ = 'Hwaipy'

import LabAtlas
import time

if __name__ == "__main__":
    print("This is monitor-hwaipy")
    client = LabAtlas.Session('Monitor[Hwaipy]', 20001, 'localhost', [], {})
    client.start()

    time.sleep(1000)
