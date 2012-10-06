package edu.kit.aifb.IntelliCloudBench.ui;

import java.util.Collection;
import java.util.LinkedList;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

import edu.kit.aifb.IntelliCloudBench.model.User;
import edu.kit.aifb.libIntelliCloudBench.CloudBenchService;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.BenchmarkingState;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceState;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class RunningBenchmarksPanel extends Panel {
	private static final long serialVersionUID = -6990586976220174976L;

	private VerticalLayout content;

	private CloudBenchService service;
	private BenchmarkingState benchmarkingState;

	private ProgressIndicator globalProgress;
	private Label globalStatus;
	
	private Collection<InstanceProgressComponent> allInstanceProgressComponents = new LinkedList<InstanceProgressComponent>();

	private Collection<InstanceType> instances;

	public RunningBenchmarksPanel(String caption, User user) {
		super();
		this.service = user.getService();
		service.setName("IntelliCloudBench-" + user.getName().replaceAll("[^\\da-zA-Z]", ""));
		
		instances = user.getUiState().getCheckedInstanceTypes();

		setCaption(caption);
		content = ((VerticalLayout) getContent());
		content.setSpacing(true);

		globalProgress = new ProgressIndicator();
		globalProgress.setWidth("100%");
		content.addComponent(globalProgress);
		content.setComponentAlignment(globalProgress, Alignment.MIDDLE_CENTER);

		globalStatus = new Label();
		globalStatus.setWidth("100%");
		content.addComponent(globalStatus);
		content.setComponentAlignment(globalStatus, Alignment.MIDDLE_CENTER);
	}

	public void initAndStartBenchmarking() {
		service.prepareBenchmarking(instances);
		
		benchmarkingState = service.getBenchmarkingState();

		for (InstanceState instanceState : service.getAllInstanceStates()) {
			InstanceProgressComponent progress = new InstanceProgressComponent(this, instanceState);
			allInstanceProgressComponents.add(progress);
			content.addComponent(progress);
			service.getPusher().push();
		}
		
		service.startBenchmarking();
  }

	public void update() {
		float progress = benchmarkingState.getGlobalProgress();
		String status = benchmarkingState.getGlobalStatus();

		globalProgress.setValue(progress);
		globalStatus.setValue(status);
		
		service.getPusher().push();
	}
	
	public void finish() {
		for (InstanceProgressComponent progress : allInstanceProgressComponents) {
			progress.finish();
		}
	}

}
