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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.kit.aifb.libIntelliCloudBench.metrics.IMetricsType;

public class Benchmark implements IMetricsType, Serializable {
	private static final long serialVersionUID = -5675531487501813376L;

	private static final String OPENBENCHMARKING_CLIENT_ENDPOINT = "http://openbenchmarking.org/f/client.php";
	private static final String OPENBENCHMARKING_CLIENT_USER_AGENT = "PhoronixTestSuite/Suldal";
	private static final String OPENBENCHMARKING_CLIENT_VERSION = "4010";
	/* Extracted from a local installation */
	private static final String OPENBENCHMARKING_CLIENT_GSID = "NNGKDG999";

	private static final String TEST_PROFILE_DIR = File.separator + "test-profiles" + File.separator + "pts"
	    + File.separator;

	/* TODO: Make these lists external */
	/* Will not work, as we're having headless servers */
	private static final String[] TYPE_BLACKLIST = { "Graphics" };
	/* Known not to work */
	private static final String[] BENCHMARK_BLACKLIST = {
	    "aio-stress-1.1.0",
	    "battery-power-usage-1.0.0",
	    "bork-1.0.0",
	    "bullet-1.1.0",
	    "compilebench-1.0.0",
	    "crafty-1.3.0",
	    "encode-ogg-1.4.0",
	    "encode-wavpack-1.2.0",
	    "espeak-1.3.0",
	    "etqw-demo-iqc-1.0.0",
	    "ffte-1.0.1",
	    "gcrypt-1.0.0",
	    "gnupg-1.3.1",
	    "idle-1.1.0",
	    "idle-power-usage-1.0.0",
	    "interbench-1.0.1",
	    "java-scimark2-1.1.1",
	    "jgfxbat-1.1.0",
	    "juliagpu-1.2.0",
	    "luxmark-1.0.0",
	    "mandelbulbgpu-1.2.0",
	    "mandelgpu-1.2.0",
	    "mencoder-1.3.0",
	    "nexuiz-iqc-1.0.0",
	    "npb-1.1.1",
	    "pgbench-1.4.0",
	    "pyopencl-1.0.0",
	    "smallpt-gpu-1.2.0",
	    "scimark2-1.1.1",
	    "sqlite-1.8.0",
	    "stresscpu2-1.0.1",
	    "sunflow-1.1.0",
	    "systester-1.0.0",
	    "tachyon-1.1.0",
	    "tscp-1.0.0",
	    "ttsiod-renderer-1.3.0",
	    "xplane9-iqc-1.0.0" };

	private String id;
	private String name;
	private String description;
	private String type;
	private Integer avgRunTime = 0;
	List<String> options;
	LinkedListMultimap<String, String> valuesByOption;
	Map<String, Integer> selectedValueForOption = new HashMap<String, Integer>();

	public Benchmark(String id, String name, String description, String type, Integer avgRunTime, List<String> options,
	    LinkedListMultimap<String, String> valuesByOption) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.type = type;
		if (avgRunTime != null)
			this.avgRunTime = avgRunTime;
		this.options = options;
		this.valuesByOption = valuesByOption;

		/* Set default selected value */
		for (String option : options) {
			List<String> values = valuesByOption.get(option);
			if (!values.isEmpty()) {
				setSelectedValue(option, values.get(0));
			}
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getType() {
		return type;
	}

	public Integer getAvgRunTime() {
		return avgRunTime;
	}

	public List<String> getOptions() {
		return options;
	}

	public List<String> getValuesForOption(String option) {
		return valuesByOption.get(option);
	}

	public void setSelectedValue(String option, String value) {
		Integer valueId = valuesByOption.get(option).indexOf(value) + 1;
		selectedValueForOption.put(option, valueId);
	}

	public Integer getSelectedValue(String option) {
		return selectedValueForOption.get(option);
	}

	public boolean isSelectedValue(String option, String value) {
		Integer valueId = valuesByOption.get(option).indexOf(value) + 1;
		return selectedValueForOption.get(option) == valueId;
	}

	public boolean hasOptions() {

		for (String value : valuesByOption.values()) {
			if (value != null)
				return true;
		}

		return false;
	}

	public boolean optionHasValues(String option) {

		for (String value : valuesByOption.get(option)) {
			if (value != null)
				return true;
		}

		return false;
	}

	public static Multimap<String, Benchmark> getAllBenchmarks() {
		Multimap<String, Benchmark> benchmarksByType = LinkedListMultimap.create();

		String id;
		String name;
		String description;
		String type;
		List<String> options = new LinkedList<String>();
		LinkedListMultimap<String, String> valuesByOption = LinkedListMultimap.create();

		/**
		 * Getting additional info for the benchmarks like average runtime from
		 * openbenchmarking.org
		 */
		Map<String, Integer> avgRunTimeForBenchmark = requestAvgRunTimeForBenchmark();

		/**
		 * Getting all benchmarks by parsing the test profile XML files from the
		 * resources directory
		 */
		SAXBuilder saxBuilder = new SAXBuilder();

		URL testProfileResourceUrl = Benchmark.class.getResource(TEST_PROFILE_DIR);
		File testProfileResourceDir = new File(testProfileResourceUrl.getFile());

		assert testProfileResourceDir.isDirectory();

		for (File testProfileDir : testProfileResourceDir.listFiles()) {
			if (testProfileDir.isDirectory()) {
				try {
					URL testProfileUrl =
					    Benchmark.class.getResource(TEST_PROFILE_DIR + File.separator + testProfileDir.getName() + File.separator
					        + "test-definition.xml");
					File file = new File(testProfileUrl.getFile());
					Document doc = saxBuilder.build(file);
					Element rootElement = doc.getRootElement();

					id = testProfileDir.getName();

					Element testInformationChild = rootElement.getChild("TestInformation");
					if (testInformationChild != null) {
						name = testInformationChild.getChildTextNormalize("Title");
						description = testInformationChild.getChildTextNormalize("Description");

						if (name != null) {
							Element testProfileChild = rootElement.getChild("TestProfile");
							type = testProfileChild.getChildTextNormalize("TestType");

							if (!Arrays.asList(TYPE_BLACKLIST).contains(type) && !Arrays.asList(BENCHMARK_BLACKLIST).contains(id)) {

								valuesByOption = LinkedListMultimap.create();
								Element testSettingsChild = rootElement.getChild("TestSettings");

								if (testSettingsChild != null) {
									List<Element> optionsNodes = (List<Element>) testSettingsChild.getChildren("Option");

									if (!optionsNodes.isEmpty()) {
										options = new LinkedList<String>();

										for (Element optionsNode : optionsNodes) {
											/* Option */
											String optionName = optionsNode.getChildTextNormalize("DisplayName");
											/*
											 * There are options without predefined values in PTS
											 * tests, so we can already add it here, even if there are
											 * no values
											 */
											options.add(optionName);

											Element menuChild = optionsNode.getChild("Menu");
											if (menuChild != null) {
												for (Element valueNode : (List<Element>) menuChild.getChildren("Entry")) {
													String valueText;
													String valueMessage = valueNode.getChildTextNormalize("Message");
													if (valueMessage != null) {
														valueText = valueNode.getChildTextNormalize("Name") + " [" + valueMessage + "]";
													} else {
														valueText = valueNode.getChildTextNormalize("Name");
													}
													valuesByOption.put(optionName, valueText);
												}
											}
										}
									}
								}

								/* Strip the version info from the id */
								Integer avgRunTime = avgRunTimeForBenchmark.get(id.split("-(?=[^-]+$)")[0]);
								Benchmark benchmark = new Benchmark(id, name, description, type, avgRunTime, options, valuesByOption);
								benchmarksByType.put(type, benchmark);
							}
						}
					}

				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return benchmarksByType;
	}

	private static Map<String, Integer> requestAvgRunTimeForBenchmark() {
		Map<String, Integer> avgRuntimeForBenchmark = new HashMap<String, Integer>();

		HttpClient client = new DefaultHttpClient();

		HttpPost post = new HttpPost(OPENBENCHMARKING_CLIENT_ENDPOINT);
		post.addHeader("User-Agent", OPENBENCHMARKING_CLIENT_USER_AGENT);
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");

		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("r", "repo_index"));
		postParams.add(new BasicNameValuePair("client_version", OPENBENCHMARKING_CLIENT_VERSION));
		postParams.add(new BasicNameValuePair("gsid", OPENBENCHMARKING_CLIENT_GSID));
		postParams.add(new BasicNameValuePair("repo", "pts"));

		try {
			post.setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));

			HttpResponse response = client.execute(post);

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			try {
				JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
				for (Entry<String, JsonElement> benchmark : json.getAsJsonObject("tests").entrySet()) {
					int avgRunTime = benchmark.getValue().getAsJsonObject().get("average_run_time").getAsInt();
					avgRuntimeForBenchmark.put(benchmark.getKey(), avgRunTime);
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return avgRuntimeForBenchmark;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Benchmark other = (Benchmark) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
