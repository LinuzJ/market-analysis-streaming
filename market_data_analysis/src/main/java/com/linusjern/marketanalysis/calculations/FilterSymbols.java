package com.linusjern.marketanalysis.calculations;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.flink.api.common.functions.FilterFunction;

import com.linusjern.marketanalysis.types.CrossoverEventResult;

public class FilterSymbols {
    public static HashSet<String> validSymbols = new HashSet<String>(
            Arrays.asList("ALCUR.FR", "VASTN.NL", "MTRK.NL", "ALMRB.FR"));

    public static class FilterRequestedSymbols implements FilterFunction<CrossoverEventResult> {

        @Override
        public boolean filter(CrossoverEventResult event) throws Exception {
            return FilterSymbols.validSymbols.contains(event.event.symbol);
        }
    }
}