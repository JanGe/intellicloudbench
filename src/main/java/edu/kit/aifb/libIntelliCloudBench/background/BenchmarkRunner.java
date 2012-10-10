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

package edu.kit.aifb.libIntelliCloudBench.background;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jclouds.compute.ComputeService;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.io.payloads.FilePayload;
import org.jclouds.ssh.SshClient;
import org.jclouds.ssh.SshException;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;

import edu.kit.aifb.libIntelliCloudBench.CloudBenchService;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;
import edu.kit.aifb.libIntelliCloudBench.model.xml.BenchmarkResult;
import edu.kit.aifb.libIntelliCloudBench.model.xml.Result;
import edu.kit.aifb.libIntelliCloudBench.util.NodeHelper;

public class BenchmarkRunner extends Runner {

	private static final OsFamily TEMPLATE_OS = OsFamily.UBUNTU;
	private static final String TEMPLATE_OS_VERSION = "12.04";

	private static final String PTS_DIR_NAME = "phoronix-test-suite";
	private static final String PTS_DEPLOYMENT_PATH = "/opt";
	private static final String PTS_EXECUTABLE = "phoronix-test-suite";
	private static final String PTS_CONFIG_DIR_NAME = ".phoronix-test-suite";
	/*
	 * As some PTS Benchmarks only work with superuser privileges, we for now run
	 * PTS always with sudo so it doesn't ask us for the password
	 */
	private static final String PTS_CONFIG_DEPLOYMENT_PATH = "/root";
	private static final String PTS_RESULTS_DIR = PTS_CONFIG_DEPLOYMENT_PATH + "/" + PTS_CONFIG_DIR_NAME
	    + "/test-results";

	private static final int WAIT_TIME_FOR_PROVIDER_AFTER_STARTUP = 30 * 1000;

	private static final String[] INIT_PACKAGE_INSTALLATION_COMMANDS = {
	/* Set session to never be interactive */
	"export DEBIAN_FRONTEND=noninteractive",
	/* Enable all repositories */
	"sudo sed -i -e \"s/# deb/deb/g\" /etc/apt/sources.list",
	/* Update the apt sources as the image couldn't be up-to-date */
	"sudo apt-get -q -y update" };

	private static final String[] SETUP_PHP_COMMANDS = {
	/* Install PHP, as it is required for Phoronix Test Suite */
	"sudo apt-get -q -y install php5-cli" };

	private static final String[] DEPLOY_PTS_COMMANDS = {
	    "sudo mkdir " + PTS_DEPLOYMENT_PATH,
	    "sudo mkdir " + PTS_CONFIG_DEPLOYMENT_PATH,
	    "sudo tar xvfz /tmp/" + PTS_DIR_NAME + ".tar.gz -C " + PTS_DEPLOYMENT_PATH,
	    "sudo tar xvfz /tmp/" + PTS_CONFIG_DIR_NAME + ".tar.gz -C " + PTS_CONFIG_DEPLOYMENT_PATH,
	    "sudo chown -R root:root " + PTS_CONFIG_DEPLOYMENT_PATH + "/" };

	private static final String[] UPLOAD_COMMANDS = {
	    "sudo chmod 755 " + PTS_CONFIG_DEPLOYMENT_PATH,
	    "sudo chmod 755 " + PTS_RESULTS_DIR,
	    "ls -1 " + PTS_RESULTS_DIR };

	private ComputeService service;
	private Template template;

	private NodeMetadata node;
	private Object nodeLock = new Object();
	private String group;

	private SshClient ssh;

	public BenchmarkRunner(CloudBenchService cloudBenchService, InstanceType instanceType,
	    Collection<Benchmark> benchmarks) {
		super(cloudBenchService, instanceType, benchmarks);

		group =
		    (getInstanceType().getRegion().getId() + "-" + getInstanceType().getHardwareType().getId()).replaceAll(
		        "[^a-zA-Z0-9-]",
		        "");
	}

	@Override
	void prepare() {
		this.service = getCloudBenchService().getContext(getInstanceType().getProvider()).getComputeService();
		this.template =
		    service.templateBuilder().hardwareId(getInstanceType().getHardwareType().getId())
		        .locationId(getInstanceType().getRegion().getId()).osFamily(TEMPLATE_OS)
		        .osVersionMatches(TEMPLATE_OS_VERSION).os64Bit(true).build();
		template.getOptions().userMetadata("Name", getCloudBenchService().getName());
	}

	@Override
	void create() throws RunNodesException {
		/* Group name identifies the region */
		synchronized (nodeLock) {
			node = Iterables.getOnlyElement(service.createNodesInGroup(group, 1, template));
			log("Node created: " + node.getId());
		}
	}

	@Override
	void prepareDeployment() {
		try {
			ssh = service.getContext().utils().sshForNode().apply(node);
			ssh.connect();
		} catch (SshException e) {
			if (ssh != null)
				ssh.disconnect();
		}
	}

	@Override
	void deploy() throws RunScriptOnMachineException {
		try {
			if (ssh != null) {
				initPackageInstallation(ssh);
				setupPHP(ssh);
				deployPts(ssh);
			}
		} catch (RunScriptOnMachineException e) {
			if (ssh != null)
				ssh.disconnect();
			throw e;
		}
	}

	@Override
	void install(Benchmark benchmark) throws RunScriptOnMachineException {
		try {
			if (ssh != null)
				installBenchmark(ssh, benchmark);
		} catch (RunScriptOnMachineException e) {
			if (ssh != null)
				ssh.disconnect();
			throw e;
		}
	}

	@Override
	void runBenchmark(Benchmark benchmark) throws RunScriptOnMachineException {
		try {
			if (ssh != null)
				runBenchmark(ssh, benchmark);
		} catch (RunScriptOnMachineException e) {
			if (ssh != null)
				ssh.disconnect();
			throw e;
		}
	}

	@Override
	LinkedListMultimap<Benchmark, Result> upload() throws ParseXmlResultException, RunScriptOnMachineException {
		try {
			if (ssh != null)
				return uploadResults(ssh);
		} catch (ParseXmlResultException | RunScriptOnMachineException e) {
			if (ssh != null)
				ssh.disconnect();
			throw e;
		}
		return null;
	}

	@Override
	void cleanUp() {
		synchronized (nodeLock) {
			if (node != null) {
				service.destroyNode(node.getId());
				node = null;
			}
		}
	}

	@Override
	void terminate() {
		synchronized (nodeLock) {
			if (node != null) {
				service.destroyNode(node.getId());
				node = null;
			}
		}
	}

	private void setupPHP(SshClient ssh) throws RunScriptOnMachineException {
		for (String command : SETUP_PHP_COMMANDS) {
			NodeHelper.runScript(this, ssh, command);
		}
	}

	private void deployPts(SshClient ssh) throws RunScriptOnMachineException {

		/* Upload PTS */
		URL resource = this.getClass().getResource(File.separator + PTS_DIR_NAME + ".tar.gz");
		File ptsPackage = new File(resource.getFile());
		ssh.put("/tmp/" + ptsPackage.getName(), new FilePayload(ptsPackage));

		/* Upload pre-defined config */
		resource = this.getClass().getResource(File.separator + PTS_CONFIG_DIR_NAME + ".tar.gz");
		File ptsConfig = new File(resource.getFile());
		ssh.put("/tmp/" + ptsConfig.getName(), new FilePayload(ptsConfig));

		/* Just to make sure the destination directories are there */
		try {
			NodeHelper.runScript(this, ssh, DEPLOY_PTS_COMMANDS[0]);
		} catch (RunScriptOnMachineException e) {
		}
		try {
			NodeHelper.runScript(this, ssh, DEPLOY_PTS_COMMANDS[1]);
		} catch (RunScriptOnMachineException e) {
		}

		for (int i = 2; i < DEPLOY_PTS_COMMANDS.length; i++) {
			NodeHelper.runScript(this, ssh, DEPLOY_PTS_COMMANDS[i]);
		}
	}

	private void installBenchmark(SshClient ssh, Benchmark benchmark) throws RunScriptOnMachineException {
		/* Install the benchmark using PTS */
		NodeHelper.runScript(this, ssh, "cd " + PTS_DEPLOYMENT_PATH + "/" + PTS_DIR_NAME + " && sudo ./" + PTS_EXECUTABLE
		    + " batch-install " + benchmark.getId());
	}

	private void runBenchmark(SshClient ssh, Benchmark benchmark) throws RunScriptOnMachineException {
		/* Set up interactive options input */
		StringBuilder sb = new StringBuilder();
		for (String option : benchmark.getOptions()) {
			Integer valueId = benchmark.getSelectedValue(option);
			if (valueId != null) {
				sb.append(valueId.toString());
			} else {
				sb.append("1");
			}
			sb.append("\n");
		}

		/* Install the benchmark using PTS */
		NodeHelper.runScript(this, ssh, "cd " + PTS_DEPLOYMENT_PATH + "/" + PTS_DIR_NAME + " && sudo ./" + PTS_EXECUTABLE
		    + " batch-run " + benchmark.getId(), sb.toString());
	}

	private LinkedListMultimap<Benchmark, Result> uploadResults(SshClient ssh) throws ParseXmlResultException,
	    RunScriptOnMachineException {
		/* Get contents of benchmark result directory */
		/* TODO: stop doing bad things */
		NodeHelper.runScript(this, ssh, UPLOAD_COMMANDS[0]);
		NodeHelper.runScript(this, ssh, UPLOAD_COMMANDS[1]);
		String output = NodeHelper.runScript(this, ssh, UPLOAD_COMMANDS[2]);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(output.getBytes())));

		/* Get all result directories */
		Set<String> directories = new HashSet<String>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (!line.equals("pts-results-viewer"))
					directories.add(line);
			}
		} catch (IOException e) {
			throw new ParseXmlResultException(e.getMessage());
		}

		LinkedListMultimap<Benchmark, Result> resultsForBenchmark = LinkedListMultimap.create();
		BenchmarkResult result;
		for (String directory : directories) {

			/* Get the PTS benchmark results */
			String resultFilePath = PTS_RESULTS_DIR + "/" + directory + "/composite.xml";

			String command = "sudo chmod 644 " + resultFilePath;
			try {
				NodeHelper.runScript(this, ssh, command);
			} catch (RunScriptOnMachineException e) {
				log("Continuing on " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}

			log("Trying to parse this benchmark result file:");

			try {
				String xml = NodeHelper.runScript(this, ssh, "sudo cat " + resultFilePath);

				InputStream is = new ByteArrayInputStream(xml.getBytes());

				Serializer serializer = new Persister();
				result = serializer.read(BenchmarkResult.class, is);

				for (Result resultData : result.getResults()) {

					for (Benchmark benchmark : getBenchmarks()) {
						if (resultData.getId().equals("pts/" + benchmark.getId())) {
							/* Backlink for results to benchmark */
							resultData.setBenchmark(benchmark);

							resultsForBenchmark.put(benchmark, resultData);
						}
					}
				}

			} catch (RunScriptOnMachineException e) {
				log("Continuing on " + e.getClass().getSimpleName() + ": " + e.getMessage());
			} catch (Exception e) {
				ParseXmlResultException ex = new ParseXmlResultException(e.getMessage());
				log("Continuing on " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
			}
		}
		return resultsForBenchmark;
	}

	private void initPackageInstallation(SshClient ssh) throws RunScriptOnMachineException {
		log("Let the provider enough time to run startup scripts...");
		try {
			/* Let the provider enough time to run startup scripts */
			Thread.sleep(WAIT_TIME_FOR_PROVIDER_AFTER_STARTUP);
		} catch (InterruptedException e) {
			log(e.getMessage());
		}
		for (String command : INIT_PACKAGE_INSTALLATION_COMMANDS) {
			NodeHelper.runScript(this, ssh, command);
		}
	}

}
