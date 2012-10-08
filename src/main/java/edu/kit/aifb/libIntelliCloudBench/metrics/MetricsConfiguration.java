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

package edu.kit.aifb.libIntelliCloudBench.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;

public class MetricsConfiguration {

	public static final List<Class<? extends IInstanceOrderer>> INSTANCE_ORDERER = new ArrayList<>();
	public static final List<String> INSTANCE_ORDERER_NAMES = new ArrayList<>();

	static {
		INSTANCE_ORDERER.add(RelativeInstanceOrderer.class);
		INSTANCE_ORDERER_NAMES.add("Relative");
		INSTANCE_ORDERER.add(RelativeToAboluteZeroInstanceOrderer.class);
		INSTANCE_ORDERER_NAMES.add("Relative to absolute zero");
		INSTANCE_ORDERER.add(RelativeToReferenceInstanceOrderer.class);
		INSTANCE_ORDERER_NAMES.add("Relative to reference instance");
		// TODO: add "Pairwise Comparision"
	}

	private Class<? extends IInstanceOrderer> instanceOrderer = INSTANCE_ORDERER.get(0);

	private Map<IMetricsType, Double> weightsForBenchmarks = new HashMap<IMetricsType, Double>();

	private boolean costsBudgetSelected = false;
	private Double costsBudget = 100d;
	private InstanceType selectedReference;

	public void setBenchmarkWeight(IMetricsType metricsType, Double weight) {
		weightsForBenchmarks.put(metricsType, weight);
	}

	public void clearBenchmarkWeight(IMetricsType metricsType) {
		weightsForBenchmarks.remove(metricsType);
	}

	public Double getWeight(IMetricsType metricsType) {
		return weightsForBenchmarks.get(metricsType);
	}

	public Set<IMetricsType> getSelected() {
		return weightsForBenchmarks.keySet();
	}

	public Class<? extends IInstanceOrderer> getSelectedInstanceOrderer() {
		return instanceOrderer;
	}

	public void setSelectedInstanceOrderer(Class<? extends IInstanceOrderer> instanceOrderer) {
		this.instanceOrderer = instanceOrderer;
	}

	public boolean isCostsBudgetSelected() {
		return costsBudgetSelected;
	}

	public void setCostsBudgetSelected(boolean costsBudgetSelected) {
		this.costsBudgetSelected = costsBudgetSelected;
	}

	public Double getCostsBudget() {
		return costsBudget;
	}

	public void setCostsBudget(double costsBudget) {
		this.costsBudget = costsBudget;
	}

	@SuppressWarnings("unchecked")
	public Collection<Benchmark> getSelectedBenchmarks() {
		Collection<? extends IMetricsType> benchmarks = new HashSet<>(getSelected());
		benchmarks.remove(CostsStore.getInstance());
		return (Collection<Benchmark>) benchmarks;
	}

	public void setSelectedReference(InstanceType instanceType) {
		this.selectedReference = instanceType;
	}

	public InstanceType getSelectedReference() {
		return selectedReference;
	}
}
