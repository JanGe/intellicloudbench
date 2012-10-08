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

import org.jclouds.compute.ComputeServiceContext;

import com.google.common.collect.Iterators;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import edu.kit.aifb.libIntelliCloudBench.model.HardwareType;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.NotReadyException;
import edu.kit.aifb.libIntelliCloudBench.model.Region;

public class RegionNodeComponent extends NodeComponent<Region> {
	private static final long serialVersionUID = -1085233506266253530L;

	private Label textLabel;
	private ProviderNodeComponent parent;

	private Collection<HardwareTypeLeafComponent> allLeafs = new HashSet<HardwareTypeLeafComponent>();
	private Collection<InstanceType> checked;

	private ComputeServiceContext context;

	private Label langCodeLabel;

	private Label scopeLabel;

	public RegionNodeComponent(ComputeServiceContext context, ProviderNodeComponent parent, BeanItem<Region> model,
	    Collection<InstanceType> checked) {
		super(model);
		this.context = context;
		this.parent = parent;
		this.checked = checked;
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

		textLabel = new Label("Region");
		textLabel.setStyleName("h3");
		textLabel.setImmediate(false);
		textLabel.setWidth("200px");
		node.addComponent(textLabel);
		node.setComponentAlignment(textLabel, Alignment.MIDDLE_LEFT);

		scopeLabel = new Label();
		scopeLabel.setWidth("100px");
		node.addComponent(scopeLabel);
		
		langCodeLabel = new Label();
		langCodeLabel.setWidth("100%");
		node.addComponent(langCodeLabel);
		node.setExpandRatio(langCodeLabel, 1.0f);

		return node;
	}

	@Override
	void onOpened(boolean opened) {
		expandButton.setEnabled(true);
	}

	@Override
	void addSubTreeElements(VerticalLayout subTreeLayout) throws NotReadyException {

		Iterable<HardwareType> allHardwareTypes = parent.getModelBean().getAllHardwareTypes(context, this);
		HardwareTypeLeafComponent name;
		BeanItem<HardwareType> model;
		InstanceType instanceType;

		for (HardwareType hardwareType : allHardwareTypes) {
			model = new BeanItem<HardwareType>(hardwareType);
			instanceType = new InstanceType(parent.getModelBean(), this.model.getBean(), hardwareType);

			name = new HardwareTypeLeafComponent(this, model.getBean());

			if (checked.contains(instanceType))
				name.setChecked(true);

			if (!Iterators.contains(subTreeLayout.getComponentIterator(), name)) {
				subTreeLayout.addComponent(name);
				allLeafs.add(name);
			}
		}
	}

	@Override
	void updateNode() {
		textLabel.setValue(model.getBean().getId());
		langCodeLabel.setValue(model.getBean().getLangCodesAsString());
		scopeLabel.setValue(model.getBean().getScope());
	}

	public Collection<InstanceType> getCheckedInstanceTypes() {
		Collection<InstanceType> allCheckedLeafs = new HashSet<InstanceType>();
		for (HardwareTypeLeafComponent leaf : allLeafs) {
			if (leaf.isChecked()) {
				allCheckedLeafs.add(leaf.getInstanceType());
			}
		}
		return allCheckedLeafs;
	}

	public ProviderNodeComponent getParentNode() {
		return parent;
	}
}
