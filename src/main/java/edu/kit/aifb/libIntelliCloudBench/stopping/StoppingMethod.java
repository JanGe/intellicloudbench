package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.metrics.CostsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore.Costs;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;

public abstract class StoppingMethod {

	private List<InstanceType> orderedInstanceTypes;
	private List<Benchmark> orderedBenchmarks;
	private MetricsConfiguration metricsConfiguration;
	private Integer param = null;

	private LinkedList<Runner> orderedRunners = new LinkedList<>();
	private Map<InstanceType, Runner> instanceTypeToRunner = new HashMap<>();
	private Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType = new HashMap<>();

	private Collection<Runner> runnersDone = new HashSet<>();
	private Collection<Runner> runnersAborted = new HashSet<>();
	private Collection<Runner> runnersStopped = new HashSet<>();

	private Map<Runner, Boolean> scheduledToTerminate = new HashMap<>();

	private Double budgetApproved = 0d;

	private StringBuilder logBuilder = new StringBuilder();

	public StoppingMethod(IService service, Class<? extends Runner> runnerType, List<InstanceType> instanceTypes,
	    List<Benchmark> benchmarks, Integer param) {
		this.metricsConfiguration = service.getMetricsConfiguration();
		this.orderedInstanceTypes = orderInstanceTypes(instanceTypes);
		this.orderedBenchmarks = orderBenchmarks(benchmarks);
		this.param = param;

		/*
		 * Make sure a selected reference instance will be scheduled at the
		 * beginning
		 */
		if (getMetricsConfiguration().requiresReference()) {
			InstanceType foundInstanceType = null;
			for (InstanceType instanceType : this.orderedInstanceTypes) {
				if (getMetricsConfiguration().getSelectedReference().equals(instanceType)) {
					foundInstanceType = instanceType;
					continue;
				}
			}
			if (foundInstanceType != null) {
				this.orderedInstanceTypes.remove(foundInstanceType);
				this.orderedInstanceTypes.add(0, foundInstanceType);
			}
		}

		if (metricsConfiguration.getWeight(CostsStore.getInstance()) != null) {
			CostsResult costsResult;
			for (InstanceType instanceType : instanceTypes) {
				costsResult = new CostsResult(CostsStore.getInstance().getCostsForMonthsRunning(instanceType, 1));
				synchronized (resultsForAllMetricsTypesForType) {
					Multimap<IMetricsType, IMetricsResult> resultsForAllMetricsTypes =
					    resultsForAllMetricsTypesForType.get(instanceType);
					if (resultsForAllMetricsTypes == null) {
						resultsForAllMetricsTypes = LinkedListMultimap.create();
						resultsForAllMetricsTypesForType.put(instanceType, resultsForAllMetricsTypes);
					}
					resultsForAllMetricsTypes.put(CostsStore.getInstance(), costsResult);
				}
			}
		}

		Runner runner = null;
		for (InstanceType instanceType : orderedInstanceTypes) {
			try {
				runner =
				    runnerType.getConstructor(IService.class, StoppingMethod.class, InstanceType.class, List.class)
				        .newInstance(service, this, instanceType, this.orderedBenchmarks);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
			    | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
			this.scheduledToTerminate.put(runner, false);
			this.instanceTypeToRunner.put(instanceType, runner);
			this.orderedRunners.add(runner);
		}
	}

	public List<InstanceType> getOrderedInstanceTypes() {
		return orderedInstanceTypes;
	}

	public List<Runner> getRunners() {
		return orderedRunners;
	}

	public Integer getParam() {
		return param;
	}

	protected abstract List<Benchmark> orderBenchmarks(List<Benchmark> benchmarks);

	protected abstract List<InstanceType> orderInstanceTypes(List<InstanceType> instanceTypes);

	public abstract void start();

	protected abstract void notifyDone(Runner runner);

	protected abstract void notifyAborted(Runner runner);

	public abstract void notifyBenchmarkDone(Runner runner, Benchmark benchmark);

	public abstract void timedNotify(Runner runner);

	public void updateResults(Runner runner, LinkedListMultimap<Benchmark, Result> resultsForBenchmark) {
		synchronized (resultsForAllMetricsTypesForType) {
			Multimap<IMetricsType, IMetricsResult> resultsForMetricsType =
			    resultsForAllMetricsTypesForType.get(runner.getInstanceType());
			if (resultsForMetricsType == null) {
				resultsForMetricsType = LinkedListMultimap.create();
				resultsForAllMetricsTypesForType.put(runner.getInstanceType(), resultsForMetricsType);
			}

			for (Benchmark benchmark : resultsForBenchmark.keySet()) {
				for (Result result : resultsForBenchmark.get(benchmark)) {
					if (resultsForMetricsType.get(benchmark).isEmpty())
						resultsForMetricsType.put(benchmark, result);
				}
			}
		}

	}

	public Boolean shouldTerminate(Runner runner) {
		return scheduledToTerminate.get(runner);
	}

	public void setFailed(Runner runner) {
		notifyAborted(runner);
		scheduleTermination(runner);
	}

	public void terminateAll() {
		for (Runner runner : getStillRunning()) {
			runner.terminateImmediately();
			runnersAborted.add(runner);
		}
	}

	public List<Runner> getStillRunning() {
		List<Runner> stillRunning = new LinkedList<>(orderedRunners);
		stillRunning.removeAll(runnersDone);
		stillRunning.removeAll(runnersStopped);
		stillRunning.removeAll(runnersAborted);
		return stillRunning;
	}

	public List<Runner> getStillInRace() {
		List<Runner> stillInRace = new LinkedList<>(orderedRunners);
		stillInRace.removeAll(runnersStopped);
		stillInRace.removeAll(runnersAborted);
		return stillInRace;
	}

	private void scheduleTermination(Runner runner) {
		scheduledToTerminate.put(runner, true);
	}

	public void runnerDone(Runner runner) {
		runnersDone.add(runner);
		notifyDone(runner);
	}

	public void runnerStopped(Runner runner) {
		runnersStopped.add(runner);
		notifyDone(runner);
	}

	public void runnerAborted(Runner runner) {
		runnersAborted.add(runner);
		notifyDone(runner);
	}

	public Runner getRunner(InstanceType instanceType) {
		return instanceTypeToRunner.get(instanceType);
	}

	public boolean doesNotExceedBudget(Runner runner) {
		if (metricsConfiguration.isCostsBudgetSelected()) {
			Costs costs = CostsStore.getInstance().getCosts(runner.getInstanceType());
			double costsForNextStep = costs.getVariableCosts();
			return checkBudgetApproved(costsForNextStep);
		} else {
			return true;
		}
	}

	public boolean doesNotExceedBudgetOnStarting(Runner runner) {
		if (metricsConfiguration.isCostsBudgetSelected()) {
			Costs costs = CostsStore.getInstance().getCosts(runner.getInstanceType());
			double costsForFirstStep = costs.getVariableCosts() + costs.getFixedCosts();
			return checkBudgetApproved(costsForFirstStep);
		} else {
			return true;
		}
	}

	private boolean checkBudgetApproved(double costsForNextStep) {
		synchronized (budgetApproved) {
			if ((budgetApproved + costsForNextStep) <= metricsConfiguration.getCostsBudget()) {
				budgetApproved += costsForNextStep;
				return true;
			} else {
				return false;
			}
		}
	}

	protected MetricsConfiguration getMetricsConfiguration() {
		return metricsConfiguration;
	}

	protected void startRunner(Runner runner) {
		Thread t = new Thread(runner);
		t.start();
	}

	protected void stopRunner(Runner runner) {
		runner.getInstanceState().setStopped();
		runnersStopped.add(runner);
		runner.terminateImmediately();
	}

	public Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> getResultsForAllMetricsTypesForType() {
		return resultsForAllMetricsTypesForType;
	}

	public String getLog() {
		return logBuilder.toString();
	}

	protected void logLine(String logLine) {
		logBuilder.append(logLine);
		logBuilder.append("\n");
	}

	public Double getSummarizedCosts() {
		Double sumCosts = 0d;
		for (Runner runner : getRunners()) {
			sumCosts += runner.getInstanceState().getEstimatedCosts();
		}
		return sumCosts;
	}

	public List<Benchmark> getOrderedBenchmarks() {
		return orderedBenchmarks;
	}

}
