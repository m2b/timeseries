import com.b2rt.data.InvalidTypeException;
import com.b2rt.data.SupportedType;
import com.b2rt.timeseries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InvalidObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class InterpolateSandbox {
    /// Main and static methods
    final static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws InvalidTypeException, InvalidObjectException, URISyntaxException, ParseException {

        HashMap<String, SupportedType> context = new HashMap<>();
        context.put("Continent", new SupportedType("America"));
        context.put("Country", new SupportedType("Panama"));
        context.put("City", new SupportedType("David"));
        context.put("Lat", new SupportedType(8.4050104));
        context.put("Lon", new SupportedType(-82.4971238));
        context.put("YearFounded", new SupportedType(1602));

        List<Measurement> measurements = new ArrayList<>();
        Measurement temp = new Measurement(new URI("hbase://pamana.david.temp"), "Temperature (climate)", "°C",
                new SupportedType(0f), new SupportedType(35f));
        measurements.add(temp);
        Measurement hum = new Measurement(new URI("hbase://pamana.david.humidity"), "Relative Humidity (climate)", "%RH",
                new SupportedType((int) 20), new SupportedType((int) 100));
        measurements.add(hum);
        Measurement mood = new Measurement(new URI("hbase://pamana.david.mood"), "Mood", null,
                new SupportedType("good"), new SupportedType("very bad"));
        measurements.add(mood);

        // Create time series of values
        Instant t0 = SupportedType.stringToInstant("2017-01-20T00:00:01-05:00");
        Instant t1 = SupportedType.stringToInstant("2017-01-20T00:00:02-05:00");
        Instant t2 = SupportedType.stringToInstant("2017-01-20T00:01:01-05:00");
        Instant t3 = SupportedType.stringToInstant("2017-01-20T00:02:59-05:00");
        Instant t4 = SupportedType.stringToInstant("2017-01-20T00:04:00-05:00");
        Instant t5 = SupportedType.stringToInstant("2017-01-20T00:10:01-05:00");
        Instant t6 = SupportedType.stringToInstant("2017-01-20T00:15:22-05:00");
        ValueQuality<Float> temp0 = new ValueQualityObject<Float>(5.3f, OPCCode.Good);
        ValueQuality<Float> temp1 = new ValueQualityObject<Float>(8.9f, OPCCode.Bad);
        ValueQuality<Float> temp2 = new ValueQualityObject<Float>(10.7f, OPCCode.Good);
        ValueQuality<Float> temp3 = new ValueQualityObject<Float>(12.6f, OPCCode.Uncertain);
        ValueQuality<Float> temp4 = new ValueQualityObject<Float>(20.7f, OPCCode.Good);
        ValueQuality<Float> temp5 = new ValueQualityObject<Float>(25.4f, OPCCode.Good);
        ValueQuality<Float> temp6 = new ValueQualityObject<Float>(31.5f, OPCCode.Bad);
        ValueQuality<Integer> rh0 = new ValueQualityObject<Integer>(46, OPCCode.Uncertain);
        ValueQuality<Integer> rh1 = new ValueQualityObject<Integer>(78, OPCCode.Good);
        ValueQuality<Integer> rh2 = new ValueQualityObject<Integer>(67, OPCCode.Good);
        ValueQuality<Integer> rh3 = new ValueQualityObject<Integer>(89, OPCCode.Uncertain);
        ValueQuality<Integer> rh4 = new ValueQualityObject<Integer>(99, OPCCode.Bad);
        ValueQuality<Integer> rh6 = new ValueQualityObject<Integer>(87, OPCCode.Bad);
        ValueQuality<Integer> rh5 = new ValueQualityObject<Integer>(58, OPCCode.Good);
        ValueQuality<String> mood0 = new ValueQualityObject<String>("hello hello hello", OPCCode.Uncertain);
        ValueQuality<String> mood1 = new ValueQualityObject<String>("is there anybody", OPCCode.Good);
        ValueQuality<String> mood2 = new ValueQualityObject<String>("in there", OPCCode.Good);
        ValueQuality<String> mood3 = new ValueQualityObject<String>("just nod if you can hear me", OPCCode.Uncertain);
        ValueQuality<String> mood4 = new ValueQualityObject<String>("dark side", OPCCode.Bad);
        ValueQuality<String> mood5 = new ValueQualityObject<String>("Pink", OPCCode.Bad);
        ValueQuality<String> mood6 = new ValueQualityObject<String>("Floyd", OPCCode.Good);
        SortedMap<Instant,List<ValueQuality>> vtqs=new TreeMap<>();
        List<ValueQuality> vals0=Arrays.asList(temp0,rh0,mood0);
        List<ValueQuality> vals1=Arrays.asList(temp1,rh1,mood1);
        List<ValueQuality> vals2=Arrays.asList(temp2,rh2,mood2);
        List<ValueQuality> vals3=Arrays.asList(temp3,rh3,mood3);
        List<ValueQuality> vals4=Arrays.asList(temp4,rh4,mood4);
        List<ValueQuality> vals5=Arrays.asList(temp5,rh5,mood5);
        List<ValueQuality> vals6=Arrays.asList(temp6,rh6,mood6);
        vtqs.put(t0,vals0);
        vtqs.put(t1,vals1);
        vtqs.put(t2,vals2);
        vtqs.put(t3,vals3);
        vtqs.put(t4,vals4);
        vtqs.put(t5,vals5);
        vtqs.put(t6,vals6);
        TimeSeries tsIn=new TimeSeries(context,measurements);
        tsIn.setValues(vtqs);

        // Interpolate and print
        ValuesSimulator.printTimeSeries(tsIn.getValues().firstKey(),tsIn);
        Instant tStart = SupportedType.stringToInstant("2017-01-20T00:00:00-05:00");
        Instant tEnd = SupportedType.stringToInstant("2017-01-20T00:20:00-05:00");
        logger.info(String.format("%nLINEAR INTERPOLATION"));
        TimeSeries tsOut= Aggregator.interpolate(tsIn,tStart,tEnd,1,AggregateInterval.Minute, InterpolateMethod.LINEAR);
        ValuesSimulator.printTimeSeries(tStart,tsOut);
        logger.info(String.format("%nNEAREST INTERPOLATION"));
        tsOut=Aggregator.interpolate(tsIn,tStart,tEnd,1,AggregateInterval.Minute,InterpolateMethod.NEAREST);
        ValuesSimulator.printTimeSeries(tStart,tsOut);
        logger.info(String.format("%nPREVIOUS INTERPOLATION"));
        tsOut=Aggregator.interpolate(tsIn,tStart,tEnd,1,AggregateInterval.Minute,InterpolateMethod.PREVIOUS);
        ValuesSimulator.printTimeSeries(tStart,tsOut);
        logger.info(String.format("%nNEXT INTERPOLATION"));
        tsOut=Aggregator.interpolate(tsIn,tStart,tEnd,1,AggregateInterval.Minute,InterpolateMethod.NEXT);
        ValuesSimulator.printTimeSeries(tStart,tsOut);
    }

}
