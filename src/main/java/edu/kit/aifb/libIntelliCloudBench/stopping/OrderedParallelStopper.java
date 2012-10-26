package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class OrderedParallelStopper extends UnorderedParallelStopper {

	public OrderedParallelStopper(IService service, Class<? extends Runner> runnerType,
      List<InstanceType> instanceTypes, List<Benchmark> benchmarks, Integer param) {
	  super(service, runnerType, instanceTypes, benchmarks, param);
  }

	@Override
	public List<Benchmark> orderBenchmarks(List<Benchmark> benchmarks) {
		Collections.sort(benchmarks, Collections.reverseOrder(new Comparator<Benchmark>() {

			@Override
      public int compare(Benchmark o1, Benchmark o2) {
	      return getMetricsConfiguration().getWeight(o1).compareTo(getMetricsConfiguration().getWeight(o2));
      }
			
		}));
		
		logLine("Sorted benchmarks:");
		for (Benchmark benchmark : benchmarks) {
			logLine(benchmark.getName() + ", " + getMetricsConfiguration().getWeight(benchmark));
		}
		return benchmarks;
	}
	
	@Override
	public List<InstanceType> orderInstanceTypes(List<InstanceType> instanceTypes) {
		Collections.sort(instanceTypes, Collections.reverseOrder(new Comparator<InstanceType>() {

			@Override
      public int compare(InstanceType o1, InstanceType o2) {
	      return o1.getHardwareType().getSummarizedCpuSpeed().compareTo(o2.getHardwareType().getSummarizedCpuSpeed());
      }
			
		}));

		logLine("Ordered instances:");
		for(InstanceType instanceType : instanceTypes) {
			logLine(instanceType.asString(" | "));
		}
		return instanceTypes;
	}
	
	public String getName() {
		return "par-" + new Double(getParam()) / 100d;
	}

}
