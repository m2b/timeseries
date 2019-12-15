package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface Repository {

    Optional<TimeSeries> get(HashMap<String, SupportedType> context, List<Measurement> measurements, Instant start, Instant end, Aggregate aggregate, AggregateInterval period);

    int persist(Stream<TimeSeries> timeseries);  // Return values persisted

    int remove(Stream<TimeSeries> timeseries);  // Return values removed

    Stream<TimeSeries> get(HashMap<HashMap<String, SupportedType>, List<Measurement>> contextsAndMeasurements, Instant start, Instant end, Aggregate aggregate, AggregateInterval period);
}
