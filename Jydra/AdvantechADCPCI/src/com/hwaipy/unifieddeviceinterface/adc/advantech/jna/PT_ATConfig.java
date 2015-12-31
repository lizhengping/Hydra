/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hwaipy.unifieddeviceinterface.adc.advantech.jna;

import com.sun.jna.Structure;

/**
 *
 * @author Administrator
 */
public class PT_ATConfig extends Structure {

    public short dasChan = 0;
    public short dasGain = 0;

    public static class ByReference extends PT_ATConfig implements Structure.ByReference {
    }
}
