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
		int numWait = 0;
		int numPrepare = 0;
		int numInit = 0;
		int numDeploy = 0;
		int numDownload = 0;
		int numRun = 0;
		int numUpload = 0;
		int numDone = 0;
		int numAborted = 0;
		int numStopped = 0;

		for (Runner runner : runners) {
			switch (runner.getInstanceState().getState()) {
			default:
			case WAIT:
				numWait++;
				break;
			case PREPARE:
				numPrepare++;
				break;
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
			case STOPPED:
				numStopped++;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		if (numWait > 0)
			sb.append(numWait + " machines waiting");

		if (numPrepare > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numPrepare + " preparing initialization");
		}
		
		if (numInit > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numInit + " initializing machines");
		}

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
		
		if (numStopped > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numStopped + " were stopped");
		}

		if (numAborted > 0) {
			if (sb.length() > 0)
				sb.append(" | ");
			sb.append(numAborted + " did abort");
		}

		return sb.toString();
	}
}