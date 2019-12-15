package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

public class ValueQualityObject<T extends Comparable<T>> implements ValueQuality<T>{
    T value;
    OPCCode quality;

    public ValueQualityObject(SupportedType<T> value, OPCCode quality)
    {
        this.setValue(value.getValue());
        this.setQuality(quality);
    }

    public ValueQualityObject(T value,OPCCode quality)
    {
        this.setValue(value);
        this.setQuality(quality);
    }

    @Override
    public void setValue(T value){
        this.value=value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void setQuality(OPCCode quality) {
        this.quality = quality;
    }

    @Override
    public OPCCode getQuality() {
        return quality;
    }
}
