package edu.kit.aifb.libIntelliCloudBench.model.xml;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="PhoronixTestSuite")
public class BenchmarkResult {

	@Element(name="Generated")
	private GenerationInfo generationInfo;
	
	@ElementList(entry="System", inline=true)
	private List<SystemInfo> systemInfos;
	
	@ElementList(entry="Result", inline=true)
	private List<Result> results = new ArrayList<Result>();
	
	public GenerationInfo getGenerationInfo() {
		return generationInfo;
	}
	
	public List<SystemInfo> getSystemInfos() {
		return systemInfos;
	}
	
	public List<Result> getResults() {
		return results;
	}
}
