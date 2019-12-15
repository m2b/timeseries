package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

import java.io.InvalidObjectException;
import java.time.Instant;
import java.util.*;

public class Aggregator {

    public static <T extends Number> double interpolate(Instant tOut,Instant tPrev,Instant tNext,T prev,T next)
    {
        if(tOut==null || tPrev==null || tNext==null || next==null)
            throw(new IllegalArgumentException("Arguments cannot be null"));
        if(tPrev.compareTo(tNext)>=0)
            throw(new IllegalArgumentException("tNext must be greater than tPrev"));
        if(tPrev.compareTo(tOut)>=0)
            throw(new IllegalArgumentException("tOut must be greater than tPrev"));
        if(tOut.compareTo(tNext)>0)
            throw(new IllegalArgumentException("tOut must be less than or equal to tNext"));
        if(prev==null)
            prev=next;
        double slope=(next.doubleValue()-prev.doubleValue())/(tNext.toEpochMilli()-tPrev.toEpochMilli());
        double result=slope*(tOut.toEpochMilli()-tPrev.toEpochMilli())+prev.doubleValue();
        return result;
    }

    public static TimeSeries interpolate(TimeSeries in, Instant tStart, Instant tEnd,int deltaT,AggregateInterval period,InterpolateMethod method)
            throws InvalidObjectException
    {
        if(in==null || in.getValues().isEmpty())
            return in;

        if(tEnd==null)
            tEnd=Instant.now();

        if(tEnd.compareTo(tStart)<0)
           throw(new IllegalArgumentException("tEnd must be greater than or equal to tStart"));

        if(deltaT<=0)
            throw(new IllegalArgumentException("deltaT must be greater than or equal to zero"));

        // TODO: Cubic Spline
        if(method==InterpolateMethod.CUBIC_SPLINE)
            throw(new IllegalArgumentException("CUBIC_SPLINE not yet supported"));

        // Items to return
        TimeSeries ret=new TimeSeries(in.context,in.measurements);
        SortedMap<Instant,List<ValueQuality>> retVals=new TreeMap<>();
        Instant tOut=tStart;

        // Variables to track
        Instant prevT=null;
        List<ValueQuality> prevV=null;
        Instant nextT=null;
        List<ValueQuality> nextV=null;

        // TreeMap into lists
        int lastIdx=-1;
        List<Instant> times=new ArrayList<>(in.getValues().keySet());
        List<List<ValueQuality>> valQuals=new ArrayList<>(in.getValues().values());
        // Advance until reached end time
        while (tOut.compareTo(tEnd) <= 0) {

            // Advance until in range of next tOut
            for(int i=lastIdx+1;i<in.getValues().size();i++)
            {
                if (times.get(i).compareTo(tOut)>= 0)
                    break;
                lastIdx=i;
            }
            // If found last
            if(lastIdx>-1)
            {
                prevT=times.get(lastIdx);
                prevV=valQuals.get(lastIdx);
            }
            // If next exists
            if((lastIdx+1)<=(in.getValues().size()-1)) {
                nextT = times.get(lastIdx+1);
                nextV = valQuals.get(lastIdx+1);
            }
            else
            {
                nextT=null;
                nextV=null;
            }

            switch (method) {
                case NEXT:
                    if(nextV!=null)
                        retVals.put(tOut, nextV);
                    break;
                case PREVIOUS:
                    if (prevV != null)
                        retVals.put(tOut, prevV);
                    break;
                case NEAREST:
                    if (prevV == null)
                        retVals.put(tOut, nextV);
                    else if(nextV==null)
                        retVals.put(tOut,prevV);
                    else {
                        long distToPrev = tOut.toEpochMilli() - prevT.toEpochMilli();
                        long distToNext = nextT.toEpochMilli() - tOut.toEpochMilli();
                        if (distToNext < distToPrev)
                            retVals.put(tOut, nextV);
                        else
                            retVals.put(tOut, prevV);
                    }
                    break;
                default: // LINEAR
                    // Can only interpolate numerical values so all others will be treated as previous or next
                    int midx = 0;
                    List<ValueQuality> vals = new ArrayList<>();
                    for (Measurement m : in.getMeasurements()) {
                        if (SupportedType.isInterpolatable(m.getType()) && prevV != null && nextV!=null) {
                            // Interpolate previous and next and compute quality accordingly
                            if (m.getType() == Integer.class) {
                                double interp = interpolate(tOut, prevT, nextT,
                                        (int) prevV.get(midx).getValue(), (int) nextV.get(midx).getValue());
                                OPCCode q = OPCCode.getCombinedQuality(prevV.get(midx).getQuality(), nextV.get(midx).getQuality());
                                ValueQuality<Integer> vq = new ValueQualityObject<Integer>((int) Math.round(interp), q);
                                vals.add(vq);
                            } else if (m.getType() == Long.class) {
                                double interp = interpolate(tOut, prevT, nextT,
                                        (long) prevV.get(midx).getValue(), (long) nextV.get(midx).getValue());
                                OPCCode q = OPCCode.getCombinedQuality(prevV.get(midx).getQuality(), nextV.get(midx).getQuality());
                                ValueQuality<Long> vq = new ValueQualityObject<Long>(Math.round(interp), q);
                                vals.add(vq);
                            } else if (m.getType() == Float.class) {
                                double interp = interpolate(tOut, prevT, nextT,
                                        (float) prevV.get(midx).getValue(), (float) nextV.get(midx).getValue());
                                OPCCode q = OPCCode.getCombinedQuality(prevV.get(midx).getQuality(), nextV.get(midx).getQuality());
                                ValueQuality<Float> vq = new ValueQualityObject<Float>((float) interp, q);
                                vals.add(vq);
                            } else {
                                double interp = interpolate(tOut, prevT, nextT,
                                        (double) prevV.get(midx).getValue(), (double) nextV.get(midx).getValue());
                                OPCCode q = OPCCode.getCombinedQuality(prevV.get(midx).getQuality(), nextV.get(midx).getQuality());
                                ValueQuality<Double> vq = new ValueQualityObject<Double>(interp, q);
                                vals.add(vq);
                            }
                        } else if (prevV != null) {
                            vals.add(prevV.get(midx));
                        } else
                            vals.add(nextV.get(midx));
                        midx++;
                    }
                    retVals.put(tOut, vals);
            }
            // Increment time
            tOut = PrimitiveTypesSimulator.generateNextInstantValue(tOut, deltaT, period, false);
        }
        ret.setValues(retVals);
        return ret;
    }
}
