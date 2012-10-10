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
			InstanceProgressComponent progress = new InstanceProgressComponent(this, instanceState, service);
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
