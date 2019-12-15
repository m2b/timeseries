package com.b2rt.timeseries;

import java.util.stream.Stream;

@FunctionalInterface
public interface Adapter {
    Stream<TimeSeries> transform(Stream<TimeSeries> input);
}
