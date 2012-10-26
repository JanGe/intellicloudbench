package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jclouds.compute.ComputeService;

import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.background.EvaluationRunner;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.metrics.IInstanceOrderer;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;

public class EvaluationStopper extends StoppingMethod implements IService {

	private String name;

	private List<Class<? extends StoppingMethod>> stoppingMethods;

	private Map<InstanceType, Map<Benchmark, Object>> barriersForBenchmarkForType = new HashMap<>();

	private List<StoppingMethod> stoppers = new LinkedList<>();

	private List<Runner> ranking;

	private int counter = 0;

	private boolean allStopperDone = false;

	public EvaluationStopper(IService service, Class<? extends Runner> runnerType, List<InstanceType> instanceTypes,
	    List<Benchmark> benchmarks, Integer param) {
		super(service, runnerType, instanceTypes, benchmarks, param);

		this.name = service.getName();

		for (InstanceType instanceType : instanceTypes) {
			Map<Benchmark, Object> barriersForBenchmarks = new HashMap<>();
			for (Benchmark benchmark : benchmarks) {
				barriersForBenchmarks.put(benchmark, new Object());
			}
			barriersForBenchmarkForType.put(instanceType, barriersForBenchmarks);
		}

		this.stoppingMethods = new ArrayList<>(StoppingConfiguration.STOPPING_METHODS);
		this.stoppingMethods.remove(NonStopper.class);
		this.stoppingMethods.remove(EvaluationStopper.class);
	}

	@Override
	protected List<Benchmark> orderBenchmarks(List<Benchmark> benchmarks) {
		return new LinkedList<>(benchmarks);
	}

	@Override
	protected List<InstanceType> orderInstanceTypes(List<InstanceType> instanceTypes) {
		return new LinkedList<>(instanceTypes);
	}

	@Override
	public void start() {
		for (Runner runner : getRunners()) {
			logLine("Starting instance " + runner.getInstanceType().asString(" | "));
			startRunner(runner);
		}
		for (int i = 0; i < stoppingMethods.size(); i++) {
			try {
				StoppingMethod stopper =
				    StoppingConfiguration.newInstanceOf(
				        i + 1,
				        this,
				        EvaluationRunner.class,
				        getOrderedInstanceTypes(),
				        getOrderedBenchmarks());
				this.stoppers.add(stopper);
				stopper.start();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
			    | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void notifyDone(Runner runner) {
		synchronized (this) {
			try {
				if (!allStopperDone)
					this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.ranking = getRanking();
	}

	@Override
	protected void notifyAborted(Runner runner) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyBenchmarkDone(Runner runner, Benchmark benchmark) {
		Object barrier = getBarrier(runner, benchmark);
		synchronized (barrier) {
			barrier.notifyAll();
		}
	}

	@Override
	public void timedNotify(Runner runner) {
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void notifyFinished(Runner runner) {
		synchronized (this) {
			counter++;
			if (counter >= stoppers.size() * getOrderedInstanceTypes().size()) {
				this.notifyAll();
				allStopperDone = true;
			}
		}
	}

	@Override
	public ComputeService getComputeService(Provider provider) {
		return null;
	}

	public MetricsConfiguration getMetricsConfiguration() {
		return super.getMetricsConfiguration();
	}

	public void notifyInstanceStarted(StoppingMethod stopper, EvaluationRunner evaluationRunner) {

	}

	@Override
	public String getLog() {
		StringBuilder sb = new StringBuilder(super.getLog() + "\n");
		int i = 1;
		for (StoppingMethod stopper : stoppers) {
			sb.append(StoppingConfiguration.getName(i) + "\n");
			sb.append("======================\n");
			sb.append(stopper.getLog() + "\n");
			i++;
		}

		List<UnorderedSequentialStopper> seq = new LinkedList<>();
		List<UnorderedParallelStopper> par = new LinkedList<>();

		for (StoppingMethod stopper : stoppers) {
			if (stopper instanceof UnorderedSequentialStopper) {
				seq.add((UnorderedSequentialStopper) stopper);
			} else if (stopper instanceof UnorderedParallelStopper) {
				par.add((UnorderedParallelStopper) stopper);
			}
		}

		sb.append("\n");
		/* Dump CSV shit */
		/* Für seq */
		sb.append("\nSeq: #I - Methods\n");
		for (UnorderedSequentialStopper stopper : seq) {
			sb.append(stopper.getName() + "," + stopper.getInstancesBenchmarked());
			sb.append("\n");
		}

		sb.append("\nPar: #1 - Benchmarks - Methods\n");
		for (UnorderedParallelStopper stopper : par) {
			for (Benchmark benchmark : getOrderedBenchmarks()) {
				sb.append(stopper.getName());
				sb.append("," + benchmark.getId());
				sb.append("," + stopper.getNumberOfInstancesAfter(benchmark));
				sb.append("\n");
			}
		}

		sb.append("\nTime - Methods\n");
		for (UnorderedSequentialStopper stopper : seq) {
			sb.append(stopper.getName() + "," + stopper.getRunTime().getStandardSeconds());
			sb.append("\n");
		}
		for (UnorderedParallelStopper stopper : par) {
			sb.append(stopper.getName() + "," + stopper.getRunTime().getStandardSeconds());
			sb.append("\n");
		}

		sb.append("\nRanking - Methods\n");
		int j = 1;
		sb.append("Service");
		for (UnorderedSequentialStopper stopper : seq) {
			sb.append("," + stopper.getName());
		}
		for (UnorderedParallelStopper stopper : par) {
			sb.append("," + stopper.getName());
		}
		sb.append("\n");
		for (Runner runner : getRanking()) {
			InstanceType instanceType = runner.getInstanceType();
			sb.append(instanceType.asString(" | ") + "," + j);
			for (UnorderedSequentialStopper stopper : seq) {
				sb.append("," + getRankForInstanceType(instanceType, stopper));
			}
			for (UnorderedParallelStopper stopper : par) {
				sb.append("," + getRankForInstanceType(instanceType, stopper));
			}
			sb.append("\n");
			j++;
		}

		sb.append("\nError - Methods\n");
		int k = 1;
		for (Runner runner : getRanking()) {
			InstanceType instanceType = runner.getInstanceType();
			for (UnorderedSequentialStopper stopper : seq) {
				sb.append(instanceType.asString(" | "));
				sb.append("," + stopper.getName());
				Integer rank = stopper.getRankForInstanceType(instanceType);
				String s;
				if (rank == null) {
					s = "-";
				} else {
					s = Integer.toString(Math.abs(rank - k));
				}
				sb.append("," + s);
				sb.append("\n");
			}
			for (UnorderedParallelStopper stopper : par) {
				sb.append(instanceType.asString(" | "));
				sb.append("," + stopper.getName());
				Integer rank = stopper.getRankForInstanceType(instanceType);
				String s;
				if (rank == null) {
					s = "-";
				} else {
					s = Integer.toString(Math.abs(rank - k));
				}
				sb.append("," + s);
				sb.append("\n");
			}
			k++;
		}

		return sb.toString();
	}

	public String getRankForInstanceType(InstanceType instanceType, UnorderedSequentialStopper stopper) {
		Integer rank = stopper.getRankForInstanceType(instanceType);
		if (rank == null)
			return "-";
		return rank.toString();
	}
	
	public String getRankForInstanceType(InstanceType instanceType, UnorderedParallelStopper stopper) {
		Integer rank = stopper.getRankForInstanceType(instanceType);
		if (rank == null)
			return "-";
		return rank.toString();
	}

	public Object getBarrier(Runner runner, Benchmark benchmark) {
		return barriersForBenchmarkForType.get(runner.getInstanceType()).get(benchmark);
	}

	private List<Runner> getRanking() {
		Class<? extends IInstanceOrderer> instanceOrderer = getMetricsConfiguration().getSelectedInstanceOrderer();

		SortedSetMultimap<Double, InstanceType> resultForType;
		List<Runner> ranking = new LinkedList<>(getStillInRace());
		List<InstanceType> instanceTypes = new LinkedList<>();
		List<IMetricsType> allMetricsTypes = new LinkedList<>();

		if (getMetricsConfiguration().getWeight(CostsStore.getInstance()) != null)
			allMetricsTypes.add(CostsStore.getInstance());
		allMetricsTypes.addAll(getOrderedBenchmarks());

		Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType =
		    getResultsForAllMetricsTypesForType();

		for (Runner runner : ranking) {
			instanceTypes.add(runner.getInstanceType());
		}

		for (IMetricsType metricsType : allMetricsTypes) {

			try {
				resultForType =
				    MetricsConfiguration.getInstancesOrderedForMetricsType(
				        metricsType,
				        instanceOrderer,
				        instanceTypes,
				        getMetricsConfiguration().getSelectedReference(),
				        resultsForAllMetricsTypesForType);

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

		return ranking;
	}

	public Integer getRankForInstanceType(InstanceType instanceType) {
		Integer rank = ranking.indexOf(getRunner(instanceType)) + 1;
		if (rank == 0)
			return null;
		return rank;
	}

}
