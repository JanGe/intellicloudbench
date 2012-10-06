package edu.kit.aifb.libIntelliCloudBench.model.xml;

import java.io.Serializable;

import org.simpleframework.xml.Element;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsResult;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;

public class Result implements IMetricsResult, Serializable {
  private static final long serialVersionUID = 834515423663887329L;
  
  private Benchmark benchmark = null;

	@Element(name="Identifier", required=false)
	private String id;
	
	@Element(name="Title", required=false)
	private String title;
	
	@Element(name="AppVersion", required=false)
	private String appVersion;
	
	@Element(name="Arguments", required=false)
	private String arguments;
	
	@Element(name="Description", required=false)
	private String description;
	
	@Element(name="Scale", required=false)
	private String scale;
	
	@Element(name="Proportion", required=false)
	private String proportion;
	
	@Element(name="DisplayFormat", required=false)
	private String displayFormat;
	
	@Element(name="Data")
	private ResultData resultData;

	public String getId() {
  	return id;
  }

	public String getTitle() {
  	return title;
  }

	public String getAppVersion() {
  	return appVersion;
  }

	public String getArguments() {
  	return arguments;
  }

	public String getDescription() {
  	return description;
  }

	public String getScale() {
  	return scale;
  }

	public String getProportionAsString() {
  	return proportion;
  }
	
	public Proportion getProportion() {
		if (proportion.equals("LIB"))
			return Proportion.LIB;
		return Proportion.HIB;
	}

	public String getDisplayFormat() {
  	return displayFormat;
  }

	public ResultData getResultData() {
  	return resultData;
  }
	
	public Double getValue() {
		return getResultData().getEntry().getValue();
	}
	
	public String getValueAsString() {
		return getResultData().getEntry().getValueAsString();
	}

	public void setBenchmark(Benchmark benchmark) {
  	this.benchmark = benchmark;
  }
	public Benchmark getBenchmark() {
	  return benchmark;
  }
}
