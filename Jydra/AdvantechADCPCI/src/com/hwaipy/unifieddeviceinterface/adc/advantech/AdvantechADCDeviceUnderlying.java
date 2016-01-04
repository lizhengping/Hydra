package com.hwaipy.unifieddeviceinterface.adc.advantech;

import com.hwaipy.unifieddeviceinterface.adc.advantech.jna.AIVoltageIn;
import com.hwaipy.unifieddeviceinterface.adc.advantech.jna.AdvantechADCJNA;
import com.hwaipy.unifieddeviceinterface.adc.advantech.jna.PT_ATConfig;
//TODO instead Memories of Structures
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ustc
 */
class AdvantechADCDeviceUnderlying {

    private static final String LIBRARYPATH_STRING = "C:\\Windows\\System32\\adsapi32.dll";
    private static final short[] gainCodes = new short[]{19, 3, 18, 2, 17, 1, 16, 0, 4};
    private static final double[][] gainRanges = new double[][]{{0, 1.25}, {-0.625, 0.625}, {0, 2.5}, {-1.25, 1.25}, {0, 5}, {-2.5, 2.5}, {0, 10}, {-5, 5}, {-10, 10}};
    private static AdvantechADCJNA jna;

    AdvantechADCDeviceUnderlying() {
        jna = (AdvantechADCJNA) Native.loadLibrary(LIBRARYPATH_STRING, AdvantechADCJNA.class);
    }

    int getNumberOfDeviceList() throws AdvantechADCException {
        Memory memory = new Memory(2);
        checkError(jna.DRV_DeviceGetNumOfList(memory));
        return memory.getShort(0);
    }

    List<AdvantechADCDevice> getDeviceList() throws AdvantechADCException {
        Memory devList = new Memory(DeviceInfomaton.SIZE * getNumberOfDeviceList());
        Memory outEntries = new Memory(2);
        checkError(jna.DRV_DeviceGetList(devList, (short) 1000, outEntries));
        short number = outEntries.getShort(0);
        ArrayList<AdvantechADCDevice> deviceList = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            DeviceInfomaton di = DeviceInfomaton.parse(devList, i);
            AdvantechADCDevice device = new AdvantechADCDevice(
                    di.getDeviceIndex(), di.getDeviceName(), di.getNumberOfSubDevices(), this);
            deviceList.add(device);
        }
        return deviceList;
    }

    long openDevice(long deviceIndex) throws AdvantechADCException {
        Memory memory = new Memory(NativeLong.SIZE);
        checkError(jna.DRV_DeviceOpen(new NativeLong(deviceIndex), memory));
        return memory.getNativeLong(0).longValue();
    }

    void close(long handle) throws AdvantechADCException {
        Memory memory = new Memory(NativeLong.SIZE);
        memory.setNativeLong(0, new NativeLong(handle));
        checkError(jna.DRV_DeviceClose(memory));
    }

    void digialIOWriteBit(long handle, int port, int bit, boolean level) throws AdvantechADCException {
        Memory memory = new Memory(6);
        memory.setShort(0, (short) port);
        memory.setShort(2, (short) bit);
        memory.setShort(4, (short) (level ? 1 : 0));
        checkError(jna.DRV_DioWriteBit(new NativeLong(handle), memory));
    }

    //TODO turn mode into paragrams
    double analogIOReadVoltage(long handle, int port) throws AdvantechADCException {
        short maxGainCode = gainCodes[gainCodes.length - 1];
        double coarseVoltage = analogIOReadVoltage(handle, port, maxGainCode);
        short preferedGainCode = getPreferedGainCode(coarseVoltage);
        if (preferedGainCode == maxGainCode) {
            return coarseVoltage;
        }
        return analogIOReadVoltage(handle, port, preferedGainCode);
    }

    double analogIOReadVoltage(long handle, int port, double expectedValue) throws AdvantechADCException {
        return analogIOReadVoltage(handle, port, getPreferedGainCode(expectedValue));
    }

    double analogIOReadVoltage(long handle, int port, short gain) throws AdvantechADCException {
        PT_ATConfig.ByReference config = new PT_ATConfig.ByReference();
        config.dasChan = (short) port;
        config.dasGain = gain;
        AIVoltageIn.ByReference para = new AIVoltageIn.ByReference();
        para.port = (short) port;
        para.gain = gain;
        para.trigMode = 0;
        checkError(jna.DRV_AIConfig(new NativeLong(handle), config));
        checkError(jna.DRV_AIVoltageIn(new NativeLong(handle), para));
        return para.valtage.getValue();
    }

    private short getPreferedGainCode(double voltage) {
        for (int i = 0; i < gainCodes.length; i++) {
            if (voltage > gainRanges[i][0] && voltage < gainRanges[i][1]) {
                return gainCodes[i];
            }
        }
        return gainCodes[gainCodes.length - 1];
    }

    private void checkError(NativeLong errorCode) throws AdvantechADCException {
        checkError(errorCode.longValue());
    }

    private void checkError(long errorCode) throws AdvantechADCException {
        if (errorCode != 0) {
            String errorString = getErrorString(errorCode);
            throw new AdvantechADCException(errorString);
        }
    }

    private String getErrorString(long errorCode) {
        Memory memory = new Memory(80);
        jna.DRV_GetErrorMessage(new NativeLong(errorCode), memory);
        return memory.getString(0);
    }

    private static class DeviceInfomaton {

        static final int SIZE = NativeLong.SIZE + 50 + 2;
        private long deviceIndex;
        private String deviceName;
        private int numberOfSubDevices;

        public long getDeviceIndex() {
            return deviceIndex;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public int getNumberOfSubDevices() {
            return numberOfSubDevices;
        }

        public static DeviceInfomaton parse(Memory memory, int index) {
            DeviceInfomaton deviceInfomaton = new DeviceInfomaton();
            deviceInfomaton.deviceIndex = memory.getNativeLong(index * SIZE).longValue();
            deviceInfomaton.deviceName = memory.getString(index * SIZE + NativeLong.SIZE);
            deviceInfomaton.numberOfSubDevices = memory.getShort(index * SIZE + NativeLong.SIZE + 50);
            return deviceInfomaton;
        }
    }
}
