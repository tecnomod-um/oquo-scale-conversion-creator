package es.um.dis.formatOquo.pojo;

import java.io.Serializable;
import java.util.Objects;

import com.google.common.collect.Range;

/**
 * The Class OquareScaleValue.
 */
public class OquareScaleValueByRange implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3162950167312735388L;
	
	/** The Category. */
	private OquareCategory category;
	
	/** The range. */
	private Range<Double> range;
	
	
	
	public OquareScaleValueByRange(OquareCategory category, Range<Double> range) {
		super();
		this.category = category;
		this.range = range;
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public OquareCategory getCategory() {
		return category;
	}
	
	/**
	 * Sets the category.
	 *
	 * @param category the new category
	 */
	public void setCategory(OquareCategory category) {
		this.category = category;
	}
	
	/**
	 * Gets the range.
	 *
	 * @return the range
	 */
	public Range<Double> getRange() {
		return range;
	}
	
	/**
	 * Sets the range.
	 *
	 * @param range the new range
	 */
	public void setRange(Range<Double> range) {
		this.range = range;
	}
	
	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(category, range);
	}
	
	/**
	 * Equals.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OquareScaleValueByRange other = (OquareScaleValueByRange) obj;
		return category == other.category && Objects.equals(range, other.range);
	}
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OquareScaleValue [Category=");
		builder.append(category);
		builder.append(", range=");
		builder.append(range);
		builder.append("]");
		return builder.toString();
	}
	
}
