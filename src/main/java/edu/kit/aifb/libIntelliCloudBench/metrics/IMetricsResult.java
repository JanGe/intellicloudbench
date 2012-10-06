package edu.kit.aifb.libIntelliCloudBench.metrics;

public interface IMetricsResult {

	public enum Proportion {
		HIB, LIB
	}

	public String getValueAsString();

	public String getScale();

	public Double getValue();

	public Proportion getProportion();

}