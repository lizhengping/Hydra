package com.hwaipy.unifieddeviceinterface.adc.advantech;

import com.hwaipy.unifieddeviceinterface.Device;

/**
 *
 * @author ustc
 */
public class AdvantechADCDevice implements Device {

    private final long deviceIndex;
    private final String deviceName;
    private final int numberOfSubDevices;
    private final AdvantechADCDeviceUnderlying deviceUnderlying;
    private boolean opened = false;
    private long handle;

    AdvantechADCDevice(long deviceIndex, String deviceName, int numberOfSubDevices,
            AdvantechADCDeviceUnderlying deviceUnderlying) {
        this.deviceIndex = deviceIndex;
        this.deviceName = deviceName;
        this.numberOfSubDevices = numberOfSubDevices;
        this.deviceUnderlying = deviceUnderlying;
    }

    public long getDeviceIndex() {
        return deviceIndex;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getNumberOfSubDevices() {
        return numberOfSubDevices;
    }

    public void digitalIOWriteBit(int port, int bit, boolean level) throws AdvantechADCException {
        deviceUnderlying.digialIOWriteBit(handle, port, bit, level);
    }

    public double analogIOReadVoltage(int port) throws AdvantechADCException {
        return deviceUnderlying.analogIOReadVoltage(handle, port);
    }

    @Override
    public void open() throws AdvantechADCException {
        if (opened) {
            throw new IllegalStateException();
        }
        handle = deviceUnderlying.openDevice(deviceIndex);
        System.out.println("Opened: " + handle);
        opened = true;
    }

    @Override
    public void close() throws AdvantechADCException {
        if (!opened) {
            throw new IllegalStateException();
        }
        deviceUnderlying.close(handle);
        opened = false;
    }
}
