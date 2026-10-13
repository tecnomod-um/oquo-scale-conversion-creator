package es.um.dis.formatOquo.utils;

import java.util.Objects;

public class ScaleCreationConfiguration {
	private boolean overwriteScaleConversion;
	private boolean createMetricIfNotExist;
	private boolean createInputRawScaleIfNotExist;

	public ScaleCreationConfiguration(boolean overwriteScaleConversion, boolean createMetricIfNotExist,
			boolean createInputRawScaleIfNotExist) {
		super();
		this.overwriteScaleConversion = overwriteScaleConversion;
		this.createMetricIfNotExist = createMetricIfNotExist;
		this.createInputRawScaleIfNotExist = createInputRawScaleIfNotExist;
	}

	public boolean isOverwriteScaleConversion() {
		return overwriteScaleConversion;
	}

	public void setOverwriteScaleConversion(boolean overwriteScaleConversion) {
		this.overwriteScaleConversion = overwriteScaleConversion;
	}

	public boolean isCreateMetricIfNotExist() {
		return createMetricIfNotExist;
	}

	public void setCreateMetricIfNotExist(boolean createMetricIfNotExist) {
		this.createMetricIfNotExist = createMetricIfNotExist;
	}

	public boolean isCreateInputRawScaleIfNotExist() {
		return createInputRawScaleIfNotExist;
	}

	public void setCreateInputRawScaleIfNotExist(boolean createInputRawScaleIfNotExist) {
		this.createInputRawScaleIfNotExist = createInputRawScaleIfNotExist;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ScaleCreationConfiguration [overwriteScale=");
		builder.append(overwriteScaleConversion);
		builder.append(", createMetricIfNotExist=");
		builder.append(createMetricIfNotExist);
		builder.append(", createInputRawScaleIfNotExist=");
		builder.append(createInputRawScaleIfNotExist);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(createInputRawScaleIfNotExist, createMetricIfNotExist, overwriteScaleConversion);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScaleCreationConfiguration other = (ScaleCreationConfiguration) obj;
		return createInputRawScaleIfNotExist == other.createInputRawScaleIfNotExist
				&& createMetricIfNotExist == other.createMetricIfNotExist
				&& overwriteScaleConversion == other.overwriteScaleConversion;
	}

}
