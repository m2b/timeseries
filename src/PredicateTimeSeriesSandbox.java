import com.b2rt.data.InvalidTypeException;
import com.b2rt.data.SupportedType;
import com.b2rt.timeseries.*;

import java.io.InvalidObjectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredicateTimeSeriesSandbox {

    public static void main(String[] args) throws InvalidTypeException,InvalidObjectException,URISyntaxException,ParseException {

        HashMap<String,SupportedType> context=new HashMap<>();
        context.put("Continent",new SupportedType("America"));
        context.put("Country",new SupportedType("Panama"));
        context.put("City",new SupportedType("David"));
        context.put("Lat",new SupportedType(8.4050104));
        context.put("Lon",new SupportedType(-82.4971238));
        context.put("YearFounded",new SupportedType(1602));

        List<Measurement> measurements=new ArrayList<>();
        Measurement temp=new Measurement(new URI("hbase://pamana.david.temp"),"Temperature (climate)","°C",
                new SupportedType(0f),new SupportedType(35f));
        measurements.add(temp);
        Measurement hum=new Measurement(new URI("hbase://pamana.david.humidity"),"Relative Humidity (climate)","%RH",
                new SupportedType(20f),new SupportedType(100f));
        measurements.add(hum);
        Measurement windspeed=new Measurement(new URI("hbase://pamana.david.windspeed"),"Wind Speed","km/h",
                new SupportedType(0f),new SupportedType(120f));
        measurements.add(windspeed);
        Measurement winddirection=new Measurement(new URI("hbase://pamana.david.winddirection"),"Wind Direction","°",
                new SupportedType(0f),new SupportedType(360f));
        measurements.add(winddirection);
        Measurement population=new Measurement(new URI("hbase://pamana.david.population"),"Population in town",null,
                new SupportedType((int)35000),new SupportedType((int)50000));
        measurements.add(population);
        Measurement goodday=new Measurement(new URI("hbase://pamana.david.goodday"),"Is it a good day",null,
                new SupportedType(false),new SupportedType(true));
        measurements.add(goodday);
        Measurement saysomething=new Measurement(new URI("hbase://panama.david.randomword"),"Something to say",null,new SupportedType("na"),new SupportedType("na"));
        measurements.add(saysomething);

        // Time Series Simulator
        PrimitiveTypesSimulator sim=new PrimitiveTypesSimulator(context,measurements,0.8);

        // Get some values and print them
        // Start of last hour to end of last hour minute by minute
        ZonedDateTime dtEnd=ZonedDateTime.now();
        Instant end=Instant.from(new StartOfTheHourAdjuster().adjustInto(dtEnd));
        Instant start=end.minus(Duration.ofHours(1));
        PrintTimeSeries(sim.generateTimeSeries(start,end,AggregateInterval.Minute,false,false));

    }

    public static void PrintTimeSeries(TimeSeries series)
    {
        System.out.println("Context:");
        HashMap<String,SupportedType> context=series.getContext();
        for(Map.Entry<String,SupportedType> entry:context.entrySet())
        {
                System.out.printf("%s=%s%n",entry.getKey(),entry.getValue().getValue());
        }
        // Blank line
        System.out.println();
        // Title
        String uri=series.getMeasurements().get(0).getUri().toString();
        int lastperiod=uri.lastIndexOf('.');
        System.out.printf("Measurements for %s%n",uri.substring(0,lastperiod));
        System.out.print("TimeStamp,");
        int len=series.getContext().size();
        int idx=0;
        for(Measurement m:series.getMeasurements())
        {
            uri=m.getUri().toString();
            System.out.print(uri.substring(lastperiod+1));
            if(m.getUom()!=null && m.getUom().length()>0)
                System.out.print(String.format("(%s)",m.getUom()));
            if(idx++<(len-1))
                System.out.print(",");
            else
                System.out.printf("%n");
        }
        for(Map.Entry<Instant,List<ValueQuality>> vtq:series.getValues().entrySet())
        {
            System.out.printf("%s,",vtq.getKey());
            idx=0;
            len=vtq.getValue().size();
            for(ValueQuality vq:vtq.getValue())
            {
                System.out.printf("%s(%s)",vq.getValue(),vq.getQuality());
                if(idx++<(len-1))
                    System.out.print(",");
                else
                    System.out.printf("%n");
            }

        }
    }
}