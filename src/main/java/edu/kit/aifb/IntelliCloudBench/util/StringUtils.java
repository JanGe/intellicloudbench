package edu.kit.aifb.IntelliCloudBench.util;

import java.util.Collection;

public class StringUtils {
	
	/* Taken from: http://stackoverflow.com/a/524089/1467115 */
	public static String concatStringsWSep(Collection<String> strings, String separator) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String s : strings) {
			sb.append(sep).append(s);
			sep = separator;
		}
		return sb.toString();
	}
}
