package com.b2rt.timeseries;

import com.b2rt.data.InvalidTypeException;
import com.b2rt.data.SupportedType;
import org.apache.commons.cli.*;
import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import java.io.*;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValuesSimulator implements Repository {

    double pctGood;
    boolean randomizeTime=false;

    /// Constructor
    public ValuesSimulator(double pctGood)
    {
        if(pctGood<0 || pctGood>1.0)
            throw(new IllegalArgumentException("pctGood must be between 0.0 and 1.0"));
        this.pctGood=pctGood;
    }

    public ValuesSimulator(double pctGood,boolean randomizeTime)
    {
        this(pctGood);
        this.randomizeTime=randomizeTime;
    }

    @Override
    public Optional<TimeSeries> get(HashMap<String, SupportedType> context, List<Measurement> measurements, Instant start, Instant end,
                                    Aggregate aggregate, AggregateInterval period) {
        if(aggregate!=Aggregate.RAW)
            throw(new IllegalArgumentException("Only RAW values are supported"));

        PrimitiveTypesSimulator sim=new PrimitiveTypesSimulator(context,measurements,pctGood);
        Optional<TimeSeries> results=Optional.empty();
        try {
            results=Optional.ofNullable(sim.generateTimeSeries(start,end,period,true,randomizeTime));
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(),e);
        }

        return results;
    }

    @Override
    public int persist(Stream<TimeSeries> timeseries) {
        // For the purpose of this simulator just echo the values received and a total count
        Integer total=timeseries.map((ts)->
        {
            int count=ts.values.size();
            printTimeSeries(ts.values.firstKey(),ts);
            return count;
        }).reduce(0,(a,b)->a+b);
        logger.info(String.format("Persisted %s values",total));
        return total;
    }

    @Override
    public int remove(Stream<TimeSeries> timeseries) {
        throw(new NotImplementedException("Method is not supported"));
    }

    @Override
    public Stream<TimeSeries> get(HashMap<HashMap<String, SupportedType>, List<Measurement>> contextsAndMeasurements, Instant start, Instant end, Aggregate aggregate, AggregateInterval period) {

        if(aggregate!=Aggregate.RAW)
            throw(new IllegalArgumentException("Only RAW values are supported"));

        // For each context and measurement set
        Stream<TimeSeries> ret = contextsAndMeasurements.entrySet().stream().map(entry->{
            Optional<TimeSeries> ts=get(entry.getKey(),entry.getValue(),start,end,Aggregate.RAW,period);
            if(ts.isPresent())
                return ts.get();
            else
                return null;
        });

        return ret;
    }

    /// Special cases
    public HashMap<Instant,TimeSeries> getHourlyValues(HashMap<String,SupportedType> context, List<Measurement> measuremens, Instant start) throws InvalidTypeException,InvalidObjectException
    {
        HashMap<Instant,TimeSeries> ret=new HashMap<>();

        // Make sure it is start of hour
        start=Instant.from(new StartOfTheHourAdjuster().adjustInto(start));

        // Get start of current hour (can only backfill completed hours)
        Instant end=Instant.from(new StartOfTheHourAdjuster().adjustInto(Instant.now()));

        Instant hourStart=Instant.from(start);
        //  Backfill until start
        while(hourStart.compareTo(end)<0)
        {
            Instant hourEnd=hourStart.plus(1,ChronoUnit.HOURS);
            PrimitiveTypesSimulator sim=new PrimitiveTypesSimulator(context,measuremens,pctGood);
            TimeSeries results=sim.generateTimeSeries(hourStart,hourEnd,AggregateInterval.Minute,false,randomizeTime);
            ret.put(hourStart,results);
            hourStart=hourEnd;
        }
        return ret;
    }

    public HashMap<Instant,TimeSeries> getMinutelyValues(HashMap<String,SupportedType> context, List<Measurement> measuremens, Instant start) throws InvalidTypeException,InvalidObjectException
    {
        HashMap<Instant,TimeSeries> ret=new HashMap<>();

        // Make sure it is start of hour
        start=Instant.from(new StartOfTheMinuteAdjuster().adjustInto(start));

        // Get start of current hour (can only backfill completed hours)
        Instant end=Instant.from(new StartOfTheMinuteAdjuster().adjustInto(Instant.now()));

        Instant minStart=Instant.from(start);
        //  Backfill until start
        while(minStart.compareTo(end)<0)
        {
            Instant minEnd=minStart.plus(1,ChronoUnit.MINUTES);
            PrimitiveTypesSimulator sim=new PrimitiveTypesSimulator(context,measuremens,pctGood);
            TimeSeries results=sim.generateTimeSeries(minStart,minEnd,AggregateInterval.Second,false,randomizeTime);
            ret.put(minStart,results);
            minStart=minEnd;
        }
        return ret;
    }

    /// Main and static methods
    final static Logger logger = LogManager.getLogger();

    static Instant startDt = ZonedDateTime.of(ZonedDateTime.now().getYear(), 1, 1, 0, 0,0,0,ZoneId.systemDefault()).toInstant();;

    static Options getOptions() {
        Options options = new Options();
        Option tags = new Option("t", "tags", true, "path to tags file");
        tags.setArgName("tagsfile");
        tags.setRequired(true);
        Option pctGood = new Option("g", "goodPct", true, "optional - fraction of good quality values to generate (default: 1.0)");
        pctGood.setArgName("pctGoodQuality");
        pctGood.setRequired(false);
        Option mode = new Option("m", "grpMode", true, "optional - mode of simulation {h,m,n} meaning {minute values grouped by hour,second values grouped by minute,none - second values not grouped} (default: none)."
                +System.lineSeparator() + "Grouped modes will return values up to before the start of the most recent group. E.g. minute before the start of current hour, second before the start of current minute, etc.");
        mode.setArgName("groupingMode");
        mode.setRequired(false);
        Option startDateTime = new Option("s", "startDt", true, String.format("optional - datetime to start simulating/backfilling values (default: %s)."
                + System.lineSeparator() + "datetime must be in ISO format (e.g. 2007-12-03T10:15:30Z)",startDt));
        startDateTime.setArgName("startDateTime");
        startDateTime.setRequired(false);
        options.addOption(tags);
        options.addOption(pctGood);
        options.addOption(startDateTime);
        options.addOption(mode);
        return options;
    }

    static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java ValuesSimulator [options]", options);
    }

    enum GROUPMODE {HOURLY,MINUTELY,NONE}

    public static void main(String[] args) {
        Options options = getOptions();
        String tagsfile;
        Double pctGood = 1.0;
        GROUPMODE mode=GROUPMODE.NONE;
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cl = parser.parse(options, args);
            // Parse arguments
            tagsfile = cl.getOptionValue("t");
            File f = new File(tagsfile);
            if (!f.exists() && !f.isDirectory())
                throw (new IllegalArgumentException(String.format("Tags file %s does not exist or is not a file", tagsfile)));
            if (cl.hasOption("g")) {
                pctGood = Double.parseDouble(cl.getOptionValue("g"));
            }
            if (cl.hasOption("s")) {
                startDt = SupportedType.stringToInstant(cl.getOptionValue("s"));
            }
            if(cl.hasOption("m")) {
                String smode=cl.getOptionValue("m").toLowerCase();
                if(smode.startsWith("h"))
                    mode=GROUPMODE.HOURLY;
                else if(smode.startsWith("m"))
                    mode=GROUPMODE.MINUTELY;
            }

            // Get measurements from tags file
            List<Measurement> measurements = null;
            try {
                measurements = getMeasurementsFromFile(tagsfile);
            } catch (Exception e) {
                throw (new RuntimeException(e));
            }

            // Construct Value Simulator
            HashMap<String, SupportedType> context = new HashMap<>();
            try {
                context.put("TagsFile", new SupportedType(tagsfile));
            } catch (InvalidTypeException ite) {
                throw (new RuntimeException(ite));
            }

            ValuesSimulator valSim = new ValuesSimulator(pctGood);

            // Generate values according to mode
            int rows=0;
            switch(mode)
            {
                case HOURLY:
                    logger.info("Simulating HOURLY grouping");
                    HashMap<Instant,TimeSeries> hrVals=valSim.getHourlyValues(context,measurements,startDt);
                    for(HashMap.Entry<Instant,TimeSeries> entry:hrVals.entrySet())
                    {
                        printTimeSeries(entry.getKey(),entry.getValue());
                    }
                    break;
                case MINUTELY:
                    logger.info("Simulating MINUTELY grouping");
                    HashMap<Instant,TimeSeries> minVals=valSim.getMinutelyValues(context,measurements,startDt);
                    for(HashMap.Entry<Instant,TimeSeries> entry:minVals.entrySet())
                    {
                        printTimeSeries(entry.getKey(),entry.getValue());
                    }
                    break;
                default:
                    logger.info("Simulating second values without grouping");
                    HashMap<HashMap<String,SupportedType>,List<Measurement>> meas=new HashMap<>();
                    meas.put(context,measurements);
                    Stream<TimeSeries> ts=valSim.get(meas,startDt,null,Aggregate.RAW,AggregateInterval.Second);
                    Set<TimeSeries> tsSet=ts.collect(Collectors.toSet());
                    for(TimeSeries entry:tsSet)
                        printTimeSeries(startDt,entry);
            }
        } catch (Exception e) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Missing required option"))
                usage(options);
            else
                e.printStackTrace();
        }
    }

    static Measurement parseLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 6)
                throw (new ParseException("Not enough columns in line for measurement definition"));
            Measurement meas = null;
            String uri = parts[0];
            if (uri.isEmpty())
                throw (new IllegalArgumentException("tag column cannot be null or empty"));
            String desc = parts[1];
            String type = parts[2].toLowerCase();
            String units = parts[3];
            String min = parts[4];
            String max = parts[5];
            if (type.isEmpty())
                throw (new IllegalArgumentException("datatype column cannot be null or empty"));
            switch (type.substring(0, 1)) {
                case "f":
                    if (min.isEmpty())
                        throw (new IllegalArgumentException("min column cannot be null or empty"));
                    if (max.isEmpty())
                        throw (new IllegalArgumentException("max column cannot be null or empty"));
                    float fmin = Float.parseFloat(min);
                    float fmax = Float.parseFloat(max);
                    meas = new Measurement(new URI(uri), desc, units, new SupportedType(fmin), new SupportedType(fmax));
                    break;
                case "b":
                    if (min.isEmpty())
                        throw (new IllegalArgumentException("min column cannot be null or empty"));
                    if (max.isEmpty())
                        throw (new IllegalArgumentException("max column cannot be null or empty"));
                    boolean bmin = SupportedType.stringToBoolean(min);
                    boolean bmax = SupportedType.stringToBoolean(max);
                    meas = new Measurement(new URI(uri), desc, units, new SupportedType(bmin), new SupportedType(bmax));
                    break;

                case "i":
                    if (min.isEmpty())
                        throw (new IllegalArgumentException("min column cannot be null or empty"));
                    if (max.isEmpty())
                        throw (new IllegalArgumentException("max column cannot be null or empty"));
                    int imin = Integer.parseInt(min);
                    int imax = Integer.parseInt(max);
                    meas = new Measurement(new URI(uri), desc, units, new SupportedType(imin), new SupportedType(imax));
                    break;
                default:
                    meas = new Measurement(new URI(uri), desc, units, new SupportedType(min), new SupportedType(max));
            }
            return meas;
        } catch (Exception e) {
            throw (new RuntimeException(e));
        }
    }

    public static List<Measurement> getMeasurementsFromFile(String tagsFileUri) throws FileNotFoundException, IOException {
        List<Measurement> measurements = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(tagsFileUri));
            boolean isHeader = true;
            for (String line; (line = br.readLine()) != null; ) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }


                tags.add(line);
            }
        } catch (FileNotFoundException fnf) {
            System.out.println("Tags file " + tagsFileUri + " not found");
            throw (fnf);
        } catch (IOException ioe) {
            System.out.println("Exception reading tags file " + tagsFileUri);
            ioe.printStackTrace();
            throw (ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    ;
                }
            }
        }

        if (tags.size() == 0) {
            System.out.println("No tags found in file file " + tagsFileUri);
            System.exit(1);
        }

        for (String tag : tags) {
            measurements.add(parseLine(tag));
        }
        return measurements;
    }

    public static List<Measurement> getMeasurementsFromFile(InputStream stream) throws IOException {
        List<Measurement> measurements = new ArrayList<>();
        ArrayList<String> tags = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(stream));
            boolean isHeader = true;
            for (String line; (line = br.readLine()) != null; ) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }


                tags.add(line);
            }
        } catch (IOException ioe) {
            System.out.println("Exception reading tags file from stream");
            ioe.printStackTrace();
            throw (ioe);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    ;
                }
            }
        }

        if (tags.size() == 0) {
            System.out.println("No tags found in file stream");
            System.exit(1);
        }

        for (String tag : tags) {
            measurements.add(parseLine(tag));
        }
        return measurements;
    }

    public static void printTimeSeries(Instant startTime, TimeSeries series)
    {
        ZonedDateTime zdt=startTime.atZone(ZoneId.systemDefault());

        logger.info(String.format("START TIME (Local): %s", zdt));
        logger.info("VALUES (long quality or units in parentheses):");
        int len = series.getMeasurements().size();
        int idx = 0;
        // Header
        StringBuilder header=new StringBuilder();
        header.append("TIMESTAMP (Local),");
        for (Measurement m : series.getMeasurements()) {
            header.append(m.getUri().toString());
            if (m.getUom() != null && m.getUom().length() > 0)
                header.append(String.format("(%s)", m.getUom()));
            if (idx++ < (len - 1))
                header.append(",");
//            else
//                header.append(System.lineSeparator());
        }
        logger.info(header.toString());

        // Values
        StringBuilder entry=new StringBuilder();
        for (Map.Entry<Instant, List<ValueQuality>> vtq : series.getValues().entrySet()) {
            zdt = vtq.getKey().atZone(ZoneId.systemDefault());
            entry.append(String.format("%s,", zdt));
            idx = 0;
            len = vtq.getValue().size();
            for (ValueQuality vq : vtq.getValue()) {
                entry.append(String.format("%s(%s)", vq.getValue(), vq.getQuality().getValue()));
                if (idx++ < (len - 1))
                    entry.append(",");
//                else
//                    entry.append(System.lineSeparator());
            }
            logger.info(entry.toString());
            entry.setLength(0);
        }

        // Blank line
        System.out.println();
    }
}
