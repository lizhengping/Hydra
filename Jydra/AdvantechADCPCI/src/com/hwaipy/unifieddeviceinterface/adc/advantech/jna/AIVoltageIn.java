package com.hwaipy.unifieddeviceinterface.adc.advantech.jna;

import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;

/**
 *
 * @author ustc
 */
public class AIVoltageIn extends Structure {

    public short port = 0;
    public short gain = 0;
    public short trigMode = 0;
    public FloatByReference valtage = new FloatByReference();

    public static class ByReference extends AIVoltageIn implements Structure.ByReference {
    }
}
