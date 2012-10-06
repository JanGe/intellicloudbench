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
import edu.kit.aifb.IntelliCloudBench.ui.model.BenchmarkSelectionModel;
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
