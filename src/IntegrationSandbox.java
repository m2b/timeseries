import com.b2rt.data.SupportedType;
import com.b2rt.timeseries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InvalidObjectException;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.util.*;
import java.util.stream.Stream;

public class IntegrationSandbox {

    final static Logger logger = LogManager.getLogger();

    public static Double RH(Double temp,Double dewpt)
    {
        Double RH= 100-5*(temp-dewpt);
        if(RH<0)
            return 0.0;
        else if(RH>100)
            return 100.0;
        else
            return RH;
    }

    public static void main(String[] args) {

        // These are just repositories
        ValuesSimulator source=new ValuesSimulator(0.95,true);  // This one randomizes time
        ValuesSimulator destination = new ValuesSimulator(1.0);

        // Build source context stuff
        HashMap<HashMap<String, SupportedType>, List<Measurement>> contextAndMeasIn = new HashMap<>();
        HashMap<String, SupportedType> context = new HashMap<>();
        try {
            List<Measurement> measurementsIn=new ArrayList<>();
            Measurement temp = new Measurement(new URI("vzwatch:Panama.Temp"),
                    "Temperature", "°C",
                    new SupportedType(5.0),
                    new SupportedType(50.0));
            Measurement dewpttemp = new Measurement(new URI("vzwatch:Panama.DewPtTemp"),
                    "Temperature", "°C",
                    new SupportedType(-10.0),
                    new SupportedType(40.0));
            measurementsIn.add(temp);
            measurementsIn.add(dewpttemp);
            SupportedType city = new SupportedType("Panama");
            context.put("City", city);
            contextAndMeasIn.put(context, measurementsIn);
        }
        catch(Exception e)
        {
            throw (new RuntimeException(e));
        }

        // Define the adapter
        Adapter transform=(timeSeriesIn)->{
            try {
                Measurement rh=new Measurement(new URI("vzwatch:Panama.RH"),
                        "Relative Humidity","%",
                        new SupportedType(0.0),
                        new SupportedType(100.0));
                // Do the real transformation, or calculations
                Stream<TimeSeries> destTS=timeSeriesIn.map(timeSeries->{
                    // Make sure right inputs are available
                    int tempIndex=-1;
                    int dewPtIndex=-1;
                    int idx=0;
                    for(Measurement m:timeSeries.getMeasurements())
                    {
                        if(tempIndex<0 && m.getUri().toString().endsWith(".Temp"))
                            tempIndex=idx;
                        else if(dewPtIndex<0 && m.getUri().toString().endsWith(".DewPtTemp"))
                            dewPtIndex=idx;
                        idx++;
                    }
                    if(tempIndex<0)
                        throw(new IllegalArgumentException("Input time series does not contain Temp sensor"));
                    if(dewPtIndex<0)
                        throw(new IllegalArgumentException("Input time series does not contain Dew Point Temp sensor"));
                    try {
                        List<Measurement> meas=new ArrayList<>(timeSeries.getMeasurements());
                        meas.add(rh);
                        TimeSeries tsOut = new TimeSeries(context, meas);  // Same context Panama!!!
                        SortedMap<Instant,List<ValueQuality>> valsOut=new TreeMap<>();
                        for(Map.Entry<Instant,List<ValueQuality>> entry:timeSeries.getValues().entrySet())
                        {
                                ValueQuality<Double> tempIn=entry.getValue().get(0);  // This presumes knowledge of the measurements
                                ValueQuality<Double> dewPtIn=entry.getValue().get(1);
                                Double RH=RH(tempIn.getValue(),dewPtIn.getValue());
                                OPCCode RHQuality=OPCCode.getCombinedQuality(tempIn.getQuality(),dewPtIn.getQuality());
                                ValueQuality<Double> rhOut=new ValueQualityObject<>(new SupportedType<Double>(RH),RHQuality);
                                List<ValueQuality> vqsOut=new ArrayList<>(entry.getValue());
                                vqsOut.add(rhOut);
                                valsOut.put(entry.getKey(),vqsOut);
                        }
                        tsOut.setValues(valsOut);
                        return tsOut;
                    }catch(Exception e)
                    {
                        System.out.println("Exception while computing RH");
                        return null;
                    }
                });
                return destTS;
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        };

        // Define the integrator
        DataIntegrator integrator=(adapter,contextAndMeas,tstart,tend,agg,period)->
        {
            Stream<TimeSeries> sourceTs=source.get(contextAndMeas,tstart,tend,agg,period);
            Stream<TimeSeries> destTs=adapter.transform(sourceTs);
            // Write to destination
            destination.persist(destTs);
        };

        // Execute the integration
        // 8 hours worth of minutes
        Instant start=Instant.now().minus(8,ChronoUnit.HOURS);
        integrator.getTransformAndPut(transform,contextAndMeasIn,start,null,Aggregate.RAW,AggregateInterval.Minute);


        /// Interpolation example
        Adapter interpolator=(timeSeriesIn)-> {
                // Do the real transformation, or calculations
            TemporalAdjuster adjust=new StartOfTheMinuteAdjuster();
            Stream<TimeSeries> destTS=timeSeriesIn.map(timeSeries-> {
                Instant tStart=Instant.from(adjust.adjustInto(timeSeries.getValues().firstKey()));
                Instant tEnd=Instant.from(adjust.adjustInto(Instant.now().plus(1,ChronoUnit.MINUTES)));
                try {
                    TimeSeries out = Aggregator.interpolate(timeSeries, tStart, tEnd, 1, AggregateInterval.Minute, InterpolateMethod.LINEAR);
                    return out;
                }
                catch(InvalidObjectException ive)
                {
                    logger.error(ive);
                    throw(new RuntimeException(ive));
                }
            });
            return  destTS;
        };
        // Execute the integration
        // 8 hours worth of minutes
        start=Instant.now().minus(8,ChronoUnit.HOURS);
        integrator.getTransformAndPut(interpolator,contextAndMeasIn,start,null,Aggregate.RAW,AggregateInterval.Minute);

    }
}