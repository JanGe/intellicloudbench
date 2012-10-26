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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.vaadin.artur.icepush.ICEPush;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.inject.Module;

import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.BenchmarkingState;
import edu.kit.aifb.libIntelliCloudBench.model.Credentials;
import edu.kit.aifb.libIntelliCloudBench.model.ICredentialsChangedListener;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceState;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;
import edu.kit.aifb.libIntelliCloudBench.stopping.StoppingConfiguration;
import edu.kit.aifb.libIntelliCloudBench.stopping.StoppingMethod;

public class CloudBenchService extends Observable implements Serializable, ICredentialsChangedListener, IService {
	private static final long serialVersionUID = -7680311779178774123L;

	private LinkedList<Provider> providers = new LinkedList<Provider>();
	private Map<Provider, ComputeServiceContext> contextForProvider = new HashMap<>();

	private Map<InstanceType, Multimap<Benchmark, Result>> resultsForAllBenchmarksForType = new HashMap<>();

	private BenchmarkingState benchmarkingState;

	private MetricsConfiguration metricsConfiguration;
	private StoppingConfiguration stoppingConfiguration;

	private Integer numberOfRunnersDone;

	private ICEPush pusher = new ICEPush();

	private String name;

	private List<Benchmark> benchmarks;

	private StoppingMethod stopper;

	public CloudBenchService() {
		this("libIntelliCloudBench");
	}

	public CloudBenchService(String name) {
		this.name = name;
	}

	public ICEPush getPusher() {
		return pusher;
	}

	@Override
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
			        .modules(ImmutableSet.<Module> of(new JschSshClientModule())).buildView(ComputeServiceContext.class);
		}
		return context;
	}

	public MetricsConfiguration getMetricsConfiguration() {
		return metricsConfiguration;
	}

	public void setMetricsConfiguration(MetricsConfiguration metricsConfiguration) {
		this.metricsConfiguration = metricsConfiguration;
	}

	public StoppingConfiguration getStoppingConfiguration() {
		return stoppingConfiguration;
	}

	public void setStoppingConfiguration(StoppingConfiguration stoppingConfiguration) {
		this.stoppingConfiguration = stoppingConfiguration;
	}

	@Override
	public void notifyCredentialsChanged(Provider provider, Credentials credentials) {
		contextForProvider.remove(provider);
	}

	public void prepareBenchmarking(List<InstanceType> checkedInstanceTypes, Class<? extends Runner> runnerClass) {
		this.resultsForAllBenchmarksForType.clear();
		this.benchmarks = metricsConfiguration.getSelectedBenchmarks();
		this.numberOfRunnersDone = 0;

		Integer stoppingMethodIndex = stoppingConfiguration.getSelectedStoppingMethodIndex();

		try {
			this.stopper =
			    StoppingConfiguration.newInstanceOf(stoppingMethodIndex, this, runnerClass, checkedInstanceTypes, benchmarks);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
		    | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		benchmarkingState = new BenchmarkingState(stopper.getRunners());
	}

	public BenchmarkingState getBenchmarkingState() {
		return benchmarkingState;
	}

	public Collection<InstanceState> getAllInstanceStates() {
		Collection<InstanceState> allInstanceStates = new LinkedList<InstanceState>();
		for (Runner runner : stopper.getRunners()) {
			allInstanceStates.add(runner.getInstanceState());
		}
		return allInstanceStates;
	}

	public void startBenchmarking() {
		stopper.start();
	}

	public void terminateAllImmediately() {
		/* TODO: Needs a check to see if the instances really terminated... */
		this.stopper.terminateAll();
	}

	public void notifyFinished(Runner runner) {
		/* Get results */
		InstanceType instanceType = runner.getInstanceState().getInstanceType();
		Multimap<Benchmark, Result> benchmarkResults = runner.getBenchmarkResults();
		if (benchmarkResults != null)
			resultsForAllBenchmarksForType.put(instanceType, benchmarkResults);

		/* Check if all are done and notify the GUI */
		synchronized (numberOfRunnersDone) {
			if (++numberOfRunnersDone >= stopper.getRunners().size()) {
				setChanged();
				notifyObservers();
			}
		}
	}

	public Collection<Benchmark> getAllBenchmarks() {
		return benchmarks;
	}

	public Map<InstanceType, Multimap<Benchmark, Result>> getResultsForAllBenchmarksForType() {
		return resultsForAllBenchmarksForType;
	}

	public void setBenchmarkResultsForType(Map<InstanceType, Multimap<Benchmark, Result>> resultsForAllBenchmarksForType) {
		this.resultsForAllBenchmarksForType = resultsForAllBenchmarksForType;
		InstanceType instanceType = resultsForAllBenchmarksForType.keySet().iterator().next();
		this.benchmarks = new LinkedList<>(resultsForAllBenchmarksForType.get(instanceType).keySet());
	}

	public String getStopperLog() {
		if (stopper == null)
			return "";
		return stopper.getLog();
	}

	@Override
  public ComputeService getComputeService(Provider provider) {
	  return this.getContext(provider).getComputeService();
  }

}
