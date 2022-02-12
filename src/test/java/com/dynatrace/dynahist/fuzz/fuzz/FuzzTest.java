package com.dynatrace.dynahist.fuzz.fuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.dynatrace.dynahist.fuzz.Fuzz;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FuzzTest {
	private final FuzzedDataProvider provider = mock(FuzzedDataProvider.class);

	@Test
	public void test() {
		Fuzz.fuzzerTestOneInput(provider);

		Random rnd = new Random(98473L);

		when(provider.consumeDouble()).thenReturn(rnd.nextDouble());
		when(provider.consumeLong()).thenReturn(rnd.nextLong());
		when(provider.consumeInt()).thenReturn(rnd.nextInt());
		when(provider.consumeInt(anyInt(), anyInt())).thenReturn(rnd.nextInt());
		when(provider.consumeBoolean()).thenReturn(rnd.nextBoolean());

		Fuzz.fuzzerTestOneInput(provider);
	}

	@Disabled("Local test for verifying a slow run")
	@Test
	public void testSlowUnit() {
		//Fuzz.fuzzerTestOneInput(FileUtils.readFileToByteArray(new File("slow-unit-0a0b0ce97bb332cd9f8fde03e03840768a81d29d")));
	}
}