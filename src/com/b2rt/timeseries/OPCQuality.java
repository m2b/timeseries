package com.b2rt.timeseries;

public enum OPCQuality {
    Bad(0),Uncertain(64),Good(192);
    int value;
    OPCQuality(int value)
    {
        this.value=value;
    }

    public static OPCQuality getFromInt(int value)
    {
        if(value<0)
            throw(new IllegalArgumentException("value cannot be less than 0"));
        if(value<Uncertain.getInt())
            return OPCQuality.Bad;
        else if(value<Good.getInt())
            return OPCQuality.Uncertain;
        else if(value>255)
            throw(new IllegalArgumentException("value cannot be more than 255"));
        else
            return OPCQuality.Good;
    }

    public int getInt()
    {
        return value;
    }
}
