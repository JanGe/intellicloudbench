package edu.kit.aifb.libIntelliCloudBench.metrics;

import java.util.Map;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult.Proportion;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class RelativeToAboluteZeroInstanceOrderer implements IInstanceOrderer {

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
			highest = Math.max(highest, curResult);
			lowest = Math.min(lowest, curResult);
		}

		Double relativeResult;

		for (InstanceType instanceType : resultsForType.keySet()) {
			if (proportion == Proportion.HIB) {

				if (highest > lowest) {
					Double curResult = resultsForType.get(instanceType).getValue();
					relativeResult = curResult / highest;
				} else {
					relativeResult = 1d;
				}

			} else {
				
				if (highest > lowest) {
					Double curResult = resultsForType.get(instanceType).getValue();
					relativeResult = (lowest - (curResult - lowest)) / lowest;
				} else {
					relativeResult = 1d;
				}
				
			}

			orderedInstances.put(relativeResult, instanceType);
		}

		return (SortedSetMultimap<Double, InstanceType>) orderedInstances;
	}
	
}
