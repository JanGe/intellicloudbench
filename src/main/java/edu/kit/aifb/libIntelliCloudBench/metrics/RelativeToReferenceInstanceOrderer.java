package edu.kit.aifb.libIntelliCloudBench.metrics;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult.Proportion;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class RelativeToReferenceInstanceOrderer implements IInstanceOrderer {
	public static final boolean REQUIRES_REFERENCE = true;

	@Override
	public SortedSetMultimap<Double, InstanceType> orderInstances(Map<InstanceType, IMetricsResult> resultsForType,
	    InstanceType referenceInstance) {
		TreeMultimap<Double, InstanceType> orderedInstances =
		    TreeMultimap.create(Ordering.natural(), Ordering.usingToString());
		Preconditions.checkNotNull(referenceInstance, "No reference instance set.");

		/* Get if lower or higher is better */
		Proportion proportion = resultsForType.get(resultsForType.keySet().iterator().next()).getProportion();

		/* Find highest and lowest result */
		Double reference = Double.MAX_VALUE;
		for (InstanceType instanceType : resultsForType.keySet()) {
			if (referenceInstance == instanceType) {
				Double curResult = resultsForType.get(instanceType).getValue();
				reference = curResult;
			}
		}

		Double relativeResult;

		for (InstanceType instanceType : resultsForType.keySet()) {
			Double curResult = resultsForType.get(instanceType).getValue();
			relativeResult = curResult / reference;

			if (proportion == Proportion.LIB)
				relativeResult = 1d / relativeResult;

			orderedInstances.put(relativeResult, instanceType);
		}

		return (SortedSetMultimap<Double, InstanceType>) orderedInstances;
	}

}