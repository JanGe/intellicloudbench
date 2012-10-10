/*
 * This file is part of libIntelliCloudBench.
 *
 * Copyright (c) 2012, Jan Gerlinger <jan.gerlinger@gmx.de>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Institute of Applied Informatics and Formal
 * Description Methods (AIFB) nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
			instanceState.setInit();
			try {
				create();
			} catch (RunNodesException e) {
				instanceState.setAborted(e);
				terminate = true;
			}
		}

		if (!terminate) {
			instanceState.setDeploy();
			prepareDeployment();
		}

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
			instanceState.setUpload();
			try {
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

	abstract void create() throws RunNodesException;

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
