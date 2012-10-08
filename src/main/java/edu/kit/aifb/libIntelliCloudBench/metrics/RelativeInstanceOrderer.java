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

import java.util.Map;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult.Proportion;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class RelativeInstanceOrderer implements IInstanceOrderer {

	@Override
	public SortedSetMultimap<Double, InstanceType> orderInstances(Map<InstanceType, IMetricsResult> resultsForType,
	    InstanceType referenceInstance) {
		TreeMultimap<Double, InstanceType> orderedInstances =
		    TreeMultimap.create(Ordering.natural(), Ordering.usingToString());

		/* Get if lower or higher is better */
		Proportion proportion = resultsForType.get(resultsForType.keySet().iterator().next()).getProportion();

		/* Find highest and lowest result */
		Double lowest = Double.MAX_VALUE;
		Double highest = Double.MIN_VALUE;
		for (InstanceType instanceType : resultsForType.keySet()) {
			Double curResult = resultsForType.get(instanceType).getValue();
			lowest = Math.min(lowest, curResult);
			highest = Math.max(highest, curResult);
		}

		Double relativeResult;

		for (InstanceType instanceType : resultsForType.keySet()) {
			if (highest > lowest) {
				Double curResult = resultsForType.get(instanceType).getValue();
				relativeResult = (curResult - lowest) / (highest - lowest);
			} else {
				relativeResult = 1d;
			}

			if (proportion == Proportion.LIB)
				relativeResult = 1d - relativeResult;

			orderedInstances.put(relativeResult, instanceType);
		}

		return (SortedSetMultimap<Double, InstanceType>) orderedInstances;
	}

}
