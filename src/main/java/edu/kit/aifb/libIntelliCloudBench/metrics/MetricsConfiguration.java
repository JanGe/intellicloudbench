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
