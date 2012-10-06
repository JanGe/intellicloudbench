package edu.kit.aifb.libIntelliCloudBench.model.xml;

import java.io.Serializable;

import org.simpleframework.xml.Element;

public class ResultData implements Serializable {
  private static final long serialVersionUID = -7009280259919920518L;

  @Element(name="Entry")
	private Entry entry;

	public Entry getEntry() {
	  return entry;
  }
}
