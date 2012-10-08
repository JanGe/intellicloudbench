/*
* This file is part of IntelliCloudBench.
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

package edu.kit.aifb.IntelliCloudBench.ui.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

import edu.kit.aifb.libIntelliCloudBench.metrics.IInstanceOrderer;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;

public class BenchmarkSelectionModel extends Observable {

	private MetricsConfiguration metricsConfiguration;
	private List<InstanceType> instanceTypes;

	public BenchmarkSelectionModel(MetricsConfiguration metricsConfiguration) {
		this.metricsConfiguration = metricsConfiguration;
	}

	public boolean isSelected(Benchmark benchmark) {
		return metricsConfiguration.getWeight(benchmark) != null;
	}

	public void select(Benchmark benchmark, Double weight) {
		metricsConfiguration.setBenchmarkWeight(benchmark, weight);
	}

	public void unselect(Benchmark benchmark) {
		metricsConfiguration.clearBenchmarkWeight(benchmark);
	}

	public Double getWeight(Benchmark benchmark) {
		return metricsConfiguration.getWeight(benchmark);
	}

	public Collection<IMetricsType> getSelected() {
		return metricsConfiguration.getSelected();
	}

	public boolean isOneSelectedOfType(String type) {
		Collection<? extends IMetricsType> benchmarksForType = Benchmark.getAllBenchmarks().get(type);
		Collection<IMetricsType> intersection = new LinkedList<>(benchmarksForType);
		intersection.retainAll(getSelected());

		return intersection.size() > 0;
	}

	public Collection<Benchmark> getBenchmarksForType(String type) {
		return Benchmark.getAllBenchmarks().get(type);
	}

	public Collection<String> getTypes() {
		return Benchmark.getAllBenchmarks().keySet();
	}

	public Class<? extends IInstanceOrderer> getSelectedInstancesOrderer() {
		return metricsConfiguration.getSelectedInstanceOrderer();
	}
	
	public void setSelectedInstanceOrderer(Class<? extends IInstanceOrderer> instanceOrderer) {
		metricsConfiguration.setSelectedInstanceOrderer(instanceOrderer);
	}

	public MetricsConfiguration getMetricsConfiguration() {
		return metricsConfiguration;
	}

	public boolean isCostsSelected() {
		return metricsConfiguration.getWeight(CostsStore.getInstance()) != null;
	}

	public void selectCosts(Double weight) {
		metricsConfiguration.setBenchmarkWeight(CostsStore.getInstance(), weight);
	}

	public void unselectCosts() {
		metricsConfiguration.clearBenchmarkWeight(CostsStore.getInstance());
	}

	public Double getCostsWeight() {
		return metricsConfiguration.getWeight(CostsStore.getInstance());
	}

	public int getNumberOfSelectedBenchmarks() {
		int numSelected = metricsConfiguration.getSelected().size();
		if (isCostsSelected())
			numSelected--;
		return numSelected;
	}

	public Double getWeight(IMetricsType metricsType) {
		if (metricsType instanceof CostsStore) {
			return getCostsWeight();
		} else if (metricsType instanceof Benchmark) {
			return getWeight((Benchmark) metricsType);
		} else {
			return 0d;
		}
	}

	public void setSelectedReference(InstanceType instanceType) {
		metricsConfiguration.setSelectedReference(instanceType);
	}

	public InstanceType getSelectedReference() {
		return metricsConfiguration.getSelectedReference();
	}

	public List<InstanceType> getInstancesTypes() {
		return instanceTypes;
	}

	public void setInstanceTypes(List<InstanceType> instanceTypes) {
		this.instanceTypes = instanceTypes;
  }
	
	public Double getCostsBudget() {
		return metricsConfiguration.getCostsBudget();
	}
	
	public void setCostsBudget(Double budget) {
		metricsConfiguration.setCostsBudget(budget);
	}
	
	public boolean isCostsBudgetSelected() {
		return metricsConfiguration.isCostsBudgetSelected();
	}

	public void setCostsBudgetSelected(boolean costsBudgetSelected) {
		metricsConfiguration.setCostsBudgetSelected(costsBudgetSelected);
	}
}
