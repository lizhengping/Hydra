package com.hwaipy.unifieddeviceinterface.adc.advantech;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author ustc
 */
public class AdvantechADCDeviceManager {

    private final AdvantechADCDeviceUnderlying deviceUnderlying;
    private final List<AdvantechADCDevice> deviceList;

    private AdvantechADCDeviceManager() throws AdvantechADCException {
        deviceUnderlying = new AdvantechADCDeviceUnderlying();
        List<AdvantechADCDevice> ds = deviceUnderlying.getDeviceList();
        deviceList = Collections.unmodifiableList(ds);
    }

    public List<AdvantechADCDevice> getDeviceList() throws AdvantechADCException {
        return deviceUnderlying.getDeviceList();
    }
    private static AdvantechADCDeviceManager INSTANCE;

    public static AdvantechADCDeviceManager getManager() throws AdvantechADCException {
        synchronized (AdvantechADCDeviceManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new AdvantechADCDeviceManager();
            }
        }
        return INSTANCE;
    }
}
