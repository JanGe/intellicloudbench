package edu.kit.aifb.libIntelliCloudBench.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.kit.aifb.libIntelliCloudBench.metrics.CostsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;

public class MetricsHelper {
	public static Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> combineBenchmarksAndCosts(boolean costsChecked,
      Map<InstanceType, Multimap<Benchmark, Result>> resultsForAllBenchmarksForType) {
	  Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType = new HashMap<>();
		for (InstanceType instanceType : resultsForAllBenchmarksForType.keySet()) {
			Multimap<IMetricsType, IMetricsResult> resultsForAllMetricsTypes = LinkedListMultimap.create();
			for (Benchmark benchmark : resultsForAllBenchmarksForType.get(instanceType).keySet()) {
				for (Result result : resultsForAllBenchmarksForType.get(instanceType).get(benchmark)) {
					resultsForAllMetricsTypes.put(benchmark, result);
				}
			}
			if (costsChecked) {
				/* TODO: Remove new instance creation */
				CostsResult costsResult = new CostsResult(CostsStore.getInstance().getCostsForMonthsRunning(instanceType, 1));
				resultsForAllMetricsTypes.put(CostsStore.getInstance(), costsResult);
			}

			resultsForAllMetricsTypesForType.put(instanceType, resultsForAllMetricsTypes);
		}
	  return resultsForAllMetricsTypesForType;
  }
}
