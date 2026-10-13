package es.um.dis.formatOquo.pojo;

import java.io.Serializable;
import java.util.Objects;


/**
 * The Class OquareScaleValue.
 */
public class OquareScaleValueByOrdinalItem implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3162950167312735388L;

	/** The Category. */
	private OquareCategory category;


	private String ordinalItem;


	public OquareScaleValueByOrdinalItem(OquareCategory category, String ordinalItem) {
		super();
		this.category = category;
		this.ordinalItem = ordinalItem;
	}


	public OquareCategory getCategory() {
		return category;
	}


	public void setCategory(OquareCategory category) {
		this.category = category;
	}


	public String getOrdinalItem() {
		return ordinalItem;
	}


	public void setOrdinalItem(String ordinalItem) {
		this.ordinalItem = ordinalItem;
	}


	@Override
	public int hashCode() {
		return Objects.hash(category, ordinalItem);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OquareScaleValueByOrdinalItem other = (OquareScaleValueByOrdinalItem) obj;
		return category == other.category && Objects.equals(ordinalItem, other.ordinalItem);
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OquareScaleValueByOrdinalItem [category=");
		builder.append(category);
		builder.append(", ordinalItem=");
		builder.append(ordinalItem);
		builder.append("]");
		return builder.toString();
	}







}
