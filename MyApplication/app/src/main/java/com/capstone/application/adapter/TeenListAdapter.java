package com.capstone.application.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.application.R;
import com.capstone.application.model.FollowDataRequest;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;
import com.capstone.application.utils.RestUriConstants;
import com.capstone.application.volley.AppController;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

public class TeenListAdapter extends RecyclerView.Adapter<TeenListAdapter.ViewHolderItem> {
    private static final String TAG = TeenListAdapter.class.getName();

    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();

    private Activity mActivity;

    private List<User> mUserItems;

    private User mRequester;

    public class ViewHolderItem extends RecyclerView.ViewHolder {
        NetworkImageView thumbNail;

        TextView fullName;
        TextView medicalNumber;
        TextView birthday;

        ToggleButton followRequest;

        public ViewHolderItem(View itemView) {
            super(itemView);
            thumbNail = (NetworkImageView) itemView.findViewById(R.id.imgThumbnail);
            fullName = (TextView) itemView.findViewById(R.id.txtFullName);
            medicalNumber = (TextView) itemView.findViewById(R.id.txtMedicalNumber);
            birthday = (TextView) itemView.findViewById(R.id.txtBirthday);
            followRequest = (ToggleButton) itemView.findViewById(R.id.btnFollowRequest);
        }
    }

    public TeenListAdapter(Activity activity, List<User> userItems, User requester) {
        mActivity = activity;
        mUserItems = userItems;
        mRequester = requester;
    }

    @Override
    public ViewHolderItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_teen, parent, false);
        return new ViewHolderItem(view);
    }

    @Override
    public void onBindViewHolder(ViewHolderItem holder, final int position) {
        if (mImageLoader == null) {
            mImageLoader = AppController.getInstance().getImageLoader();
        }

        holder.followRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton toggleButton = (ToggleButton) v;
                if (toggleButton.isChecked()) {
                    toggleButton.setTextOn(mActivity.getString(R.string.requested));
                    v.setEnabled(false);
                }
                sendFollowRequest(mUserItems.get(position), toggleButton.isChecked());
            }
        });

        // getting user data for the row
        User user = mUserItems.get(position);

        if (user != null) {
            String url = Constants.getServerUrl(mActivity) + RestUriConstants.TEEN_CONTROLLER +
                    File.separator + RestUriConstants.PHOTO + "?" +
                    RestUriConstants.PARAM_EMAIL + "=" + user.getEmail();

            holder.thumbNail.setImageUrl(url, mImageLoader);

            if (user.getFirstName() != null) {
                holder.fullName.setText(user.getFirstName());
            }

            if (user.getLastName() != null) {
                holder.fullName.setText(holder.fullName.getText() + " " + user.getLastName());
            }

            if (user.getTeen() != null) {
                if (user.getTeen().getMedicalNumber() != null) {
                    holder.medicalNumber.setText(mActivity.getString(R.string.teen_medical_number)
                            + user.getTeen().getMedicalNumber());
                }
                if (user.getTeen().getBirthday() != null) {
                    holder.birthday.setText(mActivity.getString(R.string.teen_birthday)
                            + user.getTeen().getBirthday());
                }
            }

            List<Teen> pendingTeenList = mRequester.getFollower().getPendingTeenList();
            for (Teen item : pendingTeenList) {
                if (item.getEmail().equals(user.getEmail())) {
                    // if follow request if pending, mark button as checked but disable it
                    holder.followRequest.setChecked(true);
                    holder.followRequest.setTextOn(mActivity.getString(R.string.requested));
                    holder.followRequest.setEnabled(false);
                    break;
                }
            }

            List<Teen> teenList = mRequester.getFollower().getTeenList();
            for (Teen item : teenList) {
                if (item.getEmail().equals(user.getEmail())) {
                    // if follow request is approved, mark button as checked and enables it
                    holder.followRequest.setChecked(true);
                    holder.followRequest.setTextOn(mActivity.getString(R.string.unfollow));
                    holder.followRequest.setEnabled(true);
                    break;
                }
            }
        }
    }

    public User getItem(int position) {
        return mUserItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mUserItems.size();
    }

    private void sendFollowRequest(User user, boolean follow) {
        if (user != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            if (loggedEmail != null) {
                Teen teen = new Teen();
                teen.setEmail(user.getEmail());

                User loggedUser = new User();
                loggedUser.setEmail(loggedEmail);

                new FollowTask().execute(new FollowDataRequest(loggedUser, teen, follow));
            }
        }
    }

    private class FollowTask extends AsyncTask<FollowDataRequest, Void, JsonResponse> {

        private ProgressDialog dialog;

        public FollowTask() {
            dialog = new ProgressDialog(mActivity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage(mActivity.getString(R.string.progress_dialog_sending));
            dialog.show();
        }

        @Override
        protected JsonResponse doInBackground(FollowDataRequest... params) {
            Log.d(TAG, "Contacting server to send follow data update");

            FollowDataRequest followData = params[0];

            JsonResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.getServerUrl(mActivity) +
                        RestUriConstants.TEEN_CONTROLLER + File.separator +
                        RestUriConstants.FOLLOW + File.separator + RestUriConstants.SEND;

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
}