package com.b2rt.timeseries.build;
import java.io.InvalidObjectException;

// Convenient class for reading from csv file
public class OPCCodeValue
{
    String hex;
    String description;
    String code;

    private OPCCodeValue(String code,String hex,String description) throws InvalidObjectException
    {
        this.code=code;
        this.hex=hex;
        this.description=description;
    }

    public static OPCCodeValue createCode(String[] metadata) throws InvalidObjectException
    {
        if(metadata.length<3)
            throw(new InvalidObjectException("Invalid metadata must contain three strings"));
        if(!metadata[1].startsWith("0x"))
            throw(new InvalidObjectException("Invalid metadata second item in array must be hex starting with 0x"));
        // create and return code of this metadata
        return new OPCCodeValue(metadata[0], metadata[1], metadata[2]);
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getHex() {
        return hex;
    }
}

