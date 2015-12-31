package com.hwaipy.unifieddeviceinterface.adc.advantech;

import com.hwaipy.unifieddeviceinterface.DeviceException;

/**
 *
 * @author ustc
 */
public class AdvantechADCException extends DeviceException {

    public AdvantechADCException() {
    }

    public AdvantechADCException(String message) {
        super(message);
    }

    public AdvantechADCException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdvantechADCException(Throwable cause) {
        super(cause);
    }

    public AdvantechADCException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
