package edu.kit.aifb.libIntelliCloudBench.metrics;

import java.util.Map;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult.Proportion;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class RelativeInstanceOrderer implements IInstanceOrderer {

	@Override
	public SortedSetMultimap<Double, InstanceType> orderInstances(Map<InstanceType, IMetricsResult> resultsForType,
	    InstanceType referenceInstance) {
		TreeMultimap<Double, InstanceType> orderedInstances =
		    TreeMultimap.create(Ordering.natural(), Ordering.usingToString());

		/* Get if lower or higher is better */
		Proportion proportion = resultsForType.get(resultsForType.keySet().iterator().next()).getProportion();

		/* Find highest and lowest result */
		Double lowest = Double.MAX_VALUE;
		Double highest = Double.MIN_VALUE;
		for (InstanceType instanceType : resultsForType.keySet()) {
			Double curResult = resultsForType.get(instanceType).getValue();
			lowest = Math.min(lowest, curResult);
			highest = Math.max(highest, curResult);
		}

		Double relativeResult;

		for (InstanceType instanceType : resultsForType.keySet()) {
			if (highest > lowest) {
				Double curResult = resultsForType.get(instanceType).getValue();
				relativeResult = (curResult - lowest) / (highest - lowest);
			} else {
				relativeResult = 1d;
			}

			if (proportion == Proportion.LIB)
				relativeResult = 1d - relativeResult;

			orderedInstances.put(relativeResult, instanceType);
		}

		return (SortedSetMultimap<Double, InstanceType>) orderedInstances;
	}

}
