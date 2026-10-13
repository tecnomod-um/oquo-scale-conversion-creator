package es.um.dis.formatOquo.pojo;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;


// TODO: Auto-generated Javadoc
/**
 * The Class CardinalToOquareFiveCategoriesScaleConversion.
 */
public class CardinalToOrdinalScaleConversion implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4036342114643478191L;

	/** The input scale IRI. */
	private String inputScaleIRI;

	/** The output scale IRI. */
	private String outputScaleIRI;


	/** The scale values. */
	private List<OquareScaleValueByRange> scaleValues;

	public CardinalToOrdinalScaleConversion() {
		super();
	}

	/**
	 * Instantiates a new cardinal to ordinal scale conversion.
	 *
	 * @param inputScaleIRI the input scale IRI
	 * @param outputScaleIRI the output scale IRI
	 * @param scaleValues the scale values
	 */
	public CardinalToOrdinalScaleConversion(String inputScaleIRI, String outputScaleIRI,
			List<OquareScaleValueByRange> scaleValues) {
		super();
		this.inputScaleIRI = inputScaleIRI;
		this.outputScaleIRI = outputScaleIRI;
		this.scaleValues = scaleValues;
	}

	/**
	 * Gets the input scale IRI.
	 *
	 * @return the input scale IRI
	 */
	public String getInputScaleIRI() {
		return inputScaleIRI;
	}

	/**
	 * Sets the input scale IRI.
	 *
	 * @param inputScaleIRI the new input scale IRI
	 */
	public void setInputScaleIRI(String inputScaleIRI) {
		this.inputScaleIRI = inputScaleIRI;
	}

	/**
	 * Gets the output scale IRI.
	 *
	 * @return the output scale IRI
	 */
	public String getOutputScaleIRI() {
		return outputScaleIRI;
	}


	/**
	 * Sets the output scale IRI.
	 *
	 * @param outputScaleIRI the new output scale IRI
	 */
	public void setOutputScaleIRI(String outputScaleIRI) {
		this.outputScaleIRI = outputScaleIRI;
	}

	/**
	 * Gets the scale values.
	 *
	 * @return the scale values
	 */
	public List<OquareScaleValueByRange> getScaleValues() {
		return scaleValues;
	}

	/**
	 * Sets the scale values.
	 *
	 * @param scaleValues the new scale values
	 */
	public void setScaleValues(List<OquareScaleValueByRange> scaleValues) {
		this.scaleValues = scaleValues;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(inputScaleIRI, outputScaleIRI, scaleValues);
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
		CardinalToOrdinalScaleConversion other = (CardinalToOrdinalScaleConversion) obj;
		return Objects.equals(inputScaleIRI, other.inputScaleIRI)
				&& Objects.equals(outputScaleIRI, other.outputScaleIRI)
				&& Objects.equals(scaleValues, other.scaleValues);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CardinalToOrdinalScaleConversion [inputScaleIRI=");
		builder.append(inputScaleIRI);
		builder.append(", outputScaleIRI=");
		builder.append(outputScaleIRI);
		builder.append(", scaleValues=");
		builder.append(scaleValues);
		builder.append("]");
		return builder.toString();
	}

}
