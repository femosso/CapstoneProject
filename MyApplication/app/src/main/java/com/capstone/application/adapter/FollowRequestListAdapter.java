package com.capstone.application.adapter;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.application.R;
import com.capstone.application.activity.FollowRequestActivity;
import com.capstone.application.model.FollowDataRequest;
import com.capstone.application.model.FollowDataResponse;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;
import com.capstone.application.volley.AppController;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

public class FollowRequestListAdapter extends RecyclerView.Adapter<FollowRequestListAdapter.ViewHolderItem> {
    private static final String TAG = FollowRequestListAdapter.class.getName();

    private FollowRequestActivity mActivity;

    private List<User> mUserItems;

    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();

    public class ViewHolderItem extends RecyclerView.ViewHolder {
        NetworkImageView thumbNail;

        TextView fullName;
        TextView userType;

        Button confirm;
        Button deny;

        public ViewHolderItem(View itemView) {
            super(itemView);
            thumbNail = (NetworkImageView) itemView.findViewById(R.id.imgThumbnail);

            fullName = (TextView) itemView.findViewById(R.id.txtFullName);
            userType = (TextView) itemView.findViewById(R.id.txtUserType);

            confirm = (Button) itemView.findViewById(R.id.btnConfirm);
            deny = (Button) itemView.findViewById(R.id.btnDeny);
        }
    }

    public FollowRequestListAdapter(FollowRequestActivity activity, List<User> userItems) {
        mActivity = activity;
        mUserItems = userItems;
    }

    @Override
    public ViewHolderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_follow_request, parent, false);
        return new ViewHolderItem(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderItem holder, final int position) {
        if (mImageLoader == null) {
            mImageLoader = AppController.getInstance().getImageLoader();
        }

        holder.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFollowRequest(mUserItems.get(position), true);
            }
        });

        holder.deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFollowRequest(mUserItems.get(position), false);
            }
        });

        // getting user data for the row
        User user = mUserItems.get(position);

        if (user != null) {
            String url = Constants.getServerUrl(mActivity) + RestUriConstants.TEEN_CONTROLLER
                    + File.separator + RestUriConstants.PHOTO + "?" + RestUriConstants.PARAM_EMAIL
                    + "=" + user.getEmail();

            // thumbnail image of the follower
            holder.thumbNail.setImageUrl(url, mImageLoader);

            if (user.getFirstName() != null) {
                holder.fullName.setText(user.getFirstName() + " " + user.getLastName());
            }

            holder.userType.setText(mActivity.getString(R.string.user_type) +
                    (user.getType() == Constants.UserType.TEEN.ordinal() ?
                            mActivity.getString(R.string.user_type_teen) :
                            mActivity.getString(R.string.user_type_follower)));
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mUserItems.size();
    }

    public void setData(List<User> userItems) {
        mUserItems = userItems;
    }

    private void confirmFollowRequest(User user, boolean follow) {
        if (user != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            if (loggedEmail != null) {
                Teen loggedTeen = new Teen();
                loggedTeen.setEmail(loggedEmail);

                // associate the logged teen with the follower (which can be a teen or not)
                FollowDataRequest followData = new FollowDataRequest(user, loggedTeen, follow);
                new ConfirmFollowTask().execute(followData);
            }
        }
    }

    private class ConfirmFollowTask extends AsyncTask<FollowDataRequest, Void, FollowDataResponse> {
        private ProgressDialog dialog;

        public ConfirmFollowTask() {
            dialog = new ProgressDialog(mActivity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(mActivity.getString(R.string.progress_dialog_sending));
            dialog.show();
        }

        @Override
        protected FollowDataResponse doInBackground(FollowDataRequest... params) {
            Log.d(TAG, "Contacting server to send follow request confirmation");

            FollowDataRequest followData = params[0];

            FollowDataResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mActivity) +
                        RestUriConstants.TEEN_CONTROLLER + File.separator +
                        RestUriConstants.FOLLOW + File.separator + RestUriConstants.SUBMIT;

                // Create a new RestTemplate instance
                RestTemplate restTemplate = new RestTemplate();

                // Add the String message converter
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

                // Make the HTTP POST request, marshaling the response to FollowDataResponse object
                result = restTemplate.postForObject(url, followData, FollowDataResponse.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(FollowDataResponse result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result != null) {
                JsonResponse jsonResponse = result.getResponse();
                List<User> updatedUserList = result.getFollowRequestList();

                // refresh UI with the new follow request list update
                if (jsonResponse.getStatus() == HttpStatus.OK && updatedUserList != null) {
                    setData(updatedUserList);
                    notifyDataSetChanged();

                    mActivity.refreshUi(updatedUserList);
                }
            }
        }
    }
}