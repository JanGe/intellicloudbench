package edu.kit.aifb.IntelliCloudBench.util;

import edu.kit.aifb.IntelliCloudBench.model.User;

public interface IOAuthListener {
	
	public abstract void login(User user);

	public abstract void logout();
	
	public abstract void setErrorMessage(String message);
}
