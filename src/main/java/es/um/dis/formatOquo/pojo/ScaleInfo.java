package es.um.dis.formatOquo.pojo;

import java.io.Serializable;
import java.util.Objects;

// TODO: Auto-generated Javadoc
/**
 * The Class ScaleInfo.
 */
public class ScaleInfo implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7183248276980966275L;
	
	/** The metric IRI. */
	private String metricIRI;
	
	/** The scale IRI. */
	private String scaleIRI;
	
	/** The scale label. */
	private String scaleLabel;
	
	/** The scale type. */
	private String scaleType;
	
	/** The scale definition. */
	private String scaleDefinition;
	
	/** The ranking function IRI. */
	private String rankingFunctionIRI;
	
	
	/**
	 * Instantiates a new scale info.
	 *
	 * @param metricIRI the metric IRI
	 * @param scaleIRI the scale IRI
	 * @param scaleLabel the scale label
	 * @param scaleType the scale type
	 * @param scaleDefinition the scale definition
	 * @param rankingFunctionIRI the ranking function IRI
	 */
	public ScaleInfo(String metricIRI, String scaleIRI, String scaleLabel, String scaleType, String scaleDefinition,
			String rankingFunctionIRI) {
		super();
		this.metricIRI = metricIRI;
		this.scaleIRI = scaleIRI;
		this.scaleLabel = scaleLabel;
		this.scaleType = scaleType;
		this.scaleDefinition = scaleDefinition;
		this.rankingFunctionIRI = rankingFunctionIRI;
	}

	/**
	 * Gets the metric IRI.
	 *
	 * @return the metric IRI
	 */
	public String getMetricIRI() {
		return metricIRI;
	}
	
	/**
	 * Sets the metric IRI.
	 *
	 * @param metricIRI the new metric IRI
	 */
	public void setMetricIRI(String metricIRI) {
		this.metricIRI = metricIRI;
	}
	
	/**
	 * Gets the scale IRI.
	 *
	 * @return the scale IRI
	 */
	public String getScaleIRI() {
		return scaleIRI;
	}
	
	/**
	 * Sets the scale IRI.
	 *
	 * @param scaleIRI the new scale IRI
	 */
	public void setScaleIRI(String scaleIRI) {
		this.scaleIRI = scaleIRI;
	}
	
	/**
	 * Gets the scale label.
	 *
	 * @return the scale label
	 */
	public String getScaleLabel() {
		return scaleLabel;
	}
	
	/**
	 * Sets the scale label.
	 *
	 * @param scaleLabel the new scale label
	 */
	public void setScaleLabel(String scaleLabel) {
		this.scaleLabel = scaleLabel;
	}
	
	/**
	 * Gets the scale type.
	 *
	 * @return the scale type
	 */
	public String getScaleType() {
		return scaleType;
	}
	
	/**
	 * Sets the scale type.
	 *
	 * @param scaleType the new scale type
	 */
	public void setScaleType(String scaleType) {
		this.scaleType = scaleType;
	}
	
	/**
	 * Gets the scale definition.
	 *
	 * @return the scale definition
	 */
	public String getScaleDefinition() {
		return scaleDefinition;
	}
	
	/**
	 * Sets the scale definition.
	 *
	 * @param scaleDefinition the new scale definition
	 */
	public void setScaleDefinition(String scaleDefinition) {
		this.scaleDefinition = scaleDefinition;
	}

	/**
	 * Gets the ranking function IRI.
	 *
	 * @return the ranking function IRI
	 */
	public String getRankingFunctionIRI() {
		return rankingFunctionIRI;
	}

	/**
	 * Sets the ranking function IRI.
	 *
	 * @param rankingFunctionIRI the new ranking function IRI
	 */
	public void setRankingFunctionIRI(String rankingFunctionIRI) {
		this.rankingFunctionIRI = rankingFunctionIRI;
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return Objects.hash(metricIRI, rankingFunctionIRI, scaleDefinition, scaleIRI, scaleLabel, scaleType);
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
		ScaleInfo other = (ScaleInfo) obj;
		return Objects.equals(metricIRI, other.metricIRI)
				&& Objects.equals(rankingFunctionIRI, other.rankingFunctionIRI)
				&& Objects.equals(scaleDefinition, other.scaleDefinition) && Objects.equals(scaleIRI, other.scaleIRI)
				&& Objects.equals(scaleLabel, other.scaleLabel) && Objects.equals(scaleType, other.scaleType);
	}

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScaleInfo [metricIRI=");
		builder.append(metricIRI);
		builder.append(", scaleIRI=");
		builder.append(scaleIRI);
		builder.append(", scaleLabel=");
		builder.append(scaleLabel);
		builder.append(", scaleType=");
		builder.append(scaleType);
		builder.append(", scaleDefinition=");
		builder.append(scaleDefinition);
		builder.append(", rankingFunctionIRI=");
		builder.append(rankingFunctionIRI);
		builder.append("]");
		return builder.toString();
	}
	

	
	
}
