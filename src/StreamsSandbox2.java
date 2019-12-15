import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamsSandbox2 {

    static class TagVTQs
    {
        String tag;
        Instant startTime;
        List<VTQ> vtqs;
        TagVTQs(String tag,Instant startTime,List<VTQ> vtqs)
        {
            this.tag=tag;
            this.startTime=startTime;
            this.vtqs =vtqs;
        }
    }

    static class VTQ
    {
        ValueQuality2 vq;
        Instant timeStamp;
        VTQ(ValueQuality2 vq, Instant timeStamp)
        {
            this.vq=vq;
            this.timeStamp=timeStamp;
        }
    }

    static class VTQs
    {
        public Instant getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Instant timestamp) {
            this.timestamp = timestamp;
        }

        public List<ValueQuality2> getValues() {
            return values;
        }

        public void setValues(List<ValueQuality2> values) {
            this.values = values;
        }

        Instant timestamp;
        List<ValueQuality2> values;

        VTQs(Instant timestamp,List<ValueQuality2> values)
        {
            this.timestamp=timestamp;
            this.values=values;
        }
    }
    static class ValueQuality2
    {
        double value;
        int quality;

        ValueQuality2(double value, int quality)
        {
            this.value=value;
            this.quality=quality;
        }

        @Override
        public String toString()
        {
            return String.format("value=%s, quality=%s",value,quality);
        }
    }

    static class TimeSeries2 {  // Static so it can be accessed by Main
        String context;
        List<String> tags;
        SortedMap<Instant,List<ValueQuality2>> values;

        TimeSeries2(String context, List<String> tags, SortedMap<Instant,List<ValueQuality2>> values) {
            this.context= context;
            this.tags = tags;
            this.values=values;
        }
        @Override
        public String toString() {
            return context;
        }
    }

    static SortedMap<Instant,List<ValueQuality2>> generateVTQs(int valsCount, int tagsCount)
    {
        Instant now=Instant.now();
        Stream<Instant> gen=Stream.iterate(now,n->n.minus(1,ChronoUnit.HOURS)).limit(valsCount);
        TreeMap<Instant,List<ValueQuality2>> map=new TreeMap<>(gen.map(i->{
            VTQs vtqs=new VTQs(i,generateValQuals(tagsCount));
            return vtqs;
        }).collect(Collectors.toMap(VTQs::getTimestamp, VTQs::getValues)));
        return map;
    }

    static List<ValueQuality2> generateValQuals(int count)
    {
        Stream<ValueQuality2> gen=Stream.generate(()-> new ValueQuality2(Math.random(),(int)Math.round(100*Math.random()))).limit(count);
        return gen.collect(Collectors.toList());
    }


    static int tagsCount=10;
    static int valsCount=60;
    public static void main(String[] args)
    {
        // Generate tags
        List<String> tags=Stream.iterate(0,n->n+1).limit(tagsCount).map(n->"tag"+n).collect(Collectors.toList());

        // Try generating simulated values
        SortedMap<Instant,List<ValueQuality2>> tsVals=generateVTQs(valsCount,tagsCount);

        // Create time series
        TimeSeries2 ts=new TimeSeries2("test ts",tags,tsVals);
        // Split them by tag+timestamp
        List<TagVTQs> tagVTQs=Stream.iterate(0,n->n+1).limit(ts.tags.size()).map(n-> {
                    List<VTQ> vtqs = ts.values.entrySet().stream().map(entry -> {
                        return new VTQ(entry.getValue().get(n), entry.getKey());
                    }).collect(Collectors.toList());
                    return new TagVTQs(ts.tags.get(n), vtqs.get(0).timeStamp, vtqs);
                }).collect(Collectors.toList());

        System.out.println("got here");
    }
}
