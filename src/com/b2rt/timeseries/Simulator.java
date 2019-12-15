package com.b2rt.timeseries;

import com.b2rt.data.InvalidTypeException;
import java.io.InvalidObjectException;
import java.time.Instant;

public interface Simulator {
    TimeSeries generateTimeSeries(Instant start, Instant end, AggregateInterval period, boolean includeEnd, boolean randomizeTime) throws InvalidObjectException, InvalidTypeException;
}
