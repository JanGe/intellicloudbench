/*
 * This file is part of libIntelliCloudBench.
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

package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

import org.joda.time.Duration;

import edu.kit.aifb.libIntelliCloudBench.logging.Logger;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore;
import edu.kit.aifb.libIntelliCloudBench.model.json.CostsStore.Costs;

public class InstanceState extends Logger implements Serializable {
	private static final long serialVersionUID = 2512010109245300904L;

	public enum State {
		PREPARE, INIT, DEPLOY, DOWNLOAD, RUN, UPLOAD, DONE, ABORTED, WAIT, STOPPED
	}

	private State state = State.WAIT;
	private InstanceType instanceType;
	private int numberOfBenchmarks;

	private Float percentage = 0f;
	private String status = "Waiting...";
	private Float period;

	private Long startTime = null;
	private Duration durationAfterFinish = null;

	public InstanceState(InstanceType instanceType, int numberOfBenchmarks) {
		this.instanceType = instanceType;
		this.numberOfBenchmarks = numberOfBenchmarks;
		this.period = 1f / new Float(numberOfBenchmarks + State.values().length - 6);
	}

	public InstanceType getInstanceType() {
		return instanceType;
	}

	public void setPrepare() {
		String message = "Preparing to initialize machine...";
		set(State.PREPARE, 0f, message);
	}

	public void setStopped() {
		if (startTime != null)
			durationAfterFinish = new Duration(startTime, System.currentTimeMillis());
		String message = "Stopped.";
		set(State.STOPPED, 1f, message);
	}

	public void setDone() {
		durationAfterFinish = new Duration(startTime, System.currentTimeMillis());
		String message = "Done.";
		set(State.DONE, 1f, message);
	}

	public void setUpload(int step, String name) {
		String message = "(" + step + "/" + numberOfBenchmarks + ") Uploading benchmark results for \"" + name + "\"";
		set(State.UPLOAD, (period * 2.8f) + new Float(step) * period, message);
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

	public void set(State state, Float percentage, String status) {
		log(status);

		this.state = state;
		this.percentage = percentage;
		this.status = status;

		forceUpdate();
	}

	public void forceUpdate() {
		this.setChanged();
		this.notifyObservers();
	}

	public State getState() {
		return state;
	}

	public Float getPercentage() {
		return percentage;
	}

	public String getStatus() {
		return status;
	}

	public Duration getRunningDuration() {
		if (startTime == null)
			return new Duration(0);
		if (durationAfterFinish != null)
			return durationAfterFinish;
		return new Duration(startTime, System.currentTimeMillis() + 1);
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
