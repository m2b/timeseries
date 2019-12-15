package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

import java.io.InvalidObjectException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

public class TimeSeries {
    HashMap<String,SupportedType> context;
    List<Measurement> measurements;  // Names of measurements that will be contained in values
    SortedMap<Instant,List<ValueQuality>> values;
    public TimeSeries(HashMap<String,SupportedType> context,
                      List<Measurement> measurements)
    {
        setContext(context);
        setMeasurements(measurements);
    }

    public HashMap<String, SupportedType> getContext() {
        return context;
    }

    public void setContext(HashMap<String, SupportedType> context) {
        if(context==null || context.isEmpty())
            throw(new NullPointerException("Context must define at least one item"));
        for(String key:context.keySet())
            if(key==null || key.isEmpty())
                throw(new NullPointerException("Context key cannot be null"));
        this.context = context;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        if(measurements==null || measurements.isEmpty())
            throw(new NullPointerException("Measurements must define at least one item"));
        this.measurements = measurements;
    }

    public SortedMap<Instant, List<ValueQuality>> getValues() {
        return values;
    }

    public void setValues(SortedMap<Instant, List<ValueQuality>> values) throws InvalidObjectException {
        if(values==null || values.isEmpty())
            throw(new NullPointerException("Values must contain at least one row"));
        Iterator it = values.entrySet().iterator();
        int row=0;
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            if(pair.getKey()==null)
                throw(new NullPointerException(String.format("Date at row %s cannot be null",row)));
            if(pair.getValue()==null)
                throw(new NullPointerException(String.format("Values at row %s cannot be null",row)));
            List<ValueQuality> value=(List<ValueQuality>)pair.getValue();
            if(value.size()!=measurements.size())
                throw(new InvalidObjectException(String.format("Values at row %s must equal the quantity of measurements",row)));
            // Validate they are the right type
            for(int i=0;i<measurements.size();i++)
            {
                if(value.get(i).getValue()==null)
                    continue;
                if(!value.get(i).getValue().getClass().isAssignableFrom(measurements.get(i).getType()))
                    throw(new InvalidObjectException(String.format("Value at row %s at column %s does not match measurement defined type",row,i)));
            }
            row++;
        }
        this.values = values;
    }
}