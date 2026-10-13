package es.um.dis.formatOquo.pojo;

import es.um.dis.formatOquo.utils.OWLUtils;

/**
 * The Enum OquareCategory.
 */
public enum OquareCategory {

	/** The category1. */
	CATEGORY1(OWLUtils.OQUO_NS + "OquareCategory1", "Not acceptable", 1),

	/** The category2. */
	CATEGORY2(OWLUtils.OQUO_NS + "OquareCategory2", "Insufficient", 2),

	/** The category3. */
	CATEGORY3(OWLUtils.OQUO_NS + "OquareCategory3", "Minimally acceptable", 3),

	/** The category4. */
	CATEGORY4(OWLUtils.OQUO_NS + "OquareCategory4", "Acceptable", 4),

	/** The category5. */
	CATEGORY5(OWLUtils.OQUO_NS + "OquareCategory5", "Exceeds the requirements", 5);

	/** The iri. */
	private String iri;

	/** The description. */
	private String description;

	/** The value. */
	private int value;

	/**
	 * Instantiates a new oquare category.
	 *
	 * @param iri the iri
	 * @param description the description
	 * @param value the position
	 */
	private OquareCategory(String iri, String description, int value) {
		this.description = description;
		this.iri = iri;
		this.value = value;
	}

	/**
	 * Gets the iri.
	 *
	 * @return the iri
	 */
	public String getIri() {
		return iri;
	}

	/**
	 * Sets the iri.
	 *
	 * @param iri the new iri
	 */
	public void setIri(String iri) {
		this.iri = iri;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the position.
	 *
	 * @return the position
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Sets the position.
	 *
	 * @param value the new position
	 */
	public void setValue(int value) {
		this.value = value;
	}

	public static OquareCategory getByIRI (String iri) {
		if(OquareCategory.CATEGORY1.getIri().equals(iri)) {
			return OquareCategory.CATEGORY1;
		}
		if(OquareCategory.CATEGORY2.getIri().equals(iri)) {
			return OquareCategory.CATEGORY2;
		}
		if(OquareCategory.CATEGORY3.getIri().equals(iri)) {
			return OquareCategory.CATEGORY3;
		}
		if(OquareCategory.CATEGORY4.getIri().equals(iri)) {
			return OquareCategory.CATEGORY4;
		}
		if(OquareCategory.CATEGORY5.getIri().equals(iri)) {
			return OquareCategory.CATEGORY5;
		}
		return null;
	}
}
