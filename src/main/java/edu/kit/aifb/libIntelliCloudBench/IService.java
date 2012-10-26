package edu.kit.aifb.libIntelliCloudBench;

import org.jclouds.compute.ComputeService;

import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;

public interface IService {
	public String getName();
	
	public void notifyFinished(Runner runner);

	public ComputeService getComputeService(Provider provider);

	public MetricsConfiguration getMetricsConfiguration();
}
