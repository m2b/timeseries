package com.b2rt.timeseries;

import com.b2rt.data.InvalidTypeException;
import com.b2rt.data.SupportedType;

import java.io.InvalidObjectException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrimitiveTypesSimulator implements Simulator {

    static final List<String> wordsList=Stream.of("Hello","World","Alexa","Google","Siri","Cortana","Amazon","Microsoft").collect(Collectors.toList());
    static final int maxUnicodeChars =1111988;
    static final float pctEntropy=0.05f;

    double pctGoodQuality=1;

    public HashMap<String, SupportedType> getContext() {
        return context;
    }

    public void setContext(HashMap<String, SupportedType> context) {
        if(context==null || context.isEmpty())
            throw(new IllegalArgumentException("context cannot be null or empty"));
        this.context = context;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        if(measurements==null || measurements.isEmpty())
            throw(new IllegalArgumentException("measurements cannot be null or empty"));
        this.measurements = measurements;
    }

    HashMap<String,SupportedType> context;
    List<Measurement> measurements;
    int codes=0;
    List<OPCCode> sortedCodes=new ArrayList<>();
    int goodCodes=0;


    int sumofgoods=0;
    int totalsum=0;
    OPCCode simulateQuality()
    {
        OPCCode ret=OPCCode.Good;
        if(codes!=0) {
            if (sumofgoods > 0) {
                double rnd = Math.random();
                if (((1.0*sumofgoods)/totalsum) < pctGoodQuality) {
                    ret = sortedCodes.get(codes-goodCodes-1 + (int)Math.round(rnd * goodCodes));
                    sumofgoods++;
                }
                else
                    ret = sortedCodes.get((int) Math.round(rnd * (codes-goodCodes)));

            }
            else
                sumofgoods++; // Initialize since first values is good anyways
            totalsum++;
        }
        return ret;
    }

    public PrimitiveTypesSimulator(HashMap<String,SupportedType> context, List<Measurement> measurements, double pctGoodQuality)
    {
        setContext(context);
        setMeasurements(measurements);
        if(pctGoodQuality<0 || pctGoodQuality>1.0)
            throw(new IllegalArgumentException("pctGoodQuality must be between 0.0 and 1.0"));

        this.pctGoodQuality=pctGoodQuality;
        codes=pctGoodQuality>=1.0?0:OPCCode.values().length;

        // get sorted opc codes and number of good codes
        if(codes>0) {
            sortedCodes = OPCCode.sortedValues();
            for (OPCCode code : sortedCodes) {
                if (code.getQuality() == OPCQuality.Good)
                    goodCodes++;
                else
                    continue;
            }
        }
    }

    @Override
    public TimeSeries generateTimeSeries(Instant start,Instant end,AggregateInterval period,boolean includeEnd,boolean randomizeTime) throws InvalidObjectException,InvalidTypeException
    {
        if(start==null)
            throw(new RuntimeException("Must provide a start time"));
        if(end==null)
            end=Instant.now();

        TimeSeries ret=new TimeSeries(context,measurements);
        SortedMap<Instant, List<ValueQuality>> values=new TreeMap<>();
        List<ValueQuality> prevs=new ArrayList<>();
        // For each time slot
        Instant t=start;
        while(t.compareTo(end)<0 || (includeEnd && t.compareTo(end)==0)) {
            List<ValueQuality> vals = new ArrayList<>();
            int idx=0;
            for (Measurement m : measurements) {
                if (m.type == String.class) {
                    String v = generateNextStringValue();
                    vals.add(new ValueQualityObject(new SupportedType(v), simulateQuality()));
                } else if (m.type == Character.class) {
                    char c = generateNextCharacterValue();
                    vals.add(new ValueQualityObject(new SupportedType(c), simulateQuality()));
                } else if (m.type == Boolean.class) {
                    boolean b = generateNextBooleanValue();
                    vals.add(new ValueQualityObject(new SupportedType(b), simulateQuality()));
                } else if (m.type == Byte.class) {
                    Measurement<Byte> mb = m;
                    Byte prev = (prevs.size() == 0 ? null : (byte)prevs.get(idx).getValue());
                    byte b = generateNextByteValue(prev, mb.getMinimum(), mb.getMaximum(), (byte)(pctEntropy*(mb.getMaximum()-mb.getMinimum())));
                    vals.add(new ValueQualityObject(new SupportedType(b), simulateQuality()));
                } else if (m.type == Short.class) {
                    Measurement<Short> ms = m;
                    Short prev = (prevs.size() == 0 ? null : (short)prevs.get(idx).getValue());
                    short s = generateNextShortValue(prev, ms.getMinimum(), ms.getMaximum(), (short)(pctEntropy*(ms.getMaximum()-ms.getMinimum())));
                    vals.add(new ValueQualityObject(new SupportedType(s), simulateQuality()));
                } else if (m.type == Integer.class) {
                    Measurement<Integer> mi = m;
                    Integer prev = (prevs.size() == 0 ? null : (int)prevs.get(idx).getValue());
                    int i = generateNextIntegerValue(prev, mi.getMinimum(), mi.getMaximum(), (int)(pctEntropy*(mi.getMaximum()-mi.getMinimum())));
                    vals.add(new ValueQualityObject(new SupportedType(i), simulateQuality()));
                } else if (m.type == Long.class) {
                    Measurement<Long> ml = m;
                    Long prev = (prevs.size() == 0 ? null : (long)prevs.get(idx).getValue());
                    long l = generateNextLongValue(prev, ml.getMinimum(), ml.getMaximum(), (long)(pctEntropy*(ml.getMaximum()-ml.getMinimum())));
                    vals.add(new ValueQualityObject(new SupportedType(l), simulateQuality()));
                } else if (m.type == Float.class) {
                    Measurement<Float> mf = m;
                    Float prev = (prevs.size() == 0 ? null : (float)prevs.get(idx).getValue());
                    float f = generateNextFloatValue(prev, mf.getMinimum(), mf.getMaximum(),pctEntropy*(mf.getMaximum()-mf.getMinimum()));
                    vals.add(new ValueQualityObject(new SupportedType(f), simulateQuality()));
                } else if (m.type == Double.class) {
                    Measurement<Double> md = m;
                    Double prev = (prevs.size() == 0 ? null : (double)prevs.get(idx).getValue());
                    double d = generateNextDoubleValue(prev, md.getMinimum(), md.getMaximum(),pctEntropy*(md.getMaximum()-md.getMinimum()));
                    vals.add(new ValueQualityObject(new SupportedType(d), simulateQuality()));
                } else {
                    throw(new InvalidTypeException("Type "+m.type.getName()+" is not supported by this simulator"));
                }

                // Increment measurements index
                idx++;
            }

            // Insert values at this time (instant)
            values.put(t,vals);

            // Simulate next time
            t = generateNextInstantValue(t, 1, period,randomizeTime);

            // Store prev values
            prevs = vals;
        }
        ret.setValues(values);
        return ret;
    }

    static public String generateNextStringValue()
    {
        return wordsList.get((int)Math.round((wordsList.size()-1)*Math.random()));
    }

    static public char generateNextCharacterValue()
    {
        long c=Math.round(maxUnicodeChars*Math.random());
        return (char)c;
    }

    static public boolean generateNextBooleanValue()
    {
       return Math.random()>0.5;
    }

    static public double generateNextDoubleValue(Double prev,double min,double max,double entropy)
    {
        if (prev==null)
            return min+(max - min) * Math.random();
        double sign = Math.random() > 0.5 ? 1.0 : -1.0;
        double next = prev + sign * entropy * Math.random();
        return next < min ? min : (next > max ? max : next);
    }

    static public float generateNextFloatValue(Float prev,float min,float max,float entropy)
    {
        if(prev!=null)
            return (float)generateNextDoubleValue(prev.doubleValue(),min,max,entropy);
        else
            return (float)generateNextDoubleValue(null,min,max,entropy);
    }

    static public int generateNextIntegerValue(Integer prev,int min,int max,int entropy)
    {
        if(prev!=null)
            return Math.round(generateNextFloatValue(prev.floatValue(),min,max,entropy));
        else
            return Math.round(generateNextFloatValue(null,min,max,entropy));
    }

    static public long generateNextLongValue(Long prev,long min,long max,long entropy)
    {
        if(prev!=null)
            return Math.round(generateNextDoubleValue(prev.doubleValue(),min,max,entropy));
        else
            return Math.round(generateNextDoubleValue(null,min,max,entropy));

    }

    static public short generateNextShortValue(Short prev,short min,short max,short entropy)
    {
        if(prev!=null)
            return (short)generateNextIntegerValue(prev.intValue(),min,max,entropy);
        else
            return (short)generateNextIntegerValue(null,min,max,entropy);
    }

    static public byte generateNextByteValue(Byte prev,byte min,byte max,byte entropy)
    {
        if(prev!=null)
            return (byte)generateNextShortValue(prev.shortValue(),min,max,entropy);
        else
            return (byte)generateNextShortValue(null,min,max,entropy);
    }

    static public Instant generateNextInstantValue(Instant prev,int deltaT,AggregateInterval period,boolean randomizeTime)
    {
        if (prev==null)
            return Instant.now();
        Duration delta=Duration.ZERO;
        switch(period)
        {
            case Hour:
                if(!randomizeTime)
                    delta=Duration.ofHours(deltaT);
                else
                    delta=Duration.ofMinutes(Math.round(deltaT*60*Math.random()));
                break;
            case Day:
                if(!randomizeTime)
                    delta=Duration.ofDays(deltaT);
                else
                    delta=Duration.ofHours(Math.round(deltaT*24*Math.random()));
                break;
            case Minute:
                if(!randomizeTime)
                    delta=Duration.ofMinutes(deltaT);
                else
                    delta=Duration.ofSeconds(Math.round(deltaT*60*Math.random()));
                break;
            default:
                if(!randomizeTime)
                    delta=Duration.ofSeconds(deltaT);
                else
                    delta=Duration.ofMillis(Math.round(deltaT*1000*Math.random()));
        }
        Instant next=prev.plus(delta);
        return next;
    }


}
