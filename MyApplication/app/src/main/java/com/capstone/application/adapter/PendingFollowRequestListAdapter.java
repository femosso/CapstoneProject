package com.capstone.application.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.application.AppController;
import com.capstone.application.R;
import com.capstone.application.activity.PendingFollowRequestActivity;
import com.capstone.application.model.FollowDataRequest;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PendingFollowRequestListAdapter extends BaseAdapter {
    private static String TAG = "TeenListAdapter";

    private Activity mActivity;
    private LayoutInflater inflater;
    private List<User> mUserItems;
    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();

    public PendingFollowRequestListAdapter(Activity activity, List<User> userItems) {
        mActivity = activity;
        mUserItems = (userItems == null ? new ArrayList<User>() : userItems);
    }

    @Override
    public int getCount() {
        return mUserItems.size();
    }

    @Override
    public Object getItem(int location) {
        return mUserItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setData(List<User> userItems) {
        mUserItems = userItems;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            if (inflater == null) {
                inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.row_pending_follow_request, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.thumbNail = (NetworkImageView) convertView.findViewById(R.id.thumbnail);

            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.rating = (TextView) convertView.findViewById(R.id.rating);
            viewHolder.genre = (TextView) convertView.findViewById(R.id.genre);

            viewHolder.confirm = (Button) convertView.findViewById(R.id.confirm);
            viewHolder.confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmFollowRequest(mUserItems.get(position), true);
                }
            });

            viewHolder.deny = (Button) convertView.findViewById(R.id.deny);
            viewHolder.deny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmFollowRequest(mUserItems.get(position), false);
                }
            });

            // store the holder with the view.
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        if (mImageLoader == null) {
            mImageLoader = AppController.getInstance().getImageLoader();
        }

        // getting user data for the row
        User user = mUserItems.get(position);

        if (user != null) {
            // thumbnail image
            //thumbNail.setImageUrl(m.getThumbnailUrl(), mImageLoader);

            if (user.getFirstName() != null) {
                // title
                viewHolder.title.setText(user.getFirstName());
            }

            viewHolder.rating.setText("User type " + user.getType());
        }

        return convertView;
    }

    public static class ViewHolderItem {
        NetworkImageView thumbNail;

        TextView title;
        TextView rating;
        TextView genre;

        Button confirm;
        Button deny;
    }

    private void confirmFollowRequest(User user, boolean follow) {
        if (user != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            if (loggedEmail != null) {
                Teen loggedTeen = new Teen();
                loggedTeen.setEmail(loggedEmail);

                FollowDataRequest followData = new FollowDataRequest(user, loggedTeen, follow);
                new ConfirmFollowTask().execute(followData);
            }
        }
    }

    public void updatePendingFollowRequestList(PendingFollowRequestActivity activity) {
        new RetrievePendingFollowRequestTask(activity).execute();
    }

    private class ConfirmFollowTask extends AsyncTask<FollowDataRequest, Void, JsonResponse> {

        private ProgressDialog dialog;

        public ConfirmFollowTask() {
            dialog = new ProgressDialog(mActivity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected JsonResponse doInBackground(FollowDataRequest... params) {
            Log.d(TAG, "Contacting server to send follow request confirmation");

            FollowDataRequest followData = params[0];

            JsonResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "teen/follow/submit";

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to User object
                result = restTemplate.postForObject(url, followData, JsonResponse.class);
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

    private class RetrievePendingFollowRequestTask extends AsyncTask<Void, Void, List<User>> {

        private ProgressDialog dialog;
        private Activity mActivity;

        public RetrievePendingFollowRequestTask(PendingFollowRequestActivity activity) {
            mActivity = activity;
            dialog = new ProgressDialog(mActivity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected List<User> doInBackground(Void... params) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            Log.d(TAG, loggedEmail + " is contacting server to retrieve list of pending follow requests");

            List<User> userList = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "teen/pending/list?email=" + loggedEmail;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP GET request, marshaling the response to Teen object
                User[] result = restTemplate.getForObject(url, User[].class);

                if(result != null)  {
                    userList = Arrays.asList(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return userList;
        }

        @Override
        protected void onPostExecute(List<User> result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result != null) {
                setData(result);
                notifyDataSetChanged();
            }
        }
    }
}