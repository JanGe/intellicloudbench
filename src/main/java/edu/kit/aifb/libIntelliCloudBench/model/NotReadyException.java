package edu.kit.aifb.libIntelliCloudBench.model;

public class NotReadyException extends Exception {
	private static final long serialVersionUID = 8644160866421343976L;
	
	private String variable;
	
	public NotReadyException(String variable) {
	  super();
	  this.variable = variable;
  }

	@Override
  public String getMessage() {
	  return "The value for " + variable + " is not ready yet.";
  }

}
