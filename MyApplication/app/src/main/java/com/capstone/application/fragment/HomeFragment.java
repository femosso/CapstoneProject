package com.capstone.application.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.capstone.application.R;
import com.capstone.application.activity.CheckInDetailsActivity;
import com.capstone.application.adapter.CheckInListAdapter;
import com.capstone.application.model.CheckIn;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getName();

    private Context mContext;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    private TextView mEmptyView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);

        mEmptyView = (TextView) rootView.findViewById(R.id.txtEmptyView);

        new RetrieveCheckInsTask(HomeFragment.this).execute();

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
    }

    private class RetrieveCheckInsTask extends AsyncTask<Void, Void, List<CheckIn>> {
        private ProgressDialog dialog;

        public RetrieveCheckInsTask(HomeFragment fragment) {
            dialog = new ProgressDialog(fragment.getActivity());
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_loading));
            dialog.show();
        }

        @Override
        protected List<CheckIn> doInBackground(Void... params) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            Log.d(TAG, loggedEmail + " is contacting server to retrieve list of latest check-ins");

            List<CheckIn> result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.CHECK_IN_CONTROLLER + File.separator + RestUriConstants.LIST +
                        "?" + RestUriConstants.PARAM_EMAIL + "=" + loggedEmail;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to CheckIn[] object
                CheckIn[] checkIns = restTemplate.getForObject(url, CheckIn[].class);

                if (checkIns != null) {
                    result = Arrays.asList(checkIns);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<CheckIn> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            result = result == null ? new ArrayList<CheckIn>() : result;

            mAdapter = new CheckInListAdapter(result);
            mRecyclerView.setAdapter(mAdapter);

            ((CheckInListAdapter) mAdapter).setOnItemClickListener(new CheckInListAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(TAG, "Clicked on Item " + position);

                    CheckIn checkIn = ((CheckInListAdapter) mAdapter).getItem(position);
                    new RetrieveCheckInDetailTask(HomeFragment.this).execute(checkIn.getId());
                }
            });

            if (result.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private class RetrieveCheckInDetailTask extends AsyncTask<Long, Void, CheckIn> {
        private ProgressDialog dialog;

        public RetrieveCheckInDetailTask(HomeFragment fragment) {
            dialog = new ProgressDialog(fragment.getActivity());
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_loading));
            dialog.show();
        }

        @Override
        protected CheckIn doInBackground(Long... params) {
            return retrieveCheckInFromServer(mContext, params[0]);
        }

        @Override
        protected void onPostExecute(CheckIn result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Intent intent = new Intent(mContext, CheckInDetailsActivity.class);
            intent.putExtra("checkIn", result);

            startActivity(intent);
        }
    }

    public static CheckIn retrieveCheckInFromServer(Context context, long checkInId) {
        Log.d(TAG, "Contacting server to retrieve details of a check in");

        CheckIn result = null;
        try {
            // The URL for making the GET request
            final String url = Constants.getServerUrl(context) +
                    RestUriConstants.CHECK_IN_CONTROLLER + File.separator +
                    RestUriConstants.VIEW + "?" + RestUriConstants.PARAM_ID + "=" + checkInId;

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();

            // Add the String message converter
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            // Make the HTTP GET request, marshaling the response to CheckIn object
            result = restTemplate.getForObject(url, CheckIn.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}