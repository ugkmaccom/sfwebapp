package controllers;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Contents extends Controller {

	public static void index(String id) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, String> headers = new HashMap<String, String>();
		params.put("grant_type", "password");
		params.put("client_id",
				Play.configuration.getProperty("oauth.client_id"));
		params.put("client_secret",
				Play.configuration.getProperty("oauth.client_secret"));
		params.put("username", Play.configuration.getProperty("oauth.username"));
		params.put("password", Play.configuration.getProperty("oauth.password"));
		params.put("format", Play.configuration.getProperty("oauth.json"));
		Logger.info("auth stast");
		HttpResponse httpResponse = WS
				.url("https://login.salesforce.com/services/oauth2/token")
				.params(params).headers(headers).post();
		Logger.info("auth end");

		Logger.info("httpResponse:%s", httpResponse.getString());
		JsonElement authJson = httpResponse.getJson();
		JsonObject jo = authJson.getAsJsonObject();
		JsonElement errorMsg = jo.get("error");
		if (null != errorMsg) {
			Exception exception = new Exception(String.format(
					"error=%s, error_description=%s", errorMsg,
					jo.get("error_description")));
			Logger.error(exception.getMessage());
			System.out.println(authJson);
			return;
		}
		String instance_url = jo.get("instance_url").getAsString();
		String access_token = jo.get("access_token").getAsString();

		headers = new HashMap<String, String>();
		headers.put("Authorization", "OAuth " + access_token);
		headers.put("Content-Type", "application/json");

		Map<String, String> param = new HashMap<String, String>();
		param.put("id", id);
		param.put("kiss", "");
		Gson gson = new Gson();
		String bodyParam = gson.toJson(param);

		Logger.info("stast");
		HttpResponse post = WS
				.url(instance_url + "/services/apexrest/contents/doPost")
				.body(bodyParam).headers(headers).post();
		Logger.info("end");

		String jsonResponse = post.getJson().toString();
		Logger.info("jsonResponse:%s", jsonResponse);

		render(jsonResponse);
	}
}