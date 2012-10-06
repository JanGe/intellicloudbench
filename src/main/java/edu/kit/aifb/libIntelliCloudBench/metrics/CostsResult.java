package edu.kit.aifb.libIntelliCloudBench.metrics;

import java.text.DecimalFormat;
import java.util.Locale;

public class CostsResult implements IMetricsResult {
	Double costs;
	
	public CostsResult(Double costs) {
		this.costs = costs;
	}

	@Override
	public String getValueAsString() {
		return DecimalFormat.getInstance(Locale.US).format(costs);
	}

	@Override
	public String getScale() {
		return DecimalFormat.getInstance(Locale.US).getCurrency().getSymbol(Locale.US);
	}

	@Override
	public Double getValue() {
		return costs;
	}

	@Override
	public Proportion getProportion() {
		return Proportion.LIB;
	}

}
