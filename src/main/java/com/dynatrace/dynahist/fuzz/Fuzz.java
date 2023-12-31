package com.dynatrace.dynahist.fuzz;


import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.dynatrace.dynahist.Histogram;
import com.dynatrace.dynahist.layout.Layout;
import com.dynatrace.dynahist.layout.LogLinearLayout;
import com.dynatrace.dynahist.layout.LogOptimalLayout;
import com.dynatrace.dynahist.layout.LogQuadraticLayout;
import com.dynatrace.dynahist.layout.OpenTelemetryExponentialBucketsLayout;
import com.dynatrace.dynahist.value.ValueEstimator;

import java.io.*;

/**
 * This class provides a simple target for fuzzing the schemaless
 * metric parser with Jazzer
 *
 * It uses DatapointParserFactory.parse() calls to produce the
 */
public class Fuzz {
	public static void fuzzerTestOneInput(FuzzedDataProvider data) {
		try {
			// Defining a bin layout
			int select = data.consumeInt(0, 3);

			final Layout layout;
			switch (select) {
				case 0:
					layout = LogQuadraticLayout.create(data.consumeDouble(), data.consumeDouble(), data.consumeDouble(), data.consumeDouble());
					break;
				case 1:
					layout = LogLinearLayout.create(data.consumeDouble(), data.consumeDouble(), data.consumeDouble(), data.consumeDouble());
					break;
				case 2:
					layout = OpenTelemetryExponentialBucketsLayout.create(data.consumeInt());
					break;
				case 3:
					layout = LogOptimalLayout.create(data.consumeDouble(), data.consumeDouble(), data.consumeDouble(), data.consumeDouble());
					break;
				default:
					throw new IllegalStateException("Unexpected value: " + select);
			}

			// Creating a dynamic histogram
			Histogram histogram = Histogram.createDynamic(layout);

			// Adding values to the histogram
			while (data.remainingBytes() > 0) {
				if (data.consumeBoolean()) {
					histogram.addValue(data.consumeDouble());
				}

				if (data.consumeBoolean()) {
					histogram.addAscendingSequence(i -> i + data.consumeLong(), data.consumeLong());
				}
			}

			// Querying the histogram
			histogram.getTotalCount();
			histogram.getMin();
			histogram.getMax();
			histogram.getValue(data.consumeLong()); // returns an estimate of the 2nd smallest value
			histogram.getValue(data.consumeLong(), getEstimator(data)); // returns an upper bound of the 4th smallest value
			histogram.getQuantile(data.consumeDouble()); // returns an estimate of the sample median
			histogram.getQuantile(data.consumeDouble(), getEstimator(data)); // returns a lower bound of the sample median

			// Merging histograms
			histogram.addHistogram(histogram);

			// Serialization
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			try {
				histogram.write(new DataOutputStream(bytes)); // write histogram to a java.io.DataOutput

				// read dynamic histogram from a java.io.DataInput
				Histogram.readAsDynamic(layout, new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
			} catch (IOException e) {
				// expected here
			}
		} catch (IllegalArgumentException | ArithmeticException e) {
			// expected here
		}
	}

	private static ValueEstimator getEstimator(FuzzedDataProvider data) {
		int i = data.consumeInt(0, 3);
		switch (i) {
			case 0:
				return ValueEstimator.LOWER_BOUND;
			case 1:
				return ValueEstimator.MID_POINT;
			case 2:
				return ValueEstimator.UPPER_BOUND;
			default:
				return ValueEstimator.UNIFORM;
		}
	}
}
