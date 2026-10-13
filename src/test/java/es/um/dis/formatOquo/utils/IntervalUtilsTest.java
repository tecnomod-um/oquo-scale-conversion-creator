package es.um.dis.formatOquo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

class IntervalUtilsTest {

	@Test
	void parsesClosedOpenInterval() {
		Range<Double> range = IntervalUtils.parseInterval("[0, 0.1)");
		assertEquals(0.0, range.lowerEndpoint());
		assertEquals(BoundType.CLOSED, range.lowerBoundType());
		assertEquals(0.1, range.upperEndpoint());
		assertEquals(BoundType.OPEN, range.upperBoundType());
	}

	@Test
	void parsesOpenClosedInterval() {
		Range<Double> range = IntervalUtils.parseInterval("(8, 11]");
		assertEquals(Range.openClosed(8.0, 11.0), range);
	}

	@Test
	void parsesPositiveInfinity() {
		Range<Double> range = IntervalUtils.parseInterval("[1.5, Inf)");
		assertEquals(Range.closedOpen(1.5, Double.POSITIVE_INFINITY), range);
	}

	@Test
	void parsesNegativeInfinity() {
		Range<Double> range = IntervalUtils.parseInterval("(-Inf, 5]");
		assertEquals(Range.openClosed(Double.NEGATIVE_INFINITY, 5.0), range);
	}

	@Test
	void infinityIsCaseInsensitive() {
		assertEquals(Range.closedOpen(0.0, Double.POSITIVE_INFINITY), IntervalUtils.parseInterval("[0, INF)"));
	}

	@Test
	void toleratesExtraWhitespace() {
		assertEquals(Range.closedOpen(0.97, 1.03), IntervalUtils.parseInterval("  [ 0.97 ,  1.03 )  "));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "   ", "-", " - " })
	void returnsNullForEmptyValues(String value) {
		assertNull(IntervalUtils.parseInterval(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "[0)", "[0, 1, 2)" })
	void returnsNullWhenNotTwoEndpoints(String value) {
		assertNull(IntervalUtils.parseInterval(value));
	}

	@Test
	void failsOnNonNumericEndpoint() {
		assertThrows(NumberFormatException.class, () -> IntervalUtils.parseInterval("[a, 1)"));
	}
}
