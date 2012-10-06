package edu.kit.aifb.libIntelliCloudBench.model.xml;

import java.io.Serializable;

import org.simpleframework.xml.Element;

public class Entry implements Serializable {
  private static final long serialVersionUID = 8209846337291519875L;

	@Element(name="Identifier", required=false)
	private String id;
	
	@Element(name="Value")
	private String value;
	
	@Element(name="RawString", required=false)
	private String rawString;
	
	@Element(name="JSON", required=false)
	private String json;

	public String getId() {
  	return id;
  }

	public String getValueAsString() {
  	return value;
  }
	
	public Double getValue() {
		return Double.parseDouble(value);
	}

	public String getRawString() {
  	return rawString;
  }

	public String getJson() {
		return json;
	}
}
