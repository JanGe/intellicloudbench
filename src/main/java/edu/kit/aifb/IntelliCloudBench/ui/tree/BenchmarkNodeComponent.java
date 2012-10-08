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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;

import edu.kit.aifb.IntelliCloudBench.ui.OptionsWindow;
import edu.kit.aifb.IntelliCloudBench.ui.model.BenchmarkSelectionModel;
import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.NotReadyException;

public class BenchmarkNodeComponent extends NodeComponent<Collection<Benchmark>> {
	private static final long serialVersionUID = -7117550318213468517L;

	private Collection<CheckBox> allLeafs = new HashSet<>();
	private Map<CheckBox, IMetricsType> checkBoxForMetricsType = new HashMap<>();

	private Label textLabel;

	private String type;
	private BenchmarkSelectionModel benchmarkSelectionModel;
	private Collection<Benchmark> benchmarks;

	public BenchmarkNodeComponent(String type, BenchmarkSelectionModel benchmarkSelectionModel) {
		super();
		this.type = type;
		this.benchmarkSelectionModel = benchmarkSelectionModel;
		this.benchmarks = benchmarkSelectionModel.getBenchmarksForType(type);

		if (isOpenedOnStartup())
			open(true);

		updateNode();
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
		textLabel.setValue("Unsorted");
		node.addComponent(textLabel);
		node.setExpandRatio(textLabel, 1.0f);
		node.setComponentAlignment(textLabel, new Alignment(48));

		return node;
	}

	private boolean isOpenedOnStartup() {
		return benchmarkSelectionModel.isOneSelectedOfType(type);
	}

	@Override
	void addSubTreeElements(VerticalLayout subTreeLayout) throws NotReadyException {

		HorizontalLayout layout;
		Button optionsButton;

		for (final Benchmark benchmark : benchmarks) {

			layout = new HorizontalLayout();
			layout.setWidth("100%");
			layout.setHeight("-1px");
			layout.setSpacing(true);

			final CheckBox checkBox = new CheckBox(benchmark.getName());
			checkBox.setWidth("200px");
			checkBox.setStyleName("big_checkbox");
			checkBox.setValue(benchmarkSelectionModel.isSelected(benchmark));
			checkBoxForMetricsType.put(checkBox, benchmark);
			layout.addComponent(checkBox);

			Label descriptionLabel = new Label(benchmark.getDescription());
			descriptionLabel.setWidth("100%");
			descriptionLabel.setStyleName("small");
			layout.addComponent(descriptionLabel);
			layout.setExpandRatio(descriptionLabel, 1f);
			
			final Slider weightSlider = buildWeightSlider(benchmark);
			layout.addComponent(weightSlider);
			layout.setExpandRatio(weightSlider, 1f);

			Label avgRunTimeLabel = new Label(benchmark.getAvgRunTime() + " Seconds");
			avgRunTimeLabel.setWidth("100px");
			layout.addComponent(avgRunTimeLabel);

			if (benchmark.hasOptions()) {
				optionsButton = new Button("Options");
				optionsButton.setWidth("100px");
				optionsButton.setStyleName("big");
				optionsButton.addListener(new ClickListener() {

					private static final long serialVersionUID = -6831310704210387358L;

					@Override
					public void buttonClick(ClickEvent event) {
						BenchmarkNodeComponent.this.getApplication().getMainWindow().addWindow(new OptionsWindow(benchmark));
					}

				});
				layout.addComponent(optionsButton);
				layout.setComponentAlignment(optionsButton, Alignment.TOP_RIGHT);
			} else {
				Label spacer = new Label();
				spacer.setWidth("100px");
				layout.addComponent(spacer);
			}
			
			checkBox.addListener(new ValueChangeListener() {
				private static final long serialVersionUID = -4280328711187657772L;
				
				@Override
				public void valueChange(ValueChangeEvent event) {
					Benchmark benchmark = (Benchmark) checkBoxForMetricsType.get(checkBox);
					if ((boolean) checkBox.getValue()) {
						benchmarkSelectionModel.select(benchmark, (Double) weightSlider.getValue());
					} else {
						benchmarkSelectionModel.unselect(benchmark);
					}
				}
				
			});
			
			weightSlider.addListener(new ValueChangeListener() {
				private static final long serialVersionUID = -4280328711187657772L;
				
				@Override
				public void valueChange(ValueChangeEvent event) {
					if ((boolean) checkBox.getValue()) {
						Benchmark benchmark = (Benchmark) checkBoxForMetricsType.get(checkBox);
						benchmarkSelectionModel.select(benchmark, (Double) weightSlider.getValue());
					}
				}
				
			});
			
			subTreeLayout.addComponent(layout);
			allLeafs.add(checkBox);
		}
	}
	
	private Slider buildWeightSlider(final IMetricsType metricsType) {
		Double weight = benchmarkSelectionModel.getWeight(metricsType);
		if (weight == null)
			weight = 100d;

		final Slider weightSlider = new Slider("How strong should this be weighted?");
		weightSlider.setWidth("100%");
		try {
			weightSlider.setValue(weight);
		} catch (ValueOutOfBoundsException e) {
			e.printStackTrace();
		}
		weightSlider.setMin(0);
		weightSlider.setMax(100);
		weightSlider.setLocale(Locale.US);
		weightSlider.setResolution(1);
		weightSlider.setImmediate(true);

		return weightSlider;
	}

	@Override
	void updateNode() {
		if (type != null)
			textLabel.setValue(type);
	}

	@Override
	void onOpened(boolean opened) {
		expandButton.setEnabled(true);
	}

}
