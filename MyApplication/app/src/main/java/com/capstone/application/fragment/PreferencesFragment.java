package com.capstone.application.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.capstone.application.R;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Teen;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Set;

public class PreferencesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = PreferencesFragment.class.getName();

    private Context mContext;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.preferences);

        mContext = getActivity().getApplicationContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int userType = sharedPreferences.getInt(Constants.USER_TYPE, -1);

        // if user is not a teen, remove teen-specific preferences from settings
        if (userType != Constants.UserType.TEEN.ordinal()) {
            getPreferenceScreen().removePreference(findPreference(Constants.REMINDER_FREQUENCY_KEY));
            getPreferenceScreen().removePreference(findPreference(Constants.SHARED_DATA_KEY));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // if teen changes the shared data preference, we update such values in app's server as well
        if (key.equals(Constants.SHARED_DATA_KEY)) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // get email of the logged teen
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            // get only the selected values
            Set<String> selections = sharedPrefs.getStringSet(key, null);

            Teen teen = new Teen();
            teen.setEmail(loggedEmail);
            teen.setSharedDataAsList(selections);

            new UpdateTeenSharedDataTask(getActivity()).execute(teen);
        }
    }

    private class UpdateTeenSharedDataTask extends AsyncTask<Teen, Void, JsonResponse> {
        private ProgressDialog dialog;

        public UpdateTeenSharedDataTask(Activity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_sending));
            dialog.show();
        }

        @Override
        protected JsonResponse doInBackground(Teen... params) {
            Log.d(TAG, "Contacting server to send updated list of shared data");

            Teen teen = params[0];

            JsonResponse result = null;
            try {
                // The URL for making the POST request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.TEEN_CONTROLLER + File.separator + RestUriConstants.SHARED_DATA;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP POST request, marshaling the response to JsonResponse object
                result = restTemplate.postForObject(url, teen, JsonResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(JsonResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
