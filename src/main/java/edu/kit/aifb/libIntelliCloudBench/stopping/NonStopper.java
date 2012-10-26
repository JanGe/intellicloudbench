package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.util.LinkedList;
import java.util.List;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class NonStopper extends StoppingMethod {

	public NonStopper(IService service, Class<? extends Runner> runnerType,
      List<InstanceType> instanceTypes, List<Benchmark> benchmarks, Integer param) {
	  super(service, runnerType, instanceTypes, benchmarks, param);
  }

	@Override
  public List<Benchmark> orderBenchmarks(List<Benchmark> benchmarks) {
	  return new LinkedList<>(benchmarks);
  }

	@Override
  public List<InstanceType> orderInstanceTypes(List<InstanceType> instanceTypes) {
		return new LinkedList<>(instanceTypes);
  }

	@Override
  public void start() {
	  for (Runner runner : getRunners()) {
			logLine("Starting instance " + runner.getInstanceType().asString(" | "));
	  	startRunner(runner);
	  }
  }

	@Override
  public void notifyDone(Runner runner) {
  }

	@Override
  public void timedNotify(Runner runner) {
  }

	@Override
  public void notifyBenchmarkDone(Runner runner, Benchmark benchmark) {
  }

	@Override
  protected void notifyAborted(Runner runner) {
  }

}
