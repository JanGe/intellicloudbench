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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.SortedSetMultimap;

import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;

public class MetricsConfiguration implements Serializable {
	private static final long serialVersionUID = 5888441610004496580L;

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

	private Map<IMetricsType, Double> weightsForMetrics = new HashMap<IMetricsType, Double>();

	private Map<InstanceType, Map<IMetricsType, Double>> relativeResults =
	    new HashMap<InstanceType, Map<IMetricsType, Double>>();

	private boolean costsBudgetSelected = false;
	private Double costsBudget = 100d;
	private InstanceType selectedReference;

	public MetricsConfiguration(Collection<InstanceType> instanceTypes) {
		for (InstanceType instanceType : instanceTypes) {
			relativeResults.put(instanceType, new HashMap<IMetricsType, Double>());
		}
	}

	public void setWeight(IMetricsType metricsType, Double weight) {
		weightsForMetrics.put(metricsType, weight);
	}

	public void clearWeight(IMetricsType metricsType) {
		weightsForMetrics.remove(metricsType);
	}

	public Double getWeight(IMetricsType metricsType) {
		return weightsForMetrics.get(metricsType);
	}

	public Set<IMetricsType> getSelected() {
		return weightsForMetrics.keySet();
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
	public List<Benchmark> getSelectedBenchmarks() {
		List<? extends IMetricsType> benchmarks = new LinkedList<>(getSelected());
		benchmarks.remove(CostsStore.getInstance());
		return (List<Benchmark>) benchmarks;
	}

	public void setSelectedReference(InstanceType instanceType) {
		this.selectedReference = instanceType;
	}

	public InstanceType getSelectedReference() {
		/* TODO: make generic */
		if (getSelectedInstanceOrderer().equals(RelativeToReferenceInstanceOrderer.class)) {
			return selectedReference;
		} else {
			return null;
		}
	}

	public Double getWeightedRelative(InstanceType instanceType) {
		Double sumWeight = 0d;
		Double sumWeighted = 0d;

		for (IMetricsType iMetricsType : relativeResults.get(instanceType).keySet()) {
			Double weight = getWeight(iMetricsType);
			if (weight == null)
				weight = 100d;
			Double relativeResult = relativeResults.get(instanceType).get(iMetricsType);

			sumWeight += weight;
			sumWeighted += weight * relativeResult;
		}
		return sumWeighted / sumWeight;
	}

	public Map<InstanceType, Map<IMetricsType, Double>> getRelativeResults() {
		return relativeResults;
	}

	public void putRelativeResults(IMetricsType metricsType, SortedSetMultimap<Double, InstanceType> resultForType) {
		for (Double relativeResult : resultForType.keySet()) {
			Collection<InstanceType> instanceTypes = resultForType.get(relativeResult);
			for (InstanceType instanceType : instanceTypes) {
				Map<IMetricsType, Double> relResults = relativeResults.get(instanceType);
				if (relResults == null) {
					relResults = new HashMap<>();
					relativeResults.put(instanceType, relResults);
				}
				relResults.put(metricsType, relativeResult);
			}
		}
	}

	public static SortedSetMultimap<Double, InstanceType> getInstancesOrderedForMetricsType(
	    final IMetricsType metricsType, Class<? extends IInstanceOrderer> instanceOrderer,
	    Collection<InstanceType> instanceTypes, InstanceType referenceInstance,
	    Map<InstanceType, Multimap<IMetricsType, IMetricsResult>> resultsForAllMetricsTypesForType)
	    throws InstantiationException, IllegalAccessException {

		Map<InstanceType, IMetricsResult> resultsForType = new HashMap<InstanceType, IMetricsResult>();
		for (InstanceType instanceType : instanceTypes) {

			Multimap<IMetricsType, IMetricsResult> resultsForAllMetricsTypes =
			    resultsForAllMetricsTypesForType.get(instanceType);

			if (resultsForAllMetricsTypes != null) {

				Collection<IMetricsResult> resultForType = resultsForAllMetricsTypes.get(metricsType);

				if (resultForType.size() == 1) {
					resultsForType.put(instanceType, (IMetricsResult) resultForType.toArray()[0]);

				} else if (resultForType.isEmpty()) {

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

	public boolean requiresReference() {
		boolean requiresReference = false;
		try {
			requiresReference = instanceOrderer.getField("REQUIRES_REFERENCE").getBoolean(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return requiresReference;
	}
}
