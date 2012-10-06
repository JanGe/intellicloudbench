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
