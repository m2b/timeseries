package com.b2rt.timeseries;

public interface ValueQuality<T> {
    void setValue(T value);
    T getValue();
    void setQuality(OPCCode quality);
    OPCCode getQuality();
}
