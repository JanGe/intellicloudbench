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