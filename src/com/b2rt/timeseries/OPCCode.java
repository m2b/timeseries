// Code generated by IT Vizion OPCUACodeGenerator
// DO NOT MODIFY
package com.b2rt.timeseries;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public enum OPCCode implements Describable {
// http://support.softwaretoolbox.com/app/answers/detail/a_id/414/~/opc-da-quality-codes
Good("0xC0","The value is good. There are no special conditions."),
GoodLocalOverride("0xD8","Good--Local Override, Value Forced"),
Bad("0x00","The value is bad but no specific reason is known"),
BadConfigError("0x04","Bad - Configuration Error in Server"),
BadNotConnected("0x08","Bad - Not connected"),
BadDeviceFail("0x0C","Bad - Device Failure"),
BadSensorFail("0x10","Bad - Sensor Failure"),
BadLastKnown("0x240","Bad - Last Known Value Passed"),
BadCommFail("0x18","Bad - Comm Failure"),
BadInactive("0x1C","Bad - Item Set Inactive"),
Uncertain("0x40","Ther value is uncertain.  There is no specific reason why."),
UncertainLastValue("0x44","Uncertain - Last Usable Value - timeout of some kind"),
UncertainSensor("0x50","Uncertain - Sensor not Accurate - outside of limits"),
UncertainUnitsExceeded("0x54","Uncertain - Engineering Units exceeded"),
UncertainMultipleSources("0x58","Uncertain - Value from multiple sources----------with less than required good values");
/* FROM XHQ
    // BAD quality with substatus bit fields set
    public static final byte BAD_NONSPECIFIC           = (byte)(0x00);
    public static final byte BAD_CONFIG_ERROR          = (byte)(0x04);
    public static final byte BAD_NOT_CONNECTED         = (byte)(0x08);
    public static final byte BAD_DEVICE_FAILURE        = (byte)(0x0c);
    public static final byte BAD_SENSOR_FAILURE        = (byte)(0x10);
    public static final byte BAD_LAST_KNOWN_VALUE      = (byte)(0x14);
    public static final byte BAD_COMM_FAILURE          = (byte)(0x18);
    public static final byte BAD_OUT_OF_SERVICE        = (byte)(0x1c);

    // UNCERTAIN quality with substatus bit fields set
    public static final byte UNCERTAIN_NONSPECIFIC           = (byte)(0x40);
    public static final byte UNCERTAIN_LAST_USABLE_VALUE     = (byte)(0x44);
    public static final byte UNCERTAIN_SENSOR_NOT_ACCURATE   = (byte)(0x50);
    public static final byte UNCERTAIN_EUNITS_EXCEEDED       = (byte)(0x54);
    public static final byte UNCERTAIN_SUBNORMAL             = (byte)(0x58);

    // GOOD quality with substatus bit fields set
    public static final byte GOOD_NONSPECIFIC         = (byte)(0xc0);
*/

    // Constructor
    OPCCode(String hex, String description)
    {
        this.hex= coherceHex(hex);
        this.description=description;
        this.value=Integer.decode(this.hex);
    }

    String description;
    String hex;
    int value;

    public static OPCCode getFromInt(int value)
    {
        for(OPCCode exist:values())
        {
            if(exist.getValue()==value)
                return exist;
        }
        /*
        // Try also masking low 6 bytes
        // In case vendor has provided status bits not defined in set
        long mask= 0x00;
        long masked=value & mask;
        for(OPCCode exist:values())
        {
            if(exist.getValue()==masked)
                return exist;
        }
        */
        throw(new IllegalArgumentException("value not found in defined OPC codes"));
    }

    public static OPCCode getFromHex(String hex)
    {
        String h= coherceHex(hex);
        for(OPCCode exist:values())
        {
            if(exist.getHex().equals(h))
                return exist;
        }
        return null;
    }

    static String coherceHex(String hex)
    {
        // Pad if necessary
        StringBuilder bhex;
        if(!hex.startsWith("0x"))
            bhex=new StringBuilder("0x"+hex);
        else
            bhex=new StringBuilder(hex);
        while(bhex.length()<4) {
            bhex.append("0");
        }
        return bhex.toString().substring(0,4); // Limit to 4 characters
    }

    static List<OPCCode> sortedValues()
    {
        // Comparator based on their long value
        Comparator<OPCCode> comparator=new Comparator<OPCCode>() {
            @Override
            public int compare(OPCCode o1, OPCCode o2) {
                return Long.compare(o1.getValue(),o2.getValue());
            }
        };

        List<OPCCode> sortedCodes=Arrays.asList(values());
        Collections.sort(sortedCodes,comparator);
        return sortedCodes;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    public int getValue()
    {
        return value;
    }

    public String getHex()
    {
        return hex;
    }

    public OPCQuality getQuality()
    {
        return OPCQuality.getFromInt(value);
    }

    public static OPCCode getCombinedQuality(OPCCode qual1,OPCCode qual2)
    {
        if(qual1.getValue()>=qual2.getValue())
            return qual2;
        else
            return qual1;
    }
}

