package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

public class Credentials implements Serializable {
	private static final long serialVersionUID = 914070703917165745L;

	private String key = "";
	private String secret = "";

	public Credentials() {
		this("", "");
	}

	public Credentials(String key, String secret) {
		this.key = key;
		this.secret = secret;
	}

	public Credentials(Credentials anotherCredentials) {
		this(anotherCredentials.key, anotherCredentials.secret);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((secret == null) ? 0 : secret.hashCode());
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
		Credentials other = (Credentials) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (secret == null) {
			if (other.secret != null)
				return false;
		} else if (!secret.equals(other.secret))
			return false;
		return true;
	}

}