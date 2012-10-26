package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;

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

public class UnorderedSequentialStopper extends StoppingMethod {

	private int indexNextInstance = 0;
	private int lookbackThreshold = 4;
	private int counterMarked = 0;

	private ArrayList<Runner> currentRanking = new ArrayList<>();
	private Double currentAvgMean = 1d;
	private Double newAvgMean = 1d;
	private List<Runner> abortedRunners = new LinkedList<>();

	public UnorderedSequentialStopper(IService service, Class<? extends Runner> runnerType,
	    List<InstanceType> instanceTypes, List<Benchmark> benchmarks, Integer param) {
		super(service, runnerType, instanceTypes, benchmarks, param);
		if (param != null)
			this.lookbackThreshold = param;
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
		startNext();
	}

	private void startNext() {
		if (indexNextInstance < getRunners().size()) {
			Runner runner = getRunners().get(indexNextInstance);
			logLine("Starting instance " + runner.getInstanceType().asString(" | "));
			startRunner(runner);
			indexNextInstance++;
		} else {
			logLine("Run time [s]:" + getRunTime().getStandardSeconds());
			logLine("Summarized est. costs: $ " + getSummarizedCosts());
		}
	}

	@Override
	public void notifyDone(Runner runner) {
		if (checkNextToStart(runner)) {
			startNext();
		} else {
			for (int i = indexNextInstance; i < getRunners().size(); i++) {
				Runner nextRunner = getRunners().get(i);
				if (!abortedRunners.contains(nextRunner)) {
					logLine("Stopping instance " + nextRunner.getInstanceType().asString(" | "));
					stopRunner(nextRunner);
					indexNextInstance++;
				}
			}
			logLine("Run time [s]:" + getRunTime().getStandardSeconds());
			logLine("Summarized est. costs: $ " + getSummarizedCosts());
		}
	}

	@Override
	public void timedNotify(Runner runner) {
	}

	private boolean checkNextToStart(Runner runner) {
		Boolean cont = true;
		ArrayList<Runner> newRanking = getNewRanking(runner);
		if ((!currentRanking.isEmpty()) && (currentRanking.get(0) == newRanking.get(0))) {
			if (currentRanking.get(currentRanking.size() - 1) == newRanking.get(newRanking.size() - 1)) {

				if (newAvgMean < currentAvgMean) {
					logLine("Mean did decrease -> mark");
					counterMarked++;
				} else {
					logLine("Mean did increase -> continue");
					counterMarked = 0;
					cont = true;
				}

			} else {
				logLine("New last place -> mark");
				counterMarked++;
			}

			if (counterMarked >= lookbackThreshold) {
				logLine(counterMarked + " last were marked -> stop all");
				cont = false;
			} else {
				logLine(counterMarked + " last were marked -> continue");
				cont = true;
			}
		} else {
			logLine("New leader -> continue");
			counterMarked = 0;
			cont = true;
		}

		currentAvgMean = newAvgMean;
		currentRanking = newRanking;

		logLine("Marked: " + counterMarked);

		return cont;
	}

	private ArrayList<Runner> getNewRanking(Runner runner) {
		ArrayList<Runner> newRanking = new ArrayList<Runner>();
		SortedSetMultimap<Double, InstanceType> resultForType;
		if (currentRanking.isEmpty()) {
			newRanking.add(runner);
			newAvgMean = 1d;
		} else {
			Class<? extends IInstanceOrderer> instanceOrderer = getMetricsConfiguration().getSelectedInstanceOrderer();

			LinkedList<InstanceType> instanceTypes = new LinkedList<>();
			for (Runner olderRunner : currentRanking) {
				instanceTypes.add(olderRunner.getInstanceType());
				newRanking.add(olderRunner);
			}
			instanceTypes.add(runner.getInstanceType());
			newRanking.add(runner);

			Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType =
			    getResultsForAllMetricsTypesForType();

			synchronized (resultsForAllMetricsTypesForType) {
				Multimap<IMetricsType, IMetricsResult> resultsForAllMetrics =
				    resultsForAllMetricsTypesForType.get(resultsForAllMetricsTypesForType.keySet().iterator().next());

				if (resultsForAllMetrics != null) {

					for (IMetricsType metricsType : resultsForAllMetrics.keySet()) {

						try {
							resultForType =
							    MetricsConfiguration.getInstancesOrderedForMetricsType(
							        metricsType,
							        instanceOrderer,
							        instanceTypes,
							        getMetricsConfiguration().getSelectedReference(),
							        resultsForAllMetricsTypesForType);

							getMetricsConfiguration().putRelativeResults(metricsType, resultForType);

							Double sumMeans = 0d;
							for (Double mean : resultForType.keys()) {
								sumMeans += mean;
							}
							newAvgMean = sumMeans / resultForType.keys().size();

						} catch (InstantiationException | IllegalAccessException e) {
							e.printStackTrace();
						}

					}
				}
			}

			Collections.sort(newRanking, Collections.reverseOrder(new Comparator<Runner>() {

				@Override
				public int compare(Runner o1, Runner o2) {
					return getMetricsConfiguration().getWeightedRelative(o1.getInstanceType()).compareTo(
					    getMetricsConfiguration().getWeightedRelative(o2.getInstanceType()));
				}

			}));
		}
		return newRanking;
	}

	@Override
	public void notifyBenchmarkDone(Runner runner, Benchmark benchmark) {
	}

	@Override
	protected void notifyAborted(Runner runner) {
		abortedRunners.add(runner);
	}

	public int getInstancesBenchmarked() {
		return currentRanking.size();
	}

	public Integer getRankForInstanceType(InstanceType instanceType) {
		Integer rank = currentRanking.indexOf(getRunner(instanceType)) + 1;
		if (rank == 0)
			return null;
		return rank;
	}

	public Duration getRunTime() {
		Duration sumDuration = new Duration(0);
		for (Runner runner : currentRanking) {
			sumDuration = sumDuration.plus(runner.getInstanceState().getRunningDuration());
		}
		return sumDuration;
	}

	public String getName() {
		return "seq-unordered-" + lookbackThreshold;
	}

}
