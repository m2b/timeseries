package com.b2rt.timeseries;

import com.b2rt.data.SupportedType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;

@FunctionalInterface
public interface DataIntegrator {
    void getTransformAndPut(Adapter transform,
                            HashMap<HashMap<String, SupportedType>, List<Measurement>> sourceContextsAndMeasurements,
                            Instant start, Instant end, Aggregate sourceAggregate, AggregateInterval sourcePeriod);
}


// Usage example
/*
    sourceRep=new Repository
    destRep=new Repository
    transform=(sourceTs)->
    {
        // must know how to read measurements from context
        // Sandbox time... do interpolation from source ts to dest ts using streams
        destTS=stream of ts;
        return destTS
    };

    integrator=(transform,sourceCtxAndMeas,....)->
    {
        sourceTS=sourceRep.get
        destTs=transform.transform(sourceTs);
        destRep.put(destTs)
    };
 */
