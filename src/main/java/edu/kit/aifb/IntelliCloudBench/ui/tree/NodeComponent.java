package edu.kit.aifb.IntelliCloudBench.ui.tree;

import java.util.Observable;
import java.util.Observer;

import com.vaadin.data.Item.PropertySetChangeEvent;
import com.vaadin.data.Item.PropertySetChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.VerticalLayout;

import edu.kit.aifb.libIntelliCloudBench.model.NotReadyException;

public abstract class NodeComponent<T> extends CustomComponent implements PropertySetChangeListener,
    Button.ClickListener, Observer {
	private static final long serialVersionUID = -1085233506266253530L;

	private VerticalLayout mainLayout;

	private HorizontalLayout node;

	protected Button expandButton;
	private boolean isOpened = false;

	private HorizontalLayout subTree;
	private VerticalLayout subTreeLayout;
	private ProgressIndicator spinner;
	private Label spacer;

	protected BeanItem<T> model;

	protected NodeComponent(BeanItem<T> model) {
		this.model = model;

		buildMainLayout();
		setCompositionRoot(mainLayout);

		expandButton.addListener(this);

		update();
	}

	protected NodeComponent() {
		this(null);
	}

	final private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setStyleName("darker");
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("-1px");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("-1px");

		// node
		node = buildNode();

		// expandButton
		expandButton = new Button();
		expandButton.setStyleName("big");
		expandButton.setCaption("+");
		expandButton.setEnabled(false);
		expandButton.setImmediate(true);
		expandButton.setWidth("-1px");
		expandButton.setHeight("-1px");
		node.addComponentAsFirst(expandButton);
		node.setComponentAlignment(expandButton, new Alignment(33));

		mainLayout.addComponent(node);
		mainLayout.setComponentAlignment(node, new Alignment(20));

		return mainLayout;
	}

	final private HorizontalLayout buildSubTree() {
		// common part: create layout
		subTree = new HorizontalLayout();
		subTree.setImmediate(false);
		subTree.setWidth("100.0%");
		subTree.setHeight("-1px");
		subTree.setMargin(false);
		subTree.setSpacing(true);

		// spacer
		spacer = new Label();
		spacer.setImmediate(false);
		spacer.setWidth("40px");
		spacer.setHeight("100.0%");
		subTree.addComponent(spacer);

		// subTreeLayout
		buildSubTreeLayout();
		subTree.addComponent(subTreeLayout);
		subTree.setExpandRatio(subTreeLayout, 1.0f);

		return subTree;
	}

	final private VerticalLayout buildSubTreeLayout() {
		// common part: create layout
		subTreeLayout = new VerticalLayout();
		subTreeLayout.setImmediate(false);
		subTreeLayout.setWidth("100.0%");
		subTreeLayout.setHeight("-1px");
		subTreeLayout.setMargin(false);

		// spinner
		spinner = new ProgressIndicator();
		spinner.setIndeterminate(true);
		spinner.setImmediate(false);
		spinner.setWidth("-1px");
		spinner.setHeight("-1px");
		spinner.setVisible(false);
		subTreeLayout.addComponent(spinner);
		subTreeLayout.setComponentAlignment(spinner, new Alignment(20));

		return subTreeLayout;
	}

	@Override
	final public void buttonClick(ClickEvent event) {
		if (event.getButton() == expandButton) {
			if (isOpened == false) {
				isOpened = true;
			} else {
				isOpened = false;
			}
			updateSubTree();
		}
	}

	@Override
	final public void itemPropertySetChange(PropertySetChangeEvent event) {
		if (model != null)
			model = (BeanItem<T>) event.getItem();
		update();
	}

	final private void update() {
		updateNode();
		updateSubTree();
	}

	@Override
	final public void update(Observable observable, Object object) {
		update();
	}

	final void updateSubTree() {
		if (isOpened) {

			if (subTree == null) {
				// subTree
				buildSubTree();
				mainLayout.addComponent(subTree);
			}

			try {
				addSubTreeElements(subTreeLayout);
				spinner.setVisible(false);
			} catch (NotReadyException e) {
				spinner.setVisible(true);
			}

			expandButton.setCaption("-");
			onOpened(true);

		} else {
			try {
				if (subTree != null) {
					mainLayout.removeComponent(subTree);
					subTree = null;
				}
			} finally {
				onOpened(false);
				expandButton.setCaption("+");
			}
		}
	}

	final public T getModelBean() {
		return model.getBean();
	}

	HorizontalLayout buildNode() {
		return new HorizontalLayout();
	}

	abstract void addSubTreeElements(VerticalLayout subTreeLayout) throws NotReadyException;

	abstract void onOpened(boolean opened);

	abstract void updateNode();
	
	public void open(boolean isOpened) {
		this.isOpened = isOpened;
		update();
	}

}