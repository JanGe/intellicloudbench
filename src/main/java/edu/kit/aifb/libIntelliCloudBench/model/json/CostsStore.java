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

package edu.kit.aifb.libIntelliCloudBench.model.json;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class CostsStore implements IMetricsType, Serializable {
	private static final long serialVersionUID = -3637457131231220514L;

	private static final String costsFilename = "/costs.json";

	private Map<String, Costs> costsByType = new HashMap<String, Costs>();

	private static Gson gson;

	@Override
	public String getId() {
		return "costs";
	}

	@Override
	public String getName() {
		return "Monthly costs";
	}

	public Double getCostsForMonthsRunning(InstanceType instanceType, int months) {
		Costs costs =
		    getCosts(instanceType.getProvider().getId(), instanceType.getRegion().getId(), instanceType.getHardwareType()
		        .getId());
		return costs.getFixedCosts() + months * 30 * 24 * costs.getVariableCosts();
	}

	public Costs getCosts(InstanceType instanceType) {
		return getCosts(instanceType.getProvider().getId(), instanceType.getRegion().getId(), instanceType
		    .getHardwareType().getId());
	}

	public Costs getCosts(String providerId, String regionId, String hardwareTypeId) {
		Costs costs = costsByType.get(providerId + "-" + regionId + "-" + hardwareTypeId);
		if (costs == null) {
			costs = new Costs(0d, 0d);
			costsByType.put(providerId + "-" + regionId + "-" + hardwareTypeId, costs);
		}
		return costs;
	}

	public void setVariableCosts(String providerId, String regionId, String hardwareTypeId, double variableCosts) {
		String id = providerId + "-" + regionId + "-" + hardwareTypeId;
		Costs costs = costsByType.get(id);
		if (costs == null) {
			costs = new Costs(variableCosts, 0d);
			costsByType.put(id, costs);
		} else {
			costs.setVariableCosts(variableCosts);
		}
	}

	public void setFixedCosts(String providerId, String regionId, String hardwareTypeId, double fixedCosts) {
		String id = providerId + "-" + regionId + "-" + hardwareTypeId;
		Costs costs = costsByType.get(id);
		if (costs == null) {
			costs = new Costs(0d, fixedCosts);
			costsByType.put(id, costs);
		} else {
			costs.setFixedCosts(fixedCosts);
		}
	}

	private static CostsStore instance;

	public static CostsStore getInstance() {
		if (instance == null)
			instance = loadCostsStore();
		return instance;
	}

	private static CostsStore loadCostsStore() {
		CostsStore costsStore = null;

		URL costsResourceUrl = Benchmark.class.getResource(costsFilename);
		FileInputStream costsResourceFile = null;
		try {
			costsResourceFile = new FileInputStream(costsResourceUrl.getFile());
			costsStore = getGson().fromJson(new InputStreamReader(costsResourceFile), CostsStore.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} finally {
			if (costsResourceFile != null)
				try {
					costsResourceFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		if (costsStore == null)
			costsStore = new CostsStore();

		return costsStore;
	}

	public static void dumpCostsStore(CostsStore costsStore) {
		Writer costsResourceWriter = null;
		try {
			costsResourceWriter = new FileWriter("/tmp" + costsFilename);
			getGson().toJson(costsStore, costsResourceWriter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (costsResourceWriter != null)
				try {
					costsResourceWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private static Gson getGson() {
		if (gson == null)
			gson = new GsonBuilder().setPrettyPrinting().create();
		return gson;
	}

	public class Costs implements Serializable {
		private static final long serialVersionUID = -1632289006194114301L;

		/* Variable costs per hour in 1/10 USD cent */
		private double variableCosts;
		/* Fixed costs per month in 1/10 USD cent */
		private double fixedCosts;

		public Costs(double variableCosts, double fixedCosts) {
			this.setVariableCosts(variableCosts);
			this.setFixedCosts(fixedCosts);
		}

		public double getVariableCosts() {
			return variableCosts;
		}

		public void setVariableCosts(double variableCosts) {
			this.variableCosts = variableCosts;
		}

		public double getFixedCosts() {
			return fixedCosts;
		}

		public void setFixedCosts(double fixedCosts) {
			this.fixedCosts = fixedCosts;
		}
	}

}
