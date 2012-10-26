/*
 * This file is part of IntelliCloudBench.
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

package edu.kit.aifb.IntelliCloudBench.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.ui.Panel;

import edu.kit.aifb.IntelliCloudBench.ui.model.BenchmarkSelectionModel;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.stopping.StoppingConfiguration;

public class UIState implements Serializable {
	private static final long serialVersionUID = 3078144997173911292L;

	public enum Screen {
		PROVIDERS, BENCHMARKS, RUNS, RESULTS
	}

	private Screen currentScreen = Screen.PROVIDERS;

	private List<InstanceType> checkedInstanceTypes = new LinkedList<InstanceType>();
	private MetricsConfiguration metricsConfiguration = new MetricsConfiguration(checkedInstanceTypes);
	private StoppingConfiguration stoppingConfiguration = new StoppingConfiguration();
	private BenchmarkSelectionModel benchmarkSelectionModel = new BenchmarkSelectionModel(
	    metricsConfiguration,
	    stoppingConfiguration);

	private Panel panel;

	public Screen getCurrentScreen() {
		return currentScreen;
	}

	public MetricsConfiguration getMetricsConfiguration() {
		return metricsConfiguration;
	}

	public Screen nextScreen() {
		switch (currentScreen) {
		case PROVIDERS:
		default:
			currentScreen = Screen.BENCHMARKS;
			break;
		case BENCHMARKS:
			currentScreen = Screen.RUNS;
			break;
		case RUNS:
			currentScreen = Screen.RESULTS;
			break;
		case RESULTS:
			break;
		}
		return currentScreen;
	}

	public Screen previousScreen() {
		switch (currentScreen) {
		case PROVIDERS:
		default:
			break;
		case BENCHMARKS:
			currentScreen = Screen.PROVIDERS;
			break;
		case RUNS:
			currentScreen = Screen.BENCHMARKS;
			break;
		case RESULTS:
			currentScreen = Screen.PROVIDERS;
			break;
		}
		return currentScreen;
	}

	public List<InstanceType> getCheckedInstanceTypes() {
		return checkedInstanceTypes;
	}

	public void setCheckedInstanceTypes(List<InstanceType> checkedInstanceTypes) {
		this.checkedInstanceTypes = checkedInstanceTypes;
		benchmarkSelectionModel.setInstanceTypes(checkedInstanceTypes);
	}

	public void setScreen(Screen screen) {
		this.currentScreen = screen;
	}

	public void setPanel(Panel panel) {
		this.panel = panel;
	}

	public Panel getPanel() {
		return panel;
	}

	public BenchmarkSelectionModel getBenchmarkSelectionModel() {
		return benchmarkSelectionModel;
	}

	public void setBenchmarkSelectionModel(BenchmarkSelectionModel benchmarkSelectionModel) {
		this.benchmarkSelectionModel = benchmarkSelectionModel;
	}

}
