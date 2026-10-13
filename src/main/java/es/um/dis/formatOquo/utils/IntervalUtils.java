package es.um.dis.formatOquo.utils;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

public class IntervalUtils {
	public static Range<Double> parseInterval(String intervalString) {
		if (intervalString == null || intervalString.trim().isEmpty() || "-".equals(intervalString.trim())) {
			return null;
		}
		String [] range = intervalString.split(",");
		if (range.length != 2) {
			return null;
		}
		
		String lowerNumberString = range[0].trim().substring(1).trim();
		Double lowerNumber = parseNumber(lowerNumberString);
		
		String lowerBoundString = range[0].trim().substring(0, 1);
		BoundType lowerBoundType = parseBoundType(lowerBoundString);
		
		String upperNumberString = range[1].trim().substring(0, range[1].trim().length()-1).trim();
		Double upperNumber = parseNumber(upperNumberString);
		
		String upperBoundString = range[1].trim().substring(range[1].trim().length()-1, range[1].trim().length());
		BoundType upperBoundType = parseBoundType(upperBoundString);
		
		return Range.range(lowerNumber, lowerBoundType, upperNumber, upperBoundType);
		
	}

	private static BoundType parseBoundType(String boundString) {
		BoundType boundType;
		if("[".equals(boundString) || "]".equals(boundString)) {
			boundType = BoundType.CLOSED;
		} else if ("(".equals(boundString) || ")".equals(boundString)) {
			boundType = BoundType.OPEN;
		} else {
			return null;
		}
		return boundType;
	}

	private static Double parseNumber(String numberString) {
		Double number;
		if ("-inf".equalsIgnoreCase(numberString)) {
			number = Double.NEGATIVE_INFINITY;
		} else if ("inf".equalsIgnoreCase(numberString)) {
			number = Double.POSITIVE_INFINITY;
		} else {
			number = Double.valueOf(numberString);
		}
		
		return number;
	}
}
