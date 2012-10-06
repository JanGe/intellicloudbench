package edu.kit.aifb.libIntelliCloudBench.background;

public class RunScriptOnMachineException extends Exception {
	private static final long serialVersionUID = -5268622504458723962L;

  public RunScriptOnMachineException(int exitStatus, String command, String output) {
  	super("Error " + exitStatus + " on running command '" + command + "'. Output was:\n" + output);
  }

}
