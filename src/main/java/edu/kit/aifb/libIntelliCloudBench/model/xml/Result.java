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
