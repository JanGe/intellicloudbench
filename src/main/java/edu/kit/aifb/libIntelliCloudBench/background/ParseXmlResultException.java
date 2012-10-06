package edu.kit.aifb.libIntelliCloudBench.background;

public class ParseXmlResultException extends Exception {
	private static final long serialVersionUID = -5268622504458723962L;

  public ParseXmlResultException(String message) {
  	super("Error when parsing XML benchmark result: " + message);
  }

}