package com.linusjern.marketanalysis.calculations;

import java.util.Arrays;
import java.util.HashSet;

import com.linusjern.marketanalysis.types.SignalType;

public class ExponentialMovingAverage {
    private HashSet<Integer> smoothingFactors = new HashSet<Integer>(Arrays.asList(38, 100));

    public Float calculateEMA(Float newClose, Float prevWindow, Integer smoothingFactor) throws Exception {
        if (!this.smoothingFactors.contains(smoothingFactor)) {
            throw new Exception("wrong smoothing factor");
        }
        return (newClose * (2f / (1f + smoothingFactor))) + (prevWindow * (1f - (2f / (1f + smoothingFactor))));
    }

    public SignalType evaluateBreakoutType(Float prevEMA38, Float curEMA38, Float prevEMA100, Float curEMA100) {
        if (curEMA38 > curEMA100 && prevEMA38 <= prevEMA100) {
            return SignalType.Long;
        }

        if (curEMA38 < curEMA100 && prevEMA38 >= prevEMA100) {
            return SignalType.Short;
        }

        return SignalType.None;
    }
}
