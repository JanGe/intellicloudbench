package edu.kit.aifb.IntelliCloudBench.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import edu.kit.aifb.IntelliCloudBench.WelcomeScreen;
import edu.kit.aifb.libIntelliCloudBench.logging.ILogListener;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceState;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class LogWindow extends Window implements ILogListener {
	private static final long serialVersionUID = -5700883676747080306L;

	private TextField logField;

	private StringBuilder sb = new StringBuilder();

	public LogWindow(final InstanceState instanceState) {

		InstanceType instanceType = instanceState.getInstanceType();

		setWidth("750px");
		setHeight("500px");
		setCaption("Log for " + instanceType.getProvider().getName() + ", " + instanceType.getRegion().getId() + ", "
		    + instanceType.getHardwareType().getId());
		center();

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setSpacing(true);
		layout.setMargin(true);
		setContent(layout);

		logField = new TextField();
		logField.setSizeFull();
		logField.setImmediate(true);
		layout.addComponent(logField);
		layout.setExpandRatio(logField, 1.0f);

		Button close = new Button("Close");
		close.setWidth("-1px");
		close.setHeight("-1px");
		close.setStyleName("big");
		close.addListener(new ClickListener() {
			private static final long serialVersionUID = 5356614540664446119L;

			@Override
			public void buttonClick(ClickEvent event) {
				instanceState.unregisterListener(LogWindow.this);
				close();
			}

		});
		layout.addComponent(close);
		layout.setComponentAlignment(close, Alignment.BOTTOM_RIGHT);

		instanceState.registerListener(this);
		sb.append(instanceState.getLog());
		String log = sb.toString();
		logField.setValue(log);
		logField.setCursorPosition(log.length() - 1);
		logField.setReadOnly(true);
	}

	@Override
	public void updateLog(String newLine) {
		sb.append("\n");
		sb.append(newLine);
		String log = sb.toString();

		logField.setReadOnly(false);
		logField.setValue(log);
		logField.setReadOnly(true);
		logField.setCursorPosition(log.length() - 1);

		((WelcomeScreen) getApplication()).getPusher().push();
	}

}
