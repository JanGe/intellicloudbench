package edu.kit.aifb.libIntelliCloudBench.model;


public interface ICredentialsChangedListener {
	public void notifyCredentialsChanged(Provider provider, Credentials credentials);
}
