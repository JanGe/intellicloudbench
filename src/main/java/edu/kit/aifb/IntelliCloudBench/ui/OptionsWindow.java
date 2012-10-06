package edu.kit.aifb.IntelliCloudBench.ui;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;

public class OptionsWindow extends Window {

	private static final long serialVersionUID = -8076849220438969634L;

	private List<ListSelect> selectors = new LinkedList<ListSelect>();

	public OptionsWindow(final Benchmark benchmark) {

		setModal(true);
		setWidth("400px");
		setCaption("Options for " + benchmark.getName());

		center();

		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setMargin(true);
		setContent(layout);

		for (String option : benchmark.getOptions()) {
			List<String> values = benchmark.getValuesForOption(option);
			if (!values.isEmpty()) {
				ListSelect selector = new ListSelect(option, values);
				selector.setNullSelectionAllowed(false);
				
				for (String value : values) {
					if (benchmark.isSelectedValue(option, value)) {
						selector.setValue(value);
					}
				}
				selector.setWidth("100%");
				selectors.add(selector);
				layout.addComponent(selector);
			}
		}

		Button saveButton = new Button("Save");
		saveButton.setStyleName("big");
		saveButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 3866491750016040044L;

			@Override
			public void buttonClick(ClickEvent event) {
				for (ListSelect selector : selectors) {
					String option = selector.getCaption();
					String value = (String) selector.getValue();

					benchmark.setSelectedValue(option, value);
				}
				OptionsWindow.this.close();
			}

		});
		layout.addComponent(saveButton);
		layout.setComponentAlignment(saveButton, Alignment.BOTTOM_RIGHT);
	}

}
