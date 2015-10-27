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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.capstone.application.AppController;
import com.capstone.application.R;
import com.capstone.application.model.FollowDataRequest;
import com.capstone.application.model.Follower;
import com.capstone.application.model.JsonResponse;
import com.capstone.application.model.Teen;
import com.capstone.application.model.User;
import com.capstone.application.utils.Constants;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class TeenListAdapter extends BaseAdapter {
    private static final String TAG = TeenListAdapter.class.getName();

    private Activity mActivity;
    private LayoutInflater inflater;
    private List<User> mUserItems;
    private User mRequester;
    private ImageLoader mImageLoader = AppController.getInstance().getImageLoader();

    public TeenListAdapter(Activity activity, List<User> userItems, User requester) {
        mActivity = activity;
        mUserItems = (userItems == null ? new ArrayList<User>() : userItems);
        mRequester = requester;
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            if (inflater == null) {
                inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.row_teen, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.thumbNail = (NetworkImageView) convertView.findViewById(R.id.thumbnail);
            viewHolder.title = (TextView) convertView.findViewById(R.id.title);
            viewHolder.rating = (TextView) convertView.findViewById(R.id.rating);
            viewHolder.genre = (TextView) convertView.findViewById(R.id.genre);
            //viewHolder.year = (TextView) convertView.findViewById(R.id.releaseYear);
            viewHolder.follow = (Switch) convertView.findViewById(R.id.switch1);

            viewHolder.follow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    buttonView.setText(isChecked ? "follow request sent" : "follow");
                    if(isChecked) {
                        buttonView.setEnabled(false);
                    }
                    sendFollowRequest(mUserItems.get(position), isChecked);
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

            if (user.getTeen() != null) {
                if (user.getTeen().getMedicalNumber() != null) {
                    // rating
                    //rating.setText("Rating: " + String.valueOf(m.getRating()));
                    viewHolder.rating.setText(user.getTeen().getMedicalNumber());
                }
                if (user.getTeen().getBirthday() != null) {
                    // release year
                    //viewHolder.year.setText(user.getTeen().getBirthday());
                }
            }
            // genre
        /*String genreStr = "";
        for (String str : m.getGenre()) {
            genreStr += str + ", ";
        }
        genreStr = genreStr.length() > 0 ? genreStr.substring(0,
                genreStr.length() - 2) : genreStr;
        genre.setText(genreStr);*/

            if(mRequester.getType() == Constants.UserType.TEEN.ordinal()) {
                List<Follower> followerList = mRequester.getTeen().getPendingFollowerList();
                for (Follower item : followerList) {
                    if(item.getEmail().equals(user.getEmail())) {
                        viewHolder.follow.setEnabled(false);
                    }
                }
            } else if (mRequester.getType() == Constants.UserType.FOLLOWER.ordinal()) {
                List<Teen> teenList = mRequester.getFollower().getPendingTeenList();
                for (Teen item : teenList) {
                    if(item.getEmail().equals(user.getEmail())) {
                        viewHolder.follow.setEnabled(false);
                    }
                }

            }

        }

        return convertView;
    }

    public static class ViewHolderItem {
        NetworkImageView thumbNail;
        TextView title;
        TextView rating;
        TextView genre;
        //TextView year;
        Switch follow;
    }

    private void sendFollowRequest(User user, boolean follow) {
        if (user != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
            String loggedEmail = sharedPreferences.getString(Constants.LOGGED_EMAIL, null);

            if(loggedEmail != null) {
                Teen teen = new Teen();
                teen.setEmail(user.getEmail());

                User loggedUser = new User();
                loggedUser.setEmail(loggedEmail);

                Log.d(TAG, "User " + loggedUser.getEmail() + " teen " + teen.getEmail() + " follow -> " + follow);
                FollowDataRequest followData = new FollowDataRequest(loggedUser, teen, follow);
                new FollowTask().execute(followData);
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
            dialog.setMessage("Doing something, please wait.");
            dialog.show();
        }

        @Override
        protected JsonResponse doInBackground(FollowDataRequest... params) {
            Log.d(TAG, "Contacting server to send follow data update");

            FollowDataRequest followData = params[0];

            JsonResponse result = null;
            try {
                // The URL for making the GET request
                final String url = Constants.SERVER_URL + "teen/follow/send";

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