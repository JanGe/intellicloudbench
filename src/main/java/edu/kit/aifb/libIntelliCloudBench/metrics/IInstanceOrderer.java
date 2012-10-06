package edu.kit.aifb.libIntelliCloudBench.metrics;

import java.util.Map;

import com.google.common.collect.SortedSetMultimap;

import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public interface IInstanceOrderer {
	public static final boolean REQUIRES_REFERENCE = false;

	public SortedSetMultimap<Double, InstanceType> orderInstances(Map<InstanceType, IMetricsResult> resultsForType,
	    InstanceType referenceInstance);
}
