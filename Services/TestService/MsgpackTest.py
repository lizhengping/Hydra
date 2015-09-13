__author__ = 'Hwaipy'
import msgpack
from io import BytesIO

if __name__ == "__main__":
    messageRegisterClient = {"Request": "Connection", "ID": 0, "Name": "VirtualPowerMeter"}
    packeddata = msgpack.packb(messageRegisterClient)
    print(packeddata)

    data = packeddata + packeddata[0:20]
    print(data)

    # unpack1 = msgpack.unpackb(data, encoding='utf-8')
    # print(unpack1)
    buf = BytesIO()
    buf.write(data)
    buf.seek(0)
    unpacker = msgpack.Unpacker(buf)
    for unpacked in unpacker:
        print(unpacked)

    print(buf.tell())