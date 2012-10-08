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

package edu.kit.aifb.libIntelliCloudBench;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Observable;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.vaadin.artur.icepush.ICEPush;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;
import com.google.inject.Module;

import edu.kit.aifb.libIntelliCloudBench.background.BenchmarkRunner;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.metrics.CostsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IInstanceOrderer;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.BenchmarkingState;
import edu.kit.aifb.libIntelliCloudBench.model.Credentials;
import edu.kit.aifb.libIntelliCloudBench.model.ICredentialsChangedListener;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceState;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;

public class CloudBenchService extends Observable implements Serializable, ICredentialsChangedListener {
	private static final long serialVersionUID = -7680311779178774123L;

	private LinkedList<Provider> providers = new LinkedList<Provider>();
	private Map<Provider, ComputeServiceContext> contextForProvider = new HashMap<Provider, ComputeServiceContext>();

	private Collection<InstanceType> instances;
	private Map<InstanceType, Multimap<Benchmark, Result>> resultsForAllBenchmarksForType =
	    new HashMap<InstanceType, Multimap<Benchmark, Result>>();

	private Map<InstanceType, Runner> runnerForInstanceType = new HashMap<InstanceType, Runner>();
	private BenchmarkingState benchmarkingState;
	
  private MetricsConfiguration metricsConfiguration;
	
	private Integer numberOfRunnersDone;

	private ICEPush pusher = new ICEPush();

	private String name;

	private Collection<Benchmark> benchmarks;

	public CloudBenchService() {
		this("libIntelliCloudBench");
	}

	public CloudBenchService(String name) {
		this.name = name;
	}

	public ICEPush getPusher() {
		return pusher;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Iterable<Provider> getAllProviders() {
		if (providers.isEmpty()) {
			for (ProviderMetadata provider : Providers.viewableAs(ComputeServiceContext.class)) {
				providers.add(new Provider(provider));
			}
		}
		return providers;
	}

	public Provider getProviderById(String id) {
		for (Provider provider : providers) {
			if (provider.getId().equals(id))
				return provider;
		}
		return null;
	}

	public ComputeServiceContext getContext(Provider provider) {
		ComputeServiceContext context = contextForProvider.get(provider);
		Credentials credentials = provider.getCredentials();
		if (context == null && provider.areCredentialsSetup()) {
			provider.registerCredentialsChangedListener(this);
			context =
			    ContextBuilder.newBuilder(provider.getId()).credentials(credentials.getKey(), credentials.getSecret())
			        .modules(ImmutableSet.<Module> of(new SshjSshClientModule())).buildView(ComputeServiceContext.class);
		}
		return context;
	}

	public MetricsConfiguration getMetricsConfiguration() {
  	return metricsConfiguration;
  }
	
	public void setMetricsConfiguration(MetricsConfiguration metricsConfiguration) {
		this.metricsConfiguration = metricsConfiguration;
	}

	@Override
	public void notifyCredentialsChanged(Provider provider, Credentials credentials) {
		contextForProvider.remove(provider);
	}

	public void prepareBenchmarking(Collection<InstanceType> checkedInstanceTypes) {
		this.benchmarks = metricsConfiguration.getSelectedBenchmarks();
		this.instances = checkedInstanceTypes;
		this.numberOfRunnersDone = 0;

		BenchmarkRunner runner;
		for (InstanceType instanceType : instances) {
			runner = new BenchmarkRunner(this, instanceType, benchmarks);
			runnerForInstanceType.put(instanceType, runner);
		}
		benchmarkingState = new BenchmarkingState(runnerForInstanceType.values());
	}

	public BenchmarkingState getBenchmarkingState() {
		return benchmarkingState;
	}

	public Collection<InstanceState> getAllInstanceStates() {
		Collection<InstanceState> allInstanceStates = new LinkedList<InstanceState>();
		for (Runner runner : runnerForInstanceType.values()) {
			allInstanceStates.add(runner.getInstanceState());
		}
		return allInstanceStates;
	}

	public void startBenchmarking() {
		for (Runner runner : runnerForInstanceType.values()) {
			new Thread(runner).start();
		}
	}

	public void terminateAllImmediately() {
		for (Runner runner : runnerForInstanceType.values()) {
			runner.terminateImmediately();
		}
		runnerForInstanceType.clear();
	}

	public void notifyDone(Runner runner) {
		/* Get results */
		InstanceType instanceType = runner.getInstanceState().getInstanceType();
		resultsForAllBenchmarksForType.put(instanceType, runner.getBenchmarkResults());

		/* Check if all are done and notify the GUI */
		synchronized (numberOfRunnersDone) {
			if (++numberOfRunnersDone >= runnerForInstanceType.values().size()) {
				setChanged();
				notifyObservers();
			}
		}
	}

	public Collection<Runner> getAllRunner() {
		return runnerForInstanceType.values();
	}

	public Collection<Benchmark> getAllBenchmarks() {
		return benchmarks;
	}

	public Map<InstanceType, Multimap<Benchmark, Result>> getResultsForAllBenchmarksForType() {
		return resultsForAllBenchmarksForType;
	}

	public void setBenchmarkResultsForType(Map<InstanceType, Multimap<Benchmark, Result>> resultsForAllBenchmarksForType) {
		this.resultsForAllBenchmarksForType = resultsForAllBenchmarksForType;
		this.benchmarks =
		    resultsForAllBenchmarksForType.get(resultsForAllBenchmarksForType.keySet().iterator().next()).keySet();
	}

	public SortedSetMultimap<Double, InstanceType> getInstancesOrderedForMetricsType(final IMetricsType metricsType,
	    Class<? extends IInstanceOrderer> instanceOrderer, InstanceType referenceInstance) throws InstantiationException,
	    IllegalAccessException {

		Collection<InstanceType> instanceTypes = resultsForAllBenchmarksForType.keySet();
		Map<InstanceType, IMetricsResult> resultsForType = new HashMap<InstanceType, IMetricsResult>();

		if (metricsType instanceof CostsStore) {
			for (InstanceType instanceType : instanceTypes) {
				CostsResult resultForType = new CostsResult(CostsStore.getInstance().getCostsForMonthsRunning(instanceType, 1));
				resultsForType.put(instanceType, resultForType);
			}

		} else {

			for (InstanceType instanceType : instanceTypes) {

				Collection<Result> resultForType = resultsForAllBenchmarksForType.get(instanceType).asMap().get(metricsType);

				if (resultForType.size() == 1) {
					resultsForType.put(instanceType, resultForType.iterator().next());

				} else {
					throw new RuntimeException() {
						private static final long serialVersionUID = 4561200421717233183L;

						@Override
						public String getMessage() {
							return "Somehow there was more than one benchmark result for " + metricsType.getName();
						}

					};
				}
			}
		}

		return instanceOrderer.newInstance().orderInstances(resultsForType, referenceInstance);
	}
}
