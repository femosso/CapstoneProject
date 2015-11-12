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
import com.capstone.application.utils.RestUriConstants;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = RegistrationIntentService.class.getName();

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // Get GCM token
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            sendRegistrationToServer(token, sharedPreferences.getString(Constants.LOGGED_EMAIL, null));

            // Store a boolean that indicates whether the generated token has been
            // sent to server. If the boolean is false, send the token to server,
            // otherwise the server should have already received the token.
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, true).apply();
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);

            // If an exception happens while fetching the new token or updating our registration data
            // on the server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Constants.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    /**
     * Persist registration to application server.
     *
     * @param token The new token.
     * @param loggedEmail email of the logged user.
     */
    private void sendRegistrationToServer(String token, String loggedEmail) throws Exception {
        // only send registration to the server if user is already logged in
        if (loggedEmail != null) {
            Device device = new Device(loggedEmail, token);

            JsonResponse result;

            // The URL for making the POST request
            final String url = Constants.getServerUrl(getApplicationContext()) +
                    RestUriConstants.DEVICE_CONTROLLER + File.separator + RestUriConstants.REGISTER;

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Add the String message converter
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            // Make the HTTP POST request, marshaling the response to JsonResponse object
            result = restTemplate.postForObject(url, device, JsonResponse.class);

            if (result == null || result.getStatus() != HttpStatus.OK) {
                throw new Exception("Fail to register token in application's server");
            }
        }
    }
}