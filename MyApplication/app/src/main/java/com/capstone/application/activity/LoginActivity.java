package com.capstone.application.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.database.PendingCheckInProvider;
import com.capstone.application.gcm.RegistrationIntentService;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.LoginResponse;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.Constants.SignInProvider;
import com.capstone.application.utils.Crypto;
import com.capstone.application.utils.RestUriConstants;
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

import java.io.File;
import java.util.Arrays;

public class LoginActivity extends FragmentActivity {
    private static final String TAG = LoginActivity.class.getName();

    private static final int REGISTER_REQUEST = 0;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private Context mContext;

    // callback manager for facebook login
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();

        FacebookSdk.sdkInitialize(mContext);

        setContentView(R.layout.activity_login);

        if (checkPlayServices()) {
            if (isUserLoggedIn()) {
                onLoginSuccess(null);
            }

            // initialize views of both login via application registering or facebook account
            initCustomLogin();
            initFacebookLogin();

            final TextView registerLink = (TextView) findViewById(R.id.txtRegisterLink);
            registerLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the registering activity
                    Intent intent = new Intent(mContext, RegisterActivity.class);
                    startActivityForResult(intent, REGISTER_REQUEST);
                }
            });
        }

        // FIXME - remove this later
        mContext.getContentResolver().delete(PendingCheckInProvider.CONTENT_URI, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);
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

        super.onPause();
    }

    private void initCustomLogin() {
        final Button loginButton = (Button) findViewById(R.id.btnLogin);
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
        user.setProvider(SignInProvider.APPLICATION.ordinal());

        TextInputLayout emailWrapper = (TextInputLayout) findViewById(R.id.emailWrapper);
        TextInputLayout passwordWrapper = (TextInputLayout) findViewById(R.id.passwordWrapper);

        String email = emailWrapper.getEditText().getText().toString();
        String password = passwordWrapper.getEditText().getText().toString();

        if (!validateEmail(email)) {
            emailWrapper.setError(getString(R.string.not_valid_email));
            valid = false;
        } else {
            emailWrapper.setErrorEnabled(false);
            user.setEmail(email);
        }

        if (!validatePassword(password)) {
            passwordWrapper.setError(getString(R.string.not_valid_password));
            valid = false;
        } else {
            passwordWrapper.setErrorEnabled(false);
            user.setPassword(Crypto.md5(password));
        }

        // if some validation has failed, return null
        if (!valid) {
            user = null;
        }

        return user;
    }

    private void initFacebookLogin() {
        // set required permission
        final LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.btnFacebookLogin);
        facebookLoginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));

        // register Facebook callback
        mCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {

                                        User user = buildUserObjectFromJson(response.getJSONObject());
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
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private User buildUserObjectFromJson(JSONObject jsonObject) {
        User user;
        try {
            String id = (String) jsonObject.get("id");
            String email = (String) jsonObject.get("email");

            user = new User();
            user.setFacebookId(id);
            user.setEmail(email);
            user.setProvider(SignInProvider.FACEBOOK.ordinal());
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

        return loggedEmail != null && (signInProvider == SignInProvider.APPLICATION.ordinal() ||
                (signInProvider == SignInProvider.FACEBOOK.ordinal() && isUserLoggedInViaFacebook()));
    }

    public void doLogin(User user) {
        new PerformLoginTask(LoginActivity.this).execute(user);
    }

    public static boolean validatePassword(String password) {
        //return password.length() > 5;
        return true;
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
            dialog.setMessage(getString(R.string.progress_dialog_sending));
            dialog.show();

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // TODO - log out from facebook if dialog is dismissed and user not authenticate yet
                    PerformLoginTask.this.cancel(true);
                }
            });
        }

        @Override
        protected LoginResponse doInBackground(User... params) {
            Log.d(TAG, "Contacting server to login user");

            User user = params[0];

            LoginResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.LOGIN_CONTROLLER + File.separator + RestUriConstants.SEND;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP POST request, marshaling the response to LoginResponse object
                result = restTemplate.postForObject(url, user, LoginResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // log out from Facebook if user couldn't be authenticated in app's server
            if (user.getProvider() == SignInProvider.FACEBOOK.ordinal() &&
                    (result == null || result.getJsonResponse() == null
                            || !result.getJsonResponse().getStatus().equals(HttpStatus.OK))) {
                Log.d(TAG, "Logging out from Facebook - user could not be authenticated in server");
                LoginManager.getInstance().logOut();
            }

            return result;
        }

        @Override
        protected void onPostExecute(LoginResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result != null) {
                JsonResponse jsonResponse = result.getJsonResponse();
                User user = result.getUser();

                handleLoginResult(jsonResponse);

                if (jsonResponse != null && jsonResponse.getStatus().equals(HttpStatus.OK)) {
                    onLoginSuccess(user);
                }
            } else {
                Toast.makeText(mContext, getString(R.string.login_failure), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onLoginSuccess(User user) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        // if user is not logged in yet, write info to shared preferences
        if (user != null) {
            sharedPreferences.edit().putInt(Constants.SIGN_IN_PROVIDER, user.getProvider()).apply();
            sharedPreferences.edit().putInt(Constants.USER_TYPE, user.getType()).apply();
            sharedPreferences.edit().putString(Constants.LOGGED_EMAIL, user.getEmail()).apply();

            // if logged user is a teen, fill the shared data preferences as well
            if (user.getType() == Constants.UserType.TEEN.ordinal()) {
                sharedPreferences.edit().putStringSet(Constants.SHARED_DATA_KEY,
                        user.getTeen().getSharedDataAsList()).apply();
            }
        }

        boolean sentToken = sharedPreferences.getBoolean(Constants.SENT_TOKEN_TO_SERVER, false);
        if (!sentToken) {
            // send token if app's server does not have it for the logged user yet
            setUpGcmConfiguration();
        }

        // kills this login screen
        finish();

        // initialize main screen of the app
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
    }

    private void setUpGcmConfiguration() {
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
        String outputMessage = getString(R.string.unexpected_result);
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
}
