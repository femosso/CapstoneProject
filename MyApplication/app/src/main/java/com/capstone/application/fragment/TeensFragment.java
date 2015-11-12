package com.capstone.application.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.capstone.application.adapter.TeenListAdapter;
import com.capstone.application.model.TeenListRequest;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TeensFragment extends Fragment {
    private static final String TAG = TeensFragment.class.getName();

    private Context mContext;

    private RecyclerView mRecyclerView;

    private TextView mEmptyView;

    public TeensFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teens, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.listFragmentTeens);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);

        mEmptyView = (TextView) rootView.findViewById(R.id.txtEmptyView);

        new RetrieveTeensTask(TeensFragment.this).execute();

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

    private class RetrieveTeensTask extends AsyncTask<Void, Void, TeenListRequest> {
        private ProgressDialog dialog;

        public RetrieveTeensTask(TeensFragment fragment) {
            dialog = new ProgressDialog(fragment.getActivity());
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(getString(R.string.progress_dialog_loading));
            dialog.show();
        }

        @Override
        protected TeenListRequest doInBackground(Void... params) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            Log.d(TAG, loggedEmail + " is contacting server to retrieve list of teens");

            TeenListRequest result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mContext) +
                        RestUriConstants.TEEN_CONTROLLER + File.separator + RestUriConstants.LIST +
                        "?" + RestUriConstants.PARAM_EMAIL + "=" + loggedEmail;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to TeenListRequest object
                result = restTemplate.getForObject(url, TeenListRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(TeenListRequest result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            List<User> teenList = new ArrayList<>();
            User requester = new User();

            if (result != null) {
                teenList = result.getTeenList();
                requester = result.getRequester();
            }

            teenList = teenList == null ? new ArrayList<User>() : teenList;

            RecyclerView.Adapter adapter = new TeenListAdapter(getActivity(), teenList, requester);
            mRecyclerView.setAdapter(adapter);

            if (teenList.isEmpty()) {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}