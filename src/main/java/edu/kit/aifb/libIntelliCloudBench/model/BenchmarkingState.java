package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;
import java.util.Collection;

import edu.kit.aifb.libIntelliCloudBench.background.Runner;

public class BenchmarkingState implements Serializable {
	private static final long serialVersionUID = -410794580440304778L;

	private Collection<Runner> runners;

	public BenchmarkingState(Collection<Runner> runners) {
		this.runners = runners;
	}

	public float getGlobalProgress() {
		float progress = 0;

		for (Runner runner : runners) {
			progress += runner.getInstanceState().getPercentage();
		}
		progress /= runners.size();

		return progress;
	}

	public String getGlobalStatus() {
		int numInit = 0;
		int numDeploy = 0;
		int numDownload = 0;
		int numRun = 0;
		int numUpload = 0;
		int numDone = 0;
		int numAborted = 0;

		for (Runner runner : runners) {
			switch (runner.getInstanceState().getState()) {
			default:
			case INIT:
				numInit++;
				break;
			case DEPLOY:
				numDeploy++;
				break;
			case DOWNLOAD:
				numDownload++;
				break;
			case RUN:
				numRun++;
				break;
			case UPLOAD:
				numUpload++;
				break;
			case DONE:
				numDone++;
				break;
			case ABORTED:
				numAborted++;
				break;
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if (numInit > 0)
			sb.append(numInit + " initializing machines");
		
		if (numDeploy > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numDeploy + " deploying benchmark suite");
		}
		
		if (numDownload > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numDownload + " downloading benchmarks");
		}
		
		if (numRun > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numRun + " running benchmarks");
		}
		
		if (numUpload > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numUpload + " uploading benchmark results");
		}
		
		if (numDone > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numDone + " are done");
		}
		
		if (numAborted > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numAborted + " did abort");
		}
		
		return sb.toString();
	}
}