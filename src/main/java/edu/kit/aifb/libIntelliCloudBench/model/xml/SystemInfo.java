package edu.kit.aifb.libIntelliCloudBench.model.xml;

import org.simpleframework.xml.Element;

public class SystemInfo {

	@Element(name="Identifier", required=false)
	private String id;
	
	@Element(name="Hardware", required=false)
	private String hardware;
	
	@Element(name="Software", required=false)
	private String software;
	
	@Element(name="User", required=false)
	private String user;
	
	@Element(name="TimeStamp", required=false)
	private String timeStamp;
	
	@Element(name="TestClientVersion", required=false)
	private String testClientVersion;
	
	@Element(name="Notes", required=false)
	private String notes;
	
	@Element(name="JSON", required=false)
	private String json;

	public String getId() {
  	return id;
  }

	public String getHardware() {
  	return hardware;
  }

	public String getSoftware() {
  	return software;
  }

	public String getUser() {
  	return user;
  }

	public String getTimeStamp() {
  	return timeStamp;
  }

	public String getTestClientVersion() {
  	return testClientVersion;
  }

	public String getNotes() {
  	return notes;
  }
	
	public String getJson() {
		return json;
	}
}
