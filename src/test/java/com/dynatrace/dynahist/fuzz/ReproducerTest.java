package com.dynatrace.dynahist.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import java.io.IOException;

public class ReproducerTest {
    // see how to do this at https://github.com/CodeIntelligenceTesting/jazzer/discussions/600
    @FuzzTest
    public void testSlowAndOOM(FuzzedDataProvider data) throws IOException {
        Fuzz.fuzzerTestOneInput(data);
    }
}
