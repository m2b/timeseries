package com.b2rt.timeseries;

public enum AggregateInterval {
    None(0),Second(1000),Minute(60000),Hour(3600000),Day(86400000);

    long millis=0;
    AggregateInterval(long millis) {
        this.millis=millis;
    }

    public long getMillis()
    {
        return millis;
    }
}
