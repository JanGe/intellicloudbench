package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;
import java.util.Observable;
import java.util.Set;

import org.jclouds.domain.Location;

import edu.kit.aifb.IntelliCloudBench.util.StringUtils;

public class Region extends Observable implements Serializable, Comparable<Region> {
	private static final long serialVersionUID = 7566290420436619833L;

	private String id;
	private Set<String> langCode;
	private String scope = "";

	public Region(Location location) {
		this.id = location.getId();
		this.langCode = location.getIso3166Codes();
		if (location.getScope() != null)
			this.scope = location.getScope().toString();
	}

	public String getId() {
		return id;
	}

	public Set<String> getLangCodes() {
		return langCode;
	}
	
	public String getLangCodesAsString() {
		return StringUtils.concatStringsWSep(langCode, ", ");
	}

	public String getScope() {
		return scope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Region other = (Region) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int compareTo(Region anotherRegion) {
		return this.id.compareTo(anotherRegion.getId());
	}
}
