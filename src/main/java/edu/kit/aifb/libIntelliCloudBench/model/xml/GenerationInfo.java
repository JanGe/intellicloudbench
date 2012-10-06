package edu.kit.aifb.libIntelliCloudBench.model.xml;

import org.simpleframework.xml.Element;

public class GenerationInfo {

	@Element(name="Title", required=false)
	private String title;
	
	@Element(name="LastModified", required=false)
	private String lastModified;
	
	@Element(name="TestClient", required=false)
	private String testClient;
	
	@Element(name="Description", required=false)
	private String description;

	public String getTitle() {
  	return title;
  }

	public String getLastModified() {
  	return lastModified;
  }

	public String getTestClient() {
  	return testClient;
  }

	public String getDescription() {
  	return description;
  }
}
