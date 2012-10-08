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

package edu.kit.aifb.libIntelliCloudBench.logging;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

public class Logger extends Observable {

	private static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

	private Deque<LogEntry> logBuffer = new ArrayDeque<LogEntry>();

	private List<ILogListener> listeners = new LinkedList<ILogListener>();

	public void log(String newEntry) {
		LogEntry logEntry;
		synchronized (logBuffer) {
			logEntry = new LogEntry(newEntry);
			logBuffer.add(logEntry);
		}

		notifyListeners(logEntry.toString());
	}
	
	public void appendToLog(String line) {
		/* TODO: Implement appending to last log entry */
		log(line);
  }

	public void registerListener(ILogListener listener) {
		listeners.add(listener);
	}

	public void unregisterListener(ILogListener listener) {
		listeners.remove(listener);
	}

	public String getLog() {
		return getLog(this.logBuffer);
	}

	public String getLog(Collection<LogEntry> logBuffer) {
		StringBuilder sb = new StringBuilder();

		synchronized (logBuffer) {
			for (LogEntry logEntry : logBuffer) {
				sb.append(logEntry.toString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public Collection<LogEntry> getAllLogEntries() {
		return logBuffer;
	}

	private void notifyListeners(String newEntry) {
		for (ILogListener listener : listeners) {
			listener.updateLog(newEntry);
		}
	}

	public class LogEntry implements Comparable<LogEntry> {
		private long timestamp;
		private String entry;

		public LogEntry(String entry) {
			this.timestamp = System.currentTimeMillis();
			this.entry = entry;
		}

		public String getEntry() {
			return entry;
		}

		public void setEntry(String entry) {
			this.entry = entry;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		@Override
		public int compareTo(LogEntry anotherLogEntry) {
			return new Long(this.timestamp).compareTo(anotherLogEntry.getTimestamp());
		}

		@Override
		public String toString() {
			return "[" + df.format(new Date(timestamp)) + "] " + entry;
		}

	}
}