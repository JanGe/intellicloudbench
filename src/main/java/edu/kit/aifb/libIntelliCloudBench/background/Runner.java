package edu.kit.aifb.libIntelliCloudBench.background;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jclouds.compute.RunNodesException;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.kit.aifb.libIntelliCloudBench.CloudBenchService;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceState;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceState.State;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;

public abstract class Runner implements Runnable {

	private CloudBenchService cloudBenchService;
	private Collection<Benchmark> benchmarks;
	private InstanceType instanceType;
	private InstanceState instanceState;

	private LinkedListMultimap<Benchmark, Result> resultsForBenchmark;
	private Set<Benchmark> failedBenchmarks = new HashSet<Benchmark>();

	private volatile boolean terminate = false;

	public Runner(CloudBenchService cloudBenchService, InstanceType instanceType, Collection<Benchmark> benchmarks) {
		this.cloudBenchService = cloudBenchService;
		this.instanceType = instanceType;
		this.benchmarks = benchmarks;
		this.instanceState = new InstanceState(instanceType, benchmarks.size());
	}

	@Override
	public void run() {
		if (!terminate) {
			log("Preparing initialization...");
			prepare();
		}

		if (!terminate) {
			try {
				instanceState.setInit();
				initialize();
			} catch (RunNodesException e) {
				instanceState.setAborted(e);
				return;
			}
		}

		if (!terminate) {
			instanceState.setDeploy();
		}

		prepareDeployment();

		if (!terminate) {
			try {
				deploy();
			} catch (RunScriptOnMachineException e) {
				instanceState.setAborted(e);
				terminate = true;
			}
		}

		if (!terminate) {
			int i = 1;
			for (Benchmark benchmark : benchmarks) {
				instanceState.setDownload(i, benchmark.getName());
				try {
					install(benchmark);
				} catch (RunScriptOnMachineException e) {
					failedBenchmarks.add(benchmark);
				}
				i++;
			}
		}

		if (!terminate) {
			try {
				int i = 1;
				for (Benchmark benchmark : benchmarks) {
					instanceState.setRun(i, benchmark.getName());
					runBenchmark(benchmark);
					i++;
				}
			} catch (RunScriptOnMachineException e) {
				instanceState.setAborted(e);
				terminate = true;
			}
		}

		if (!terminate) {
			try {
				instanceState.setUpload();
				resultsForBenchmark = upload();
			} catch (ParseXmlResultException | RunScriptOnMachineException e) {
				instanceState.setAborted(e);
				terminate = true;
			}
		}

		if (!terminate) {

			cleanUp();
			log("Terminated machine normally.");
			instanceState.setDone();

		} else {

			cleanUp();
			if (instanceState.getState() != State.ABORTED)
				instanceState.setAborted();

		}
		cloudBenchService.notifyDone(this);

	}

	abstract void prepare();

	abstract void initialize() throws RunNodesException;

	abstract void prepareDeployment();

	abstract void deploy() throws RunScriptOnMachineException;

	abstract void install(Benchmark benchmark) throws RunScriptOnMachineException;

	abstract void runBenchmark(Benchmark benchmark) throws RunScriptOnMachineException;

	abstract LinkedListMultimap<Benchmark, Result> upload() throws ParseXmlResultException, RunScriptOnMachineException;

	abstract void cleanUp();

	abstract void terminate();

	public void log(String newEntry) {
		instanceState.log(newEntry);
	}

	public void appendToLog(String line) {
		instanceState.appendToLog(line);
	}

	public InstanceState getInstanceState() {
		return instanceState;
	}

	public List<Result> getResultsForBenchmark(Benchmark benchmark) {
		return resultsForBenchmark.get(benchmark);
	}

	public Multimap<Benchmark, Result> getBenchmarkResults() {
		return resultsForBenchmark;
	}

	public CloudBenchService getCloudBenchService() {
		return cloudBenchService;
	}

	public InstanceType getInstanceType() {
		return instanceType;
	}

	public Collection<Benchmark> getBenchmarks() {
  	return benchmarks;
  }

	public final void terminateImmediately() {
		log("Trying to terminate the instance immediately...");
		terminate();
		log("Instance terminated.");
	}

}
