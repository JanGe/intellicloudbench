package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.metrics.IInstanceOrderer;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;

public class UnorderedParallelStopper extends StoppingMethod {

	/* By which factor the stopping threshold is shifted */
	private Integer shift;

	private Long startTime = null;
	private Long endTime = null;

	private Map<Benchmark, Collection<Runner>> runnersDoneForBenchmark = new HashMap<>();

	private Map<Benchmark, List<Runner>> rankingAfterBenchmark = new HashMap<>();

	private List<Runner> runnerStopped = new LinkedList<>();

	private List<Runner> lastRanking = new LinkedList<>();

	public UnorderedParallelStopper(IService service, Class<? extends Runner> runnerType,
	    List<InstanceType> instanceTypes, List<Benchmark> benchmarks, Integer param) {
		super(service, runnerType, instanceTypes, benchmarks, param);
		this.shift = param;
	}

	@Override
	public List<Benchmark> orderBenchmarks(List<Benchmark> benchmarks) {
		Collections.shuffle(benchmarks);
		logLine("Randomized benchmarks:");
		for (Benchmark benchmark : benchmarks) {
			logLine(benchmark.getName() + ", " + getMetricsConfiguration().getWeight(benchmark));
		}
		return benchmarks;
	}

	@Override
	public List<InstanceType> orderInstanceTypes(List<InstanceType> instanceTypes) {
		Collections.shuffle(instanceTypes);
		logLine("Randomized instances:");
		for (InstanceType instanceType : instanceTypes) {
			logLine(instanceType.asString(" | "));
		}
		return instanceTypes;
	}

	@Override
	public void start() {
		startTime = System.currentTimeMillis();
		for (Runner runner : getRunners()) {
			logLine("Starting instance " + runner.getInstanceType().asString(" | "));
			startRunner(runner);
		}
	}

	@Override
	protected void notifyDone(Runner runner) {
		if (getStillRunning().isEmpty()) {
			endTime = System.currentTimeMillis();
			logLine("Run time [s]:" + getRunTime().getStandardSeconds());
			logLine("Summarized est. costs: $ " + getSummarizedCosts());
		}
	}

	@Override
	public void timedNotify(Runner runner) {
	}

	@Override
	public synchronized void notifyBenchmarkDone(Runner runner, Benchmark benchmark) {
		synchronized (runnersDoneForBenchmark) {
			Collection<Runner> runnersDone = runnersDoneForBenchmark.get(benchmark);
			if (runnersDone == null) {
				runnersDone = new LinkedList<>();
				runnersDoneForBenchmark.put(benchmark, runnersDone);
			}
			runnersDone.add(runner);

			if (checkAllDoneForBenchmark(benchmark)) {
				logLine("All finished " + benchmark.getId());
				lastRanking = getRanking(benchmark);
				rankingAfterBenchmark.put(benchmark, lastRanking);
				List<Runner> shouldStop = new LinkedList<>(getStillInRace());
				shouldStop.removeAll(lastRanking);

				for (Runner toStop : shouldStop) {
					logLine("Stopping instance " + toStop.getInstanceType().asString(" | "));
					stopRunner(toStop);
					runnerStopped.add(toStop);
				}
			}
		}
	}

	private List<Runner> getRanking(Benchmark lastBenchmark) {
		Class<? extends IInstanceOrderer> instanceOrderer = getMetricsConfiguration().getSelectedInstanceOrderer();

		Double sumDoneWeights = 0d;
		Double sumRemainingWeights = 0d;

		SortedSetMultimap<Double, InstanceType> resultForType;
		List<Runner> ranking = new LinkedList<>(getStillInRace());
		List<InstanceType> instanceTypes = new LinkedList<>();
		List<IMetricsType> doneMetricsTypes = new LinkedList<>();

		List<Benchmark> benchmarks = new ArrayList<>(getOrderedBenchmarks());
		int indexLast = benchmarks.indexOf(lastBenchmark);

		for (int i = 0; i <= indexLast; i++) {
			doneMetricsTypes.add(benchmarks.get(i));
		}
		if (getMetricsConfiguration().getWeight(CostsStore.getInstance()) != null) {
			doneMetricsTypes.add(CostsStore.getInstance());
		}

		benchmarks.removeAll(doneMetricsTypes);
		for (Benchmark remainingBenchmark : benchmarks) {
			sumRemainingWeights += getMetricsConfiguration().getWeight(remainingBenchmark);
		}

		Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForDoneMetricsTypesForType = new HashMap<>();
		Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType =
		    getResultsForAllMetricsTypesForType();

		synchronized (resultsForAllMetricsTypesForType) {

			for (Runner runner : ranking) {
				instanceTypes.add(runner.getInstanceType());

				Multimap<IMetricsType, IMetricsResult> resultsForDoneMetrics = LinkedListMultimap.create();
				Multimap<IMetricsType, IMetricsResult> resultsForAllMetrics =
				    resultsForAllMetricsTypesForType.get(runner.getInstanceType());

				if (resultsForAllMetrics != null) {

					for (IMetricsType metricsType : doneMetricsTypes) {
						resultsForDoneMetrics.putAll(metricsType, resultsForAllMetrics.get(metricsType));
					}

					resultsForDoneMetricsTypesForType.put(runner.getInstanceType(), resultsForDoneMetrics);
				}
			}

		}

		for (IMetricsType metricsType : doneMetricsTypes) {

			sumDoneWeights += getMetricsConfiguration().getWeight(metricsType);

			try {
				resultForType =
				    MetricsConfiguration.getInstancesOrderedForMetricsType(
				        metricsType,
				        instanceOrderer,
				        instanceTypes,
				        getMetricsConfiguration().getSelectedReference(),
				        resultsForDoneMetricsTypesForType);

				getMetricsConfiguration().putRelativeResults(metricsType, resultForType);

			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		Collections.sort(ranking, Collections.reverseOrder(new Comparator<Runner>() {

			@Override
			public int compare(Runner o1, Runner o2) {
				return getMetricsConfiguration().getWeightedRelative(o1.getInstanceType()).compareTo(
				    getMetricsConfiguration().getWeightedRelative(o2.getInstanceType()));
			}

		}));

		Double threshold =
		    (1d - (sumRemainingWeights / (sumDoneWeights + sumRemainingWeights))) - (new Double(shift) / 100d);
		logLine("Threshold: " + threshold);
		for (InstanceType instanceType : instanceTypes) {
			if (getMetricsConfiguration().getWeightedRelative(instanceType) < threshold) {
				logLine(instanceType.asString(" | ") + " has results smaller than threshold ("
				    + getMetricsConfiguration().getWeightedRelative(instanceType) + ")");
				ranking.remove(getRunner(instanceType));
			}
		}

		return ranking;
	}

	private boolean checkAllDoneForBenchmark(Benchmark benchmark) {
		List<Benchmark> orderedBenchmarks = getOrderedBenchmarks();
		if (benchmark.equals(orderedBenchmarks.get(orderedBenchmarks.size() - 1)))
			return false;
		synchronized (runnersDoneForBenchmark) {
			return getStillInRace().size() <= runnersDoneForBenchmark.get(benchmark).size();
		}
	}

	@Override
	protected void notifyAborted(Runner runner) {
		synchronized (runnersDoneForBenchmark) {
			runnersDoneForBenchmark.remove(runner);
		}
	}

	public int getNumberOfInstancesAfter(Benchmark benchmark) {
		List<Runner> ranking = rankingAfterBenchmark.get(benchmark);
		if (ranking == null)
			return 0;
		return ranking.size();
	}

	public Integer getRankForInstanceTypeAfter(InstanceType instanceType, Benchmark benchmark) {
		List<Runner> ranking = rankingAfterBenchmark.get(benchmark);
		if (ranking == null)
			return null;
		Integer rank = ranking.indexOf(getRunner(instanceType)) + 1;
		return rank;
	}

	public Integer getRankForInstanceType(InstanceType instanceType) {
		List<Runner> ranking = runnerStopped;
		ranking.removeAll(lastRanking);
		for (int i = lastRanking.size() - 1; i >= 0; i--) {
			ranking.add(lastRanking.get(i));
		}
		Collections.reverse(ranking);

		Integer rank = ranking.indexOf(getRunner(instanceType)) + 1;
		return rank;
	}

	public Duration getRunTime() {
		if (startTime == null)
			return new Duration(0);
		if (endTime == null)
			return new Duration(startTime, System.currentTimeMillis() + 1);
		return new Duration(startTime, endTime);
	}

	public String getName() {
		return "par-unordered-" + new Double(getParam()) / 100d;
	}

}
