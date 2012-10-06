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
	public String getName() {
		return "Monthly costs";
	}

	public Double getCostsForMonthsRunning(InstanceType instanceType, int months) {
		Costs costs =
		    getCosts(instanceType.getProvider().getId(), instanceType.getRegion().getId(), instanceType.getHardwareType()
		        .getId());
		return costs.getFixedCosts() + months * 30 * 24 * costs.getVariableCosts();
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
