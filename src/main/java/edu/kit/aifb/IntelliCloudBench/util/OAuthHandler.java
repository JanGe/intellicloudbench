package edu.kit.aifb.IntelliCloudBench.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi20;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import com.google.gson.Gson;
import com.vaadin.terminal.ParameterHandler;

import edu.kit.aifb.IntelliCloudBench.model.ApplicationState;
import edu.kit.aifb.IntelliCloudBench.model.User;

@SuppressWarnings("serial")
public class OAuthHandler implements ParameterHandler {
	private static final String OAUTH_CONFIG_FILE = "/oauth.properties";
	/* Infos for redirection to Google Login Page */
	private static final Token EMPTY_TOKEN = null;
	private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
	/* Infos for response verification and access token obtainment */
	private static final String VERIFIER_NAME = "code";
	private static final String GRANT_TYPE = "authorization_code";
	private static final String GET_PROFILE_URL = "https://www.googleapis.com/oauth2/v1/userinfo";

	private OAuthService service;
	private User user;
	private IOAuthListener listener;

	public OAuthHandler(IOAuthListener listener, String callbackUrl) {
		Properties properties = new Properties();
		URL oauthResourceUrl = OAuthHandler.class.getResource(OAUTH_CONFIG_FILE);
		File oauthResourceFile = new File(oauthResourceUrl.getFile());
		try {
	    properties.load(new FileReader(oauthResourceFile));
    } catch (IOException e) {
	    e.printStackTrace();
    }
		
		String apiKey = properties.getProperty("googleapikey");
		String apiSecret = properties.getProperty("googleapisecret");
		
		this.listener = listener;
		this.service = new ServiceBuilder().provider(GoogleApi20.class).apiKey(apiKey)
		    .apiSecret(apiSecret).scope(OAuthHandler.SCOPE).grantType(OAuthHandler.GRANT_TYPE)
		    .callback(callbackUrl).build();
	}

	public String getRedirectUrl() {
		return service.getAuthorizationUrl(OAuthHandler.EMPTY_TOKEN);
	}

	@Override
	public void handleParameters(Map<String, String[]> parameters) {
		try {

			if (parameters.containsKey(OAuthHandler.VERIFIER_NAME)) {
				/*
				 * User logged in via Provider, now get Access Token from response
				 */
				Verifier verifier = new Verifier(parameters.get(OAuthHandler.VERIFIER_NAME)[0]);
				Token accessToken = service.getAccessToken(null, verifier);

				/* and request User info */
				OAuthRequest userRequest = new OAuthRequest(Verb.GET, OAuthHandler.GET_PROFILE_URL);
				service.signRequest(accessToken, userRequest);
				userRequest.addHeader("GData-Version", "3.0");
				Response userResponse = userRequest.send();

				/* Retrieve user info from JSON response */
				Gson gson = new Gson();
				user = gson.fromJson(userResponse.getBody(), User.class);
				
				/* Check if this user already has an object */
				User oldUser = ApplicationState.getUserById(user.getId());
				if (oldUser != null) {
					user = oldUser;
				} else {
					ApplicationState.addUser(user);
				}
				
				listener.login(user);

			} else {
				if (parameters.containsKey("oauth_problems")) {
					listener.setErrorMessage(parameters.get("oauth_problems")[0]);
				}
			}

		} catch (Exception e) {
			listener.setErrorMessage(e.getMessage());
		}
	}

}
