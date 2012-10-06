package edu.kit.aifb.IntelliCloudBench.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.ui.Panel;

import edu.kit.aifb.IntelliCloudBench.ui.model.BenchmarkSelectionModel;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class UIState implements Serializable {
  private static final long serialVersionUID = 3078144997173911292L;

	public enum Screen {
		PROVIDERS, BENCHMARKS, RUNS, RESULTS
	}
	
	private Screen currentScreen = Screen.PROVIDERS;
	
	private List<InstanceType> checkedInstanceTypes = new LinkedList<InstanceType>();
	private MetricsConfiguration metricsConfiguration = new MetricsConfiguration();
	private BenchmarkSelectionModel benchmarkSelectionModel = new BenchmarkSelectionModel(metricsConfiguration);

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
