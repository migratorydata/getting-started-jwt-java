package com.migratorydata.jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.migratorydata.client.*;

public class Main {
    
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static final StatusNotification TOKEN_EXPIRED = new StatusNotification("NOTIFY_TOKEN_EXPIRED", "NOTIFY_TOKEN_EXPIRED");
    public static final StatusNotification TOKEN_TO_EXPIRE = new StatusNotification("NOTIFY_TOKEN_TO_EXPIRE", "NOTIFY_TOKEN_TO_EXPIRE");
    public static final StatusNotification TOKEN_INVALID = new StatusNotification("NOTIFY_TOKEN_INVALID", "NOTIFY_TOKEN_INVALID");

    private static final String backendEndpoint = "127.0.0.1:8080";
    private static final String server = "demo.migratorydata.com:80";
    private static final String subject = "/server/status";

    public static void main(String[] args) throws Exception {

        String token = loginAndGetToken();

		// create a MigratoryData client
		final MigratoryDataClient client = new MigratoryDataClient();

		// Define the log listener and verbosity
		client.setLogListener(new MigratoryDataLogListener() {
			private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");

			@Override
			public void onLog(String log, MigratoryDataLogLevel level) {
				String isoDateTime = sdf.format(new Date(System.currentTimeMillis()));
				System.out.println(String.format("[%1$s] [%2$s] %3$s", isoDateTime, level, log));
			}
		}, MigratoryDataLogLevel.DEBUG);

		// attach the entitlement token
		client.setEntitlementToken(token);


		// Define the listener to handle live message and status notifications
		// In your application it is recommended to define a regular class
		// instead of the anonymous class we define here for concision
		client.setListener(new MigratoryDataListener() {

			public void onStatus(String status, String info) {
				System.out.println("Got Status: " + status + " - " + info);

                if (MigratoryDataClient.NOTIFY_CONNECT_DENY.equals(status) ||
                        TOKEN_EXPIRED.getStatus().equals(status) ||
                        TOKEN_INVALID.getStatus().equals(status)) {
                    // invalid token or expired token
                    // renew token
                    String newToken = loginAndGetToken();
                    client.setEntitlementToken(newToken);
                    client.subscribe(Arrays.asList(subject));
                }

                if (TOKEN_TO_EXPIRE.getStatus().equals(status)) {
                    // client token is about to expire
                    // generate a new token and update server
                    String newToken = loginAndGetToken();
                    client.setEntitlementToken(newToken);
                }

			}

			public void onMessage(MigratoryDataMessage message) {
				System.out.println("Got Message: " + message);
			}

		});

		// set server to connect to the MigratoryData server
		client.setServers(new String[] { server });

		// subscribe
		client.subscribe(Arrays.asList(subject));

		// connect to the MigratoryData server
		client.connect();

		// publish a message every 5 seconds
		executor.scheduleAtFixedRate(() -> {
			String content = "data - " + System.currentTimeMillis();
			String closure = "id" + System.currentTimeMillis();
			client.publish(new MigratoryDataMessage(subject, content.getBytes(), closure));
		}, 3000, 5000, TimeUnit.MILLISECONDS);

		// add a shutdown hook to catch CTRL-C and cleanly shutdown this client
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				client.disconnect();
			}
		});

    }

    public static String loginAndGetToken() {
        // curl -X POST http://127.0.0.1:8080/token/generate -H 'Content-Type: application/json' -d '{"username":"admin","password":"password", "ttlSeconds": 360000, "permissions": { "all" :["/server/status"]}}'
        // login simulation 
        // Http backend request for token
        
        String payload = "{\"username\":\"admin\",\"password\":\"password\",\"ttlSeconds\":360000,\"permissions\":{\"all\":[\"/server/status\"]}}";

        StringEntity entity = new StringEntity(payload,
                ContentType.APPLICATION_JSON);

		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpPost request = new HttpPost("http://" + backendEndpoint + "/token/generate");
			request.setEntity(entity);

			HttpResponse response = client.execute(request);
			System.out.println(response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
                if (response.getEntity() != null) {
                    // return it as a String
                    return parseResponse(EntityUtils.toString(response.getEntity()));
                }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "some-token";
    }

	public static String parseResponse(String response) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject jsonResponse = (JSONObject) parser.parse(response);
		return (String)jsonResponse.get("response");
	}
}
