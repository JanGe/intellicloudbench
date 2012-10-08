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

package edu.kit.aifb.libIntelliCloudBench.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.jclouds.compute.domain.ExecChannel;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.ssh.SshClient;

import edu.kit.aifb.libIntelliCloudBench.background.BenchmarkRunner;
import edu.kit.aifb.libIntelliCloudBench.background.RunScriptOnMachineException;

public class NodeHelper {

	public static Set<NodeMetadata> filterNodes(Set<NodeMetadata> nodes, String hardwareId, String regionId)
	    throws NoNodesFoundException {
		/*
		 * Get all machines by hardware type and region (group name)
		 */
		Set<NodeMetadata> foundNodes = new HashSet<NodeMetadata>();
		for (NodeMetadata node : nodes) {
			if (node.getHardware().getId().equals(hardwareId)) {
				if (node.getGroup().equals(regionId)) {
					foundNodes.add(node);
				}
			}
		}
		if (foundNodes.size() == 0) {
			throw new NoNodesFoundException("No nodes found for this provider and configuration.");
		}
		return foundNodes;
	}

	public static String runScript(BenchmarkRunner runner, SshClient ssh, String command)
	    throws RunScriptOnMachineException {
		return runScript(runner, ssh, command, null);
	}

	public static String runScript(BenchmarkRunner runner, SshClient ssh, String command, String input)
	    throws RunScriptOnMachineException {
		runner.log("$ " + command);

		ExecChannel channel = ssh.execChannel(command);

		if (input != null) {
			try {
				setInput(runner, input, channel);
			} catch (IOException e) {
				throw new RunScriptOnMachineException(Integer.MAX_VALUE, command, "Error when providing interactive options:\n"
				    + input);
			}
		}

		String output = getOutput(runner, command, channel);

		if (channel.getExitStatus() != null) {
			logErrorOutput(runner, command, channel, output);
		}
		return output;
	}

	private static void logErrorOutput(BenchmarkRunner runner, String command, ExecChannel channel, String output)
	    throws RunScriptOnMachineException {
		Integer exitCode = channel.getExitStatus().get();
		if ((exitCode != null) && (exitCode != 0)) {
			InputStream errorIs = channel.getError();
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorIs));

			String line;
			StringBuilder errorSb = new StringBuilder();
			try {
				while ((line = errorReader.readLine()) != null) {
					runner.appendToLog(line);
					errorSb.append(line);
					errorSb.append("\n");
				}
			} catch (IOException e) {
				try {
					channel.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				errorSb.append("\n");
				errorSb.append(e.getMessage());
				throw new RunScriptOnMachineException(Integer.MIN_VALUE, command, errorSb.toString());
			}

			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			throw new RunScriptOnMachineException(exitCode, command, output);
		}
	}

	private static String getOutput(BenchmarkRunner runner, String command, ExecChannel channel)
	    throws RunScriptOnMachineException {
		InputStream is = channel.getOutput();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = reader.readLine()) != null) {
				runner.appendToLog(line);
				sb.append(line);
				sb.append("\n");
//				/* As PTS doesn't return useful status codes, we have to do some additional checks here */
//				if (line.equals("The following tests failed to install:")) {
//					throw new RunScriptOnMachineException(0, command, "Benchmark could not be installed:\n" + sb.toString());
//				}
			}
		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			sb.append(e.getMessage());
			throw new RunScriptOnMachineException(Integer.MIN_VALUE, command, sb.toString());
		}
		return sb.toString();
	}

	private static void setInput(BenchmarkRunner runner, String input, ExecChannel channel) throws IOException {
		OutputStream os = channel.getInput();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));

		writer.append(input);
		writer.flush();

		writer.close();
	}
}
