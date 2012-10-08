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

import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import com.vaadin.data.Property.ConversionException;
import com.vaadin.data.Property.ReadOnlyException;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Slider;
import com.vaadin.ui.Slider.ValueOutOfBoundsException;
import com.vaadin.ui.VerticalLayout;

import de.essendi.vaadin.ui.component.numberfield.NumberField;
import edu.kit.aifb.IntelliCloudBench.ui.model.BenchmarkSelectionModel;
import edu.kit.aifb.IntelliCloudBench.ui.tree.BenchmarkNodeComponent;
import edu.kit.aifb.libIntelliCloudBench.metrics.IInstanceOrderer;
import edu.kit.aifb.libIntelliCloudBench.metrics.MetricsConfiguration;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class BenchmarkSelectionPanel extends Panel {
	private static final long serialVersionUID = 7884854283154917934L;

	private VerticalLayout content;

	private BenchmarkSelectionModel benchmarkSelectionModel;

	private VerticalLayout referenceSelectionLayout;

	public BenchmarkSelectionPanel(String caption, BenchmarkSelectionModel benchmarkSelectionModel) {
		super(caption);
		this.benchmarkSelectionModel = benchmarkSelectionModel;

		content = ((VerticalLayout) getContent());
		content.setSpacing(true);

		VerticalLayout metricsLayout = buildMetricsLayout();
		metricsLayout.setStyleName("grey_background");
		content.addComponent(metricsLayout);

		/* Provider tree */
		BenchmarkNodeComponent branch;
		for (String type : benchmarkSelectionModel.getTypes()) {
			branch = new BenchmarkNodeComponent(type, benchmarkSelectionModel);

			branch.setWidth("100%");
			addComponent(branch);
			content.setComponentAlignment(branch, Alignment.TOP_CENTER);
		}
	}

	private VerticalLayout buildMetricsLayout() {
		final VerticalLayout metricsLayout = new VerticalLayout();
		metricsLayout.setWidth("100%");
		metricsLayout.setSpacing(true);
		metricsLayout.setMargin(true);
		metricsLayout.setStyleName("grey_background");

		HorizontalLayout metricsSelectionLayout = buildMetricsSelectionLayout();
		metricsLayout.addComponent(metricsSelectionLayout);

		return metricsLayout;
	}

	private HorizontalLayout buildMetricsSelectionLayout() {
		HorizontalLayout metricsSelectionLayout = new HorizontalLayout();
		metricsSelectionLayout.setWidth("100%");
		metricsSelectionLayout.setSpacing(true);
		metricsSelectionLayout.setMargin(true);

		final VerticalLayout metricsSelectorLayout = new VerticalLayout();

		final OptionGroup metricsSelector = new OptionGroup("Please select how benchmark results should be compared:");
		for (Class<? extends IInstanceOrderer> instanceOrderer : MetricsConfiguration.INSTANCE_ORDERER) {
			int index = MetricsConfiguration.INSTANCE_ORDERER.indexOf(instanceOrderer);
			metricsSelector.addItem(instanceOrderer);
			metricsSelector.setItemCaption(instanceOrderer, MetricsConfiguration.INSTANCE_ORDERER_NAMES.get(index));
		}
		metricsSelector.setNullSelectionAllowed(false);
		metricsSelector.select(benchmarkSelectionModel.getSelectedInstancesOrderer());
		metricsSelector.setImmediate(true);
		metricsSelectorLayout.addComponent(metricsSelector);

		referenceSelectionLayout = new VerticalLayout();
		if (requiresReference(benchmarkSelectionModel.getSelectedInstancesOrderer())) {
			NativeSelect referenceSelector = buildReferenceSelector(benchmarkSelectionModel.getInstancesTypes());
			referenceSelectionLayout.addComponent(referenceSelector);
		}
		metricsSelectorLayout.addComponent(referenceSelectionLayout);

		metricsSelector.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6167753078722196865L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				benchmarkSelectionModel.setSelectedInstanceOrderer((Class<? extends IInstanceOrderer>) metricsSelector
				    .getValue());

				VerticalLayout oldReferenceSelectionLayout = referenceSelectionLayout;
				referenceSelectionLayout = new VerticalLayout();
				if (requiresReference(benchmarkSelectionModel.getSelectedInstancesOrderer())) {
					NativeSelect referenceSelector = buildReferenceSelector(benchmarkSelectionModel.getInstancesTypes());
					referenceSelectionLayout.addComponent(referenceSelector);
				}
				metricsSelectorLayout.replaceComponent(oldReferenceSelectionLayout, referenceSelectionLayout);
			}

		});

		metricsSelectionLayout.addComponent(metricsSelectorLayout);

		VerticalLayout costsMetricsLayout = new VerticalLayout();

		final CheckBox costsCheckbox = new CheckBox("Include monthly costs", benchmarkSelectionModel.isCostsSelected());
		costsCheckbox.setImmediate(true);
		costsMetricsLayout.addComponent(costsCheckbox);

		final Slider costsWeightSlider = buildCostsWeightSlider();
		costsMetricsLayout.addComponent(costsWeightSlider);

		costsCheckbox.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = -2462873118115972047L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((boolean) costsCheckbox.getValue()) {
					benchmarkSelectionModel.selectCosts((Double) costsWeightSlider.getValue());
				} else {
					benchmarkSelectionModel.unselectCosts();
				}
			}

		});

		costsWeightSlider.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = -4202721080880004598L;

			public void valueChange(ValueChangeEvent event) {
				if ((boolean) costsCheckbox.getValue())
					benchmarkSelectionModel.selectCosts((Double) costsWeightSlider.getValue());
			}
		});

		final CheckBox budgetCheckbox = new CheckBox("Stop before hitting hard budget limit ($)");
		budgetCheckbox.setImmediate(true);
		budgetCheckbox.setValue(benchmarkSelectionModel.isCostsBudgetSelected());
		costsMetricsLayout.addComponent(budgetCheckbox);

		final NumberField budgetField = new NumberField();
		budgetField.setImmediate(true);
		budgetField.setDecimalAllowed(true);
		budgetField.setDecimalPrecision(2);
		budgetField.setDecimalSeparator(DecimalFormatSymbols.getInstance(Locale.US).getDecimalSeparator());
		budgetField.setDecimalSeparatorAlwaysShown(true);
		budgetField.setMinimumFractionDigits(2);
		budgetField.setNegativeAllowed(false);
		budgetField.setValue(benchmarkSelectionModel.getCostsBudget());
		budgetField.setTextChangeEventMode(TextChangeEventMode.LAZY);
		costsMetricsLayout.addComponent(budgetField);
		
		budgetCheckbox.addListener(new ValueChangeListener() {
      private static final long serialVersionUID = -4106157808026234149L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((boolean) budgetCheckbox.getValue()) {
					benchmarkSelectionModel.setCostsBudgetSelected(true);
					benchmarkSelectionModel.setCostsBudget(budgetField.getDoubleValueDoNotThrow());
				} else {
					benchmarkSelectionModel.setCostsBudgetSelected(false);
				}
			}

		});
		
		budgetField.addListener(new ValueChangeListener() {
      private static final long serialVersionUID = 2728447996398268613L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				if ((boolean) budgetCheckbox.getValue())
					benchmarkSelectionModel.setCostsBudget(budgetField.getDoubleValueDoNotThrow());
			}

		});

		metricsSelectionLayout.addComponent(costsMetricsLayout);
		return metricsSelectionLayout;
	}

	private boolean requiresReference(Class<? extends IInstanceOrderer> instanceOrderer) {
		boolean requiresReference = false;
		try {
			requiresReference = instanceOrderer.getField("REQUIRES_REFERENCE").getBoolean(null);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return requiresReference;
	}

	private Slider buildCostsWeightSlider() {
		Double weight = benchmarkSelectionModel.getCostsWeight();
		if (weight == null)
			weight = 100d;

		final Slider weightSlider = new Slider("How strong should the monthly costs be weighted?");
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

	private NativeSelect buildReferenceSelector(List<InstanceType> instanceTypes) throws UnsupportedOperationException,
	    ReadOnlyException, ConversionException {
		final NativeSelect referenceSelector = new NativeSelect("Please select the reference instance:");
		for (InstanceType instanceType : instanceTypes) {
			referenceSelector.addItem(instanceType);
			referenceSelector.setItemCaption(instanceType, instanceType.asString(" | "));
		}

		InstanceType selectedReference = benchmarkSelectionModel.getSelectedReference();
		if (selectedReference == null) {
			selectedReference = instanceTypes.get(0);
			benchmarkSelectionModel.setSelectedReference(selectedReference);
		}

		referenceSelector.setValue(selectedReference);
		referenceSelector.setNullSelectionAllowed(false);
		referenceSelector.setImmediate(true);
		referenceSelector.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = -2462873118115972047L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				InstanceType instanceType = (InstanceType) referenceSelector.getValue();
				benchmarkSelectionModel.setSelectedReference(instanceType);
			}

		});
		return referenceSelector;
	}
}
