package com.capstone.application.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.capstone.application.R;
import com.capstone.application.model.Device;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.utils.Constants;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token, sharedPreferences);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        // FIXME - uncomment this when I get how to make broadcast receiver synchronous
        //Intent registrationComplete = new Intent(Constants.REGISTRATION_COMPLETE_ACTION);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to application server.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, SharedPreferences sharedPreferences) throws Exception {
        String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

        // only send registration to the server if user is already logged in
        if (loggedEmail != null) {
            Device device = new Device(loggedEmail, token);

            JsonResponse result;
            // The URL for making the GET request
            final String url = Constants.SERVER_URL + "device/register";

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Add the String message converter
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            // Make the HTTP GET request, marshaling the response to User object
            result = restTemplate.postForObject(url, device, JsonResponse.class);

            if (result == null || result.getStatus() != HttpStatus.OK) {
                throw new Exception("Fail to register token in application's server");
            }
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}