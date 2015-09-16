package com.capstone.application.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.capstone.application.R;
import com.capstone.application.model.User;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class HomeFragment extends Fragment {

    private Context mContext;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        new TwitterSearchTask().execute();
    }

    private class TwitterSearchTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String ret = "";
            try {
                // The URL for making the GET request
                final String url = "http://192.168.0.103:8080/WebServer/android/view";

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to User object
                User result = restTemplate.getForObject(url, User.class);

                ret = "E-mail: " + result.getEmail() + " Password: " + result.getPassword();
            } catch (final Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
            return ret;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(mContext, "result: " + result, Toast.LENGTH_LONG).show();
        }
    }
}