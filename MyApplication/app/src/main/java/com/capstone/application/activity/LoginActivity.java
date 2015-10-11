package com.capstone.application.activity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.gcm.RegistrationIntentService;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.Crypto;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static com.capstone.application.utils.Constants.SignInProvider.APPLICATION;
import static com.capstone.application.utils.Constants.SignInProvider.FACEBOOK;

public class LoginActivity extends FragmentActivity {

    private static final String TAG = LoginActivity.class.getName();

    private static final int REGISTER_REQUEST = 0;

    private CallbackManager mCallbackManager;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        FacebookSdk.sdkInitialize(mContext);

        setContentView(R.layout.activity_login);

        if (checkPlayServices()) {
            // FIXME - maybe consider save password hash and login in sharedPreferences and
            // automatically authenticate using those saved credentials
            if (isUserLoggedIn()) {
                onLoginSuccess(null);
            }

            // FIXME - temporary code
            //sendNotification(1, "testing 1");
            //sendNotification(2, "testing 2");

            // initialize both login via application registering or facebook account
            initCustomLogin();
            initFacebookLogin();

            final TextView registerLink = (TextView) findViewById(R.id.link_register);
            registerLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the registering activity
                    Intent intent = new Intent(mContext, RegisterActivity.class);
                    startActivityForResult(intent, REGISTER_REQUEST);
                }
            });
        }
    }

    // FIXME - Remove this method later
    private void sendNotification(int type, String message) {
        Intent intent = new Intent(this, DialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("type", type);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_home)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                        //.setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Constants.REGISTRATION_COMPLETE_ACTION));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQUEST) {
            if (resultCode == RESULT_OK) {
                User user = data.getExtras().getParcelable("result");
                doLogin(user);
            }
        }
    }

    @Override
    public void onPause() {
        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(this);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private void initCustomLogin() {
        final Button loginButton = (Button) findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                User user = validateFields();
                if (user != null) {
                    doLogin(user);
                }
            }
        });
    }

    /**
     * Validate fields and, if everything is ok, return User object fulfilled with such field values
     */
    private User validateFields() {
        boolean valid = true;

        User user = new User();
        user.setProvider(APPLICATION.ordinal());

        final TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);
        final TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);

        String email = emailWrapper.getEditText().getText().toString();
        String password = passwordWrapper.getEditText().getText().toString();

        if (!validateEmail(email)) {
            emailWrapper.setError("Not a valid email address!");
            valid = false;
        } else {
            emailWrapper.setErrorEnabled(false);
            user.setEmail(email);
        }

        if (!validatePassword(password)) {
            passwordWrapper.setError("Not a valid password!");
            valid = false;
        } else {
            passwordWrapper.setErrorEnabled(false);
            user.setPassword(Crypto.md5(password));
        }

        // if some validation failed, return null
        if (!valid) {
            user = null;
        }

        return user;
    }

    private void initFacebookLogin() {
        // set required permission
        final LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.btn_facebook_login);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));

        // register Facebook callback
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {

                                        User user = buildUserObject(response.getJSONObject());
                                        if (user != null) {
                                            doLogin(user);
                                        }
                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id, name, email, gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(mContext, "onCancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(mContext, "onError", Toast.LENGTH_LONG).show();
                    }
                });

    }

    private User buildUserObject(JSONObject jsonObject) {
        User user;
        try {
            String id = (String) jsonObject.get("id");
            String email = (String) jsonObject.get("email");

            user = new User();
            user.setFacebookId(id);
            user.setEmail(email);
            user.setProvider(Constants.SignInProvider.FACEBOOK.ordinal());
        } catch (JSONException e) {
            user = null;
        }

        return user;
    }

    public boolean isUserLoggedInViaFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        int signInProvider = sharedPreferences.getInt(Constants.SIGN_IN_PROVIDER, -1);
        String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);
        if (loggedEmail != null && (signInProvider == APPLICATION.ordinal() ||
                (signInProvider == FACEBOOK.ordinal() && isUserLoggedInViaFacebook()))) {
            return true;
        }
        return false;
    }

    public void doLogin(User user) {
        Toast.makeText(mContext, "OK! I'm performing login.", Toast.LENGTH_SHORT).show();

        new PerformLoginTask(LoginActivity.this).execute(user);
    }

    public static boolean validatePassword(String password) {
        return true;
        // FIXME - define password validity
        //return password.length() > 5;
    }

    public static boolean validateEmail(String email) {
        return !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private class PerformLoginTask extends AsyncTask<User, Void, LoginResponse> {

        private ProgressDialog dialog;

        public PerformLoginTask(LoginActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected LoginResponse doInBackground(User... params) {
            Log.d(TAG, "Contacting server to login user");

            User user = params[0];

            JsonResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "login/send";

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to User object
                result = restTemplate.postForObject(url, user, JsonResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // log out from Facebook if user couldn't be authenticated in server
            if (user.getProvider() == Constants.SignInProvider.FACEBOOK.ordinal() &&
                    (result == null || !result.getStatus().equals(HttpStatus.OK))) {
                Log.d(TAG, "Logging out from Facebook - user couldn't be authenticated in server");
                LoginManager.getInstance().logOut();
            }

            return new LoginResponse(result, user);
        }

        @Override
        protected void onPostExecute(LoginResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            JsonResponse jsonResponse = result.jsonResponse;
            User user = result.user;

            handleLoginResult(jsonResponse);

            if (result != null && jsonResponse.getStatus().equals(HttpStatus.OK)) {
                onLoginSuccess(user);
            }
        }
    }

    private void onLoginSuccess(User user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        // if user is not logged in yet, write info to shared preferences
        if(user != null) {
            sharedPreferences.edit().putInt(Constants.SIGN_IN_PROVIDER, user.getType()).apply();
            sharedPreferences.edit().putString(Constants.LOGGED_EMAIL, user.getEmail()).apply();
        }

        boolean sentToken = sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);
        if(!sentToken) {
            // send token if app's server does not have it for the logged user yet
            setUpGcmConfiguration();
        }

        // kills this login screen
        finish();

        // initialize main screen of the app
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private void setUpGcmConfiguration() {
        // FIXME - make this synchronous
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);

                if (sentToken) {
                    Toast.makeText(mContext, "Token retrieved and sent to server! You can now use " +
                            "gcmsender to send downstream messages to this app.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "An error occurred while either fetching the InstanceID token,\n" +
                            "        sending the fetched token to the server or subscribing to the PubSub topic. Please try\n" +
                            "        running the sample again.", Toast.LENGTH_LONG).show();
                }
            }
        };

        // Start IntentService to register this application with GCM.
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void handleLoginResult(JsonResponse result) {
        // TODO - treat better the types of return (make it compliant with internationalization method)
        String outputMessage = "Unexpected result";
        if (result != null) {
            switch (result.getStatus()) {
                case OK:
                case BAD_REQUEST:
                    outputMessage = result.getMessage();
                    break;
            }
        }
        Toast.makeText(mContext, outputMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Auxiliary class to pass necessary objects from PerformLoginTask::doInBackground() method
     * of AsyncTask to PerformLoginTask::onPostExecute()
     */
    private static class LoginResponse {
        JsonResponse jsonResponse;
        User user;

        LoginResponse(JsonResponse jsonResponse, User user) {
            this.jsonResponse = jsonResponse;
            this.user = user;
        }
    }
}
