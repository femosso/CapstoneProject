package com.capstone.application.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.fragment.DatePickerFragment;
import com.capstone.application.model.Follower;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.Constants.SignInProvider;
import com.capstone.application.utils.Constants.UserType;
import com.capstone.application.utils.Crypto;
import com.capstone.application.utils.RestUriConstants;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class RegisterActivity extends FragmentActivity {
    private static final String TAG = RegisterActivity.class.getName();

    private TextInputLayout mUsernameWrapper;
    private TextInputLayout mEmailWrapper;
    private TextInputLayout mPasswordWrapper;

    // teen specific inputs
    private TextInputLayout mMedicalNumberWrapper;
    private TextInputLayout mBirthdayWrapper;

    private CheckBox mCheckBoxTeen;

    // callback manager for facebook login
    private CallbackManager mCallbackManager;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        FacebookSdk.sdkInitialize(mContext);

        setContentView(R.layout.activity_register);

        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initViews() {
        mUsernameWrapper = (TextInputLayout) findViewById(R.id.inputUsernameWrapper);
        mEmailWrapper = (TextInputLayout) findViewById(R.id.inputEmailWrapper);
        mPasswordWrapper = (TextInputLayout) findViewById(R.id.inputPasswordWrapper);

        initCustomRegister();
        initFacebookRegister();

        // initialize the views related to teen registration
        mCheckBoxTeen = (CheckBox) findViewById(R.id.checkBoxTeen);
        mCheckBoxTeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = ((CheckBox) v).isChecked() ? View.VISIBLE : View.GONE;
                // if checkbox is disable, hides medical number and date of birth fields
                mMedicalNumberWrapper.setVisibility(visibility);
                mBirthdayWrapper.setVisibility(visibility);
            }
        });

        mMedicalNumberWrapper = (TextInputLayout) findViewById(R.id.inputMedicalNumberWrapper);

        mBirthdayWrapper = (TextInputLayout) findViewById(R.id.inputBirthdayWrapper);
        mBirthdayWrapper.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void initCustomRegister() {
        final Button registerButton = (Button) findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();

                User user = validateFields();
                if (user != null) {
                    doRegistering(user);
                }
            }
        });
    }

    private void initFacebookRegister() {
        // set required permission
        final LoginButton facebookLoginButton = (LoginButton) findViewById(R.id.btnFacebookRegister);
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

                                        User user = buildUserObjectFromJson(response.getJSONObject());
                                        if (user != null) {
                                            doRegistering(user);
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

    private User buildUserObjectFromJson(JSONObject jsonObject) {
        boolean valid = true;

        User user;
        try {
            String id = (String) jsonObject.get("id");
            String[] name = parseName((String) jsonObject.get("name"));
            String email = (String) jsonObject.get("email");

            user = new User();
            user.setFacebookId(id);
            user.setEmail(email);
            user.setFirstName(name[0]);
            user.setLastName(name[1]);
            user.setProvider(SignInProvider.FACEBOOK.ordinal());

            // if it is a teen, we get its medical number and birthday
            if (mCheckBoxTeen.isChecked()) {
                String birthday = (String) jsonObject.get("birthday");
                String medicalNumber = mMedicalNumberWrapper.getEditText().getText().toString();

                Teen teen = new Teen();
                if (!isValidString(medicalNumber)) {
                    mMedicalNumberWrapper.setError(getString(R.string.not_valid_medical_number));
                    valid = false;
                } else {
                    mMedicalNumberWrapper.setErrorEnabled(false);
                    teen.setMedicalNumber(medicalNumber);
                }

                teen.setBirthday(birthday);

                user.setType(UserType.TEEN.ordinal());
                user.setTeen(teen);
            } else {
                user.setType(UserType.FOLLOWER.ordinal());
                user.setFollower(new Follower());
            }
        } catch (JSONException e) {
            user = null;
        }

        // if some validation failed, return null
        if (!valid) {
            user = null;
        }

        return user;
    }

    private String[] parseName(String name) {
        String[] ret = new String[2];

        // split the full name string at one or more non-word character(s)
        if (name != null && name.split("\\W+").length > 1) {
            ret[0] = name.substring(0, name.lastIndexOf(' '));
            ret[1] = name.substring(name.lastIndexOf(" ") + 1);
        } else {
            ret[0] = name;
        }
        return ret;
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /**
     * Validate fields and, if everything is ok, return User object fulfilled with such field values
     */
    private User validateFields() {
        boolean valid = true;

        User user = new User();
        user.setProvider(SignInProvider.APPLICATION.ordinal());

        String username = mUsernameWrapper.getEditText().getText().toString();
        String email = mEmailWrapper.getEditText().getText().toString();
        String password = mPasswordWrapper.getEditText().getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            mUsernameWrapper.setError(getString(R.string.not_valid_name));
            valid = false;
        } else {
            mUsernameWrapper.setErrorEnabled(false);

            String[] name = parseName(username);
            user.setFirstName(name[0]);
            user.setLastName(name[1]);
        }

        if (!LoginActivity.validateEmail(email)) {
            mEmailWrapper.setError(getString(R.string.not_valid_email));
            valid = false;
        } else {
            mEmailWrapper.setErrorEnabled(false);
            user.setEmail(email);
        }

        if (!LoginActivity.validatePassword(password)) {
            mPasswordWrapper.setError(getString(R.string.not_valid_password));
            valid = false;
        } else {
            mPasswordWrapper.setErrorEnabled(false);
            user.setPassword(Crypto.md5(password));
        }

        // if it is a teen, we get its medical number and birthday
        if (mCheckBoxTeen.isChecked()) {
            String birthday = mBirthdayWrapper.getEditText().getText().toString();
            String medicalNumber = mMedicalNumberWrapper.getEditText().getText().toString();

            Teen teen = new Teen();
            if (!isValidDate(birthday)) {
                mBirthdayWrapper.setError(getString(R.string.not_valid_date));
                valid = false;
            } else {
                mBirthdayWrapper.setErrorEnabled(false);
                teen.setBirthday(birthday);
            }

            if (!isValidString(medicalNumber)) {
                mMedicalNumberWrapper.setError(getString(R.string.not_valid_medical_number));
                valid = false;
            } else {
                mMedicalNumberWrapper.setErrorEnabled(false);
                teen.setMedicalNumber(medicalNumber);
            }

            user.setType(UserType.TEEN.ordinal());
            user.setTeen(teen);
        } else {
            user.setType(UserType.FOLLOWER.ordinal());
            user.setFollower(new Follower());
        }

        // if some validation failed, return null
        if (!valid) {
            user = null;
        }

        return user;
    }

    private void doRegistering(User user) {
        new RegisterAccountTask(RegisterActivity.this).execute(user);
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private class RegisterAccountTask extends AsyncTask<User, Void, RegistrationResponse> {
        private ProgressDialog dialog;

        public RegisterAccountTask(RegisterActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_sending));
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // if dialog is dismissed, make sure we log out from facebook as well
                    RegisterAccountTask.this.cancel(true);
                    LoginManager.getInstance().logOut();
                }
            });
            dialog.show();
        }

        @Override
        protected RegistrationResponse doInBackground(User... params) {
            Log.d(TAG, "Contacting server to register user");

            User user = params[0];

            JsonResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.LOGIN_CONTROLLER + File.separator +
                        RestUriConstants.REGISTER + File.separator + RestUriConstants.SUBMIT;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP POST request, marshaling the response to JsonResponse object
                result = restTemplate.postForObject(url, user, JsonResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // log out from Facebook if user couldn't be authenticated in server
            if (user.getProvider() == SignInProvider.FACEBOOK.ordinal() &&
                    (result == null || !result.getStatus().equals(HttpStatus.OK))) {
                Log.d(TAG, "Logging out from Facebook - user couldn't be authenticated in server");
                LoginManager.getInstance().logOut();
            }

            return new RegistrationResponse(result, user);
        }

        @Override
        protected void onPostExecute(RegistrationResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            JsonResponse jsonResponse = result.jsonResponse;
            User user = result.user;

            handleRegisteringResult(jsonResponse);

            if (jsonResponse != null && jsonResponse.getStatus().equals(HttpStatus.OK)) {
                onRegisterSuccess(user);
            }
        }
    }

    public void onRegisterSuccess(User user) {
        Intent result = new Intent();
        result.putExtra("result", user);

        setResult(RESULT_OK, result);
        finish();
    }

    private void handleRegisteringResult(JsonResponse result) {
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

    private static boolean isValidDate(String date) {
        boolean ret = false;
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
            simpleDateFormat.setLenient(false);
            try {
                simpleDateFormat.parse(date);
                ret = true;
            } catch (ParseException e) {
                Log.e(TAG, "Not a valid dae " + e.getMessage());
            }
        }
        return ret;
    }

    private static boolean isValidString(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Auxiliary class to pass necessary objects from RegisterAccountTask::doInBackground() method
     * of AsyncTask to RegisterAccountTask::onPostExecute()
     */
    private static class RegistrationResponse {
        JsonResponse jsonResponse;
        User user;

        RegistrationResponse(JsonResponse jsonResponse, User user) {
            this.jsonResponse = jsonResponse;
            this.user = user;
        }
    }
}
