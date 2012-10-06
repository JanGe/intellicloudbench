package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

public class InstanceType implements Serializable {
	private static final long serialVersionUID = -9139382679909105340L;

	private Provider provider;
	private Region region;
	private HardwareType hardwareType;

	public InstanceType(Provider provider, Region region, HardwareType hardwareType) {
		this.provider = provider;
		this.region = region;
		this.hardwareType = hardwareType;
	}

	public Provider getProvider() {
		return provider;
	}

	public Region getRegion() {
		return region;
	}

	public HardwareType getHardwareType() {
		return hardwareType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hardwareType == null) ? 0 : hardwareType.hashCode());
		result = prime * result + ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InstanceType other = (InstanceType) obj;
		if (hardwareType == null) {
			if (other.hardwareType != null)
				return false;
		} else if (!hardwareType.equals(other.hardwareType))
			return false;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		return true;
	}

	public String asString(String delimiter) {
		return getProvider().getId() + delimiter + getRegion().getId() + delimiter + getHardwareType().getId();
	}
}
