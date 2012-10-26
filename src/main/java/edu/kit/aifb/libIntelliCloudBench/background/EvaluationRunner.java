package edu.kit.aifb.libIntelliCloudBench.background;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jclouds.compute.RunNodesException;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;
import edu.kit.aifb.libIntelliCloudBench.stopping.EvaluationStopper;
import edu.kit.aifb.libIntelliCloudBench.stopping.StoppingMethod;

public class EvaluationRunner extends Runner {

	private EvaluationStopper evalStopper;

	public EvaluationRunner(IService service, StoppingMethod stopper, InstanceType instanceType,
	    List<Benchmark> benchmarks) {
		super(service, stopper, instanceType, benchmarks);

		this.evalStopper = (EvaluationStopper) service;
	}

	@Override
	void prepare() {
	}

	@Override
	void create() throws RunNodesException {
	}

	@Override
	void prepareDeployment() {
	}

	@Override
	void deploy() throws RunScriptOnMachineException {
	}

	@Override
	void install(Benchmark benchmark) throws RunScriptOnMachineException {
	}

	@Override
	void runBenchmark(Benchmark benchmark) throws RunScriptOnMachineException {
	}

	@Override
	LinkedListMultimap<Benchmark, Result> upload(Benchmark benchmark) throws ParseXmlResultException,
	    RunScriptOnMachineException {
		Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType =
		    evalStopper.getResultsForAllMetricsTypesForType();
		Multimap<IMetricsType, IMetricsResult> resultsForAllMetricsTypes =
		    resultsForAllMetricsTypesForType.get(getInstanceType());
		
		if (resultsForAllMetricsTypes == null) {
			resultsForAllMetricsTypes = LinkedListMultimap.create();
			resultsForAllMetricsTypesForType.put(getInstanceType(), resultsForAllMetricsTypes);
		}

		LinkedListMultimap<Benchmark, Result> resultsForBenchmarkForType = LinkedListMultimap.create();
		Collection<IMetricsResult> results = resultsForAllMetricsTypes.get(benchmark);

		if (results.isEmpty()) {
			try {
				Object barrier = evalStopper.getBarrier(this, benchmark);
				synchronized (barrier) {
					barrier.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			results = resultsForAllMetricsTypes.get(benchmark);
		}

		for (IMetricsResult result : results) {
			resultsForBenchmarkForType.put(benchmark, (Result) result);
		}

		return resultsForBenchmarkForType;
	}

	@Override
	void cleanUp() {
	}

	@Override
	void terminate() {
	}

}
