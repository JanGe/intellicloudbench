package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

import org.joda.time.Duration;

import edu.kit.aifb.libIntelliCloudBench.logging.Logger;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore.Costs;

public class InstanceState extends Logger implements Serializable {
	private static final long serialVersionUID = 2512010109245300904L;

	public enum State {
		INIT, DEPLOY, DOWNLOAD, RUN, UPLOAD, DONE, ABORTED
	}

	private State state = State.INIT;
	private InstanceType instanceType;
	private int numberOfBenchmarks;

	private Float percentage = 0f;
	private String status = "Preparing to initialize machine...";
	private Float period;

	private Long startTime = null;
	private Duration durationAfterFinish = null;

	public InstanceState(InstanceType instanceType, int numberOfBenchmarks) {
		this.instanceType = instanceType;
		this.numberOfBenchmarks = numberOfBenchmarks;
		this.period = 1f / new Float(numberOfBenchmarks + State.values().length - 3);
	}

	public InstanceType getInstanceType() {
		return instanceType;
	}

	public void setDone() {
		durationAfterFinish = new Duration(startTime, System.currentTimeMillis());
		String message = "Done.";
		set(State.DONE, 1f, message);
	}

	public void setUpload() {
		String message = "Uploading benchmark results...";
		set(State.UPLOAD, (period * (new Float(numberOfBenchmarks + 3))), message);
	}

	public void setRun(int step, String name) {
		String message =
		    "(" + step + "/" + numberOfBenchmarks + ") Running bechmark \"" + name + "\". This could take a while...";
		set(State.RUN, (period * 2f) + new Float(step) * period, message);
	}

	public void setDownload(int step, String name) {
		String message = "(" + step + "/" + numberOfBenchmarks + ") Downloading bechmark \"" + name + "\"...";
		set(State.DOWNLOAD, period + (new Float(step) / new Float(numberOfBenchmarks)) * period, message);
	}

	public void setDeploy() {
		String message = "Deploying benchmark suite...";
		set(State.DEPLOY, period, message);
	}

	public void setInit() {
		String message = "Initializing machine...";
		startTime = System.currentTimeMillis();
		set(State.INIT, period / 2f, message);
	}

	public void setAborted() {
		durationAfterFinish = new Duration(startTime, System.currentTimeMillis());
		String message = "Aborted by user.";
		set(State.ABORTED, 1f, message);
	}

	public void setAborted(Exception e) {
		durationAfterFinish = new Duration(startTime, System.currentTimeMillis());
		StringBuilder sb = new StringBuilder();
		sb.append("Aborted by ");
		sb.append(e.getClass().getSimpleName());
		sb.append(": ");
		sb.append(e.getMessage());
		sb.append("\n");
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		set(State.ABORTED, 1f, sb.toString());
	}

	private void set(State state, Float percentage, String status) {
		log(status);
		setState(state);
		setPercentage(percentage);
		setStatus(status);
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.setChanged();
		this.state = state;
		this.notifyObservers();
	}

	public Float getPercentage() {
		return percentage;
	}

	public void setPercentage(Float percentage) {
		this.setChanged();
		this.percentage = percentage;
		this.notifyObservers();
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.setChanged();
		this.status = status;
		this.notifyObservers();
	}

	private Duration getRunningDuration() {
		if (startTime == null)
			return new Duration(0);
		if (durationAfterFinish != null)
			return durationAfterFinish;
		return new Duration(startTime, System.currentTimeMillis());
	}

	public double getEstimatedCosts() {
		Costs costs =
		    CostsStore.getInstance().getCosts(
		        getInstanceType().getProvider().getId(),
		        getInstanceType().getRegion().getId(),
		        getInstanceType().getHardwareType().getId());
		Duration duration = getRunningDuration();
		
		long paidHours = 0;
		if (duration.getMillis() > 0)
			paidHours = getRunningDuration().getStandardHours() + 1;
		
		return costs.getFixedCosts() + paidHours * costs.getVariableCosts();
	}

}
