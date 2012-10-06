package edu.kit.aifb.IntelliCloudBench.model;

import java.util.HashMap;
import java.util.Map;

public class ApplicationState {
	
	private static Map<String, User> usersById = new HashMap<String, User>();
	private static Map<String, UIState> uiStateForUser = new HashMap<String, UIState>();
	
	public static UIState getUIStateForUser(User user) {
		UIState state = uiStateForUser.get(user.getId());
		if (state == null) {
			state = new UIState();
			uiStateForUser.put(user.getId(), state);
		}
		return state;
	}

	public static void addUser(User user) {
		usersById.put(user.getId(), user);
	}
	
	public static User getUserById(String userId) {
		return usersById.get(userId);
	}
	
}
