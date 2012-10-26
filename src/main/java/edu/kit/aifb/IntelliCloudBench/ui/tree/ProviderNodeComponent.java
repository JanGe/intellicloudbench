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

package edu.kit.aifb.IntelliCloudBench.ui.tree;

import java.util.Collection;
import java.util.HashSet;

import com.google.common.collect.Iterators;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

import edu.kit.aifb.IntelliCloudBench.ui.CredentialsWindow;
import edu.kit.aifb.libIntelliCloudBench.CloudBenchService;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.NotReadyException;
import edu.kit.aifb.libIntelliCloudBench.model.Provider;
import edu.kit.aifb.libIntelliCloudBench.model.Region;

public class ProviderNodeComponent extends NodeComponent<Provider> {
	private static final long serialVersionUID = -1085233506266253530L;

	private Button optionsButton;

	private Label textLabel;

	private Collection<RegionNodeComponent> allBranches = new HashSet<RegionNodeComponent>();
	private Collection<InstanceType> checked;

	private CloudBenchService service;

	public ProviderNodeComponent(CloudBenchService service, BeanItem<Provider> model, Collection<InstanceType> checked) {
		super(model);
		this.service = service;
		this.checked = checked;

		optionsButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 8497238811727702919L;

			@Override
			public void buttonClick(ClickEvent event) {
				BeanItem<Provider> model = ProviderNodeComponent.this.model;
				Window window = new CredentialsWindow(model.getBean(), model.getBean().getCredentials());
				window.addListener(new CloseListener() {
					private static final long serialVersionUID = -2858835926678578605L;

					@Override
					public void windowClose(CloseEvent e) {
						updateSubTree();
					}

				});
				ProviderNodeComponent.this.getApplication().getMainWindow().addWindow(window);
			}

		});
	}

	@Override
	HorizontalLayout buildNode() {
		// common part: create layout
		HorizontalLayout node = new HorizontalLayout();
		node.setImmediate(false);
		node.setWidth("100.0%");
		node.setHeight("-1px");
		node.setMargin(false);
		node.setSpacing(true);

		textLabel = new Label();
		textLabel.setStyleName("h3");
		textLabel.setImmediate(false);
		textLabel.setWidth("100.0%");
		textLabel.setHeight("-1px");
		textLabel.setValue("Provider");
		node.addComponent(textLabel);
		node.setExpandRatio(textLabel, 1.0f);
		node.setComponentAlignment(textLabel, new Alignment(48));

		// optionsButton
		optionsButton = new Button();
		optionsButton.setStyleName("big");
		optionsButton.setCaption("Enter Credentials...");
		optionsButton.setImmediate(true);
		optionsButton.setWidth("-1px");
		optionsButton.setHeight("-1px");
		node.addComponent(optionsButton);
		node.setComponentAlignment(optionsButton, new Alignment(34));

		return node;
	}

	@Override
	void onOpened(boolean opened) {
		if (opened) {
			optionsButton.setEnabled(false);
			expandButton.setEnabled(true);
		} else {
			optionsButton.setEnabled(true);
			if (model.getBean().areCredentialsSetup()) {
				expandButton.setEnabled(true);
			} else {
				expandButton.setEnabled(false);
			}
		}
	}

	@Override
	void addSubTreeElements(VerticalLayout subTreeLayout) throws NotReadyException {

		Iterable<Region> allRegions = model.getBean().getAllRegions(service.getContext(this.model.getBean()), this);
		RegionNodeComponent regionNode;
		BeanItem<Region> model;

		synchronized (allRegions) {
			for (Region region : allRegions) {
				model = new BeanItem<Region>(region);

				regionNode = new RegionNodeComponent(service.getContext(this.model.getBean()), this, model, checked);
				model.addListener(regionNode);

				if (!Iterators.contains(subTreeLayout.getComponentIterator(), regionNode)) {
					subTreeLayout.addComponent(regionNode);
					allBranches.add(regionNode);
				}
			}
		}
	}

	@Override
	void updateNode() {
		textLabel.setValue(model.getBean().getName());
	}

	public Collection<InstanceType> getCheckedInstanceTypes() {
		Collection<InstanceType> allCheckedLeafs = new HashSet<InstanceType>();
		for (RegionNodeComponent regionNode : allBranches) {
			allCheckedLeafs.addAll(regionNode.getCheckedInstanceTypes());
		}
		return allCheckedLeafs;
	}
}
