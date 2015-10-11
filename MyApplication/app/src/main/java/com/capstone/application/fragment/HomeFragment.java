package com.capstone.application.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.capstone.application.R;
import com.capstone.application.activity.PieChartActivity;
import com.capstone.application.adapter.CheckInListAdapter;
import com.capstone.application.model.CheckIn;
import com.capstone.application.model.Question;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private static String TAG = "HomeFragment";

    private Context mContext;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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
        View rootView = inflater.inflate(R.layout.activity_card_view, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Code to Add an item with default animation
        //((CheckInListAdapter) mAdapter).addItem(obj, index);

        // Code to remove an item with default animation
        //((CheckInListAdapter) mAdapter).deleteItem(index);

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

    private ArrayList<CheckIn> getDataSet() {
        ArrayList results = new ArrayList<CheckIn>();
        for (int index = 0; index < 10; index++) {
            User user = new User();
            user.setFirstName("Name " + index);

            Teen teen = new Teen();
            teen.setUser(user);

            Question question = new Question();
            question.setText("Question " + index);

            CheckIn obj = new CheckIn(teen, question, new Date());
            results.add(index, obj);
        }
        return results;
    }

    private class RetrieveCheckInsTask extends AsyncTask<Void, Void, List<CheckIn>> {

        private ProgressDialog dialog;

        public RetrieveCheckInsTask(HomeFragment fragment) {
            dialog = new ProgressDialog(fragment.getActivity());
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected List<CheckIn> doInBackground(Void... params) {
            Log.d(TAG, "Contacting server to retrieve list of latest check-ins");

            List<CheckIn> result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "checkIn/list";

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to CheckIn object
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

            mAdapter = new CheckInListAdapter(getDataSet());
            mRecyclerView.setAdapter(mAdapter);

            ((CheckInListAdapter) mAdapter).setOnItemClickListener(new CheckInListAdapter.MyClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Log.i(TAG, " Clicked on Item " + position);
                    startActivity(new Intent(mContext, PieChartActivity.class));
                }
            });
        }
    }
}