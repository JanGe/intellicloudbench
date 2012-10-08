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

package edu.kit.aifb.IntelliCloudBench;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.vaadin.artur.icepush.ICEPush;

import com.google.common.collect.Multimap;
import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import edu.kit.aifb.IntelliCloudBench.model.UIState;
import edu.kit.aifb.IntelliCloudBench.model.UIState.Screen;
import edu.kit.aifb.IntelliCloudBench.model.User;
import edu.kit.aifb.IntelliCloudBench.ui.BenchmarkSelectionPanel;
import edu.kit.aifb.IntelliCloudBench.ui.RunningBenchmarksPanel;
import edu.kit.aifb.IntelliCloudBench.ui.ServiceSelectionPanel;
import edu.kit.aifb.IntelliCloudBench.ui.ShowResultsPanel;
import edu.kit.aifb.IntelliCloudBench.ui.UserInfoBox;
import edu.kit.aifb.IntelliCloudBench.ui.model.BenchmarkSelectionModel;
import edu.kit.aifb.IntelliCloudBench.util.IOAuthListener;
import edu.kit.aifb.IntelliCloudBench.util.OAuthHandler;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;

@SuppressWarnings("serial")
public class WelcomeScreen extends Application implements Observer, IOAuthListener {
	private static final String THEME = "custom";

	private Window window =
	    new Window("IntelliCloudBench - Intelligent Cloud Service Benchmarking", new VerticalLayout());
	private ICEPush pusher;

	private OAuthHandler handler;

	private User user;

	private Button nextButton;
	private Button previousButton;

	private Panel panel;

	private ClickListener nextListener = new ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
			saveState();

			UIState uiState = user.getUiState();
			switch (uiState.getCurrentScreen()) {
			case PROVIDERS:
			default:
				Collection<InstanceType> checkedInstanceTypes = ((ServiceSelectionPanel) panel).getCheckedInstanceTypes();
				if (checkedInstanceTypes.size() == 0) {
					showWarningMessage("Please select at least one service.");
					return;
				}
				break;
			case BENCHMARKS:
				int numSelected = uiState.getBenchmarkSelectionModel().getNumberOfSelectedBenchmarks();
				if (numSelected < 1) {
					showWarningMessage("Please select at least one benchmark.");
					return;
				}
				break;
			case RUNS:
				break;
			case RESULTS:
				return;
			}

			uiState.nextScreen();
			updateContent();
		}

		private void showWarningMessage(String message) {
			WelcomeScreen.this.getMainWindow().showNotification(message, Notification.TYPE_WARNING_MESSAGE);
		}

	};

	private ClickListener previousListener = new ClickListener() {

		@Override
		public void buttonClick(ClickEvent event) {
			saveState();

			UIState uiState = user.getUiState();
			switch (uiState.getCurrentScreen()) {
			case RUNS:
				user.getService().terminateAllImmediately();
				((RunningBenchmarksPanel) panel).finish();
				break;
			case PROVIDERS:
			case BENCHMARKS:
			case RESULTS:
			default:
				break;
			}

			uiState.previousScreen();
			updateContent();
		}

	};

	private Embedded logoBox;

	public void saveState() {
		UIState uiState = user.getUiState();
		switch (uiState.getCurrentScreen()) {
		case PROVIDERS:
		default:
			uiState.setCheckedInstanceTypes(((ServiceSelectionPanel) panel).getCheckedInstanceTypes());
			break;
		case RUNS:
			user.getService().terminateAllImmediately();
			((RunningBenchmarksPanel) panel).finish();
			break;
		case BENCHMARKS:
		case RESULTS:
			break;
		}
	}

	@Override
	public void init() {
		setTheme(THEME);

		window.removeAllComponents();
		window.removeAllActionHandlers();

		((VerticalLayout) window.getContent()).setSpacing(true);
		((VerticalLayout) window.getContent()).setMargin(true);

		FileResource logo = new FileResource(new File(Application.class.getResource("/logo.svg").getFile()), this);
		logoBox = new Embedded();
		logoBox.setSource(logo);
		logoBox.setType(Embedded.TYPE_IMAGE);

		/* Logos KIT, AIFB, etc. */
		HorizontalLayout footer = buildFooter();

		if (user != null) {
			pusher = user.getService().getPusher();
			window.addComponent(pusher);

			initContentLayout();
			updateContent();

		} else {
			/* User not logged in */
			initLoginWindow();
		}

		window.addComponent(footer);
		((VerticalLayout) window.getContent()).setComponentAlignment(footer, Alignment.BOTTOM_CENTER);
		setMainWindow(window);
	}

	private HorizontalLayout buildFooter() {
		HorizontalLayout footer = new HorizontalLayout();
		footer.setSpacing(true);

		FileResource eOrg = new FileResource(new File(Application.class.getResource("/eOrg.png").getFile()), this);
		Link eOrgLink = new Link();
		eOrgLink.setIcon(eOrg);
		eOrgLink.setResource(new ExternalResource("http://eorganization.de"));
		eOrgLink.setTargetName("_blank");
		eOrgLink.setStyleName("footer");
		footer.addComponent(eOrgLink);

		FileResource aifb = new FileResource(new File(Application.class.getResource("/aifb.png").getFile()), this);
		Link aifbLink = new Link();
		aifbLink.setIcon(aifb);
		aifbLink.setResource(new ExternalResource("http://aifb.kit.edu"));
		aifbLink.setTargetName("_blank");
		aifbLink.setStyleName("footer");
		footer.addComponent(aifbLink);

		FileResource kit =
		    new FileResource(new File(Application.class.getResource("/kit_logo_en_farbe_positiv.png").getFile()), this);
		Link kitLink = new Link();
		kitLink.setIcon(kit);
		kitLink.setResource(new ExternalResource("http://kit.edu"));
		kitLink.setTargetName("_blank");
		kitLink.setStyleName("footer");
		footer.addComponent(kitLink);

		return footer;
	}

	private void updateContent() {
		Panel oldPanel = panel;
		switch (user.getUiState().getCurrentScreen()) {
		case PROVIDERS:
		default:
			nextButton.setCaption("Next");
			nextButton.setVisible(true);
			previousButton.setVisible(false);
			if (!(panel instanceof ServiceSelectionPanel))
				panel = new ServiceSelectionPanel("Please select the Cloud Services you want to compare...", user);
			break;
		case BENCHMARKS:
			nextButton.setCaption("Start benchmarking");
			nextButton.setVisible(true);
			previousButton.setCaption("Back to Service Selection");
			previousButton.setVisible(true);
			if (!(panel instanceof BenchmarkSelectionPanel)) {
				BenchmarkSelectionModel benchmarkSelectionModel = user.getUiState().getBenchmarkSelectionModel();
				user.getService().setMetricsConfiguration(benchmarkSelectionModel.getMetricsConfiguration());
				panel = new BenchmarkSelectionPanel("Please select the Benchmarks you want to run...", benchmarkSelectionModel);
			}
			break;
		case RUNS:
			nextButton.setVisible(false);
			previousButton.setCaption("Terminate all instances!");
			previousButton.setVisible(true);
			if (!(panel instanceof RunningBenchmarksPanel))
				panel = new RunningBenchmarksPanel("Running Benchmarks...", user);
			user.getService().addObserver(this);
			break;
		case RESULTS:
			nextButton.setVisible(false);
			previousButton.setCaption("Start all over again...");
			previousButton.setVisible(true);
			panel = new ShowResultsPanel("Benchmarking results", user.getService());
			break;
		}
		panel.setWidth("100%");
		window.getContent().replaceComponent(oldPanel, panel);
		user.getUiState().setPanel(panel);
		pusher.push();

		if (user.getUiState().getCurrentScreen() == Screen.RUNS)
			((RunningBenchmarksPanel) panel).initAndStartBenchmarking();
	}

	private void initLoginWindow() {

		/* Logo and Login Button */
		VerticalLayout loginBox = new VerticalLayout();
		loginBox.setSpacing(true);

		logoBox.setWidth("500px");
		loginBox.addComponent(logoBox);
		loginBox.setComponentAlignment(logoBox, Alignment.BOTTOM_CENTER);

		Label spacer = new Label();
		spacer.setHeight("40px");
		loginBox.addComponent(spacer);

		Button loginButton = new Button("Login with Google", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				if (handler == null) {
					handler = new OAuthHandler(WelcomeScreen.this, WelcomeScreen.this.window.getURL().toString());
					WelcomeScreen.this.window.addParameterHandler(handler);
				}

				/* Send user to google to authenticate */
				WelcomeScreen.this.window.open(new ExternalResource(handler.getRedirectUrl()));
			}

		});
		loginButton.setStyleName("big");
		loginButton.setWidth("-1px");
		loginBox.addComponent(loginButton);
		loginBox.setComponentAlignment(loginButton, Alignment.TOP_CENTER);

		window.addComponent(loginBox);
	}

	private void initContentLayout() {

		/* Title and User Info */
		HorizontalLayout header = new HorizontalLayout();
		header.setWidth("100%");
		header.setSpacing(true);
		header.setHeight("-1px");

		logoBox.setHeight("120px");
		header.addComponent(logoBox);

		Component userInfo = new UserInfoBox(user.getName(), user.getPictureUrl());
		header.addComponent(userInfo);
		header.setComponentAlignment(userInfo, Alignment.TOP_RIGHT);

		window.addComponent(header);

		HorizontalLayout buttons = new HorizontalLayout();
		buttons.setWidth("-1px");
		buttons.setHeight("-1px");
		buttons.setSpacing(true);

		previousButton = new Button("Previous");
		previousButton.setStyleName("big");
		previousButton.addListener(previousListener);
		buttons.addComponent(previousButton);
		buttons.setComponentAlignment(previousButton, Alignment.TOP_CENTER);

		nextButton = new Button("Next");
		nextButton.setStyleName("big");
		nextButton.addListener(nextListener);
		buttons.addComponent(nextButton);
		buttons.setComponentAlignment(nextButton, Alignment.TOP_CENTER);

		window.addComponent(buttons);
		((VerticalLayout) window.getContent()).setComponentAlignment(buttons, Alignment.TOP_CENTER);

		// panel = user.getUiState().getPanel();
		// if (panel == null)
		panel = new Panel();
		panel.setWidth("100%");
		window.addComponent(panel);
		((VerticalLayout) window.getContent()).setComponentAlignment(panel, Alignment.TOP_CENTER);

		final HorizontalLayout debugButtons = new HorizontalLayout();

		Button lastResultsButton = new Button("Load last results (does NOT terminate running machines!)");
		lastResultsButton.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				/* Load saved results */
				Map<InstanceType, Multimap<Benchmark, Result>> lastBenchmarkResults = user.loadLastBenchmarkResults();
				if (lastBenchmarkResults != null) {
					user.getService().setBenchmarkResultsForType(lastBenchmarkResults);
					/* Jump to results screen */
					user.getUiState().setScreen(Screen.RESULTS);
					updateContent();
				} else {
					getMainWindow().showNotification("Could not load old benchmark results.", Notification.TYPE_WARNING_MESSAGE);
				}
			}

		});
		debugButtons.addComponent(lastResultsButton);

		Button saveCostsStoreButton = new Button("Dump costs store to disk.");
		saveCostsStoreButton.addListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				CostsStore.dumpCostsStore(CostsStore.getInstance());
				getMainWindow().showNotification("Dumped costs store to /tmp/costs.json", Notification.TYPE_HUMANIZED_MESSAGE);
			}

		});
		debugButtons.addComponent(saveCostsStoreButton);

		window.addComponent(debugButtons);
	}

	@Override
	public void login(User user) {
		window.removeParameterHandler(handler);
		handler = null;

		this.user = user;
		init();
	}

	@Override
	public void logout() {
		// user.getUiState().setPanel(panel);
		saveState();

		window.removeParameterHandler(handler);
		handler = null;

		this.user = null;
		close();
	}

	@Override
	public void setErrorMessage(String message) {
		window.removeParameterHandler(handler);
		handler = null;
		getMainWindow().showNotification("Error on logging in: " + message, Notification.TYPE_ERROR_MESSAGE);
	}

	/* Gets called when all benchmarks are done */
	@Override
	public void update(Observable arg0, Object arg1) {
		/* Store last results permanently */
		user.storeLastBenchmarkResults();

		user.getUiState().nextScreen();
		synchronized (this) {
			updateContent();
		}
	}

	public ICEPush getPusher() {
		return pusher;
	}
}
