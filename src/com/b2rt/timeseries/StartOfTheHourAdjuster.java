package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;

public class StartOfTheHourAdjuster implements TemporalAdjuster
{
    @Override
    public Temporal adjustInto(Temporal temporal) {
        LocalDateTime dt=null;
        if(temporal instanceof Instant)
            dt=LocalDateTime.ofInstant((Instant)temporal,ZoneOffset.UTC);
        else
            dt=LocalDateTime.from(temporal);
        int month=dt.getMonthValue();
        int day=dt.getDayOfMonth();
        int hour=dt.getHour();
        String strStartOfHour=dt.getYear()+"-"+(month<10?"0":"")+month+"-"+(day<10?"0":"")+day+"T"+(hour<10?"0":"")+hour+":00:00.00Z";
        try {
            Instant start = SupportedType.stringToInstant(strStartOfHour);
            return start;
        }
        catch(ParseException pe)
        {
            throw(new RuntimeException(pe));
        }
    }
}